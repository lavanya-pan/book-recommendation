package com.sds.bookrecommendation.quiz;

import java.util.Map;

/**
 * One answer choice of a personality question and the archetype points it gives.
 */
public record PersonalityOption(String id, String text, Map<String, Integer> points) {
}