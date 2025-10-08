# 🛍️ I-Love-Shopping

**I-Love-Shopping** is a modular Spring Boot e-commerce platform built as part of a full-stack learning project.  
The goal is to design a **scalable and secure foundation** for a Business-to-Consumer (B2C) online store using best development practices.

---

## 🚀 Project Overview

This project demonstrates how to build an e-commerce system from scratch —  
from the database schema and authentication to the product catalog and API documentation.

### 🎯 Objectives
- Modular monolith architecture (multi-module Maven project)
- Secure authentication system (JWT + refresh tokens)
- Well-structured database with Flyway migrations
- Product catalog with brands, categories, and filters
- OpenAPI documentation for REST endpoints
- Docker-based local environment (PostgreSQL + backend)

---

## 🧩 Modules

| Module | Description |
|---------|-------------|
| **app** | Main application module (Spring Boot entry point, configs, dependencies) |
| **common** | Shared DTOs, exceptions, and utilities |
| **auth** | Authentication and authorization (JWT, refresh tokens) |
| **catalog** | Product, brand, and category management |

---

## 🗂️ Tech Stack

**Backend:** Spring Boot 3, Java 21  
**Database:** PostgreSQL + Flyway  
**Build:** Maven (multi-module)  
**Docs:** OpenAPI / Swagger UI  
**Containerization:** Docker Compose  
**Tests:** JUnit 5, Mockito (planned)

---

## 🧱 Project Structure

```
-love-shopping/
│
├── app/ # Main module (Spring Boot application)
├── common/ # Shared logic, utils, DTOs
├── auth/ # Auth module (JWT, users, refresh tokens)
├── catalog/ # Catalog module (products, categories, brands)
│
├── docs/ # ERD, guides, and documentation
│ ├── erd.png
│ ├── roadmap.md
│ ├── docker-guide.md
│ └── flyway.md
│
├── docker-compose.yml
├── pom.xml
└── README.md
```
## ⚙️ Setup & Run (Dev Mode)

### 1. Clone the repository
```bash
  git clone https://gitea.kood.tech/iljaaituganov/dot-com-retail.git
cd i-love-shopping 
```
### 2. Start Docker services
```bash
  docker-compose up -d
```
### 3. Run the backend
dev
```bash
  ./mvnw spring-boot:run -pl app -Dspring-boot.run.profiles=dev
```
---
## 🧠 Development Notes
* Migrations are stored in src/main/resources/db/migration
* Database schema is managed via Flyway
* To fix checksum mismatch after editing migration:
```bash
    ./mvnw flyway:repair
   ```
---
## 📅 Roadmap
 See detailed progress and sprint plan in 📘 /docs/roadmap.md


```
👤 Author
Ilja Aituganov
📧 ilja.samalea@gmail.com
🗓️ Started October 2025
```