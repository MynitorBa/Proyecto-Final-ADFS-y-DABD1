# ─────────────────────────────────────────────
# ETAPA 1 — Build: compila el frontend Svelte
# ─────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# 1. Copia archivos de dependencias
COPY package.json package-lock.json* ./
RUN npm install --ignore-scripts
RUN npm rebuild rollup

# 2. Copia el resto del código fuente
COPY . .

# 3. DECLARACIÓN CRÍTICA:
# Definimos el ARG sin valor por defecto aquí para que tome el que viene de docker-compose.
# Debe estar JUSTO ANTES del comando build.
ARG VITE_API_URL
ENV VITE_API_URL=$VITE_API_URL

# 4. Ejecuta el build (aquí es donde Vite "lee" la variable y la quema en el JS)
RUN npm run build

# ─────────────────────────────────────────────
# ETAPA 2 — Runtime: Nginx sirve el build estatico
# ─────────────────────────────────────────────
FROM nginx:alpine

# Copia el build generado por Vite desde la etapa anterior
COPY --from=builder /app/dist /usr/share/nginx/html

# Configuración de Nginx para SPA
RUN printf 'server {\n    listen 80;\n    root /usr/share/nginx/html;\n    index index.html;\n    location / {\n        try_files $uri $uri/ /index.html;\n    }\n}\n' > /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
