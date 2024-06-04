#!/bin/bash
# Automatically redeploy the application
# This script will change the state of the server
# So make sure you know what you are doing!

# Optionally you can run a cronjob for this deployment
# to enable continuous deployment.
# Open crontab configuration with: crontab -e
# and add a line like: */5 * * * * cd /path/to/dir && yes | ./scripts/deploy.sh >> /path/to/logdir/deployment.log 2>&1
# THIS WILL OVERRIDE PREVIOUS DEPLOYMENTS, MAKE SURE YOU KNOW WHAT YOU ARE DOING!
# I doubt you want this for a production server

start=`date +%s`

# exit when any command fails
set -e

# keep track of the last executed command
trap 'last_command=$current_command; current_command=$BASH_COMMAND' DEBUG
# echo an error message before exiting
trap 'exit_code=$?; final_command=$last_command;
if [ $exit_code -ne 0 ]; then 
    echo "\"${final_command}\" command failed with exit code $exit_code. Please fix the error and rerun the deployment. Current deployment maybe in an incomplete state." 
fi' EXIT

echo ""
echo "--------------------------------------------------------------------------"
echo "$(date) $(realpath $0)"
echo ""
echo "Update and redeploy application, this will execute:"
echo " - git pull"
echo " - docker compose pull"
echo " - docker compose up"
echo " - docker image prune"
echo ""
echo "Optionally:"
echo " - reset server data"
echo ""
while true; do
    read -p "Do you know what you are doing? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "You could start by reading $0 or visit https://github.com/INL/Galahad"; exit;;
        * ) echo "Please answer yes or no.";;
    esac
done
while true; do
    read -p "Are you sure? " yn
    case $yn in
        [Yy]* ) break;;
        [Nn]* ) echo "Ok then, keep your secrets"; exit;;
        * ) echo "Please answer yes or no.";;
    esac
done

echo "Very well, deploy will now begin"
echo ""

git pull | while IFS= read -r line; do printf '[%s git pull] %s\n' "$(date '+%H:%M:%S')" "$line"; done
echo "" 
docker compose pull | while IFS= read -r line; do printf '[%s docker compose pull] %s\n' "$(date '+%H:%M:%S')" "$line"; done
echo ""
docker compose --compatibility up -d | while IFS= read -r line; do printf '[%s docker compose up] %s\n' "$(date '+%H:%M:%S')" "$line"; done
echo ""
yes | docker image prune | while IFS= read -r line; do printf '[%s docker image prune] %s\n' "$(date '+%H:%M:%S')" "$line"; done
echo ""

# reset the data
while true; do
    read -p "Do you want to rest server data (formats, taggers, tagsets)? 
    [Note: to avoid accidentaly removing user data, private corpora always need to be reset manually.]
" yn
    case $yn in
        [Yy]* ) echo "I will reset the data"; ./scripts/reset-data.sh; break;;
        [Nn]* ) echo "I will not reset the data"; break;;
        * ) echo "Please answer yes or no.";;
    esac
done

echo "$(date) deployment finished."
echo "Visit https://github.com/INL/Galahad for more"
end=`date +%s`
runtime=$((end-start))
echo "Runtime $runtime seconds"
echo "--------------------------------------------------------------------------"
echo ""
