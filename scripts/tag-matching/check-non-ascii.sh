for file in truth-CLVN/*.xml
do
    echo "$file"
    cat $file | \
        # awk works per line so we need to remove newlines.
        tr --delete '\n' | \ 
        awk -F '<body>' '{print $2}' | \
        grep --color=always -nio -e '[^ a-z äëöüï éó 0-9 <> \\ \( \) \. \? \+ °‘’,;:!-_=&%$#@*/~`^" \{ \} \[ | ]'
done
