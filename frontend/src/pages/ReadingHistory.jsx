
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import {
    getReadingHistory,
    updateReadingStatus,
} from "../api/readingHistory.js";

const STATUS_LABELS = {
    READING: "Currently Reading",
    FINISHED: "Finished",
    DROPPED: "Dropped",
};

export default function ReadingHistory() {
    const { user } = useAuth();
    const [books, setBooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const loadHistory = useCallback(async () => {
        if (!user?.id) return;

        setLoading(true);
        setError("");

        try {
            const data = await getReadingHistory(user.id);
            setBooks(data);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                    "Could not load your reading history."
            );
        } finally {
            setLoading(false);
        }
    }, [user?.id]);

    useEffect(() => {
        loadHistory();
    }, [loadHistory]);

    async function handleStatusChange(bookId, status) {
        try {
            const updated = await updateReadingStatus(
                user.id,
                bookId,
                status
            );

            setBooks((current) =>
                current.map((book) =>
                    book.bookId === bookId ? updated : book
                )
            );
        } catch (err) {
            setError(
                err.response?.data?.message ||
                    "Could not update the reading status."
            );
        }
    }

    return (
        <main className="mx-auto max-w-5xl p-8">
            <h1 className="text-3xl font-bold">My Reading History</h1>
            <p className="mt-2 opacity-80">
                Keep track of the books you are reading.
            </p>

            {loading && <p className="mt-6">Loading reading history...</p>}

            {error && (
                <p className="mt-6 font-medium" role="alert">
                    {error}
                </p>
            )}

            {!loading && !error && books.length === 0 && (
                <p className="mt-6">
                    You haven't added any books yet. Visit{" "}
                    <Link to="/for-you" className="font-semibold underline">
                        For You
                    </Link>{" "}
                    to get started.
                </p>
            )}

            {!loading && books.length > 0 && (
                <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {books.map((book) => (
                        <article
                            key={book.id}
                            className="rounded-xl border border-[var(--color-accent)] p-4 shadow-sm"
                        >
                            <h2 className="text-lg font-semibold">
                                {book.bookTitle}
                            </h2>

                            <p className="mt-1 text-sm opacity-80">
                                {book.author}
                            </p>

                            {book.genre && (
                                <p className="mt-2 text-sm">{book.genre}</p>
                            )}

                            <label className="mt-4 flex flex-col gap-2 text-sm font-medium">
                                Reading status

                                <select
                                    value={book.status}
                                    onChange={(e) =>
                                        handleStatusChange(
                                            book.bookId,
                                            e.target.value
                                        )
                                    }
                                    className="rounded-lg border border-[var(--color-accent)] bg-[var(--color-bg)] px-3 py-2"
                                >
                                    <option value="READING">
                                        Currently Reading
                                    </option>
                                    <option value="FINISHED">Finished</option>
                                    <option value="DROPPED">Dropped</option>
                                </select>
                            </label>

                            <p className="mt-3 text-sm opacity-80">
                                Status: {STATUS_LABELS[book.status] || book.status}
                            </p>
                        </article>
                    ))}
                </div>
            )}
        </main>
    );
}
