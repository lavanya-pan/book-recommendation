import { createContext, useContext, useState } from "react";
import { loginUser } from "../api/auth.js";

const AuthContext = createContext(null);

function getSavedUser() {
    try {
        const saved = localStorage.getItem("user");
        return saved ? JSON.parse(saved) : null;
    } catch {
        return null;
    }
}

export function AuthProvider({ children }) {
    const [user, setUser] = useState(getSavedUser);

    async function login(email, password) {
        const data = await loginUser(email, password);

        if (!data.token) {
            throw new Error("The server did not return an authentication token.");
        }

        const loggedInUser = {
            id: data.id,
            name: data.name,
            email: data.email,
        };

        localStorage.setItem("token", data.token);
        localStorage.setItem("user", JSON.stringify(loggedInUser));
        setUser(loggedInUser);

        return loggedInUser;
    }

    function logout() {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        setUser(null);
    }

    const isLoggedIn = Boolean(
        user && localStorage.getItem("token")
    );

    return (
        <AuthContext.Provider value={{ user, login, logout, isLoggedIn }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error("useAuth must be used inside AuthProvider");
    }

    return context;
}