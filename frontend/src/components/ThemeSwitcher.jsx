import { useTheme } from "../theme/ThemeProvider.jsx";

const LABELS = { fantasy: "Fantasy", mystery: "Mystery", romance: "Romance" };

// A small drop-down that lets the user pick a site theme.
export default function ThemeSwitcher() {
    const { theme, setTheme, themes } = useTheme();

    return (
        <label className="flex items-center gap-2 text-sm">
            <span>Theme</span>
            <select
                value={theme}
                onChange={(e) => setTheme(e.target.value)}
                className="rounded border px-2 py-1"
                style={{
                    background: "var(--color-surface)",
                    color: "var(--color-text)",
                    borderColor: "var(--color-border)",
                }}
            >
                <option value="">Default</option>
                {themes.map((t) => (
                    <option key={t} value={t}>
                        {LABELS[t]}
                    </option>
                ))}
            </select>
        </label>
    );
}