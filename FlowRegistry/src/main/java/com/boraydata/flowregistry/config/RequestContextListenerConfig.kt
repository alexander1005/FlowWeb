package com.boraydata.flowregistry.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.context.request.RequestContextListener
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.context.annotation.Bean
import java.util.*


/**
 * 配置RequestContextListener监听器
 * 监听请求对象，RequestContextHolder.getRequestAttributes()可以获取到request和response
 */
@Configuration
open class RequestContextListenerConfig {

  @Bean
  open fun getDemoListener(): ServletListenerRegistrationBean<EventListener> {
    val registrationBean = ServletListenerRegistrationBean<EventListener>()
    registrationBean.listener = RequestContextListener()
    return registrationBean
  }
}