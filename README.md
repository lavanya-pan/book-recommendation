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

## Recommendations module

Owner: Person 4. Code is in the `recommendation` package (backend) and `ForYou`, `SimilarBooks` and `PopularRows` (frontend).

### Methods

| Method | How it works |
| --- | --- |
| Top rated | Best average rating, only books with at least 3 ratings. Also used as the fallback for new users. |
| Genre | Books from the reader's liked genres that are well rated. |
| Content-based | TF-IDF text vectors (built from scratch) over title, author, genre, tags and description. Recommends books most similar (cosine similarity) to books the reader rated 4 or higher. |
| Collaborative | Item-based. Finds books that readers who liked the same books also liked. Uses only ratings of 4 or higher. |
| Hybrid | Blends content and collaborative scores. New readers lean on content, readers with many ratings lean on collaborative. |

Every recommendation includes a short reason, such as "Readers who liked X also liked this".

### Endpoints

| Endpoint | Returns |
| --- | --- |
| `GET /api/users/{userId}/recommendations` | Recommendations for a reader. Optional query parameters choose the method and the number of results. |
| `GET /api/books/{bookId}/similar` | Books similar to one book (for the Book Detail page). |
| `GET /api/recommendation-types` | The available methods. |
| `GET /api/books/top-rated` | Top rated books. |
| `GET /api/books/trending` | Books with the most ratings in the last 30 days. |

Frontend: the For You page is at `/for-you`. Book Detail can show similar books with `<SimilarBooks bookId={id} />`.

### Genre names

Genre matching ignores case but is otherwise exact, so `sci-fi` and `science fiction` are different genres. All modules must use the same genre names.

### Evaluation

`RecommendationEvaluationTest` hides 20% of each reader's ratings, recommends from the other 80%, and checks how many of the hidden liked books were recommended. Run it alone and read the table it prints.

Results on simulated data (90 readers, 60 books, 3 genres). Real data will score lower, so compare the methods rather than the absolute numbers.

| Method | P@5 | R@5 | Hit@5 | P@10 | R@10 | Hit@10 |
| --- | --- | --- | --- | --- | --- | --- |
| Popularity (baseline) | 0.030 | 0.081 | 0.150 | 0.036 | 0.194 | 0.338 |
| Content | 0.162 | 0.438 | 0.700 | 0.162 | 0.873 | 0.975 |
| Collaborative | 0.195 | 0.519 | 0.763 | 0.161 | 0.854 | 0.963 |
| Hybrid | 0.175 | 0.450 | 0.663 | 0.169 | 0.896 | 0.975 |

P = precision, R = recall, Hit = share of readers who got at least one liked book, all at 5 or 10 recommendations.
