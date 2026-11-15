package com.sds.bookrecommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.bookrecommendation.dto.PersonalityAnswer;
import com.sds.bookrecommendation.dto.PersonalityQuizRequest;
import com.sds.bookrecommendation.dto.PersonalityResultResponse;
import com.sds.bookrecommendation.quiz.Archetype;
import com.sds.bookrecommendation.quiz.PersonalityOption;
import com.sds.bookrecommendation.quiz.PersonalityQuestion;
import com.sds.bookrecommendation.quiz.PersonalityQuizData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests personality quiz scoring with two small fake questions and with the real question file.
 */
class QuizServiceTest {

    private QuizService service;

    /** Builds a service with two fake questions: option A favours Adventurer, B favours Detective. */
    @BeforeEach
    void setUp() {
        PersonalityQuizData data = mock(PersonalityQuizData.class);
        when(data.getQuestions()).thenReturn(List.of(question(1), question(2)));
        service = new QuizService(data);
    }

    /** Builds a fake question whose option A gives Adventurer 2 and option B gives Detective 2. */
    private static PersonalityQuestion question(int id) {
        return new PersonalityQuestion(id, "Question " + id, List.of(
                new PersonalityOption("A", "a", Map.of("ADVENTURER", 2)),
                new PersonalityOption("B", "b", Map.of("DETECTIVE", 2))));
    }

    /** Builds an answer. */
    private static PersonalityAnswer answer(int questionId, String optionId) {
        return new PersonalityAnswer(questionId, optionId);
    }

    /** The archetype with the most points wins. */
    @Test
    void highestTotalWins() {
        assertEquals(Archetype.ADVENTURER,
                service.scorePersonality(List.of(answer(1, "A"), answer(2, "A"))));
        assertEquals(Archetype.DETECTIVE,
                service.scorePersonality(List.of(answer(1, "B"), answer(2, "B"))));
    }

    /** On a tie, the answer to the first question decides. */
    @Test
    void tieIsBrokenByFirstQuestion() {
        assertEquals(Archetype.ADVENTURER,
                service.scorePersonality(List.of(answer(1, "A"), answer(2, "B"))));
        assertEquals(Archetype.DETECTIVE,
                service.scorePersonality(List.of(answer(1, "B"), answer(2, "A"))));
    }

    /** The same answers always give the same result. */
    @Test
    void sameAnswersGiveSameResult() {
        List<PersonalityAnswer> answers = List.of(answer(1, "A"), answer(2, "B"));
        assertEquals(service.scorePersonality(answers), service.scorePersonality(answers));
    }

    /** The result response carries the display name, genres and theme. */
    @Test
    void submitReturnsReaderTypeGenresAndTheme() {
        PersonalityResultResponse result = service.submitPersonality(
                new PersonalityQuizRequest(List.of(answer(1, "A"), answer(2, "A"))));

        assertEquals("Adventurer", result.readerType());
        assertEquals("fantasy", result.theme());
        assertTrue(result.genres().size() <= 3);
        assertEquals("fantasy", result.genres().get(0));
    }

    /** Unknown question ids, unknown options, repeated and missing answers are rejected. */
    @Test
    void invalidAnswersAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> service.scorePersonality(List.of(answer(9, "A"), answer(2, "A"))));
        assertThrows(IllegalArgumentException.class,
                () -> service.scorePersonality(List.of(answer(1, "Z"), answer(2, "A"))));
        assertThrows(IllegalArgumentException.class,
                () -> service.scorePersonality(List.of(answer(1, "A"), answer(1, "B"))));
        assertThrows(IllegalArgumentException.class,
                () -> service.scorePersonality(List.of(answer(1, "A"))));
    }

    /** With the real question file, picking option A every time gives Adventurer. */
    @Test
    void realFileAllFirstOptionsGivesAdventurer() {
        QuizService real = new QuizService(new PersonalityQuizData(new ObjectMapper()));
        List<PersonalityAnswer> answers = real.getPersonalityQuestions().stream()
                .map(q -> answer(q.id(), "A"))
                .toList();

        assertEquals(Archetype.ADVENTURER, real.scorePersonality(answers));
    }
}