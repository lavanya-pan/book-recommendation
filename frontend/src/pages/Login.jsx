import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [fieldErrors, setFieldErrors] = useState({});
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    function validate() {
        const nextErrors = {};
        const trimmedEmail = email.trim();

        if (!trimmedEmail) {
            nextErrors.email = "Email is required.";
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(trimmedEmail)) {
            nextErrors.email = "Please enter a valid email address.";
        }

        if (!password) {
            nextErrors.password = "Password is required.";
        } else if (password.length < 6) {
            nextErrors.password = "Password must be at least 6 characters.";
        }

        setFieldErrors(nextErrors);
        return Object.keys(nextErrors).length === 0;
    }

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");

        if (!validate()) {
            return;
        }

        setSubmitting(true);

        try {
            await login(email.trim(), password);
            const redirectTo = location.state?.from?.pathname || "/";
            navigate(redirectTo, { replace: true });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                err.message ||
                "Login failed. Please check your details."
            );
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="mx-auto max-w-md p-8">
            <h1 className="text-3xl font-bold">Welcome back</h1>

            {location.state?.message && (
                <p className="mt-3 text-sm text-green-600">
                    {location.state.message}
                </p>
            )}

            <p className="mt-2" style={{ color: "var(--color-muted)" }}>
                Log in to your book recommendation account.
            </p>

            <form onSubmit={handleSubmit} noValidate className="mt-6 space-y-4">
                <label className="block">
                    <span className="text-sm font-medium">Email</span>
                    <input
                        type="email"
                        autoComplete="email"
                        value={email}
                        onChange={(e) => {
                            setEmail(e.target.value);
                            setFieldErrors((prev) => ({ ...prev, email: "" }));
                        }}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                    {fieldErrors.email && (
                        <p role="alert" className="mt-1 text-sm text-red-600">
                            {fieldErrors.email}
                        </p>
                    )}
                </label>

                <label className="block">
                    <span className="text-sm font-medium">Password</span>
                    <input
                        type="password"
                        autoComplete="current-password"
                        value={password}
                        onChange={(e) => {
                            setPassword(e.target.value);
                            setFieldErrors((prev) => ({ ...prev, password: "" }));
                        }}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                    {fieldErrors.password && (
                        <p role="alert" className="mt-1 text-sm text-red-600">
                            {fieldErrors.password}
                        </p>
                    )}
                </label>

                {error && (
                    <p role="alert" className="text-sm text-red-600">
                        {error}
                    </p>
                )}

                <button
                    type="submit"
                    disabled={submitting}
                    className="w-full rounded-lg p-3 font-semibold disabled:opacity-60"
                    style={{
                        background: "var(--color-accent)",
                        color: "var(--color-accent-text)",
                    }}
                >
                    {submitting ? "Logging in..." : "Log in"}
                </button>
            </form>

            <p className="mt-5 text-sm">
                Don't have an account?{" "}
                <Link to="/register" className="font-semibold underline">
                    Register
                </Link>
            </p>
        </main>
    );
}