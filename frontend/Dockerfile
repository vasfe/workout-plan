# --- Build stage ---
FROM node:20-alpine AS build
ARG VITE_API_BASE_URL=http://localhost:8080
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN VITE_API_BASE_URL=${VITE_API_BASE_URL} npm run build

# --- Serve stage ---
FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
