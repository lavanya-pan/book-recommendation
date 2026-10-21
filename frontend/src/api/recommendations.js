import api from "./client.js";

// The recommendation types the backend supports (content, genre, hybrid, ...).
export async function getRecommendationTypes() {
  const res = await api.get("/recommendation-types");
  return res.data;
}

// Recommended books for one user. type and limit are optional.
export async function getRecommendations(userId, type, limit = 10) {
  const res = await api.get(`/users/${userId}/recommendations`, {
    params: { type, limit },
  });
  return res.data;
}

// Books similar to one book, for the "similar books" row on a book page.
export async function getSimilarBooks(bookId, limit = 6) {
  const res = await api.get(`/books/${bookId}/similar`, { params: { limit } });
  return res.data;
}
