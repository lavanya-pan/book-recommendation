
package com.sds.bookrecommendation.security;

import com.sds.bookrecommendation.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    // Get the currently authenticated user
    public static User getUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal() instanceof User)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return (User) authentication.getPrincipal();
    }

    // Get the ID of the currently authenticated user
    public static Long getId() {
        return getUser().getId();
    }
}
