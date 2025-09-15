# Servidor Web 2 - Proyecto con Docker y AWS EC2

## 📖 Descripción
Este proyecto implementa un **mini-framework web en Java** (similar a Spring Boot pero simplificado), que permite manejar rutas HTTP y responder con contenido dinámico y estático.  
El objetivo del taller fue:
- Compilar el proyecto con **Maven**.
- Empaquetar la aplicación en un **contenedor Docker**.
- Publicar la imagen en **Docker Hub**.
- Desplegar la aplicación en una instancia de **AWS EC2**.
- Acceder a la aplicación desde el navegador usando la IP pública de la instancia.

---

## 🚀 Tecnologías utilizadas
- **Java 17**
- **Maven**
- **Docker**
- **Amazon EC2 (Amazon Linux 2023)**
- **Docker Hub**

## Creación del Dockerfile y Construcción de la Imagen

- En la raíz del proyecto se creó un archivo llamado Dockerfile con el siguiente contenido:
```java
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package
FROM eclipse-temurin:17-jre
WORKDIR /usrapp/bin
ENV PORT=8080
COPY --from=build /app/target/classes ./classes
CMD ["bash","-lc","java -cp ./classes ServidorWeb.MicroSpringBoot"]
EXPOSE 8080
```
- Antes de construir la imagen, se debe empaquetar el proyecto en un archivo .jar:
```java
mvn clean package
```

- Esto genera el archivo: **target/servidor-web2-1.0-SNAPSHOT.jar**
   
## 🐳 Construcción de la imagen Docker

- Con el Dockerfile listo, se construyó la imagen con:
```java
docker push saracastillo/servidor-web2:1.0 
```
<p align="center">
<img width="1259" height="493" alt="image" src="https://github.com/user-attachments/assets/077e67b3-785f-46b4-a717-6d17e1e5a3d9" />
</p>

▶️ Ejecución local de la imagen

- Para probar localmente, se ejecutó el contenedor:

```java
docker run -d -p 8080:8080 saracastillo/servidor-web2
```

<p align="center">
<img width="589" height="69" alt="image" src="https://github.com/user-attachments/assets/dd95c455-a648-4690-b366-eb969fce6f98" />
</p>

- Probamos localmente con:  http://localhost:8080/greeting?name=Sara
<p align="center">
<img width="633" height="178" alt="image" src="https://github.com/user-attachments/assets/9913a761-95d7-4468-be79-f02a3e3407f7" />
</p>

## ☁️ Despliegue en AWS EC2

1. Creamos la instancia EC2:

<p align="center">
<img width="1124" height="284" alt="image" src="https://github.com/user-attachments/assets/a10b6dc3-6e92-4dd5-8880-966a8198c69b" />
</p>

2. Conectarse a la instancia vía SSH, desde la terminal local (ubicándose en la carpeta donde está la llave .pem):

```java
ssh -i "servidor-web2-aws.pem" ec2-user@54.196.189.165
```
<p align="center">
<img width="921" height="211" alt="image" src="https://github.com/user-attachments/assets/8d424e19-42eb-4916-a801-5e922b591328" />
</p>

3. Instalar Docker en Amazon Linux:
```java
sudo yum update -y
sudo yum install docker -y
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ec2-user
```
<p align="center">
<img width="966" height="383" alt="image" src="https://github.com/user-attachments/assets/a4a5dce3-be4a-4c83-a865-0f8999e287cf" />
</p>

4. Descargar y ejecutar la imagen desde Docker Hub:
```java
docker pull saracastillo/servidor-web2
docker run -d -p 8080:8080 saracastillo/servidor-web2
```
<p align="center">
<img width="963" height="309" alt="image" src="https://github.com/user-attachments/assets/ba6cb6e0-89c4-465f-b812-ab06b7a4eeed" />
</p>

5. Probar la aplicación desde la EC2 y desde el PC con la IP 54.196.189.165:
```java
curl http://localhost:8080/greeting?name=Sara
```
<p align="center">
<img width="655" height="71" alt="image" src="https://github.com/user-attachments/assets/9ee3b774-b7ac-4916-b819-c7b7b98b1c3d" />
</p>

<p align="center">
<img width="585" height="191" alt="image" src="https://github.com/user-attachments/assets/493104cb-50c2-4a1c-bbe5-52e14c44e948" />
</p>

