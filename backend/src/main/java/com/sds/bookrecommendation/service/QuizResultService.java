package com.sds.bookrecommendation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sds.bookrecommendation.dto.PersonalityQuizRequest;
import com.sds.bookrecommendation.dto.PersonalityResultResponse;
import com.sds.bookrecommendation.entity.QuizResponse;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.repository.QuizResponseRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Saves quiz results for logged-in users. Visitors who are not logged in are ignored.
 */
@Service
public class QuizResultService {

    private final UserRepository userRepository;
    private final QuizResponseRepository quizResponseRepository;
    private final ObjectMapper objectMapper;

    public QuizResultService(UserRepository userRepository,
                             QuizResponseRepository quizResponseRepository,
                             ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.quizResponseRepository = quizResponseRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Stores the reader type on the logged-in user and keeps the answers.
     * Does nothing when nobody is logged in or the user no longer exists.
     */
    @Transactional
    public void savePersonalityResult(PersonalityQuizRequest request, PersonalityResultResponse result) {
        Long userId = loggedInUserId();
        if (userId == null) {
            return;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return;
        }
        user.setReaderType(result.readerType());
        userRepository.save(user);
        quizResponseRepository.save(new QuizResponse(user, "PERSONALITY", toJson(request.answers())));
    }

    /** Returns the id of the logged-in user, or null for a visitor. */
    private Long loggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getId();
        }
        return null;
    }

    /** Converts the answers to JSON text for storage. */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot store quiz answers", e);
        }
    }
}