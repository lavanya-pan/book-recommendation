package com.sds.bookrecommendation.recommendation;

/**
 * One recommended book returned by any recommendation strategy.
 *
 * @param id     book id
 * @param title  book title
 * @param author book author
 * @param genre  book genre
 * @param score  how strongly the book is recommended (higher is better)
 * @param reason short text shown to the user, e.g. "Highly rated by readers"
 */
public record RecommendedBook(Long id, String title, String author,
                              String genre, double score, String reason) {
}