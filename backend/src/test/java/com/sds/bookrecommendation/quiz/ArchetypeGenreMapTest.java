package com.sds.bookrecommendation.quiz;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the archetype-to-genre map and the archetype lookup by text.
 */
class ArchetypeGenreMapTest {

    /** Every reader type has genres, and every weight is between 0 and 1. */
    @Test
    void everyArchetypeHasWeightsBetweenZeroAndOne() {
        for (Archetype archetype : Archetype.values()) {
            var weights = ArchetypeGenreMap.weightsFor(archetype);
            assertFalse(weights.isEmpty(), archetype + " has no genres");
            weights.values().forEach(w -> assertTrue(w > 0 && w <= 1.0));
        }
    }

    /** The Adventurer's best genre is fantasy. */
    @Test
    void adventurerPrefersFantasyFirst() {
        assertEquals("fantasy", ArchetypeGenreMap.topGenres(Archetype.ADVENTURER, 1).get(0));
    }

    /** The map can be looked up with the text saved on the user. */
    @Test
    void weightsCanBeLookedUpByText() {
        assertEquals(ArchetypeGenreMap.weightsFor(Archetype.COMFORT_READER),
                ArchetypeGenreMap.weightsFor("Comfort Reader"));
    }

    /** An unknown or missing reader type gives an empty map, not an error. */
    @Test
    void unknownReaderTypeGivesEmptyMap() {
        assertTrue(ArchetypeGenreMap.weightsFor("Wizard").isEmpty());
        assertTrue(ArchetypeGenreMap.weightsFor((String) null).isEmpty());
    }
}