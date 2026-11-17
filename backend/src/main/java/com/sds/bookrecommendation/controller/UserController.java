
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

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {

        userProfileService.deleteAccount(CurrentUser.getId());

        return ResponseEntity.noContent().build();
    }
}
