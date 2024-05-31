#!/bin/bash

# This script tests a reference corpus on galahad server and taggers
# It takes as input the files in ./reference-corpus and outputs to ./output
# Then it diffs the output. If there are changes creates a branch containing the changes and then exits non-zero.
# This test is intended to run within a git repo, and the reference corpus is supposed to be part of the git repo
# Therefore any data used should be licensce-compatible with the general git repo

# This script requires jq for processing and displaying JSON responses
# The tests themselves however can be done without it, so feel free to remove it

API_URL="http://localhost:8010"

INPUT_DIR="./reference-corpus"
OUTPUT_DIR="./output"

INCLUDE_TAGGERS=("dev-naive" "frog-nld" "pie-babpos")
FORMATS=("naf" "tei-p5" "tsv") # "merge" is not applicable to plain text files TODO make it work for non-plaintext files

# Some colors for fancy readingness
txtblk='\e[0;30m' # Black - Regular
txtred='\e[0;31m' # Red
txtgrn='\e[0;32m' # Green
txtylw='\e[0;33m' # Yellow
txtblu='\e[0;34m' # Blue
txtpur='\e[0;35m' # Purple
txtcyn='\e[0;36m' # Cyan
txtwht='\e[0;37m' # White

# Define some colored lines. $FILE $TAGGER and $FORMAT are defined most of the time.
echo_step () {
    echo -e "${txtylw}>> $FILE >> $TAGGER >> $FORMAT >> $1 ${txtwht}"
}

echo_fail () {
    echo -e "${txtred}>> $FILE >> $TAGGER >> $FORMAT >> $1 ${txtwht}"
}

echo_success () {
    echo -e "${txtgrn}>> $FILE >> $TAGGER >> $FORMAT >> $1 ${txtwht}"
}

echo -e "${txtpur}This could be ASCII art, if only we had more time${txtwht}"

echo "I will read files from $INPUT_DIR, upload them to a Galahad server at $API_URL, and write the result to $OUTPUT_DIR."
echo "If the result is different from the previously commited result, I will create a new branch and commit the changes."
echo "During this run I will have this new branch checked out. YOU SHOULD NOT MAKE CHANGES TO THE REPO DURING THE RUN!"
echo ""
echo_step "I will test the following taggers: ${INCLUDE_TAGGERS[*]}. Update INCLUDE_TAGGERS in $0 to change this."
while true; do
    read -p "Do you wish to continue with the current settings?" yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "You can find additional information by reading $0 or visiting https://github.com/INL/Galahad"; exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

if [ -n "$(git status --porcelain)" ]; then
    echo_fail "You have uncommited changes in you git. I refuse to run the test."
    exit 1
else
    echo_success "Git repo is clear, will run the tests.";
fi

echo_step " --- getting taggers --- "
# TODO: check the exit code and status code of curl (multiple places?)
# However the problem is I don't knwo how to get both the http status code and the output itself in a clean way
AVAILABLE_TAGGERS=$(curl -X GET --header 'Accept: application/json' "$API_URL/taggers" \
| jq -r '.[] | .name')
echo ""

# TODO: this code does not seem to do what it says -> fix it.
UNTESTED_TAGGERS=$(echo ${AVAILABLE_TAGGERS[@]} ${INCLUDE_TAGGERS[@]} | tr ' ' '\n' | sort | uniq -u)
echo_step "The following taggers are available, but will not be tested:\n$UNTESTED_TAGGERS"
echo ""

# TODO Fail if an included tagger is not available

echo -e "${txtpur}DEV: Place you bets, \$$(( $RANDOM % 100 + 1 )) lost sofar ...${txtwht}"
echo ""

# utility function fro when the test fails prematurely for a file
premature_fail () {
    git checkout $OLD_BRANCH_NAME
    echo -e "${txtred}I will leave corpus $CORPUS_UUID for inspection, you can remove it manually.${txtwht}"
    echo -e "${txtred}Goodbye${txtwht}"
    exit 1 # TODO make premature fail optional
}

echo_step " --- setup git branch --- "
OLD_BRANCH_NAME=$(git branch --show-current)
BRANCH_NAME="test_corpus_update_$(date +'%Y%j%H%M')"
git checkout -b $BRANCH_NAME
if [[ $? -ne 0 ]]; then
    echo_fail "Failed to create a branch for changes. Currently this is fatal and fails the test."
    exit 1
fi
echo_success "Created the branch $BRANCH_NAME for changes."

echo_step "Creating input and output dir if they don't exist"
mkdir -p $INPUT_DIR
mkdir -p $OUTPUT_DIR
# TODO: check if INPUT_DIR contains at least 1 file.

echo_step "Removing previous output from branch"
rm -r output/*

for FILE in $INPUT_DIR/*
do
    DOCNAME=$( echo $FILE | cut -d'/' -f3 | cut -d'.' -f1)
    # We create a corpus for each file because we want be able to fail early
    # If we put all files in one big corpus, we have to wait for the first result very long
    echo_step "--- creating a corpus --- "
    eval CORPUS_UUID=$(curl -X POST --header 'Content-Type: application/json' \
        --header 'Accept: application/json' \
        -d '{ "name": "testcorpus", "eraFrom": 0, "eraTo": 0, "collaborators": [ "citest" ] }' \
        "$API_URL/corpora")
    echo "Corpus uuid is <$CORPUS_UUID>"
    echo ""

    echo_step " --- uploading file --- "
    curl_result=$(curl -F "file=@$FILE" --write-out 'HTTP status: %{http_code}' "$API_URL/corpora/$CORPUS_UUID/documents")
    if [[ $? -ne 0 ]] || [[ ${curl_result}  != *'HTTP status: 200' ]]; then
        echo_fail "Uploading failed. The output of curl was $curl_result. You will fail this test."
        premature_fail
    fi
    echo $curl_result
    echo_step " --- uploading finished --- "
    echo ""

    #TODO remove this because it is dev?
    echo "Corpus now contains the following documents: "
    DOCS=$(curl -X GET --header 'Accept: application/json' "$API_URL/corpora/$CORPUS_UUID/documents")
    echo $DOCS | jq '[ .[] | { name: .name, format: .format } ]'
    echo ""

    echo_step " --- tagging with included taggers -- "
    for TAGGER in "${INCLUDE_TAGGERS[@]}"
    do
        echo_step " --- tagging started ---"
        echo  "using url: $API_URL/corpora/$CORPUS_UUID/jobs/$TAGGER"
        curl_result=$(curl -X POST --header 'Accept: application/json' "$API_URL/corpora/$CORPUS_UUID/jobs/$TAGGER")
        if [[ $? -ne 0 ]] ; then
            echo_fail "call to $TAGGER failed. The output of curl was $curl_result. You will fail this test."
            premature_fail
        fi
        if [[ $(echo $curl_result | jq -r .status) == ERROR ]]; then
            echo_fail "call to $TAGGER failed. The output of curl was $curl_result. You will fail this test."
            premature_fail
        fi
        echo $curl_result
        echo ""
        # TODO: run in parallel?
        polling_max=10
        polling=0
        timed_out=true # true unless otherwise
        while (($polling < $polling_max)); do
            echo_step "polling tagger"
            curl_result=$(curl -X GET --silent --header 'Accept: application/json' "$API_URL/corpora/$CORPUS_UUID/jobs/$TAGGER")
            if [[ $? -ne 0 ]]; then
                echo_fail "polling $TAGGER failed. The output of curl was $curl_result. You will fail this test."
                premature_fail
            fi
            STATUS_MESSAGE=$(echo $curl_result | jq ".progress")
            echo "Status message: $STATUS_MESSAGE"
            sleep 1
            polling=$((polling+1))
            error=$(echo $curl_result | jq ".progress | .hasError")
            busy=$(echo $curl_result | jq ".progress | .busy")
            if [[ $error == true ]]; then
                echo_fail "tagger $TAGGER had an error. Curl result was $curl_result. You will fail the test"
                premature_fail
            fi
            if [[ $busy == false ]]; then
                echo_step " --- tagging finished. --- "
                # We can speed ahead
                polling=$polling_max
                timed_out=false
            fi
        done # While polling < 10
        if [[ $timed_out == true ]]; then
            echo_fail "tagger $TAGGER timed out. You will fail this test."
            premature_fail
        fi
    done # For tagger in taggers
    TAGGER=""
    echo ""

    echo_step " --- making output subfolder --- "
    cd $OUTPUT_DIR
    mkdir -p $DOCNAME # TODO normalize name to safety
    cd $DOCNAME

    for TAGGER in "${INCLUDE_TAGGERS[@]}"; do
        mkdir -p $TAGGER
        cd $TAGGER
        for FORMAT in "${FORMATS[@]}"; do
            mkdir -p $FORMAT
            cd $FORMAT
            echo_step " --- downloading the corpus --- "
            URL="$API_URL/corpora/$CORPUS_UUID/jobs/$TAGGER/export/convert?format=$FORMAT"
            echo  "using url: $URL"
            curl -X GET --header 'Accept: text/plain' -OJ "$URL"
            if [[ $? -ne 0 ]]; then
                echo_fail "failed to download the corpus with layer $TAGGER. This will fail the test."
                premature_fail
            fi
            echo_step " --- unzipping and remove .zip ---"
            yes | unzip *.zip
            rm *.zip
            cd ..
        done # for format in formats
        FORMAT=""
        cd ..
    done # for tagger in include_taggers
    TAGGER=""

    echo_step " --- deleting the corpus --- "
    curl -X DELETE --header 'Accept: */*' "$API_URL/corpora/$CORPUS_UUID"

    echo ""

    cd ../
    echo_step " --- sanitize --- "
    # Some values will always changes, such as creation dates, or object uuid's.
    # We don't want to test for these values so we can sanitize them to the same string each time
    for _FILE in $(find -type f); do
        echo "Sanitizing $_FILE"
        # Dates in TEI header
        sed -i 's|<date>....-..-.. ..:..:..</date>|<date>0000-00-00 00:00:00</date>|g' $_FILE
        sed -i 's|<interp>....-..-.. ..:..:..</interp>|<interp>0000-00-00 00:00:00</interp>|g' $_FILE
        # UUIDs in TEI header (TODO: maybe make this more specific?)
        sed -i 's|<interp>........-....-....-....-............</interp>|<interp>00000000-0000-0000-0000-000000000000</interp>|g' $_FILE
    done
    cd ../

done # for FILE in INPUT_DIR
FILE=""

echo_step " --- git it --- "
git add -A
git diff $OLD_BRANCH_NAME
echo ""
if [ -n "$(git status --porcelain)" ]; then
    git commit -am "Changes in reference test results"
    echo_fail "Some files have changed. I will fail this test. Please check the output manually."
    echo_fail "I have created a branch for you containing the changes. The branch name is $BRANCH_NAME."
    echo_fail "Merge the branch to accept changes, delete it to reject."
    premature_fail
else
    echo_success "No changes; this file passes the test.";
fi

echo ""
git checkout $OLD_BRANCH_NAME
git branch -d $BRANCH_NAME # No need for it.
echo_success "All tests passed. Yay!";

WORDLIST=(Het Instituut voor de Nederlandse Taal INT is dé plek voor iedereen die iets wil weten over het Nederlands door de eeuwen heen. Het is een breed toegankelijk wetenschappelijk instituut dat alle aspecten van de Nederlandse taal bestudeert, waaronder de woordenschat, grammatica en taalvariatie.)
echo -e "${txtpur}Your word of the run is: ${WORDLIST[RANDOM%${#WORDLIST[@]}]}${txtwht}"
exit 0
