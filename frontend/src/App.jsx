import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home.jsx";

// Add your page routes here, one small commit at a time to avoid merge conflicts.
// Examples: /browse, /books/:id, /login, /quiz, /for-you, /profile
export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
    </Routes>
  );
}
