
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import {
    getWishlist,
    removeFromWishlist,
} from "../api/wishlist.js";

export default function Wishlist() {
    const { user } = useAuth();
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadWishlist = useCallback(async () => {
        if (!user?.id) return;

        setLoading(true);
        setError("");

        try {
            const data = await getWishlist(user.id);
            setBooks(data);
        } catch (err) {
            setError(
                err.response?.data?.message || "Could not load your wishlist."
            );
        } finally {
            setLoading(false);
        }
    }, [user?.id]);

    useEffect(() => {
        loadWishlist();
    }, [loadWishlist]);

    async function handleRemove(bookId) {
        try {
            await removeFromWishlist(user.id, bookId);
            setBooks((current) =>
                current.filter((book) => book.bookId !== bookId)
            );
        } catch (err) {
            setError(
                err.response?.data?.message || "Could not remove this book."
            );
        }
    }

    return (
        <main className="mx-auto max-w-5xl p-8">
            <h1 className="text-3xl font-bold">My Wishlist</h1>
            <p className="mt-2 opacity-80">
                Books you have saved for later.
            </p>

            {loading && <p className="mt-6">Loading your wishlist...</p>}

            {error && (
                <p className="mt-6 font-medium" role="alert">
                    {error}
                </p>
            )}

            {!loading && !error && books.length === 0 && (
                <p className="mt-6">
                    Your wishlist is empty. Visit{" "}
                    <Link to="/for-you" className="font-semibold underline">
                        For You
                    </Link>{" "}
                    to discover books.
                </p>
            )}

            {!loading && books.length > 0 && (
                <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {books.map((book) => (
                        <article
                            key={book.id}
                            className="rounded-xl border border-[var(--color-accent)] p-4 shadow-sm"
                        >
                            {book.coverUrl && (
                                <img
                                    src={book.coverUrl}
                                    alt={book.bookTitle}
                                    className="mb-3 h-48 w-full rounded-lg object-contain"
                                />
                            )}

                            <h2 className="text-lg font-semibold">
                                {book.bookTitle}
                            </h2>

                            <p className="mt-1 text-sm opacity-80">
                                {book.author}
                            </p>

                            {book.genre && (
                                <p className="mt-2 text-sm">{book.genre}</p>
                            )}

                            <button
                                type="button"
                                onClick={() => handleRemove(book.bookId)}
                                className="mt-4 rounded-lg border px-3 py-2 text-sm transition hover:opacity-70"
                            >
                                Remove from Wishlist
                            </button>
                        </article>
                    ))}
                </div>
            )}
        </main>
    );
}
