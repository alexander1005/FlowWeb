package com.boraydata.workflow.config

import org.springframework.security.access.ConfigAttribute
import org.springframework.security.access.SecurityConfig
import org.springframework.security.web.FilterInvocation
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource
import org.springframework.stereotype.Service
import org.springframework.util.Assert
import java.util.*


/**
 * 权限资源配置
 */
@Service
class SecurityMetadataSourceConfig : FilterInvocationSecurityMetadataSource {

    var metadataSource: FilterInvocationSecurityMetadataSource? = null

    companion object {
        /**
         * 默认角色标识，当资源没有对应的角色标识时，将资源对应此默认标识
         * 保证对所有资源都做权限控制，不对没有对应角色标识的资源放开访问权限
         */
        const val DEFAULT_ROLE = "MANAGEMENT_SERVCE_DEFAULT_ROLE"
    }

    override fun getAllConfigAttributes(): MutableCollection<ConfigAttribute> {
        return metadataSource!!.allConfigAttributes
    }

    override fun supports(clazz: Class<*>): Boolean {
        return FilterInvocation::class.java.isAssignableFrom(clazz)
    }

    override fun getAttributes(obj: Any): MutableCollection<ConfigAttribute>? {
        val request = (obj as FilterInvocation).request

        // 这一句是保证忽略路径的对应
        return metadataSource!!.getAttributes(obj)
    }

    /**
     * 创建角色集合
     */
    private fun createList(roles: List<String>): MutableCollection<ConfigAttribute> {
        Assert.notNull(roles, "You must supply an array of attribute names")
        val attributes = ArrayList<ConfigAttribute>(roles.size)

        for (attribute in roles) {
            attributes.add(SecurityConfig(attribute.trim { it <= ' ' }))
        }

        return attributes
    }
}