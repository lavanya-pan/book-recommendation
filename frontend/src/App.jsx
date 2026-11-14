import { Routes, Route } from "react-router-dom";
import Home from "./pages/Home.jsx";
import ForYou from "./pages/ForYou.jsx";
import ThemeSwitcher from "./components/ThemeSwitcher.jsx";

// Add your page routes here, one small commit at a time to avoid merge conflicts.
// Examples: /browse, /books/:id, /login, /quiz, /for-you, /profile
export default function App() {
    return (
        <>
            {/* Temporary position: move ThemeSwitcher into the Navbar when it exists. */}
            <div className="fixed right-4 top-4">
                <ThemeSwitcher />
            </div>
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/for-you" element={<ForYou />} />
            </Routes>
        </>
    );
}