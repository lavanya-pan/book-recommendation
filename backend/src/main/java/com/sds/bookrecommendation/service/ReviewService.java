
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Review;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.ResourceNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.ReviewRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // Add a review for a book
    @Transactional
    public Review addReview(Long userId, Long bookId, String text) {

        if (reviewRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException(
                    "You have already reviewed this book");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found"));

        Review review = new Review(user, book, text);

        return reviewRepository.save(review);
    }

    // Get all reviews for a particular book
    @Transactional(readOnly = true)
    public List<Review> getBookReviews(Long bookId) {

        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book not found");
        }

        return reviewRepository.findByBookId(bookId);
    }

    // Get all reviews written by a user
    @Transactional(readOnly = true)
    public List<Review> getUserReviews(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return reviewRepository.findByUserId(userId);
    }
}
