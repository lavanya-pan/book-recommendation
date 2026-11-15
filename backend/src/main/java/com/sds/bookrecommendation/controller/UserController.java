
package com.sds.bookrecommendation.controller;

import com.sds.bookrecommendation.entity.User;
import com.sds.bookrecommendation.security.CurrentUser;
import com.sds.bookrecommendation.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get own user details or another user's details as admin
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable Long id) {

        User currentUser = CurrentUser.getUser();
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!currentUser.getId().equals(id) && !isAdmin) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied"));
        }

        User user = userService.getUserById(id);

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "name", user.getName(),
                "email", user.getEmail(),
                "readerType", user.getReaderType() == null
                        ? "" : user.getReaderType(),
                "theme", user.getTheme() == null
                        ? "" : user.getTheme(),
                "role", user.getRole()
        ));
    }

    // Update own theme or another user's theme as admin
    @PutMapping("/{id}/theme")
    public ResponseEntity<?> updateTheme(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        User currentUser = CurrentUser.getUser();
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!currentUser.getId().equals(id) && !isAdmin) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied"));
        }

        String theme = request.get("theme");

        if (theme == null ||
                !(theme.equalsIgnoreCase("LIGHT")
                        || theme.equalsIgnoreCase("DARK"))) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message",
                            "Theme must be LIGHT or DARK"));
        }

        User user = userService.updateTheme(id, theme.toUpperCase());

        return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "theme", user.getTheme()
        ));
    }

    // Delete own account or another account as admin
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        User currentUser = CurrentUser.getUser();
        boolean isAdmin = "ADMIN".equals(currentUser.getRole());

        if (!currentUser.getId().equals(id) && !isAdmin) {
            return ResponseEntity.status(403)
                    .body(Map.of("message", "Access denied"));
        }

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
