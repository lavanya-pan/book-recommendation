import BookRow from "./BookRow.jsx";
import { getTopRated, getTrending } from "../api/recommendations.js";

// Two public rows that need no login: what is trending and what is rated highest.
export default function PopularRows() {
  return (
    <>
      <BookRow title="Trending now" fetchBooks={() => getTrending(6)} hideWhenEmpty />
      <BookRow title="Top rated by readers" fetchBooks={() => getTopRated(6)} hideWhenEmpty />
    </>
  );
}
