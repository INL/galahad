# GaLAHaD (1.0.0)
Generating Linguistic Annotations for Historical Dutch

[![Dev images to docker](https://github.com/INL/Galahad/actions/workflows/publish-dev.yml/badge.svg)](https://github.com/INL/Galahad/actions/workflows/publish-dev.yml)
[![Prod images to docker](https://github.com/INL/Galahad/actions/workflows/publish-prod.yml/badge.svg)](https://github.com/INL/Galahad/actions/workflows/publish-prod.yml)
[![Tests](https://github.com/INL/Galahad/actions/workflows/tests.yml/badge.svg?branch=development&event=push)](https://github.com/INL/Galahad/actions/workflows/tests.yml)

### GaLAHaD-related Repositories
- [galahad](https://github.com/INL/galahad) [you are here]
- [galahad-train-battery](https://github.com/INL/galahad-train-battery)
- [galahad-taggers-dockerized](https://github.com/INL/galahad-taggers-dockerized)
- [galahad-corpus-data](https://github.com/INL/galahad-corpus-data/)
- [int-pie](https://github.com/INL/int-pie)
- [int-huggingface-tagger](https://github.com/INL/huggingface-tagger) [to be released]

## Goal
Galahad is developed as part of the CLARIAH "Improved Infrastructure for Historical Dutch" project. The goal is an application that:

- enables linguïsts:
  - to check which taggers are suitable for tagging their corpus.
  - to have their corpus tagged
- enables computational linguists:
  - to provide their models through a unified interface
  - to have their model evaluated

This is provided through a platform that offers:
- statistics for submitted models on existing corpora
- a corpus annotation service 
- instructions on how to submit a model

Note that this infrastructure can also be of interest for other languages and eras.

## Team

### Principal engineer

- vincent.prins@ivdnt.org

### Scientific advisors

- Jesse de Does
- Katrien Depuydt

## Quick start

Do you have docker and docker-compose? Do you have access to the public Docker Hub [instituutnederlandsetaal](https://hub.docker.com/repositories/instituutnederlandsetaal)? Then you can clone this repository and run

```
docker compose up
```
This requires an external taggers network to exists. You can use the `docker-compose.yml` from `https://github.com/INL/galahad-taggers-dockerized` to start a taggers network.

To run Galahad locally. The webclient is available on port 8080.

# Setup for development

Clone the code.

`git clone https://github.com/INL/Galahad.git`

## The client

Start the client.

`cd galahad/client`

`npm install`

`npm run dev`

Go to `http://localhost:5173/` in the browser to check the client development server is running.

## The server
Go to your favourite IDE and open the Gradle project in `galahad/server`.

For development, add `spring.profiles.active=dev` to the environment variables. If you are using IntelliJ, simply use `server/.run/GalahadApplication.run.xml`. This is needed to differentiate whether we are in a docker container (production) or on the localhost (development), which in turn changes how we must communicate with the taggers (via a docker network or via the localhost). 

Run `galahad/server/src/main/kotlin/org/ivdnt/galahad/app/GalahadApplication.kt` from your IDE. Check `http://localhost:8010` to see whether see server is running.

Go back to the client in the browser and try to create a corpus and upload some documents.

## The taggers

In development the application will talk to the taggers through a port-forward. The port-forwards are defined in `docker-compose.yml` from `https://github.com/INL/galahad-taggers-dockerized`. The port-forwards should be defined accordingly as `devport` in the taggers specifications at `server/data/taggers/*.yaml` to enable communication.

## Adding a new tagger

*Asssuming you have already wrapped your tagger in a Docker image.*

First, launch your tagger. See `https://github.com/INL/galahad-taggers-dockerized`.

Now make Galahad aware of the new tagger by creating a tagger metadata yaml file. See `server/data/taggers/` in this repo for examples.

Make the specification yaml available to Galahad:
- If you are running Galahad server from a docker container, the specification yaml should be placed on the docker volume at `data/taggers/`.
- If you are running Galahad server otherwise e.g. from your IDE, you can add the specifications yaml directly to `server/data/taggers/`

Refresh the browser to load the new tagger.

## Adding admins

You can configure the admins account through a file `admins.txt`. Add the desired admin users one per line. To update the file (create it if it does not exists):
```
docker compose exec server sh
cd data
vi admins.txt # make your edits
```

App should autoreload and update to the new status, but refresh client just to be sure.


## Supported file formats
Plain text, TSV, CoNLL-U, TEI, NAF, FoLia.
For more details, see the help screen on formats on the GaLAHaD website.

## Technical notes

### Swagger UI

Once you have launched the application, you can explore the public API at

`http://localhost:8010/swagger-ui.html`

### application BasePath

The INT runs the application behind a portal on a path `/galahad`. Therefore this is set as the default path for the application. Changing this basePath requires to at least rebuild the client application with a different `vite build --base=/galahad/` set.

