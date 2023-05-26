package com.boraydata.workflow.config

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.utils.JsonUtils
import com.boraydata.workflow.utils.fail
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class FlowAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(req: HttpServletRequest?, resp: HttpServletResponse, exp: AuthenticationException?) {
        resp.writer.write(JsonUtils.toJsonStr(fail(ErrorCode.NO_AUTHORIZE.code, ErrorCode.NO_AUTHORIZE.msg)))
    }
}