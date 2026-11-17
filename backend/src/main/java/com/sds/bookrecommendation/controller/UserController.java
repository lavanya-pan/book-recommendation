
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.UserProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserProfileService userProfileService;

    public UserController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    // Get logged-in user's profile
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile() {

        Long userId = CurrentUser.getId();
        User user = userProfileService.getProfile(userId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", user.getId());
        response.put("name", user.getName());
        response.put("email", user.getEmail());
        response.put("readerType", user.getReaderType());
        response.put("theme", user.getTheme());
        response.put("role", user.getRole());
        response.put("createdAt", user.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    // Update logged-in user's name and email
    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateMyProfile(
            @RequestBody Map<String, String> request) {

        String name = request.get("name");
        String email = request.get("email");

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Name is required")
            );
        }

        if (email == null || email.isBlank()
                || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "A valid email is required")
            );
        }

        User user = userProfileService.updateProfile(
                CurrentUser.getId(),
                name,
                email
        );

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail()
                )
        );
    }

    // Update theme
    @PutMapping("/me/theme")
    public ResponseEntity<Map<String, Object>> updateMyTheme(
            @RequestBody Map<String, String> request) {

        String theme = request.get("theme");

        if (theme == null ||
                !(theme.equalsIgnoreCase("LIGHT")
                        || theme.equalsIgnoreCase("DARK"))) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Theme must be LIGHT or DARK")
            );
        }

        User user = userProfileService.updateTheme(
                CurrentUser.getId(),
                theme
        );

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getId(),
                        "theme", user.getTheme()
                )
        );
    }

    // Update reader type
    @PutMapping("/me/reader-type")
    public ResponseEntity<Map<String, Object>> updateMyReaderType(
            @RequestBody Map<String, String> request) {

        String readerType = request.get("readerType");

        if (readerType == null || readerType.isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Reader type is required")
            );
        }

        User user = userProfileService.updateReaderType(
                CurrentUser.getId(),
                readerType.trim()
        );

        return ResponseEntity.ok(
                Map.of(
                        "userId", user.getId(),
                        "readerType", user.getReaderType()
                )
        );
    }

    // Delete logged-in user's account
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {

        userProfileService.deleteAccount(CurrentUser.getId());

        return ResponseEntity.noContent().build();
    }
}
