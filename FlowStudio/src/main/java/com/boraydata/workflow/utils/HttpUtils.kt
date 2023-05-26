package com.boraydata.workflow.utils

import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.servlet.http.HttpServletRequest


/**
 * 获取请求的request和response
 */
object HttpUtils {

  const val ACCESS_TOKEN = "access_token"
  const val POINT = "."


  /**
   * 请求实体类
   */
  fun request(): HttpServletRequest {
    val attrs = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes
    return attrs.request
  }

  /**
   * 获取ip地址
   */
  fun getIpAddr(request: HttpServletRequest): String {
    var ip: String? = request.getHeader("X-Real-IP")
    if (ip != null && "" != ip && !"unknown".equals(ip, ignoreCase = true)) {
      return ip
    }
    ip = request.getHeader("X-Forwarded-For")
    if (ip != null && "" != ip && !"unknown".equals(ip, ignoreCase = true)) {
      // 多次反向代理后会有多个IP值，第一个为真实IP。
      val index = ip.indexOf(',')
      return if (index != -1) {
        ip.substring(0, index)
      } else {
        ip
      }
    } else {
      return request.remoteAddr
    }
  }

  /**
   * 获取access_token
   */
  fun getAccessToken(): String? {
    val req = request()
    var accessToken = req.getHeader(ACCESS_TOKEN)
    if (accessToken == null) {
      accessToken = req.getParameter(ACCESS_TOKEN)
    }
    return accessToken
  }

  fun getRequest(url: String, token: String): String? {

    val result = StringBuilder()
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.setRequestProperty(ACCESS_TOKEN, token)
    BufferedReader(
      InputStreamReader(conn.inputStream)
    ).use { reader ->
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        result.append(line)
      }
    }
    return result.toString()
  }

  fun getRequestForLog(url: String, token: String, flowName: String): String? {

    val result = StringBuilder()
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.setRequestProperty(ACCESS_TOKEN, token)
    conn.setRequestProperty("flow_name", flowName)
    BufferedReader(
      InputStreamReader(conn.inputStream)
    ).use { reader ->
      var line: String?
      while (reader.readLine().also { line = it } != null) {
        result.append(line).append("\n")
      }
    }
    return result.toString()
  }

}