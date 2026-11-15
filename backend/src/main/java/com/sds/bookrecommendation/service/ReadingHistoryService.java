
package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.ReadingHistory;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.ResourceNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.ReadingHistoryRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReadingHistoryService {

    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public ReadingHistoryService(
            ReadingHistoryRepository readingHistoryRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {
        this.readingHistoryRepository = readingHistoryRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    // Add a book to reading history
    @Transactional
    public ReadingHistory addToHistory(
            Long userId,
            Long bookId,
            ReadingHistory.ReadingStatus status) {

        if (readingHistoryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException(
                    "Book already exists in reading history");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Book not found"));

        ReadingHistory history =
                new ReadingHistory(user, book, status);

        return readingHistoryRepository.save(history);
    }

    // Get all reading history entries of a user
    @Transactional(readOnly = true)
    public List<ReadingHistory> getReadingHistory(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return readingHistoryRepository.findByUserId(userId);
    }

    // Update the reading status of an existing book
    @Transactional
    public ReadingHistory updateStatus(
            Long userId,
            Long bookId,
            ReadingHistory.ReadingStatus status) {

        ReadingHistory history = readingHistoryRepository
                .findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found in reading history"));

        history.setStatus(status);

        return readingHistoryRepository.save(history);
    }
}

