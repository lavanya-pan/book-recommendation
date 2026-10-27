import { useEffect, useState } from "react";
import RecommendationCard from "../components/RecommendationCard.jsx";
import {
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
  // Until login exists (Person 2), the user is picked by typing an ID.
  const [userId, setUserId] = useState("1");
  const [types, setTypes] = useState(["hybrid"]);
  const [type, setType] = useState("hybrid");
  const [books, setBooks] = useState([]);
  const [status, setStatus] = useState("loading"); // loading | ready | error
  const [error, setError] = useState("");

  // Load the list of recommendation types once.
  useEffect(() => {
    getRecommendationTypes()
      .then(setTypes)
      .catch(() => {});
  }, []);

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
    </main>
  );
}
