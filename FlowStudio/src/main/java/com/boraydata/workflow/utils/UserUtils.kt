package com.boraydata.workflow.utils

import com.boraydata.workflow.entity.User
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

object UserUtils {

    /**
     * Get current User from Security
     * */
    fun currentUser(): User {
        val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val attribute =
            attributes.request.session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) as SecurityContext
        return attribute.authentication.principal as User
    }
}