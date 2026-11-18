import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
    getMyProfile,
    getMyRatings,
    updateMyProfile,
    updateMyReaderType,
    updateMyTheme,
} from "../api/profile.js";
import { useAuth } from "../context/AuthContext.jsx";
import ThemeSwitcher from "../components/ThemeSwitcher.jsx";
import Wishlist from "./Wishlist.jsx";
import ReadingHistory from "./ReadingHistory.jsx";

const readerTypes = [
    {
        value: "Adventurer",
        description: "You love action, journeys and new worlds.",
    },
    {
        value: "Dreamer",
        description: "You enjoy imagination, feelings and beautiful language.",
    },
    {
        value: "Detective",
        description: "You enjoy puzzles, clues and unexpected twists.",
    },
    {
        value: "Thinker",
        description: "You like big ideas and stories that make you think.",
    },
    {
        value: "Scholar",
        description: "You read to learn about history, science and real lives.",
    },
    {
        value: "Comfort Reader",
        description: "You enjoy warm, familiar and feel-good stories.",
    },
];

const TABS = [
    { id: "account", label: "Account" },
    { id: "wishlist", label: "Wishlist" },
    { id: "history", label: "Reading History" },
    { id: "ratings", label: "My Ratings" },
    { id: "theme", label: "Theme" },
];

function formatDate(dateValue) {
    if (!dateValue) return "Not available";

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) return dateValue;

    return date.toLocaleDateString("en-IN", {
        day: "numeric",
        month: "long",
        year: "numeric",
    });
}

export default function Profile() {
    const { user } = useAuth();
    const [activeTab, setActiveTab] = useState("account");

    const [profile, setProfile] = useState(null);
    const [editName, setEditName] = useState("");
    const [editEmail, setEditEmail] = useState("");
    const [selectedReaderType, setSelectedReaderType] = useState("");
    const [selectedTheme, setSelectedTheme] = useState("LIGHT");

    const [ratings, setRatings] = useState([]);
    const [ratingsLoading, setRatingsLoading] = useState(false);
    const [ratingsError, setRatingsError] = useState("");

    const [loading, setLoading] = useState(true);
    const [savingProfile, setSavingProfile] = useState(false);
    const [savingReaderType, setSavingReaderType] = useState(false);
    const [savingTheme, setSavingTheme] = useState(false);

    const [profileError, setProfileError] = useState("");
    const [profileSuccess, setProfileSuccess] = useState("");
    const [readerError, setReaderError] = useState("");
    const [readerSuccess, setReaderSuccess] = useState("");
    const [themeError, setThemeError] = useState("");
    const [themeSuccess, setThemeSuccess] = useState("");

    async function loadProfile() {
        try {
            setProfileError("");

            const data = await getMyProfile();

            setProfile(data);
            setEditName(data.name || "");
            setEditEmail(data.email || "");
            setSelectedReaderType(data.readerType || "");
            setSelectedTheme(data.theme || "LIGHT");

            return data;
        } catch (err) {
            console.error("Profile loading error:", err);

            setProfileError(
                err.response?.data?.message ||
                "Unable to load your profile. Please log in again."
            );

            return null;
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadProfile();
    }, []);

    useEffect(() => {
        if (activeTab !== "ratings" || !user?.id) return;

        let cancelled = false;

        async function loadRatings() {
            setRatingsLoading(true);
            setRatingsError("");

            try {
                const data = await getMyRatings(user.id);
                if (!cancelled) {
                    setRatings(Array.isArray(data) ? data : []);
                }
            } catch (err) {
                if (!cancelled) {
                    setRatingsError(
                        err.response?.data?.message ||
                        "Could not load your ratings."
                    );
                }
            } finally {
                if (!cancelled) {
                    setRatingsLoading(false);
                }
            }
        }

        loadRatings();

        return () => {
            cancelled = true;
        };
    }, [activeTab, user?.id]);

    async function handleSaveProfile() {
        const name = editName.trim();
        const email = editEmail.trim();

        if (!name || !email) {
            setProfileError("Name and email cannot be empty.");
            return;
        }

        const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

        if (!emailPattern.test(email)) {
            setProfileError("Please enter a valid email address.");
            return;
        }

        try {
            setSavingProfile(true);
            setProfileError("");
            setProfileSuccess("");

            await updateMyProfile(name, email);

            const updatedProfile = await loadProfile();

            if (updatedProfile) {
                setProfileSuccess("Your profile was updated successfully!");
            }
        } catch (err) {
            console.error("Profile update error:", err);

            setProfileError(
                err.response?.data?.message ||
                "Unable to update your profile."
            );
        } finally {
            setSavingProfile(false);
        }
    }

    async function handleSaveReaderType() {
        if (!selectedReaderType) {
            setReaderError("Please select a reader type.");
            return;
        }

        try {
            setSavingReaderType(true);
            setReaderError("");
            setReaderSuccess("");

            await updateMyReaderType(selectedReaderType);

            const updatedProfile = await loadProfile();

            if (updatedProfile) {
                setReaderSuccess(
                    "Your reader type was updated successfully!"
                );
            }
        } catch (err) {
            console.error("Reader type update error:", err);

            setReaderError(
                err.response?.data?.message ||
                "Unable to update your reader type."
            );
        } finally {
            setSavingReaderType(false);
        }
    }

    async function handleSaveTheme() {
        try {
            setSavingTheme(true);
            setThemeError("");
            setThemeSuccess("");

            await updateMyTheme(selectedTheme);

            const updatedProfile = await loadProfile();

            if (updatedProfile) {
                setThemeSuccess("Your theme preference was saved!");
            }
        } catch (err) {
            console.error("Theme update error:", err);

            setThemeError(
                err.response?.data?.message ||
                "Unable to update your theme preference."
            );
        } finally {
            setSavingTheme(false);
        }
    }

    if (loading) {
        return (
            <p className="p-6 text-center">
                Loading your profile...
            </p>
        );
    }

    if (!profile) {
        return (
            <div className="p-6 text-center">
                <p className="text-red-500">
                    {profileError || "Profile could not be loaded."}
                </p>

                <button
                    type="button"
                    onClick={() => {
                        setLoading(true);
                        loadProfile();
                    }}
                    className="mt-4 rounded-lg border px-5 py-2"
                >
                    Try Again
                </button>
            </div>
        );
    }

    return (
        <main className="mx-auto max-w-5xl p-6">
            <h1 className="mb-2 text-3xl font-bold">
                My Profile
            </h1>

            <p className="mb-6 opacity-70">
                Manage your account, saved books, ratings, and reading preferences.
            </p>

            {/* Profile Navigation Tabs */}
            <div
                role="tablist"
                aria-label="Profile sections"
                className="mb-8 flex flex-wrap gap-2 border-b pb-3"
            >
                {TABS.map((tab) => {
                    const isActive = activeTab === tab.id;
                    return (
                        <button
                            key={tab.id}
                            type="button"
                            role="tab"
                            aria-selected={isActive}
                            onClick={() => setActiveTab(tab.id)}
                            className={`rounded-lg px-4 py-2 text-sm font-semibold transition ${
                                isActive
                                    ? "bg-red-600 text-white"
                                    : "border hover:opacity-80"
                            }`}
                        >
                            {tab.label}
                        </button>
                    );
                })}
            </div>

            {/* Tab 1: Account & Reading Preferences */}
            {activeTab === "account" && (
                <div className="mx-auto max-w-3xl">
                    <section className="mb-8 rounded-xl border p-6">
                        <h2 className="mb-5 text-xl font-semibold">
                            Account Information
                        </h2>

                        <div className="space-y-4">
                            <div>
                                <label
                                    htmlFor="editName"
                                    className="mb-2 block font-medium"
                                >
                                    Name
                                </label>

                                <input
                                    id="editName"
                                    type="text"
                                    value={editName}
                                    onChange={(event) => {
                                        setEditName(event.target.value);
                                        setProfileError("");
                                        setProfileSuccess("");
                                    }}
                                    className="w-full rounded-lg border p-3"
                                    placeholder="Enter your name"
                                />
                            </div>

                            <div>
                                <label
                                    htmlFor="editEmail"
                                    className="mb-2 block font-medium"
                                >
                                    Email
                                </label>

                                <input
                                    id="editEmail"
                                    type="email"
                                    value={editEmail}
                                    onChange={(event) => {
                                        setEditEmail(event.target.value);
                                        setProfileError("");
                                        setProfileSuccess("");
                                    }}
                                    className="w-full rounded-lg border p-3"
                                    placeholder="Enter your email"
                                />
                            </div>

                            <p>
                                <strong>Role:</strong>{" "}
                                {profile.role || "USER"}
                            </p>

                            <p>
                                <strong>Account Created:</strong>{" "}
                                {formatDate(profile.createdAt)}
                            </p>
                        </div>

                        {profileError && (
                            <p className="mt-4 text-sm text-red-600">
                                {profileError}
                            </p>
                        )}

                        {profileSuccess && (
                            <p className="mt-4 text-sm text-green-600">
                                {profileSuccess}
                            </p>
                        )}

                        <button
                            type="button"
                            onClick={handleSaveProfile}
                            disabled={
                                savingProfile ||
                                (
                                    editName.trim() === (profile.name || "") &&
                                    editEmail.trim() === (profile.email || "")
                                )
                            }
                            className="mt-6 rounded-lg border px-6 py-3 font-medium transition hover:opacity-70 disabled:cursor-not-allowed disabled:opacity-40"
                        >
                            {savingProfile ? "Saving..." : "Save Profile"}
                        </button>
                    </section>

                    <section className="rounded-xl border p-6">
                        <h2 className="mb-2 text-xl font-semibold">
                            Reading Preferences
                        </h2>

                        <p className="mb-5 opacity-70">
                            Choose the reader type that best describes you.
                        </p>

                        <label
                            htmlFor="readerType"
                            className="mb-2 block font-medium"
                        >
                            Select Reader Type
                        </label>

                        <select
                            id="readerType"
                            value={selectedReaderType}
                            onChange={(event) => {
                                setSelectedReaderType(event.target.value);
                                setReaderSuccess("");
                                setReaderError("");
                            }}
                            className="w-full rounded-lg border p-3"
                        >
                            <option value="">
                                Choose your reader type
                            </option>

                            {readerTypes.map((type) => (
                                <option
                                    key={type.value}
                                    value={type.value}
                                >
                                    {type.value}
                                </option>
                            ))}
                        </select>

                        {selectedReaderType && (
                            <p className="mt-3 text-sm opacity-70">
                                {
                                    readerTypes.find(
                                        (type) =>
                                            type.value === selectedReaderType
                                    )?.description
                                }
                            </p>
                        )}

                        {readerError && (
                            <p className="mt-4 text-sm text-red-600">
                                {readerError}
                            </p>
                        )}

                        {readerSuccess && (
                            <p className="mt-4 text-sm text-green-600">
                                {readerSuccess}
                            </p>
                        )}

                        <button
                            type="button"
                            onClick={handleSaveReaderType}
                            disabled={
                                savingReaderType ||
                                !selectedReaderType ||
                                selectedReaderType ===
                                    (profile.readerType || "")
                            }
                            className="mt-6 rounded-lg border px-6 py-3 font-medium transition hover:opacity-70 disabled:cursor-not-allowed disabled:opacity-40"
                        >
                            {savingReaderType
                                ? "Saving..."
                                : "Save Reader Type"}
                        </button>
                    </section>
                </div>
            )}

            {/* Tab 2: Wishlist */}
            {activeTab === "wishlist" && <Wishlist />}

            {/* Tab 3: Reading History */}
            {activeTab === "history" && <ReadingHistory />}

            {/* Tab 4: My Ratings */}
            {activeTab === "ratings" && (
                <section className="mx-auto max-w-3xl rounded-xl border p-6">
                    <h2 className="mb-2 text-xl font-semibold">
                        My Ratings
                    </h2>
                    <p className="mb-6 opacity-70">
                        Books you have rated and reviewed.
                    </p>

                    {ratingsLoading && <p>Loading your ratings...</p>}

                    {ratingsError && (
                        <p role="alert" className="text-sm text-red-600">
                            {ratingsError}
                        </p>
                    )}

                    {!ratingsLoading && !ratingsError && ratings.length === 0 && (
                        <p>
                            You haven&apos;t rated any books yet. Visit{" "}
                            <Link to="/for-you" className="font-semibold underline">
                                For You
                            </Link>{" "}
                            to explore books.
                        </p>
                    )}

                    {!ratingsLoading && ratings.length > 0 && (
                        <div className="grid gap-4 sm:grid-cols-2">
                            {ratings.map((item) => (
                                <article
                                    key={item.id || item.bookId}
                                    className="rounded-xl border p-4 shadow-sm"
                                >
                                    <h3 className="text-lg font-semibold">
                                        {item.bookTitle || item.title || `Book #${item.bookId}`}
                                    </h3>

                                    {item.author && (
                                        <p className="mt-1 text-sm opacity-80">
                                            {item.author}
                                        </p>
                                    )}

                                    <p className="mt-2 text-sm font-semibold">
                                        Rating: {item.rating ?? item.score} / 5
                                    </p>

                                    {item.review && (
                                        <p className="mt-2 text-sm opacity-80">
                                            {item.review}
                                        </p>
                                    )}
                                </article>
                            ))}
                        </div>
                    )}
                </section>
            )}

            {/* Tab 5: Theme */}
            {activeTab === "theme" && (
                <section className="mx-auto max-w-3xl rounded-xl border p-6">
                    <h2 className="mb-2 text-xl font-semibold">
                        Theme Preferences
                    </h2>
                    <p className="mb-6 opacity-70">
                        Switch your site theme or save your account display mode.
                    </p>

                    <div className="mb-6">
                        <ThemeSwitcher />
                    </div>

                    <div className="border-t pt-6">
                        <label
                            htmlFor="accountTheme"
                            className="mb-2 block font-medium"
                        >
                            Account Display Mode
                        </label>

                        <select
                            id="accountTheme"
                            value={selectedTheme}
                            onChange={(event) => {
                                setSelectedTheme(event.target.value);
                                setThemeSuccess("");
                                setThemeError("");
                            }}
                            className="w-full rounded-lg border p-3"
                        >
                            <option value="LIGHT">Light</option>
                            <option value="DARK">Dark</option>
                        </select>

                        {themeError && (
                            <p className="mt-4 text-sm text-red-600">
                                {themeError}
                            </p>
                        )}

                        {themeSuccess && (
                            <p className="mt-4 text-sm text-green-600">
                                {themeSuccess}
                            </p>
                        )}

                        <button
                            type="button"
                            onClick={handleSaveTheme}
                            disabled={
                                savingTheme ||
                                selectedTheme === (profile.theme || "LIGHT")
                            }
                            className="mt-6 rounded-lg border px-6 py-3 font-medium transition hover:opacity-70 disabled:cursor-not-allowed disabled:opacity-40"
                        >
                            {savingTheme ? "Saving..." : "Save Theme"}
                        </button>
                    </div>
                </section>
            )}
        </main>
    );
}