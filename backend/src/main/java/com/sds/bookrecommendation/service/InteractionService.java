
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

@Service
public class InteractionService {

    private final InteractionRepository interactionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public InteractionService(
            InteractionRepository interactionRepository,
            UserRepository userRepository,
            BookRepository bookRepository) {

        this.interactionRepository = interactionRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public InteractionResponse logInteraction(
            Long currentUserId,
            InteractionRequest request) {

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() ->
                        new UserNotFoundException(currentUserId));

        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() ->
                        new BookNotFoundException(request.bookId()));

        Interaction interaction = new Interaction(
                user,
                book,
                request.type()
        );

        Interaction saved =
                interactionRepository.save(interaction);

        return InteractionResponse.from(saved);
    }
}
