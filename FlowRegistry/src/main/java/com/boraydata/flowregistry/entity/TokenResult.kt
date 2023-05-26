package com.boraydata.flowregistry.entity


/**
 * 登录token数据类
 */
data class TokenResult(
    var token: String? = null,
    var expire: Long? = null,
    var reflashToken: String? = null
)