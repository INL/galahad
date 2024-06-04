# Set the default label
: ${VERSION_LABEL:=dev}

./scripts/build-all.sh

echo "Will push galahad images with version <$VERSION_LABEL>. Set VERSION_LABEL to override this."

docker push instituutnederlandsetaal/galahad-proxy:$VERSION_LABEL
docker push instituutnederlandsetaal/galahad-server:$VERSION_LABEL
docker push instituutnederlandsetaal/galahad-client:$VERSION_LABEL
