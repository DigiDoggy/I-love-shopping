# 🚀 Docker Commands Cheat Sheet for Spring Boot

This cheat sheet provides the most commonly used Docker commands when working with a Spring Boot project. Each command includes a short explanation.

---

## 🐳 Basic Docker Commands

```bash
# Check installed Docker version
docker --version

# Show system-wide information about Docker
docker info
```

---

## 📦 Images

```bash
# List all images
docker images

# Pull an image from Docker Hub
docker pull postgres:16

# Build an image from Dockerfile (in current directory)
docker build -t my-springboot-app .

# Remove unused images
docker image prune
```

---

## 🏃 Containers

```bash
# List running containers
docker ps

# List all containers (including stopped ones)
docker ps -a

# Start a container
docker start <container_id>

# Stop a container
docker stop <container_id>

# Remove a container
docker rm <container_id>
```

---

## 🔗 Running Spring Boot with Docker

```bash
# Run a Spring Boot JAR inside a container
docker run -p 8080:8080 my-springboot-app

# Run with environment variables (example: DB connection)
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e DB_HOST=postgres \
  my-springboot-app
```

---

## 🗄️ Volumes & Networks

```bash
# List volumes
docker volume ls

# Create a network for Spring Boot + Postgres
docker network create spring-net

# Run Postgres with volume and network
docker run -d \
  --name postgres \
  --network spring-net \
  -e POSTGRES_USER=user \
  -e POSTGRES_PASSWORD=pass \
  -e POSTGRES_DB=mydb \
  -v pgdata:/var/lib/postgresql/data \
  postgres:16

# Run Spring Boot app on the same network
docker run -d \
  --name spring-app \
  --network spring-net \
  -p 8080:8080 \
  my-springboot-app
```

---

## 🧰 Docker Compose

```bash
# Start all services (defined in docker-compose.yml)
docker-compose up -d

# Stop all services
docker-compose down

# Rebuild images and restart services
docker-compose up --build
```

---

## 🔍 Debugging & Logs

```bash
# View logs of a container
docker logs -f <container_id>

# Enter container shell
docker exec -it <container_id> bash

# Inspect container configuration
docker inspect <container_id>
```

---
