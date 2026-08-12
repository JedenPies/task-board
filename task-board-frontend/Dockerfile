FROM node:26-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .

ARG OAUTH2_GOOGLE_CLIENT_ID
ARG OAUTH2_GITHUB_CLIENT_ID
ARG OAUTH2_FACEBOOK_CLIENT_ID
ARG OAUTH2_LINKEDIN_CLIENT_ID

RUN sed -i "s/GOOGLE_CLIENT_ID_PLACEHOLDER/${OAUTH2_GOOGLE_CLIENT_ID}/g" src/environments/environment.ts
RUN sed -i "s/GITHUB_CLIENT_ID_PLACEHOLDER/${OAUTH2_GITHUB_CLIENT_ID}/g" src/environments/environment.ts
RUN sed -i "s/FACEBOOK_CLIENT_ID_PLACEHOLDER/${OAUTH2_FACEBOOK_CLIENT_ID}/g" src/environments/environment.ts
RUN sed -i "s/LINKEDIN_CLIENT_ID_PLACEHOLDER/${OAUTH2_LINKEDIN_CLIENT_ID}/g" src/environments/environment.ts

RUN npm run build --configuration=production

FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/task-board-frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
