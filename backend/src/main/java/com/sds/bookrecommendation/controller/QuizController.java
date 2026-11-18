package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.MoodQuizRequest;
import com.sds.bookrecommendation.dto.PersonalityQuizRequest;
import com.sds.bookrecommendation.dto.PersonalityResultResponse;
import com.sds.bookrecommendation.quiz.PersonalityQuestion;
import com.sds.bookrecommendation.recommendation.RecommendedBook;
import com.sds.bookrecommendation.service.MoodQuizService;
import com.sds.bookrecommendation.service.QuizService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for the quizzes. All paths start with /api/quiz.
 */
@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizService quizService;
    private final MoodQuizService moodQuizService;

    public QuizController(QuizService quizService, MoodQuizService moodQuizService) {
        this.quizService = quizService;
        this.moodQuizService = moodQuizService;
    }

    /** GET /api/quiz/personality/questions: all personality questions with their options. */
    @GetMapping("/personality/questions")
    public List<PersonalityQuestion> getPersonalityQuestions() {
        return quizService.getPersonalityQuestions();
    }

    /** POST /api/quiz/personality: scores the answers and returns the reader type. */
    @PostMapping("/personality")
    public PersonalityResultResponse submitPersonality(
            @Valid @RequestBody PersonalityQuizRequest request) {
        return quizService.submitPersonality(request);
    }

    /** POST /api/quiz/mood: returns books that match the mood answers (limit defaults to 10). */
    @PostMapping("/mood")
    public List<RecommendedBook> submitMood(@Valid @RequestBody MoodQuizRequest request,
                                            @RequestParam(defaultValue = "10") int limit) {
        return moodQuizService.recommend(request, limit);
    }
}