import { useState } from "react";
import { submitMood } from "../api/quiz.js";
import RecommendationCard from "../components/RecommendationCard.jsx";
import { Button, Card, ProgressBar } from "../components/QuizUI.jsx";

// The five mood quiz questions. "key" is the field name the backend expects.
const QUESTIONS = [
    {
        key: "mood",
        text: "What mood are you in?",
        options: [
            { value: "cozy", label: "Cozy" },
            { value: "thrilling", label: "Thrilling" },
            { value: "emotional", label: "Emotional" },
            { value: "funny", label: "Funny" },
            { value: "dark", label: "Dark" },
        ],
    },
    {
        key: "world",
        text: "Which world do you want to visit?",
        options: [
            { value: "fantasy", label: "A fantasy world" },
            { value: "real-world", label: "The real world" },
            { value: "scifi", label: "Science fiction" },
            { value: "historical", label: "The past" },
        ],
    },
    {
        key: "length",
        text: "How long should the book be?",
        options: [
            { value: "short", label: "Short (under 250 pages)" },
            { value: "medium", label: "Medium" },
            { value: "long", label: "Long (over 500 pages)" },
        ],
    },
    {
        key: "romance",
        text: "Do you want romance in the story?",
        options: [
            { value: "yes", label: "Yes, please" },
            { value: "no", label: "No, thanks" },
        ],
    },
    {
        key: "tone",
        text: "Which tone fits best?",
        options: [
            { value: "light", label: "Light and fun" },
            { value: "serious", label: "Serious and thoughtful" },
            { value: "hopeful", label: "Hopeful and uplifting" },
            { value: "mysterious", label: "Mysterious" },
        ],
    },
];

// The mood quiz: five questions, one at a time, then a grid of matching books.
export default function MoodQuiz() {
    const [index, setIndex] = useState(0);
    const [answers, setAnswers] = useState({}); // question key -> chosen value
    const [books, setBooks] = useState(null); // null until the quiz is submitted
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    const question = QUESTIONS[index];
    const chosen = answers[question.key];
    const isLast = index === QUESTIONS.length - 1;
    const percent = Math.round(((index + 1) / QUESTIONS.length) * 100);

    // Sends the answers to the backend and shows the matching books.
    function finish() {
        setSubmitting(true);
        setError("");
        submitMood({
            mood: answers.mood,
            world: answers.world,
            length: answers.length,
            romance: answers.romance === "yes",
            tone: answers.tone,
        })
            .then(setBooks)
            .catch((e) => setError(e.response?.data?.message || "Could not find books. Is the backend running?"))
            .finally(() => setSubmitting(false));
    }

    // Goes to the next question, or submits after the last one.
    function next() {
        if (isLast) {
            finish();
        } else {
            setIndex(index + 1);
        }
    }

    // Starts the quiz again from question 1.
    function retake() {
        setBooks(null);
        setAnswers({});
        setIndex(0);
        setError("");
    }

    if (books) {
        return (
            <main className="mx-auto max-w-4xl p-8">
                <h1 className="text-3xl font-bold">Books for your mood</h1>
                {books.length === 0 ? (
                    <p className="mt-4">No books matched these answers. Try different ones.</p>
                ) : (
                    <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                        {books.map((book) => (
                            <RecommendationCard key={book.id} book={book} />
                        ))}
                    </div>
                )}
                <div className="mt-6">
                    <Button onClick={retake}>Take the quiz again</Button>
                </div>
            </main>
        );
    }

    return (
        <main className="mx-auto max-w-2xl p-8">
            <h1 className="text-3xl font-bold">What are you in the mood for?</h1>

            <p className="mt-4 text-sm" style={{ color: "var(--color-muted)" }}>
                Question {index + 1} of {QUESTIONS.length}
            </p>
            <ProgressBar percent={percent} />

            <div className="mt-6">
                <Card>
                    <h2 className="text-xl font-semibold">{question.text}</h2>
                    <div className="mt-4 space-y-2">
                        {question.options.map((option) => {
                            const selected = chosen === option.value;
                            return (
                                <button
                                    key={option.value}
                                    type="button"
                                    onClick={() => setAnswers({ ...answers, [question.key]: option.value })}
                                    className="w-full rounded p-3 text-left"
                                    style={{
                                        border: `2px solid ${selected ? "var(--color-accent)" : "var(--color-border)"}`,
                                        background: selected ? "var(--color-accent)" : "transparent",
                                        color: selected ? "var(--color-accent-text)" : "var(--color-text)",
                                    }}
                                >
                                    {option.label}
                                </button>
                            );
                        })}
                    </div>
                </Card>
            </div>

            {error && (
                <p className="mt-3" style={{ color: "var(--color-accent)" }}>
                    {error}
                </p>
            )}

            <div className="mt-6 flex justify-between">
                <Button onClick={() => setIndex(index - 1)} disabled={index === 0 || submitting}>
                    Back
                </Button>
                <Button primary onClick={next} disabled={!chosen || submitting}>
                    {submitting ? "Finding books..." : isLast ? "Find my books" : "Next"}
                </Button>
            </div>
        </main>
    );
}