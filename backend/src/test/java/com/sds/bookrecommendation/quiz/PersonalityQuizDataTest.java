package com.sds.bookrecommendation.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Checks that the personality questions file loads and only uses real archetypes.
 */
class PersonalityQuizDataTest {

    private final PersonalityQuizData data = new PersonalityQuizData(new ObjectMapper());

    /** The file has 8 questions and each has options. */
    @Test
    void loadsEightQuestionsWithOptions() {
        assertEquals(8, data.getQuestions().size());
        data.getQuestions().forEach(q -> assertFalse(q.options().isEmpty()));
    }

    /** Every point key in the file is a valid archetype name. */
    @Test
    void everyPointKeyIsAKnownArchetype() {
        data.getQuestions().forEach(q -> q.options().forEach(o ->
                o.points().keySet().forEach(key -> assertNotNull(Archetype.fromText(key), key))));
    }

    /** All six archetypes can earn points somewhere in the quiz. */
    @Test
    void everyArchetypeCanEarnPoints() {
        Set<String> seen = new HashSet<>();
        data.getQuestions().forEach(q -> q.options().forEach(o -> seen.addAll(o.points().keySet())));
        assertEquals(Archetype.values().length, seen.size());
    }
}