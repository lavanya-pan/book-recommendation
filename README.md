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

## Ratings and quizzes module

Owner: Person 3. Backend code is in the `entity`, `repository`, `service`, `controller`, `dto` and `quiz` packages. Frontend code is in `RatingStars`, `QuizUI`, `ThemeSwitcher`, `PersonalityQuiz`, `MoodQuiz` and the `theme` folder.

### Ratings

A reader rates a book from 1 to 5. A reader can rate each book only once. After every create, update or delete, the book's `avgRating` (rounded to 2 decimals) and `ratingCount` are recomputed.

| Endpoint | What it does | Success |
| --- | --- | --- |
| `POST /api/ratings` | Rate a book. Body: `userId`, `bookId`, `score`. | 201 |
| `PUT /api/ratings/{id}` | Change the score. Body: `score`. | 200 |
| `DELETE /api/ratings/{id}` | Remove a rating. | 204 |
| `GET /api/users/{id}/ratings` | One page of a reader's ratings, newest first. Query parameters: `page`, `size`, `sort`. | 200 |

Errors: 400 for a score outside 1 to 5 or a missing field, 404 for an unknown user, book or rating, 409 if the reader already rated the book.

### Interactions

`POST /api/interactions` logs one action of a reader on a book. Body: `userId`, `bookId`, `type` (`VIEW`, `CLICK` or `SAVE`). Returns 201. The `interactions` table has a `created_at` column, and `InteractionRepository.countByBookSince` counts interactions per book since a given time, so the trending list can use it.

### Personality quiz

Eight questions, each with four answers. Every answer gives points to one or two reader types. The type with the most points wins. A tie goes to the type that the answer to question 1 favours, so the result is always the same for the same answers.

| Endpoint | What it does |
| --- | --- |
| `GET /api/quiz/personality/questions` | The questions and their answer options. |
| `POST /api/quiz/personality` | Body: `{ "answers": [{ "questionId", "optionId" }, ...] }` with one answer per question. Returns `readerType`, `description`, `genres` (top 3) and `theme`. |

Questions and points live in `backend/src/main/resources/quiz/personality-questions.json`. Edit that file to change the quiz. No code change is needed.

The six reader types, their best genres and their site theme (`ArchetypeGenreMap`, `Archetype`):

| Reader type | Best genres (weight) | Theme |
| --- | --- | --- |
| Adventurer | fantasy 0.9, adventure 0.8, sci-fi 0.5, thriller 0.4 | fantasy |
| Dreamer | fantasy 0.8, romance 0.7, poetry 0.6, young-adult 0.5 | romance |
| Detective | mystery 0.9, crime 0.8, thriller 0.8, horror 0.3 | mystery |
| Thinker | philosophy 0.9, sci-fi 0.7, non-fiction 0.6, classics 0.5 | mystery |
| Scholar | non-fiction 0.9, history 0.8, biography 0.7, science 0.6, classics 0.5 | fantasy |
| Comfort Reader | romance 0.8, contemporary 0.7, humor 0.6, young-adult 0.5 | romance |

Other modules can read the weights with `ArchetypeGenreMap.weightsFor("Adventurer")`. All genre names come from the official list in the Genre names section below.

### Mood quiz

`POST /api/quiz/mood` (optional query parameter `limit`, default 10, maximum 50) takes five answers and returns matching books with a short reason, in the same shape as the recommendations.

| Field | Allowed values |
| --- | --- |
| `mood` | `cozy`, `thrilling`, `emotional`, `funny`, `dark` |
| `world` | `fantasy`, `real-world`, `scifi`, `historical` |
| `length` | `short` (under 250 pages), `medium`, `long` (over 500 pages) |
| `romance` | `true` or `false` |
| `tone` | `light`, `serious`, `hopeful`, `mysterious` |

How it works: the answers become genre weights, keywords and a page range. Books from those genres are kept if their page count fits (books without a page count stay). With `romance` set to `false`, romance books are dropped. The score is 60% genre match, 20% keyword match in the title, tags and description, and 20% average rating. An unknown answer returns 400 with the list of valid values.

### Themes and frontend

- `/quiz` is the personality quiz: one question at a time, a progress bar, a Back button and a result screen. The result applies its theme automatically.
- `/mood-quiz` is the mood quiz with a grid of matching books.
- `RatingStars` lets a reader click stars to rate a book (click the same star again to remove the rating). With `readOnly` and `value` it only shows an average. Example: `<RatingStars bookId={id} userId={userId} />` or `<RatingStars value={book.avgRating} readOnly />`.
- `ThemeProvider` sets `data-theme` (`fantasy`, `mystery` or `romance`) on the page and remembers the choice. `ThemeSwitcher` is the drop-down that lets a reader override it. Colours are CSS variables in `frontend/src/themes/themes.css`. Use `var(--color-bg)`, `var(--color-text)`, `var(--color-accent)` and the other variables instead of fixed colours so every page follows the theme.
- `QuizUI` holds the shared `Card`, `Button` and `ProgressBar` components.

### Tests and Postman

Run `mvn test` in `backend`. The module has `RatingServiceTest`, `RatingRequestValidationTest`, `RatingControllerTest` (MockMvc), `QuizServiceTest`, `PersonalityQuizDataTest`, `MoodQuizServiceTest` and `ArchetypeGenreMapTest`.

Import `postman/Ratings-and-Quizzes.postman_collection.json` into Postman and use Run collection. It has 14 requests with status checks, uses the variables `baseUrl` (default `http://localhost:8080`) and `token`, and needs the test user with id 1 and book 1 in the database.

### Known limits

- Ratings and interactions need a login. Send the token in the `Authorization: Bearer <token>` header. The server uses the logged-in user and ignores the `userId` in the body, although the field is still required.
- The quiz result is not saved on the user yet. It will be stored in `User.readerType` and `User.theme` once those fields are on `main`.

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
