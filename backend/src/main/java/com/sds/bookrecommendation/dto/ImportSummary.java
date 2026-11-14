package com.sds.bookrecommendation.dto;

/**
 * Result of a book import: how many rows were read, saved and skipped.
 * fallbackGenre counts books whose tags matched no official genre.
 */
public record ImportSummary(
        int totalRows,
        int imported,
        int skippedBlankTitle,
        int skippedDuplicate,
        int skippedNoGenre,
        int fallbackGenre
) {
}
