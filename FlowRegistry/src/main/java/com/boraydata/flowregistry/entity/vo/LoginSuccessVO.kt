package com.boraydata.flowregistry.entity.vo

import com.boraydata.flowregistry.entity.TokenResult
import com.boraydata.flowregistry.entity.User
import java.util.*

/**
 * 登录成功相应实体类
 */
data class LoginSuccessVO(
    var loginTime: Date? = null,
    var token: TokenResult? = null,
    var user: com.boraydata.flowregistry.entity.User? = null
)