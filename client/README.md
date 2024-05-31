# galahad-client
Project setup: `npm install`
Compiles and hot-reloads for development: `npm run dev`
Compiles and minifies for production: `npm run build`
Run your unit tests: `npm run test:unit`
Lints and fixes files: `npm run lint`

[Configuration Reference](https://cli.vuejs.org/config/).

# Dev overview
The following folders are found in src/
## Types
Typescript defined types, mostly to reflect the API response type.
## API
Interface to call the server.
## Stores
Pinia stores for application data. Retrieves data via the API and uploads data.
## Components
Vue components. Some core components like custom tables and buttons are prefixed with G- (short for Galahad).
The interface is based around GTabs that can display different GCards in a tab-like interface.

## Views
Vue views. Views in the Annotate tab make use of the AnnotateTab component which handles errors like empty corpora or empty selections.
