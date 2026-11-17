
import { useEffect, useState } from "react";
import {
    getMyProfile,
    updateMyReaderType,
    updateMyProfile,
} from "../api/profile.js";

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
    const [profile, setProfile] = useState(null);
    const [editName, setEditName] = useState("");
    const [editEmail, setEditEmail] = useState("");
    const [selectedReaderType, setSelectedReaderType] = useState("");

    const [loading, setLoading] = useState(true);
    const [savingProfile, setSavingProfile] = useState(false);
    const [savingReaderType, setSavingReaderType] = useState(false);

    const [profileError, setProfileError] = useState("");
    const [profileSuccess, setProfileSuccess] = useState("");
    const [readerError, setReaderError] = useState("");
    const [readerSuccess, setReaderSuccess] = useState("");

    async function loadProfile() {
        try {
            setProfileError("");

            const data = await getMyProfile();

            setProfile(data);
            setEditName(data.name || "");
            setEditEmail(data.email || "");
            setSelectedReaderType(data.readerType || "");

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

    // Load profile when the page opens
    useEffect(() => {
        loadProfile();
    }, []);

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
                setProfileSuccess(
                    "Your profile was updated successfully!"
                );
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
        <main className="mx-auto max-w-3xl p-6">
            <h1 className="mb-2 text-3xl font-bold">
                My Profile
            </h1>

            <p className="mb-8 opacity-70">
                Manage your account and reading preferences.
            </p>

            {/* Account Information */}
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

            {/* Reading Preferences */}
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
                                (type) => type.value === selectedReaderType
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
                        selectedReaderType === (profile.readerType || "")
                    }
                    className="mt-6 rounded-lg border px-6 py-3 font-medium transition hover:opacity-70 disabled:cursor-not-allowed disabled:opacity-40"
                >
                    {savingReaderType ? "Saving..." : "Save Reader Type"}
                </button>
            </section>
        </main>
    );
}
