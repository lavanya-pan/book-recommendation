package com.sds.bookrecommendation.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Turns book texts into TF-IDF vectors and finds similar books with cosine similarity.
 * Pure Java: it knows nothing about the database or Spring, so it is easy to test.
 *
 * TF-IDF idea: a word counts more when it appears often in a book (TF) but in few
 * other books (IDF). Each book becomes a unit-length vector, so the dot product of two
 * vectors is their cosine similarity (1.0 = identical words, 0.0 = nothing in common).
 */
public class TfIdfIndex {

    /** One result: a book id and how similar it is (0 to 1). */
    public record Match(long bookId, double similarity) {
    }

    /** Very common words that say nothing about a book. */
    private static final Set<String> STOP_WORDS = Set.of(
            "the", "and", "for", "with", "that", "this", "from", "are", "was", "his", "her",
            "its", "has", "had", "you", "your", "but", "not", "who", "they", "them", "their",
            "into", "out", "one", "all", "can", "will", "about", "when", "what", "which",
            "there", "been", "have", "were", "she", "him", "our", "than", "then", "also",
            "more", "most", "some", "such", "only", "over", "after", "before");

    /** bookId -> (word -> weight), each vector scaled to length 1. */
    private final Map<Long, Map<String, Double>> vectors = new HashMap<>();

    /**
     * Builds the index.
     *
     * @param documents bookId -> the text that describes the book
     */
    public TfIdfIndex(Map<Long, String> documents) {
        int totalDocs = documents.size();

        // Step 1: count the words of every book and in how many books each word appears.
        Map<Long, Map<String, Integer>> counts = new HashMap<>();
        Map<String, Integer> docFrequency = new HashMap<>();
        for (Map.Entry<Long, String> doc : documents.entrySet()) {
            Map<String, Integer> termCounts = new HashMap<>();
            for (String token : tokenize(doc.getValue())) {
                termCounts.merge(token, 1, Integer::sum);
            }
            counts.put(doc.getKey(), termCounts);
            for (String term : termCounts.keySet()) {
                docFrequency.merge(term, 1, Integer::sum);
            }
        }

        // Step 2: weight = (1 + log(count)) x idf, then scale the vector to length 1.
        for (Map.Entry<Long, Map<String, Integer>> doc : counts.entrySet()) {
            Map<String, Double> vector = new HashMap<>();
            double squareSum = 0;
            for (Map.Entry<String, Integer> term : doc.getValue().entrySet()) {
                double idf = Math.log((1.0 + totalDocs) / (1.0 + docFrequency.get(term.getKey()))) + 1.0;
                double weight = (1.0 + Math.log(term.getValue())) * idf;
                vector.put(term.getKey(), weight);
                squareSum += weight * weight;
            }
            double length = Math.sqrt(squareSum);
            if (length > 0) {
                vector.replaceAll((term, weight) -> weight / length);
            }
            vectors.put(doc.getKey(), vector);
        }
    }

    /** Lower-cases the text and splits it into words, dropping stop words and very short words. */
    static List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null) {
            return tokens;
        }
        for (String word : text.toLowerCase().split("[^a-z0-9]+")) {
            if (word.length() >= 3 && !STOP_WORDS.contains(word)) {
                tokens.add(word);
            }
        }
        return tokens;
    }

    /**
     * Builds a taste profile: the weighted average of the vectors of the given books.
     * For example, books the user rated 5 count more than books rated 4.
     *
     * @param weightsByBookId bookId -> weight (for example the rating)
     * @return a unit-length word vector, empty if none of the books are in the index
     */
    public Map<String, Double> profile(Map<Long, Double> weightsByBookId) {
        Map<String, Double> sum = new HashMap<>();
        for (Map.Entry<Long, Double> entry : weightsByBookId.entrySet()) {
            Map<String, Double> vector = vectors.get(entry.getKey());
            if (vector == null) {
                continue;
            }
            for (Map.Entry<String, Double> term : vector.entrySet()) {
                sum.merge(term.getKey(), term.getValue() * entry.getValue(), Double::sum);
            }
        }
        double squareSum = sum.values().stream().mapToDouble(v -> v * v).sum();
        double length = Math.sqrt(squareSum);
        if (length > 0) {
            sum.replaceAll((term, weight) -> weight / length);
        }
        return sum;
    }

    /**
     * Finds the books most similar to a profile.
     *
     * @param profile the taste profile (see {@link #profile})
     * @param exclude book ids that must not be returned (for example already rated)
     * @param limit   maximum number of results
     * @return matches with similarity above 0, most similar first
     */
    public List<Match> findSimilar(Map<String, Double> profile, Set<Long> exclude, int limit) {
        if (profile.isEmpty() || limit <= 0) {
            return List.of();
        }
        List<Match> matches = new ArrayList<>();
        for (Map.Entry<Long, Map<String, Double>> book : vectors.entrySet()) {
            if (exclude.contains(book.getKey())) {
                continue;
            }
            double similarity = cosine(profile, book.getValue());
            if (similarity > 0) {
                matches.add(new Match(book.getKey(), similarity));
            }
        }
        matches.sort(Comparator.comparingDouble(Match::similarity).reversed());
        return matches.size() > limit ? new ArrayList<>(matches.subList(0, limit)) : matches;
    }

    /** Books most similar to one given book (the book itself is never returned). */
    public List<Match> similarTo(long bookId, int limit) {
        Map<String, Double> vector = vectors.get(bookId);
        if (vector == null) {
            return List.of();
        }
        return findSimilar(vector, Set.of(bookId), limit);
    }

    /** Dot product of two unit-length vectors = cosine similarity. */
    private double cosine(Map<String, Double> a, Map<String, Double> b) {
        Map<String, Double> small = a.size() <= b.size() ? a : b;
        Map<String, Double> large = small == a ? b : a;
        double dot = 0;
        for (Map.Entry<String, Double> term : small.entrySet()) {
            Double other = large.get(term.getKey());
            if (other != null) {
                dot += term.getValue() * other;
            }
        }
        return dot;
    }
}