import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { registerUser } from "../api/auth.js";

export default function Register() {
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();
        setError("");
        setSubmitting(true);

        try {
            await registerUser(name.trim(), email.trim(), password);

            navigate("/login", {
                state: { message: "Registration successful! Please log in." },
            });
        } catch (err) {
            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                err.message ||
                "Registration failed. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="mx-auto max-w-md p-8">
            <h1 className="text-3xl font-bold">Create an account</h1>
            <p className="mt-2" style={{ color: "var(--color-muted)" }}>
                Join us and discover your next favourite book.
            </p>

            <form onSubmit={handleSubmit} className="mt-6 space-y-4">
                <label className="block">
                    <span className="text-sm font-medium">Name</span>
                    <input
                        type="text"
                        required
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                </label>

                <label className="block">
                    <span className="text-sm font-medium">Email</span>
                    <input
                        type="email"
                        required
                        autoComplete="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
                </label>

                <label className="block">
                    <span className="text-sm font-medium">Password</span>
                    <input
                        type="password"
                        required
                        minLength={6}
                        autoComplete="new-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        className="mt-1 w-full rounded-lg border p-3"
                        style={{
                            background: "var(--color-surface)",
                            color: "var(--color-text)",
                            borderColor: "var(--color-border)",
                        }}
                    />
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
                    {submitting ? "Creating account..." : "Register"}
                </button>
            </form>

            <p className="mt-5 text-sm">
                Already have an account?{" "}
                <Link to="/login" className="font-semibold underline">
                    Log in
                </Link>
            </p>
        </main>
    );
}