#!/bin/bash
# This script resets/updates the data in the docker volumes to the data in the repo
#
# Note: we require jq. Feel free to write an implementation without jq.

echo "docker volume ls:"
docker volume ls

echo ""
echo "resetting formats ..."
FORMATS_DIR=$(docker inspect galahad_formats-volume | jq -r '.[0].Mountpoint')
# Somehow the rm fails. TODO fix it.
rm -v --interactive=never "$FORMATS_DIR/*"
cp -v ./server/data/formats/* $FORMATS_DIR
echo "Available formats are:"
ls $FORMATS_DIR

echo ""
echo "resetting taggers ..."
TAGGERS_DIR=$(docker inspect galahad_taggers-volume | jq -r '.[0].Mountpoint')
rm -v --interactive=never "$TAGGERS_DIR/*"
cp -v ./server/data/taggers/* $TAGGERS_DIR
echo "Available taggers are:"
ls $TAGGERS_DIR

echo ""
echo "resetting tagsets ..."
TAGSETS_DIR=$(docker inspect galahad_tagsets-volume | jq -r '.[0].Mountpoint')
rm -v --interactive=never "$TAGSETS_DIR/*"
cp -v ./server/data/tagsets/* $TAGSETS_DIR
echo "Available tagsets are:"
ls $TAGSETS_DIR
