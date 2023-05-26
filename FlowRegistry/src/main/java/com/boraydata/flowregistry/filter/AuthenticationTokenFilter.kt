package com.boraydata.flowregistry.filter


import com.boraydata.flowauth.utils.JsonUtils
import com.boraydata.flowregistry.entity.User
import com.boraydata.flowregistry.service.ITokenService
import com.boraydata.flowregistry.utils.HttpUtils
import com.boraydata.flowregistry.utils.PropertiesUtils
import com.boraydata.flowregistry.utils.TokenUtils
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
class AuthenticationTokenFilter(private val tokenService: ITokenService) : OncePerRequestFilter() {  //goble 全局token 鉴权


    /**
     * 日志对象
     */
    val log = LoggerFactory.getLogger(AuthenticationTokenFilter::class.java)

    override fun doFilterInternal(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        val accessToken = HttpUtils.getAccessToken()
        log.info("accessToken: ${accessToken}")
        // 将内存中的用户数据删除，保证用户数据从redis中获取
        req.session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
        if (accessToken != null) {
            val data = tokenService.receiveAccessTokenData(accessToken)
            data?.let {
                val user = JsonUtils.parse(it, User::class.java)
                // 设置为已登录
                val authenticationDetailsSource = WebAuthenticationDetailsSource()
                val userToken = UsernamePasswordAuthenticationToken(user, null, user.authorities)
                userToken.details = authenticationDetailsSource.buildDetails(req)
                val context = SecurityContextHolder.createEmptyContext()
                context.authentication = userToken
                req.session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    context
                )
                SecurityContextHolder.setContext(context)
            }
        }
        chain.doFilter(req, res)
    }
}