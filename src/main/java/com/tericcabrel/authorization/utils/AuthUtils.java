package com.tericcabrel.authorization.utils;

import com.tericcabrel.authorization.models.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

@Slf4j
public class AuthUtils {

    public static String getTenantId() {
        Object details = SecurityContextHolder.getContext().getAuthentication().getDetails();
        log.debug("authentication object from context {}", details);
        if(Objects.nonNull(details) && details instanceof User user) {
            log.debug("extracting tenantId from authentication object");
            return user.getTenantId();
        }
        return null;
    }
}
