package com.boraydata.flowregistry.entity

import com.boraydata.flowregistry.config.SecurityMetadataSourceConfig.Companion.DEFAULT_ROLE
import org.springframework.security.access.AccessDecisionVoter
import org.springframework.security.access.AccessDecisionVoter.*
import org.springframework.security.access.ConfigAttribute
import org.springframework.security.access.SecurityConfig
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.UserDetails

/**
 * security角色选举实体类，
 * 当前请求路径对应的角色集合中，当前用户是否对应有此角色
 */
class AuthorizeVoter: AccessDecisionVoter<Any> {

  override fun vote(authentication: Authentication, obj: Any,
                    attributes: MutableCollection<ConfigAttribute>): Int {
    if (authentication.principal !is UserDetails) {
      return ACCESS_ABSTAIN
    }
    /**
     * 放开没有设置权限拦截的路径
     */
    if (attributes.filter { it !is SecurityConfig || it.attribute == DEFAULT_ROLE }.isNotEmpty()) {
      return ACCESS_GRANTED
    }
    val authorities = (authentication.principal as User).authorities
    if (authorities.isNotEmpty()) {
      authorities.forEach { authority ->
        if (attributes.filter { it.attribute != null && it.attribute.equals(authority) }.isNotEmpty()) {
          return ACCESS_GRANTED
        }
      }
      return ACCESS_DENIED
    } else {
      return ACCESS_DENIED
    }
  }

  override fun supports(attribute: ConfigAttribute?): Boolean {
    return true
  }

  override fun supports(clazz: Class<*>?): Boolean {
    return true
  }
}