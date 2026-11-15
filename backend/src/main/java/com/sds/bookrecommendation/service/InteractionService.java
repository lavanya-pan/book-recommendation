package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.InteractionRequest;
import com.sds.bookrecommendation.dto.InteractionResponse;
import com.sds.bookrecommendation.entity.Book;
import com.sds.bookrecommendation.entity.Interaction;
import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.exception.BookNotFoundException;
import com.sds.bookrecommendation.exception.UserNotFoundException;
import com.sds.bookrecommendation.repository.BookRepository;
import com.sds.bookrecommendation.repository.InteractionRepository;
import com.sds.bookrecommendation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Logs user actions (view, click, save) on books.
 */
@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public InteractionService(InteractionRepository interactionRepository,
                              UserRepository userRepository,
                              BookRepository bookRepository) {
        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    /** Saves one interaction. Throws 404 for an unknown user or book. */
    @Transactional
    public InteractionResponse logInteraction(InteractionRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));
        Interaction saved = interactionRepository.save(new Interaction(user, book, request.type()));
        return InteractionResponse.from(saved);
    }
}