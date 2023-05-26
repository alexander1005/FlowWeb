package com.boraydata.flowregistry.config


import com.boraydata.flowregistry.entity.AuthorizeVoter
import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowregistry.filter.AuthenticationTokenFilter
import com.boraydata.flowregistry.service.IAccessDeniedHandlerService
import com.boraydata.flowregistry.service.ISuccessHandlerService
import com.boraydata.flowregistry.service.IUserService
import com.boraydata.flowregistry.service.impl.PwdEncoder
import com.boraydata.flowauth.utils.JsonUtils
import com.boraydata.flowregistry.utils.fail
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.access.AccessDecisionManager
import org.springframework.security.access.vote.AuthenticatedVoter
import org.springframework.security.access.vote.UnanimousBased
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.ObjectPostProcessor
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.access.expression.WebExpressionVoter
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.security.web.context.SecurityContextPersistenceFilter

/**
 * 安全框架配置
 */
@Configuration
open class SecurityConfig(
    private val userService: IUserService,
    private val successHandlerService: ISuccessHandlerService,
    private val pwdEncoder: PwdEncoder,
    private val securityMetadataSourceConfig: SecurityMetadataSourceConfig,
    private val authenticationTokenFilter: AuthenticationTokenFilter,
    private val accessDenieHandlerService: IAccessDeniedHandlerService,
    private val flowAuthenticationEntryPoint: FlowAuthenticationEntryPoint
) : WebSecurityConfigurerAdapter() {

    @Value("\${security.mvc-ignored.get}")
    private lateinit var getIgnores: String

    @Value("\${security.mvc-ignored.post}")
    private lateinit var postIgnores: String

    @Value("\${security.mvc-ignored.all}")
    private lateinit var allIgnores: String

    companion object {
        private const val DEFAULT_PATTERN_SEPERATOR = ","
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(getDaoAuthenticationProvider())
    }

    //accessDeniedHandler(accessDenieHandlerService)
    override fun configure(http: HttpSecurity) {
        http.addFilterBefore(authenticationTokenFilter, SecurityContextPersistenceFilter::class.java) // befores
        http
            .exceptionHandling().accessDeniedHandler(accessDenieHandlerService)
            .authenticationEntryPoint(flowAuthenticationEntryPoint)
            .and()
            .csrf().disable().authorizeRequests()
            .accessDecisionManager(accessDecisionManager())
            .and()
            .authorizeRequests()
            .withObjectPostProcessor(object : ObjectPostProcessor<FilterSecurityInterceptor> {
                override fun <O : FilterSecurityInterceptor> postProcess(fsi: O): O {
                    securityMetadataSourceConfig.metadataSource = fsi.securityMetadataSource
                    fsi.securityMetadataSource = securityMetadataSourceConfig
                    return fsi
                }
            })
            .and()
            .authorizeRequests()
            .antMatchers(
                HttpMethod.POST, *postIgnores.split(com.boraydata.flowregistry.config.SecurityConfig.Companion.DEFAULT_PATTERN_SEPERATOR).  // /sms/send/**,/file/**
                map { it.trim() }.toTypedArray()
            ).permitAll()
            .antMatchers(HttpMethod.GET, *getIgnores.split(com.boraydata.flowregistry.config.SecurityConfig.Companion.DEFAULT_PATTERN_SEPERATOR).map { it.trim() }.toTypedArray())
            .permitAll()
            .antMatchers(*allIgnores.split(com.boraydata.flowregistry.config.SecurityConfig.Companion.DEFAULT_PATTERN_SEPERATOR).map { it.trim() }.toTypedArray()).permitAll()
            .antMatchers("/login/**").permitAll()
            .antMatchers("/login").permitAll()
            .and()
            .formLogin().loginPage("/login")
            .successHandler(successHandlerService)
            .loginProcessingUrl("/login")
            .failureHandler { _, response, exception ->
                when (exception) {
                    is BadCredentialsException ->
                        response.writer.write(JsonUtils.toJsonStr(fail(ErrorCode.LOGIN_TYPE_ERROR.msg)))
                    is InternalAuthenticationServiceException ->
                        response.writer.write(JsonUtils.toJsonStr(fail(exception.message!!)))
                    else ->
                        response.writer.write(JsonUtils.toJsonStr(fail(exception.message!!)))
                }
            }
            .and()
            .authorizeRequests()
            .anyRequest()
            .authenticated()
    }


    /**
     * 获取授权管理器
     */
    private fun accessDecisionManager(): AccessDecisionManager {
        return UnanimousBased(listOf(WebExpressionVoter(), AuthorizeVoter(), AuthenticatedVoter()))
    }

    /**
     * 用户验证
     * @return
     */
    private fun getDaoAuthenticationProvider(): DaoAuthenticationProvider {
        val provider = DaoAuthenticationProvider()
        provider.setUserDetailsService(userService)
        provider.isHideUserNotFoundExceptions = false
        provider.setPasswordEncoder(pwdEncoder)
        return provider
    }
}
    