
package com.sds.bookrecommendation.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class CurrentUser {

    public static Long getId() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof Long)) {

            throw new IllegalStateException(
                    "User is not authenticated"
            );
        }

        return (Long) authentication.getPrincipal();
    }
}
