import api from "./client.js";

// Returns all personality questions with their answer options.
export async function getPersonalityQuestions() {
    const res = await api.get("/quiz/personality/questions");
    return res.data;
}

// Sends one answer per question ([{ questionId, optionId }, ...]) and returns the reader type.
export async function submitPersonality(answers) {
    const res = await api.post("/quiz/personality", { answers });
    return res.data;
}

// Sends the mood quiz answers and returns matching books.
export async function submitMood(answers, limit = 10) {
    const res = await api.post("/quiz/mood", answers, { params: { limit } });
    return res.data;
}