# [GaLAHaD](https://galahad.ivdnt.org)

[![Dev images to docker](https://github.com/INL/Galahad/actions/workflows/publish-dev.yml/badge.svg)](https://github.com/INL/Galahad/actions/workflows/publish-dev.yml)
[![Prod images to docker](https://github.com/INL/Galahad/actions/workflows/publish-prod.yml/badge.svg)](https://github.com/INL/Galahad/actions/workflows/publish-prod.yml)
[![Tests](https://github.com/INL/Galahad/actions/workflows/tests.yml/badge.svg?branch=development&event=push)](https://github.com/INL/Galahad/actions/workflows/tests.yml)

GaLAHaD (Generating Linguistic Annotations for Historical Dutch) enables linguists to enrich their corpora with automatic annotations and allows for evaluation of annotation tools on a deep level.
It consists of a TypeScript Vue 3 frontend, a Kotlin Spring Boot backend, and nginx proxy. Annotation tools are provided by the companion project [galahad-taggers-dockerized](https://github.com/INL/galahad-taggers-dockerized).

### GaLAHaD-related Repositories

- [galahad](https://github.com/INL/galahad) [you are here]
- [galahad-train-battery](https://github.com/INL/galahad-train-battery)
- [galahad-taggers-dockerized](https://github.com/INL/galahad-taggers-dockerized)
- [galahad-corpus-data](https://github.com/INL/galahad-corpus-data/)
- [int-pie](https://github.com/INL/int-pie)
- [int-huggingface-tagger](https://github.com/INL/int-huggingface-tagger)
- [galahad-huggingface-models](https://github.com/instituutnederlandsetaal/galahad-huggingface-models)

## Team

- Principal engineer: Vincent Prins
- Scientific advisors: Jesse de Does, Katrien Depuydt

# Development

## Client

- Install node + npm (e.g. via [nvm](https://github.com/nvm-sh/nvm)). See `client/Dockerfile` for the version.
- `cd client`
- `npm i`
- `npm run dev`, go to `http://localhost:5173`.

## Server

Go to your favourite IDE and open the Gradle project in `galahad/server`.

For development, add `spring.profiles.active=dev` to the environment variables. If you are using IntelliJ, simply use `server/.run/GalahadApplication.run.xml`. This is needed to differentiate whether we are in a docker container (production) or on the localhost (development), which in turn changes how we must communicate with the taggers (via a docker network or via the localhost).

Run `galahad/server/src/main/kotlin/org/ivdnt/galahad/app/Galahad.kt` from your IDE. Check `http://localhost:8010` to see whether see server is running.

## Taggers

In development, the application will talk to the taggers through a port-forward. The port-forwards are defined in `docker-compose.yml` from `https://github.com/INL/galahad-taggers-dockerized`. The port-forwards should be defined accordingly as `devport` in the taggers specifications at `server/data/taggers/*.yaml` to enable communication.

## Adding a new tagger

_Asssuming you have already wrapped your tagger in a Docker image._

First, launch your tagger. See `https://github.com/INL/galahad-taggers-dockerized`.

Now make Galahad aware of the new tagger by creating a tagger metadata yaml file. See `server/data/taggers/` in this repo for examples.

Make the specification yaml available to Galahad:

- If you are running Galahad server from a docker container, the specification yaml should be placed on the docker volume at `data/taggers/`.
- If you are running Galahad server otherwise e.g. from your IDE, you can add the specifications yaml directly to `server/data/taggers/`

Restart the server to load the new tagger.

## Adding admins

You can configure the admins through `server/data/admins.txt`, one per line.

# Deployment

- Install docker.
- `git clone https://github.com/instituutnederlandsetaal/galahad`
- `cd galahad`.
- Fill in the `.env`.
- `docker network create galahad-taggers`
- `docker compose up -d`

## .env

```sh
VERSION=latest # docker image version
TAGGERS_NETWORK=galahad-taggers # docker taggers network
PORT=80 # proxy port
```

## Docker images

Docker images are available on Docker Hub.

- [instituutnederlandsetaal/galahad-server](https://hub.docker.com/repository/docker/instituutnederlandsetaal/galahad-server)
- [instituutnederlandsetaal/galahad-client](https://hub.docker.com/repository/docker/instituutnederlandsetaal/galahad-client)
