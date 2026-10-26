# Build stage: install dependencies and build the Angular app
FROM node:24-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# Runtime stage: nginx serves the static files and proxies /api to the backend
FROM nginx:1.29-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/room-booking-ui/browser /usr/share/nginx/html
