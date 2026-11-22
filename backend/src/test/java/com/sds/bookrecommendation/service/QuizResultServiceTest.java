package com.sds.bookrecommendation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.bookrecommendation.dto.PersonalityAnswer;
import com.sds.bookrecommendation.dto.PersonalityQuizRequest;
import com.sds.bookrecommendation.dto.PersonalityResultResponse;
import com.sds.bookrecommendation.entity.QuizResponse;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.QuizResponseRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests that quiz results are saved for logged-in users and ignored for visitors.
 */
class QuizResultServiceTest {

    private UserRepository userRepository;
    private QuizResponseRepository quizResponseRepository;
    private QuizResultService service;

    /** Creates the service with fake repositories. */
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        quizResponseRepository = mock(QuizResponseRepository.class);
        service = new QuizResultService(userRepository, quizResponseRepository, new ObjectMapper());
    }

    /** Clears the fake login so tests do not affect each other. */
    @AfterEach
    void clearLogin() {
        SecurityContextHolder.clearContext();
    }

    /** Builds a request with two answers. */
    private static PersonalityQuizRequest request() {
        return new PersonalityQuizRequest(
                List.of(new PersonalityAnswer(1, "A"), new PersonalityAnswer(2, "B")));
    }

    /** Builds a result for the Adventurer. */
    private static PersonalityResultResponse result() {
        return new PersonalityResultResponse("Adventurer", "desc", List.of("fantasy"), "fantasy");
    }

    /** Logs a fake user in. */
    private User logIn(long id) {
        User user = new User("Test User", "test@example.com");
        user.setId(id);
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of()));
        return user;
    }

    /** A visitor who is not logged in saves nothing. */
    @Test
    void visitorSavesNothing() {
        service.savePersonalityResult(request(), result());

        verifyNoInteractions(userRepository, quizResponseRepository);
    }

    /** A logged-in user gets the reader type saved and the answers stored. */
    @Test
    void loggedInUserGetsReaderTypeAndAnswersSaved() {
        User user = logIn(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        service.savePersonalityResult(request(), result());

        assertEquals("Adventurer", user.getReaderType());
        verify(userRepository).save(user);
        ArgumentCaptor<QuizResponse> saved = ArgumentCaptor.forClass(QuizResponse.class);
        verify(quizResponseRepository).save(saved.capture());
        assertEquals("PERSONALITY", saved.getValue().getQuizType());
        assertTrue(saved.getValue().getAnswers().contains("\"questionId\":1"));
    }

    /** If the user no longer exists, nothing is saved. */
    @Test
    void unknownUserSavesNothing() {
        logIn(9L);
        when(userRepository.findById(9L)).thenReturn(Optional.empty());

        service.savePersonalityResult(request(), result());

        verify(quizResponseRepository, never()).save(any(QuizResponse.class));
    }
}