import { useState } from "react";
import { Routes, Route, Link, useLocation } from "react-router-dom";

import Home from "./pages/Home.jsx";
import ForYou from "./pages/ForYou.jsx";
import PersonalityQuiz from "./pages/PersonalityQuiz.jsx";
import MoodQuiz from "./pages/MoodQuiz.jsx";
import Login from "./pages/Login.jsx";
import Register from "./pages/Register.jsx";
import Profile from "./pages/Profile.jsx";
import Wishlist from "./pages/Wishlist.jsx";
import ReadingHistory from "./pages/ReadingHistory.jsx";

import ThemeSwitcher from "./components/ThemeSwitcher.jsx";
import ProtectedRoute from "./components/ProtectedRoute.jsx";
import { useAuth } from "./context/AuthContext.jsx";

export default function App() {
    const { user, logout, isLoggedIn } = useAuth();
    const location = useLocation();
    const [menuOpen, setMenuOpen] = useState(false);

    const navLinkClass = (path) =>
        `shrink-0 whitespace-nowrap px-3 py-2 text-sm font-medium transition-all duration-200 hover:text-red-500 ${
            location.pathname === path
                ? "text-red-500"
                : "text-[var(--color-text)]"
        }`;

    function closeMenu() {
        setMenuOpen(false);
    }

    function handleLogout() {
        logout();
        closeMenu();
    }

    return (
        <div className="min-h-screen w-full">

            {/* Netflix-inspired navigation */}
            <header className="w-full bg-[var(--color-bg)] text-[var(--color-text)] shadow-md">
                <nav className="mx-auto flex max-w-[1600px] items-center justify-between gap-4 px-4 py-4 md:px-8">

                    {/* Logo */}
                    <Link
                        to="/"
                        onClick={closeMenu}
                        className="shrink-0 text-xl font-extrabold tracking-tight sm:text-2xl"
                    >
                        <span className="text-red-600">BOOK</span>
                        <span>FLIX</span>
                    </Link>

                    {/* Desktop navigation */}
                    <div className="hidden flex-1 items-center justify-center gap-1 lg:flex">
                        <Link to="/" className={navLinkClass("/")}>Home</Link>
                        <Link to="/for-you" className={navLinkClass("/for-you")}>For You</Link>
                        <Link to="/quiz" className={navLinkClass("/quiz")}>Personality Quiz</Link>
                        <Link to="/mood-quiz" className={navLinkClass("/mood-quiz")}>Mood Quiz</Link>

                        {isLoggedIn && (
                            <>
                                <Link to="/wishlist" className={navLinkClass("/wishlist")}>Wishlist</Link>
                                <Link to="/reading-history" className={navLinkClass("/reading-history")}>Reading History</Link>
                            </>
                        )}
                    </div>

                    {/* Desktop account section */}
                    <div className="hidden shrink-0 items-center gap-3 lg:flex">
                        {isLoggedIn ? (
                            <>
                                <Link to="/profile" className={navLinkClass("/profile")}>
                                    {user?.name || "Profile"}
                                </Link>
                                <button
                                    onClick={handleLogout}
                                    className="rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-700"
                                >
                                    Log out
                                </button>
                            </>
                        ) : (
                            <>
                                <Link to="/login" className={navLinkClass("/login")}>Log in</Link>
                                <Link
                                    to="/register"
                                    className="rounded-md bg-red-600 px-4 py-2 text-sm font-semibold text-white transition hover:bg-red-700"
                                >
                                    Register
                                </Link>
                            </>
                        )}
                    </div>

                    {/* Mobile menu button */}
                    <button
                        type="button"
                        onClick={() => setMenuOpen(!menuOpen)}
                        className="rounded-md border border-[var(--color-text)]/30 px-3 py-2 text-xl lg:hidden"
                        aria-label="Toggle navigation"
                        aria-expanded={menuOpen}
                    >
                        {menuOpen ? "✕" : "☰"}
                    </button>
                </nav>

                {/* Mobile navigation */}
                {menuOpen && (
                    <div className="border-t border-[var(--color-text)]/15 px-3 py-2 lg:hidden">
                        <div className="flex items-center gap-2 overflow-x-auto whitespace-nowrap">
                            <Link to="/" className={navLinkClass("/")} onClick={closeMenu}>Home</Link>
                            <Link to="/for-you" className={navLinkClass("/for-you")} onClick={closeMenu}>For You</Link>
                            <Link to="/quiz" className={navLinkClass("/quiz")} onClick={closeMenu}>Personality Quiz</Link>
                            <Link to="/mood-quiz" className={navLinkClass("/mood-quiz")} onClick={closeMenu}>Mood Quiz</Link>

                            {isLoggedIn && (
                                <>
                                    <Link to="/wishlist" className={navLinkClass("/wishlist")} onClick={closeMenu}>Wishlist</Link>
                                    <Link to="/reading-history" className={navLinkClass("/reading-history")} onClick={closeMenu}>Reading History</Link>
                                    <Link to="/profile" className={navLinkClass("/profile")} onClick={closeMenu}>
                                        {user?.name || "Profile"}
                                    </Link>
                                </>
                            )}

                            {isLoggedIn ? (
                                <button
                                    onClick={handleLogout}
                                    className="shrink-0 rounded-md bg-red-600 px-3 py-2 text-sm font-semibold text-white"
                                >
                                    Log out
                                </button>
                            ) : (
                                <>
                                    <Link to="/login" className={navLinkClass("/login")} onClick={closeMenu}>Log in</Link>
                                    <Link to="/register" className={navLinkClass("/register")} onClick={closeMenu}>Register</Link>
                                </>
                            )}
                        </div>
                    </div>
                )}
            </header>

            {/* Theme switcher */}
            <div className="fixed bottom-4 right-4 z-30">
                <ThemeSwitcher />
            </div>

            {/* Page content */}
            <main className="w-full min-w-0">
                <Routes>
                    <Route path="/" element={<Home />} />
                    <Route path="/for-you" element={<ForYou />} />
                    <Route path="/quiz" element={<PersonalityQuiz />} />
                    <Route path="/mood-quiz" element={<MoodQuiz />} />
                    <Route path="/login" element={<Login />} />
                    <Route path="/register" element={<Register />} />

                    <Route
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <Profile />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/wishlist"
                        element={
                            <ProtectedRoute>
                                <Wishlist />
                            </ProtectedRoute>
                        }
                    />
                    <Route
                        path="/reading-history"
                        element={
                            <ProtectedRoute>
                                <ReadingHistory />
                            </ProtectedRoute>
                        }
                    />
                </Routes>
            </main>
        </div>
    );
}