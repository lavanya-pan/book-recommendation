package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.PersonalityAnswer;
import com.sds.bookrecommendation.dto.PersonalityQuizRequest;
import com.sds.bookrecommendation.dto.PersonalityResultResponse;
import com.sds.bookrecommendation.quiz.Archetype;
import com.sds.bookrecommendation.quiz.ArchetypeGenreMap;
import com.sds.bookrecommendation.quiz.PersonalityOption;
import com.sds.bookrecommendation.quiz.PersonalityQuestion;
import com.sds.bookrecommendation.quiz.PersonalityQuizData;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Quiz logic: returns the questions and scores a personality quiz.
 */
@Service
public class QuizService {

    /** How many top genres the result shows. */
    static final int RESULT_GENRES = 3;

    private final PersonalityQuizData personalityData;

    public QuizService(PersonalityQuizData personalityData) {
        this.personalityData = personalityData;
    }

    /** Returns all personality questions in order. */
    public List<PersonalityQuestion> getPersonalityQuestions() {
        return personalityData.getQuestions();
    }

    /** Scores the answers and returns the reader type with its genres and theme. */
    public PersonalityResultResponse submitPersonality(PersonalityQuizRequest request) {
        Archetype type = scorePersonality(request.answers());
        return new PersonalityResultResponse(
                type.getDisplayName(),
                type.getDescription(),
                ArchetypeGenreMap.topGenres(type, RESULT_GENRES),
                type.getTheme());
    }

    /**
     * Adds up the points of the chosen options and returns the archetype with the most points.
     * A tie goes to the archetype that the answer to question 1 favours, then to list order.
     * Throws IllegalArgumentException (400) for unknown, duplicate or missing answers.
     */
    public Archetype scorePersonality(List<PersonalityAnswer> answers) {
        List<PersonalityQuestion> questions = personalityData.getQuestions();
        Map<Integer, PersonalityOption> chosen = pickOptions(questions, answers);

        Map<Archetype, Integer> totals = new EnumMap<>(Archetype.class);
        chosen.values().forEach(option ->
                pointsOf(option).forEach((archetype, points) -> totals.merge(archetype, points, Integer::sum)));

        int best = Collections.max(totals.values());
        List<Archetype> tied = totals.entrySet().stream()
                .filter(entry -> entry.getValue() == best)
                .map(Map.Entry::getKey)
                .toList();
        if (tied.size() == 1) {
            return tied.get(0);
        }

        Map<Archetype, Integer> firstAnswerPoints = pointsOf(chosen.get(questions.get(0).id()));
        return tied.stream()
                .max(Comparator.comparingInt(a -> firstAnswerPoints.getOrDefault(a, 0)))
                .orElse(tied.get(0));
    }

    /** Matches each answer to its option and checks that every question is answered once. */
    private Map<Integer, PersonalityOption> pickOptions(List<PersonalityQuestion> questions,
                                                        List<PersonalityAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException("Please answer all " + questions.size() + " questions");
        }
        Map<Integer, PersonalityOption> chosen = new LinkedHashMap<>();
        for (PersonalityAnswer answer : answers) {
            PersonalityQuestion question = questions.stream()
                    .filter(q -> q.id() == answer.questionId())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown question id " + answer.questionId()));
            PersonalityOption option = question.options().stream()
                    .filter(o -> o.id().equalsIgnoreCase(answer.optionId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown option " + answer.optionId() + " for question " + question.id()));
            if (chosen.put(question.id(), option) != null) {
                throw new IllegalArgumentException("Question " + question.id() + " answered twice");
            }
        }
        if (chosen.size() != questions.size()) {
            throw new IllegalArgumentException("Please answer all " + questions.size() + " questions");
        }
        return chosen;
    }

    /** Converts an option's points from archetype names to Archetype values. */
    private Map<Archetype, Integer> pointsOf(PersonalityOption option) {
        Map<Archetype, Integer> result = new EnumMap<>(Archetype.class);
        option.points().forEach((name, points) -> {
            Archetype archetype = Archetype.fromText(name);
            if (archetype == null) {
                throw new IllegalStateException("Unknown archetype in quiz file: " + name);
            }
            result.merge(archetype, points, Integer::sum);
        });
        return result;
    }
}