package com.boraydata.flowregistry.utils;

import com.boraydata.flowregistry.entity.User;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserUtils {

    /**
     * Get current User from Security
     */
    public static User currentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        SecurityContext attribute =
                (SecurityContext) attributes.getRequest().getSession().getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (attribute == null) {
            return null;
        }
        if (attribute.getAuthentication() == null) {
            return null;
        }
        return (User) attribute.getAuthentication().getPrincipal();
    }
}
