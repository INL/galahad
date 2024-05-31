# Build stage
FROM node:21.6.2-alpine3.19 as build
COPY . .
# Install project dependencies (somehow Python has to be installed)
RUN apk --no-cache add --virtual .builds-deps build-base python3
RUN npm ci
# Builds to dist/
RUN npm run build

# Production stage.
FROM node:21.6.2-alpine3.19
WORKDIR /app
# Install dependencies of server.js
RUN npm install express connect-history-api-fallback && npm cache clean --force
# Copy over build & server.
COPY --from=build /dist ./dist
COPY . .

EXPOSE 8080
CMD [ "node", "server.js" ]