import BookRow from "./BookRow.jsx";
import { getSimilarBooks } from "../api/recommendations.js";

// "Similar books" row for a book detail page. Usage: <SimilarBooks bookId={id} />
// It shows nothing when there are no similar books.
export default function SimilarBooks({ bookId, limit = 6 }) {
  return (
    <BookRow
      title="Similar books"
      reloadKey={bookId}
      hideWhenEmpty
      fetchBooks={() => getSimilarBooks(bookId, limit)}
    />
  );
}
