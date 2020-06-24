FROM node:14-alpine as build

WORKDIR /src

ARG auth_api_client_base_url=http://localhost:8080
ENV NEXT_PUBLIC_AUTH_API_BASE_URL=$auth_api_client_base_url

ARG app_base_url=http://localhost:3000
ENV APP_BASE_URL=$app_base_url

ARG bootstrap_script=https://static.dev.snuderls.eu/b/local.insight.js
ENV BOOTSTRAP_SCRIPT=$bootstrap_script

COPY package.json yarn.lock tsconfig.json /src/
COPY frontend/try/package.json /src/frontend/try/package.json
COPY frontend/shared/types/package.json /src/frontend/shared/types/package.json
COPY frontend/shared/service-proxy/package.json /src/frontend/shared/service-proxy/package.json
COPY frontend/shared/testing/package.json /src/frontend/shared/testing/package.json
COPY frontend/shared/storybook/package.json /src/frontend/shared/storybook/package.json
RUN yarn install --frozen-lockfile

COPY frontend/try/ /src/frontend/try/
COPY frontend/shared/ /src/frontend/shared/

RUN yarn workspace @insight/try build
RUN yarn install --frozen-lockfile --production --ignore-scripts --prefer-offline

FROM node:14-alpine

WORKDIR /usr/app

ENV NODE_ENV production

COPY --from=build /src/node_modules /usr/app/node_modules
COPY --from=build /src/frontend/try/package.json /usr/app/package.json
COPY --from=build /src/frontend/try/.next /usr/app/.next

CMD ["npm", "start"]