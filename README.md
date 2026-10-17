# Book Recommendation System

A website that recommends books based on reader personality, quiz answers, ratings and reading behaviour.

**Stack:** Java 17, Spring Boot 3.2, Spring Data JPA, MySQL, Maven (backend); React, Vite, Tailwind CSS, Axios (frontend).

## Project layout

```
backend/    Spring Boot REST API (package com.sds.bookrecommendation)
frontend/   React website
```

Backend packages: `entity`, `repository`, `service`, `controller`, `dto`, `exception`, `security`, `config`, `recommendation`.

## Run it locally

**1. Database**

```sql
CREATE DATABASE book_recommendation;
```

**2. Backend**

```bash
cd backend
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# edit application-local.properties and put your own MySQL password
mvn spring-boot:run
```

Check: http://localhost:8080/api/health should return `{"status":"UP"}`.

**3. Frontend**

```bash
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. The home page shows "Backend status: UP" when both are connected.

**4. Tests**

```bash
cd backend
mvn test
```

## Team workflow

1. Never commit to `main`. Work on `feature/<module>` (for example `feature/books`).
2. Pull `main` into your branch every day: `git pull origin main`.
3. Open a pull request and get one teammate to review before merging.
4. Never commit `application-local.properties`, passwords or `node_modules`.

## Modules

| Module | Owner |
| --- | --- |
| Books and data | Person 1 |
| Users and security | Person 2 |
| Ratings and quizzes | Person 3 |
| Recommendations | Person 4 |

## Error format

Every error returns JSON: `{ "timestamp", "status", "error", "message" }`.
Use 400 for validation, 401/403 for authentication, 404 for not found and 409 for duplicates.
Add new handlers in `GlobalExceptionHandler`.
