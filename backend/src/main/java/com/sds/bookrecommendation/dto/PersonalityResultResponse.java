package com.sds.bookrecommendation.dto;

import java.util.List;

/**
 * The quiz result: the reader type, its description, its top genres and its site theme.
 */
public record PersonalityResultResponse(
        String readerType,
        String description,
        List<String> genres,
        String theme
) {
}