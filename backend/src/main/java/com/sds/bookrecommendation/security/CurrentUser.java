
package com.sds.bookrecommendation.security;

import com.sds.bookrecommendation.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public class CurrentUser {

    public static User getUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal() instanceof User)) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User is not authenticated"
            );
        }

        return (User) authentication.getPrincipal();
    }

    public static Long getId() {
        return getUser().getId();
    }
}
