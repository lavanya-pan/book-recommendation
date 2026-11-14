package com.sds.bookrecommendation.quiz;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Loads the personality questions from quiz/personality-questions.json once at startup,
 * so the team can edit questions and points without changing code.
 */
@Component
public class PersonalityQuizData {

    /** Shape of the JSON file: a list under the key "questions". */
    record QuestionFile(List<PersonalityQuestion> questions) {
    }

    private final List<PersonalityQuestion> questions;

    public PersonalityQuizData(ObjectMapper objectMapper) {
        try (InputStream in = new ClassPathResource("quiz/personality-questions.json").getInputStream()) {
            this.questions = List.copyOf(objectMapper.readValue(in, QuestionFile.class).questions());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load personality quiz questions", e);
        }
    }

    /** Returns all personality questions in order. */
    public List<PersonalityQuestion> getQuestions() {
        return questions;
    }
}