package com.boraydata.flowregistry.contronllers

import com.boraydata.flowregistry.entity.User
import com.boraydata.flowregistry.utils.Response
import com.boraydata.flowregistry.utils.success
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@RequestMapping("/login")
@RestController
class UserController {

    @GetMapping("/user")
    fun user(): User? {
            val attributes = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
        val atb =
            attributes.request.session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
        if (atb != null) {
            val attribute = atb as SecurityContext
            return attribute.authentication.principal as User
        }
        return null
    }
}