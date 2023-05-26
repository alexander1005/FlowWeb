package com.boraydata.flowregistry.config

import com.alibaba.fastjson.serializer.SerializerFeature
import com.alibaba.fastjson.support.config.FastJsonConfig
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport
import java.util.*


/**
 * mvc配置
 */
@Configuration
open class WebMvcConfig: WebMvcConfigurationSupport() {

  override fun addCorsMappings(registry: CorsRegistry) {
    registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowCredentials(true).allowedHeaders("Origin, X-Requested-With, Content-Type, Accept, access_token")
            .allowedMethods("GET", "POST", "DELETE", "PUT", "OPTIONS")
            .maxAge(3600)
  }

  override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
    converters.plusAssign(getFastJsonHttpMessageConverter())
    converters.plusAssign(getMappingJackson2HttpMessageConverter())
  }

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("swagger-ui.html")
        .addResourceLocations("classpath:/META-INF/resources/")

    registry.addResourceHandler("/webjars/**")
        .addResourceLocations("classpath:/META-INF/resources/webjars/")

    registry.addResourceHandler("/**")
      .addResourceLocations("classpath:/static/")
  }

  @Bean
  open fun getFastJsonHttpMessageConverter(): FastJsonHttpMessageConverter{
    //1.需要定义一个convert转换消息的对象;
    val fastJsonHttpMessageConverter = FastJsonHttpMessageConverter()
    //2:添加fastJson的配置信息;
    val fastJsonConfig = FastJsonConfig()
    fastJsonConfig.setSerializerFeatures(SerializerFeature.PrettyFormat)
    //3处理中文乱码问题
    val fastMediaTypes = ArrayList<MediaType>()
    fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8)
    //4.在convert中添加配置信息.
    fastJsonHttpMessageConverter.supportedMediaTypes = fastMediaTypes
    fastJsonHttpMessageConverter.fastJsonConfig = fastJsonConfig
    return fastJsonHttpMessageConverter
  }

  @Bean
  open fun getMappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
    val mappingJackson2HttpMessageConverter = MappingJackson2HttpMessageConverter()
    //设置日期格式
    val objectMapper = ObjectMapper()
    mappingJackson2HttpMessageConverter.objectMapper = objectMapper
    //设置中文编码格式
    val list = ArrayList<MediaType>()
    list.add(MediaType.APPLICATION_JSON_UTF8)
    mappingJackson2HttpMessageConverter.supportedMediaTypes = list
    return mappingJackson2HttpMessageConverter
  }
}