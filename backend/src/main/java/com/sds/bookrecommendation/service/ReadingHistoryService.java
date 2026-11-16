package com.sds.bookrecommendation.service;

import com.sds.bookrecommendation.dto.ReadingHistoryRequest;
import com.sds.bookrecommendation.dto.ReadingHistoryResponse;
import com.sds.bookrecommendation.entity.*;
import com.sds.bookrecommendation.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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

    @Transactional
    public ReadingHistoryResponse addOrUpdate(
            ReadingHistoryRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Book not found"));

        ReadingHistory history = readingHistoryRepository
                .findByUser_IdAndBook_Id(
                        request.getUserId(), request.getBookId())
                .orElseGet(() ->
                        new ReadingHistory(user, book, request.getStatus()));

        history.updateStatus(request.getStatus());

        return ReadingHistoryResponse.from(
                readingHistoryRepository.save(history));
    }

    @Transactional(readOnly = true)
    public List<ReadingHistoryResponse> getHistory(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "User not found");
        }

        return readingHistoryRepository
                .findAllByUser_IdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(ReadingHistoryResponse::from)
                .toList();
    }

    @Transactional
    public ReadingHistoryResponse updateStatus(
            Long userId, Long bookId, ReadingStatus status) {

        if (status == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Status is required");
        }

        ReadingHistory history = readingHistoryRepository
                .findByUser_IdAndBook_Id(userId, bookId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reading history entry not found"));

        history.updateStatus(status);

        return ReadingHistoryResponse.from(
                readingHistoryRepository.save(history));
    }
}