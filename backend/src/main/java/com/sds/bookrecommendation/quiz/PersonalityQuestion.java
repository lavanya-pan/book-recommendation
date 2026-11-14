package com.sds.bookrecommendation.quiz;

import java.util.List;

/**
 * One personality quiz question with its answer choices.
 */
public record PersonalityQuestion(int id, String text, List<PersonalityOption> options) {
}