
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.dto.InteractionRequest;
import com.sds.bookrecommendation.dto.InteractionResponse;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.InteractionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interactions")
public class InteractionController {

    private final InteractionService interactionService;

    public InteractionController(InteractionService interactionService) {
        this.interactionService = interactionService;
    }

    @PostMapping
    public ResponseEntity<InteractionResponse> logInteraction(
            @Valid @RequestBody InteractionRequest request) {

        Long currentUserId = CurrentUser.getId();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interactionService.logInteraction(
                        currentUserId, request));
    }
}
