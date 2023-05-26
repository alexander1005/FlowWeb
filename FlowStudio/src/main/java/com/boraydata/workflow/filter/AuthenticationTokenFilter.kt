package com.boraydata.workflow.filter

import com.boraydata.workflow.entity.User
import com.boraydata.workflow.utils.HttpUtils
import com.boraydata.flowauth.utils.JsonUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 将token从redis中获取用户数据，把用户数据放到内存中，作为用户登陆的状态
 */
@Component
class AuthenticationTokenFilter : OncePerRequestFilter() {  //goble 全局token 鉴权

    @Value("\${security.auth-server}")
    private lateinit var authServer: String

    /**
     * 日志对象
     */
    val log = LoggerFactory.getLogger(AuthenticationTokenFilter::class.java)

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val accessToken = HttpUtils.getAccessToken()
//        log.info("accessToken: $accessToken")
        req.session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
        if (accessToken != null) {
            val auth = authServer + if (authServer.endsWith("/")) "api/login/user" else "/api/login/user"
            val data = HttpUtils.getRequest(auth, accessToken)
            val user = data?.let { JsonUtils.parse(it, User::class.java) }
            if (user != null) {
                if (user.id != null) {
                    val authenticationDetailsSource = WebAuthenticationDetailsSource()
                    val userToken = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                    userToken.details = authenticationDetailsSource.buildDetails(req)
                    val context = SecurityContextHolder.createEmptyContext()
                    context.authentication = userToken
                    req.session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context)
                    SecurityContextHolder.setContext(context)
                }
            }
        }
        chain.doFilter(req, res)
    }
}