import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import RecommendationCard from "../components/RecommendationCard.jsx";
import PopularRows from "../components/PopularRows.jsx";
import {
  getGenreRecommendations,
  getRecommendationTypes,
  getRecommendations,
} from "../api/recommendations.js";

// Friendly names for the strategy types returned by the backend.
const TYPE_LABELS = {
  hybrid: "Best mix (hybrid)",
  content: "Similar to what you liked",
  collaborative: "Readers like you",
  genre: "Your favourite genres",
  "top-rated": "Top rated",
};

const inputClass =
  "rounded-lg border border-[var(--color-accent)] bg-[var(--color-bg)] px-3 py-2 text-[var(--color-text)]";

export default function ForYou() {
  // The quiz result page links here with ?genres=romance,humor
  const [searchParams] = useSearchParams();
  const quizGenres = (searchParams.get("genres") || "")
    .split(",")
    .map((g) => g.trim())
    .filter(Boolean);
  const quizKey = quizGenres.join(",");

  // Until login exists (Person 2), the user is picked by typing an ID.
  const [userId, setUserId] = useState("1");
  const [types, setTypes] = useState(["hybrid"]);
  const [type, setType] = useState("hybrid");
  const [books, setBooks] = useState([]);
  const [status, setStatus] = useState("loading"); // loading | ready | error
  const [error, setError] = useState("");
  const [quizBooks, setQuizBooks] = useState([]);
  const [quizStatus, setQuizStatus] = useState("idle"); // idle | loading | ready | error

  // Load the list of recommendation types once.
  useEffect(() => {
    getRecommendationTypes()
      .then(setTypes)
      .catch(() => {});
  }, []);

  // Load books for the genres from the quiz result, if there are any.
  useEffect(() => {
    if (!quizKey) {
      setQuizBooks([]);
      setQuizStatus("idle");
      return;
    }
    let cancelled = false;
    setQuizStatus("loading");
    getGenreRecommendations(quizKey.split(","), 12)
      .then((data) => {
        if (!cancelled) {
          setQuizBooks(data);
          setQuizStatus("ready");
        }
      })
      .catch(() => {
        if (!cancelled) {
          setQuizStatus("error");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [quizKey]);

  // Load recommendations whenever the user or the type changes.
  useEffect(() => {
    if (!userId) {
      setBooks([]);
      setStatus("ready");
      return;
    }
    let cancelled = false;
    setStatus("loading");
    getRecommendations(userId, type, 12)
      .then((data) => {
        if (!cancelled) {
          setBooks(data);
          setStatus("ready");
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err.response?.data?.message || "Could not reach the server");
          setStatus("error");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [userId, type]);

  return (
    <main className="mx-auto max-w-5xl p-8">
      <h1 className="text-3xl font-bold">Recommended for you</h1>

      {quizGenres.length > 0 && (
        <section className="mt-8">
          <h2 className="text-2xl font-bold">From your quiz</h2>
          <p className="mt-1 text-sm">
            Picked for your reader type: {quizGenres.join(", ")}
          </p>
          {quizStatus === "loading" && <p className="mt-4">Finding books...</p>}
          {quizStatus === "error" && (
            <p role="alert" className="mt-4 font-medium">
              Could not load your quiz picks.
            </p>
          )}
          {quizStatus === "ready" && quizBooks.length === 0 && (
            <p className="mt-4">No books found for those genres yet.</p>
          )}
          {quizStatus === "ready" && quizBooks.length > 0 && (
            <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {quizBooks.map((book) => (
                <RecommendationCard key={book.id} book={book} />
              ))}
            </div>
          )}
        </section>
      )}

      <div className="mt-6 flex flex-wrap items-end gap-4">
        <label className="flex flex-col text-sm">
          User ID
          <input
            type="number"
            min="1"
            value={userId}
            onChange={(e) => setUserId(e.target.value)}
            className={`mt-1 w-28 ${inputClass}`}
          />
        </label>
        <label className="flex flex-col text-sm">
          Recommendation type
          <select
            value={type}
            onChange={(e) => setType(e.target.value)}
            className={`mt-1 ${inputClass}`}
          >
            {types.map((t) => (
              <option key={t} value={t}>
                {TYPE_LABELS[t] || t}
              </option>
            ))}
          </select>
        </label>
      </div>

      <section className="mt-8">
        {status === "loading" && <p>Finding books for you...</p>}
        {status === "error" && (
          <p role="alert" className="font-medium">
            Could not load recommendations: {error}
          </p>
        )}
        {status === "ready" && books.length === 0 && (
          <p>
            No recommendations yet. Rate a few books you enjoyed and check back.
          </p>
        )}
        {status === "ready" && books.length > 0 && (
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {books.map((book) => (
              <RecommendationCard key={book.id} book={book} />
            ))}
          </div>
        )}
      </section>

      <PopularRows />
    </main>
  );
}
