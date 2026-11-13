import { useEffect, useState } from "react";
import RecommendationCard from "./RecommendationCard.jsx";

// A titled grid of recommended books with loading and error states.
// fetchBooks: function that returns a promise of a list of books.
// reloadKey: changing this value loads the list again (for example a book id).
// hideWhenEmpty: show nothing at all when there are no books.
export default function BookRow({ title, fetchBooks, reloadKey, hideWhenEmpty = false }) {
  const [books, setBooks] = useState([]);
  const [status, setStatus] = useState("loading"); // loading | ready | error

  useEffect(() => {
    let cancelled = false;
    setStatus("loading");
    fetchBooks()
      .then((data) => {
        if (!cancelled) {
          setBooks(data);
          setStatus("ready");
        }
      })
      .catch(() => {
        if (!cancelled) setStatus("error");
      });
    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  if (status === "ready" && books.length === 0 && hideWhenEmpty) return null;

  return (
    <section className="mt-12">
      <h2 className="text-2xl font-semibold">{title}</h2>
      {status === "loading" && <p className="mt-4">Loading...</p>}
      {status === "error" && <p className="mt-4">Could not load this list.</p>}
      {status === "ready" && books.length === 0 && (
        <p className="mt-4">Nothing to show yet.</p>
      )}
      {status === "ready" && books.length > 0 && (
        <div className="mt-4 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {books.map((book) => (
            <RecommendationCard key={book.id} book={book} />
          ))}
        </div>
      )}
    </section>
  );
}
