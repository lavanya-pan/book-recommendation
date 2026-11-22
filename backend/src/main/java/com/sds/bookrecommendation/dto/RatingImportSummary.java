package com.sds.bookrecommendation.dto;

/**
 * Result of a ratings import: rows read, users created or skipped, and ratings saved or skipped.
 */
public record RatingImportSummary(
        int totalRows,
        int newUsers,
        int skippedExistingUsers,
        int imported,
        int skippedUnknownBook,
        int skippedInvalidScore,
        int skippedDuplicate
) {
}