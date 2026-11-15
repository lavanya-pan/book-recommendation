package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.InteractionRequest;
import com.sds.bookrecommendation.dto.InteractionResponse;
import com.sds.bookrecommendation.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for logging interactions. The frontend calls it when a Book Detail page
 * opens and when a book card is clicked.
 */
@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    /** POST /api/interactions: logs one interaction and returns 201. */
    @PostMapping
    public ResponseEntity<InteractionResponse> logInteraction(
            @Valid @RequestBody InteractionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactionService.logInteraction(request));
    }
}