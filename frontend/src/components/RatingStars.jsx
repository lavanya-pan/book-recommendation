import { useEffect, useState } from "react";
import { createRating, deleteRating, getUserRatings, updateRating } from "../api/ratings.js";

const STAR_NUMBERS = [1, 2, 3, 4, 5];

// One star that is filled from 0 to 1 (for example 0.5 is a half star).
function Star({ fill }) {
    return (
        <span className="relative inline-block leading-none" aria-hidden="true">
      <span style={{ color: "var(--color-border)" }}>★</span>
      <span
          className="absolute left-0 top-0 overflow-hidden"
          style={{ width: `${fill * 100}%`, color: "var(--color-accent)" }}
      >
        ★
      </span>
    </span>
    );
}

// Five stars. With bookId and userId the user can click to rate (click the same star
// again to remove the rating). With readOnly and value it just shows an average.
export default function RatingStars({ bookId, userId, value = 0, readOnly = false, onRated }) {
    const interactive = !readOnly && bookId != null && userId != null;
    const [rating, setRating] = useState(null); // { id, score } or null
    const [hover, setHover] = useState(0);
    const [busy, setBusy] = useState(false);
    const [error, setError] = useState("");

    // Loads this user's existing rating for the book, if there is one.
    useEffect(() => {
        if (!interactive) return undefined;
        let cancelled = false;
        getUserRatings(userId)
            .then((list) => {
                if (cancelled) return;
                const mine = list.find((r) => r.bookId === bookId);
                setRating(mine ? { id: mine.id, score: mine.score } : null);
            })
            .catch(() => {
                if (!cancelled) setError("Could not load your rating");
            });
        return () => {
            cancelled = true;
        };
    }, [interactive, userId, bookId]);

    // Handles a click on star number "score": create, change or remove the rating.
    async function handleClick(score) {
        if (!interactive || busy) return;
        setBusy(true);
        setError("");
        try {
            if (rating && rating.score === score) {
                await deleteRating(rating.id);
                setRating(null);
                if (onRated) onRated(null);
            } else if (rating) {
                const saved = await updateRating(rating.id, score);
                setRating({ id: saved.id, score: saved.score });
                if (onRated) onRated(saved.score);
            } else {
                const saved = await createRating(userId, bookId, score);
                setRating({ id: saved.id, score: saved.score });
                if (onRated) onRated(saved.score);
            }
        } catch (e) {
            setError(e.response?.data?.message || "Could not save your rating");
        } finally {
            setBusy(false);
        }
    }

    const shown = interactive ? hover || rating?.score || 0 : value;

    if (!interactive) {
        return (
            <span
                className="inline-flex gap-0.5 text-xl"
                role="img"
                aria-label={`Rated ${Number(value).toFixed(1)} out of 5`}
            >
        {STAR_NUMBERS.map((n) => (
            <Star key={n} fill={Math.min(1, Math.max(0, shown - (n - 1)))} />
        ))}
      </span>
        );
    }

    return (
        <div>
            <div className="inline-flex gap-1 text-2xl" onMouseLeave={() => setHover(0)}>
                {STAR_NUMBERS.map((n) => (
                    <button
                        key={n}
                        type="button"
                        disabled={busy}
                        onClick={() => handleClick(n)}
                        onMouseEnter={() => setHover(n)}
                        aria-label={`Rate ${n} out of 5`}
                        className="cursor-pointer disabled:opacity-60"
                    >
                        <Star fill={n <= shown ? 1 : 0} />
                    </button>
                ))}
            </div>
            <p className="mt-1 text-sm" style={{ color: "var(--color-muted)" }}>
                {rating ? `Your rating: ${rating.score} / 5` : "Click a star to rate"}
            </p>
            {error && (
                <p className="mt-1 text-sm" style={{ color: "var(--color-accent)" }}>
                    {error}
                </p>
            )}
        </div>
    );
}