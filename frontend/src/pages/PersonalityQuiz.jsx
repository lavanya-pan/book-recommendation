import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getPersonalityQuestions, submitPersonality } from "../api/quiz.js";
import { useTheme } from "../theme/ThemeProvider.jsx";

// A card with the theme's surface colour, border and corner radius.
function Card({ children }) {
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
function Button({ primary, children, ...props }) {
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

// The personality quiz: one question at a time, then the reader type result.
export default function PersonalityQuiz() {
    const { setTheme } = useTheme();
    const [questions, setQuestions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [index, setIndex] = useState(0);
    const [answers, setAnswers] = useState({}); // questionId -> optionId
    const [result, setResult] = useState(null);
    const [submitting, setSubmitting] = useState(false);

    // Loads the questions once when the page opens.
    useEffect(() => {
        getPersonalityQuestions()
            .then(setQuestions)
            .catch(() => setError("Could not load the quiz. Is the backend running?"))
            .finally(() => setLoading(false));
    }, []);

    // Sends all answers to the backend, shows the result and applies its theme.
    function finish() {
        setSubmitting(true);
        setError("");
        const payload = questions.map((q) => ({ questionId: q.id, optionId: answers[q.id] }));
        submitPersonality(payload)
            .then((res) => {
                setResult(res);
                setTheme(res.theme);
            })
            .catch((e) => setError(e.response?.data?.message || "Could not score the quiz"))
            .finally(() => setSubmitting(false));
    }

    // Goes to the next question, or submits after the last one.
    function next() {
        if (index === questions.length - 1) {
            finish();
        } else {
            setIndex(index + 1);
        }
    }

    // Starts the quiz again from question 1.
    function retake() {
        setResult(null);
        setAnswers({});
        setIndex(0);
        setError("");
    }

    if (loading) {
        return <main className="mx-auto max-w-2xl p-8">Loading the quiz...</main>;
    }

    if (error && questions.length === 0) {
        return <main className="mx-auto max-w-2xl p-8">{error}</main>;
    }

    if (result) {
        return (
            <main className="mx-auto max-w-2xl p-8">
                <Card>
                    <p className="text-sm" style={{ color: "var(--color-muted)" }}>
                        Your reader type
                    </p>
                    <h1 className="mt-1 text-4xl font-bold">{result.readerType}</h1>
                    <p className="mt-3">{result.description}</p>

                    <h2 className="mt-6 text-lg font-semibold">Genres you will probably enjoy</h2>
                    <div className="mt-2 flex flex-wrap gap-2">
                        {result.genres.map((genre) => (
                            <span
                                key={genre}
                                className="rounded-full px-3 py-1 text-sm"
                                style={{ background: "var(--color-accent)", color: "var(--color-accent-text)" }}
                            >
                {genre}
              </span>
                        ))}
                    </div>

                    <p className="mt-4 text-sm" style={{ color: "var(--color-muted)" }}>
                        The site now uses the {result.theme} theme. You can change it any time with the
                        Theme drop-down.
                    </p>

                    <div className="mt-6 flex flex-wrap gap-3">
                        <Link
                            to="/for-you"
                            className="rounded px-4 py-2 font-medium"
                            style={{ background: "var(--color-accent)", color: "var(--color-accent-text)" }}
                        >
                            See my recommendations
                        </Link>
                        <Button onClick={retake}>Retake the quiz</Button>
                    </div>
                </Card>
            </main>
        );
    }

    const question = questions[index];
    const chosen = answers[question.id];
    const isLast = index === questions.length - 1;
    const percent = Math.round(((index + 1) / questions.length) * 100);

    return (
        <main className="mx-auto max-w-2xl p-8">
            <h1 className="text-3xl font-bold">What kind of reader are you?</h1>

            <p className="mt-4 text-sm" style={{ color: "var(--color-muted)" }}>
                Question {index + 1} of {questions.length}
            </p>
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

            <div className="mt-6">
                <Card>
                    <h2 className="text-xl font-semibold">{question.text}</h2>
                    <div className="mt-4 space-y-2">
                        {question.options.map((option) => {
                            const selected = chosen === option.id;
                            return (
                                <button
                                    key={option.id}
                                    type="button"
                                    onClick={() => setAnswers({ ...answers, [question.id]: option.id })}
                                    className="w-full rounded p-3 text-left"
                                    style={{
                                        border: `2px solid ${selected ? "var(--color-accent)" : "var(--color-border)"}`,
                                        background: selected ? "var(--color-accent)" : "transparent",
                                        color: selected ? "var(--color-accent-text)" : "var(--color-text)",
                                    }}
                                >
                                    {option.text}
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
                    {submitting ? "Working it out..." : isLast ? "See my result" : "Next"}
                </Button>
            </div>
        </main>
    );
}