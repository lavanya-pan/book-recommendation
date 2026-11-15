// A card with the theme's surface colour, border and corner radius.
export function Card({ children }) {
    return (
        <div
            className="p-6"
            style={{
                background: "var(--color-surface)",
                border: "1px solid var(--color-border)",
                borderRadius: "var(--radius-card)",
            }}
        >
            {children}
        </div>
    );
}

// A themed button. "primary" uses the accent colour, otherwise it is an outline button.
export function Button({ primary, children, ...props }) {
    return (
        <button
            type="button"
            {...props}
            className="rounded px-4 py-2 font-medium disabled:cursor-not-allowed disabled:opacity-50"
            style={
                primary
                    ? { background: "var(--color-accent)", color: "var(--color-accent-text)" }
                    : { border: "1px solid var(--color-border)", color: "var(--color-text)" }
            }
        >
            {children}
        </button>
    );
}

// A progress bar that shows how far through the quiz the user is (0 to 100).
export function ProgressBar({ percent }) {
    return (
        <div
            className="mt-1 h-2 w-full overflow-hidden rounded-full"
            style={{ background: "var(--color-border)" }}
            role="progressbar"
            aria-valuenow={percent}
            aria-valuemin={0}
            aria-valuemax={100}
        >
            <div className="h-full" style={{ width: `${percent}%`, background: "var(--color-accent)" }} />
        </div>
    );
}