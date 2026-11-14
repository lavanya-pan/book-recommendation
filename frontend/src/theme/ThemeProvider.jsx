import { createContext, useContext, useEffect, useState } from "react";

// The themes the site offers. "" means the default look.
export const THEMES = ["fantasy", "mystery", "romance"];

const STORAGE_KEY = "theme";

const ThemeContext = createContext({ theme: "", setTheme: () => {}, themes: THEMES });

// Reads the saved theme name, ignoring anything that is not a known theme.
function loadTheme() {
    try {
        const saved = localStorage.getItem(STORAGE_KEY);
        return THEMES.includes(saved) ? saved : "";
    } catch {
        return "";
    }
}

// Keeps the current theme, sets data-theme on the root element and remembers the choice.
export function ThemeProvider({ children }) {
    const [theme, setThemeState] = useState(loadTheme);

    useEffect(() => {
        const root = document.documentElement;
        if (theme) {
            root.setAttribute("data-theme", theme);
        } else {
            root.removeAttribute("data-theme");
        }
    }, [theme]);

    // Changes the theme; an unknown name goes back to the default look.
    function setTheme(next) {
        const value = THEMES.includes(next) ? next : "";
        setThemeState(value);
        try {
            if (value) {
                localStorage.setItem(STORAGE_KEY, value);
            } else {
                localStorage.removeItem(STORAGE_KEY);
            }
        } catch {
            // Storage can be blocked; the theme still works for this visit.
        }
    }

    return (
        <ThemeContext.Provider value={{ theme, setTheme, themes: THEMES }}>
            {children}
        </ThemeContext.Provider>
    );
}

// Lets any component read or change the theme: const { theme, setTheme } = useTheme();
export function useTheme() {
    return useContext(ThemeContext);
}