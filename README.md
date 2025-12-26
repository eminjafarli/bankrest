# 💳 Bank Card Management System

## Project Description

The goal of this project is to create a modern, secure, and scalable **Full-Stack platform** for simulating key operations with bank cards. The project demonstrates best practices in software development, including the use of **microservice architecture** (logically separated Backend/Frontend), **security** (JWT, Spring Security), and **infrastructure automation** (Docker, Liquibase).

---

## ✅ Key API Features

### 🛡️ Security and Authentication
* **Authentication/Authorization:** Implemented via **Spring Security** using **JWT** (JSON Web Tokens).
* **Role-Based Access:** Clear separation of rights between `ADMIN` and `USER` roles.
* **Data Encryption:** Card numbers and CVVs are stored in the database in encrypted form.
* **Masking:** Card numbers are displayed to users in a masked format (e.g., `**** **** **** 1234`).

### 👤 User Functionality (`USER`)
* View a list of own cards with filtering and pagination.
* Check own card balance.
* Transfer funds between own cards.
* Request card blocking.
* Create a new card.

### 👑 Admin Functionality (`ADMIN`)
* Full CRUD (create, read, update, delete) for all cards in the system.
* Manage card statuses (Activation, Blocking).
* Manage users (view, update, delete).

---

## 💡 Tech Stack

| Category | Technology | Purpose |
| :--- | :--- | :--- |
| **Backend** | Java 17+, Spring Boot, Spring Security | Core framework and business logic. |
| **Frontend** | React (JS/JSX) SPA | Stylish modern interface. |
| **Styling** | Styled Components, Framer Motion | Modern CSS-in-JS, smooth animations. |
| **Database** | PostgreSQL/MySQL | Relational RDBMS for data storage. |
| **ORM** | Spring Data JPA, Hibernate | Database interaction. |
| **Migrations** | Liquibase | Database schema version control. |
| **Infrastructure** | Docker, Docker Compose | Lightweight dev environment deployment. |
| **Docs** | Swagger UI / OpenAPI | Automated interactive API documentation. |
| **Testing** | JUnit 5, Mockito | Unit testing of key business logic. |

---

## 🛠️ Project Setup

To run the project, you will need **Docker** and **Docker Compose** installed on your machine.

### 1. Clone the repository

```bash
git clone [https://github.com/eminjafarli/bankrest.git](https://github.com/eminjafarli/bankrest.git)

### 2. Build and Run

```

The project is configured to run using Docker Compose. This command will build the application Docker image, start the PostgreSQL container, and link them.

```bash
cd bankrest-frontend
docker-compose up --build

```

> **Note:** Upon startup, the application will automatically execute all database migrations via **Liquibase**, ensuring the DB schema is up to date.

---

## 📚 API Documentation (Swagger UI)

Once the application is running, interactive API documentation is available via Swagger UI:

* **URL:** `http://localhost:8080/swagger-ui.html`

---

## 🧪 Testing

### Unit Tests

To run unit tests (e.g., for classes covered using **Mockito**):

```bash
./mvnw test

```

```

```
