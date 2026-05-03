# Usamos una imagen ligera de Node.js
FROM node:20-alpine

# Establecemos el directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los archivos de dependencias primero (optimiza el caché de Docker)
COPY package*.json ./

# Instalamos las dependencias
RUN npm install

# Copiamos el resto del código
COPY . .

# Exponemos el puerto que usa Vite
EXPOSE 5173

# Comando para iniciar Vite. El "--host" es OBLIGATORIO en Docker 
# para que el puerto sea accesible desde tu navegador en Windows.
CMD ["npm", "run", "dev", "--", "--host"]