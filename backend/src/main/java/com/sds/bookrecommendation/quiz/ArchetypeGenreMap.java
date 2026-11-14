package com.sds.bookrecommendation.quiz;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Maps each reader type to genre weights between 0 and 1 (higher means a better match).
 * This is the one place that holds these numbers. The recommendation module reads it.
 */
public final class ArchetypeGenreMap {

    private static final Map<Archetype, Map<String, Double>> WEIGHTS = new EnumMap<>(Archetype.class);

    static {
        WEIGHTS.put(Archetype.ADVENTURER, Map.of(
                "fantasy", 0.9, "adventure", 0.8, "sci-fi", 0.5, "thriller", 0.4));
        WEIGHTS.put(Archetype.DREAMER, Map.of(
                "fantasy", 0.8, "romance", 0.7, "poetry", 0.6, "young-adult", 0.5));
        WEIGHTS.put(Archetype.DETECTIVE, Map.of(
                "mystery", 0.9, "crime", 0.8, "thriller", 0.8, "horror", 0.3));
        WEIGHTS.put(Archetype.THINKER, Map.of(
                "philosophy", 0.9, "sci-fi", 0.7, "non-fiction", 0.6, "classics", 0.5));
        WEIGHTS.put(Archetype.SCHOLAR, Map.of(
                "non-fiction", 0.9, "history", 0.8, "biography", 0.7, "science", 0.6, "classics", 0.5));
        WEIGHTS.put(Archetype.COMFORT_READER, Map.of(
                "romance", 0.8, "contemporary", 0.7, "humor", 0.6, "young-adult", 0.5));
    }

    private ArchetypeGenreMap() {
    }

    /** Returns the genre weights for a reader type, or an empty map when the type is null. */
    public static Map<String, Double> weightsFor(Archetype archetype) {
        return archetype == null ? Map.of() : WEIGHTS.get(archetype);
    }

    /** Same as above, but takes the text saved on the user, such as "Adventurer". */
    public static Map<String, Double> weightsFor(String readerType) {
        return weightsFor(Archetype.fromText(readerType));
    }

    /** Returns up to {@code limit} genres of a reader type, best match first. */
    public static List<String> topGenres(Archetype archetype, int limit) {
        return weightsFor(archetype).entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.naturalOrder())))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}