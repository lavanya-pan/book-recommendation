package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.ReadingHistoryRequest;
import com.sds.bookrecommendation.dto.ReadingHistoryResponse;
import com.sds.bookrecommendation.dto.ReadingStatusUpdateRequest;
import com.sds.bookrecommendation.service.ReadingHistoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading-history")
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    public ReadingHistoryController(
            ReadingHistoryService readingHistoryService) {
        this.readingHistoryService = readingHistoryService;
    }

    @PostMapping
    public ResponseEntity<ReadingHistoryResponse> addOrUpdate(
            @Valid @RequestBody ReadingHistoryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(readingHistoryService.addOrUpdate(request));
    }

    @GetMapping
    public ResponseEntity<List<ReadingHistoryResponse>> getHistory(
            @RequestParam Long userId) {

        return ResponseEntity.ok(
                readingHistoryService.getHistory(userId));
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<ReadingHistoryResponse> updateStatus(
            @PathVariable Long bookId,
            @RequestParam Long userId,
            @Valid @RequestBody ReadingStatusUpdateRequest request) {

        return ResponseEntity.ok(
                readingHistoryService.updateStatus(
                        userId, bookId, request.getStatus()));
    }
}