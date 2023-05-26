package com.boraydata.flowregistry.utils

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.utils.JsonUtils
import javax.servlet.http.HttpServletResponse


/**
 * 响应实体类
 */
data class Response<T>(
  var data: T? = null,
  var msg: String? = null,
  var code: String? = null
)

/**
 * 失败
 */
fun fail(msg: String): Response<String> {
  return Response<String>().apply {
    this.code = ErrorCode.SUBMIT_FAILED.code
    this.msg = msg
  }
}

/**
 * 失败返回
 */
fun fail(code: String, msg: String): Response<String> {
  return Response<String>().apply {
    this.code = code
    this.msg = msg
  }
}

/**
 * 带数据的失败返回
 */
fun <E> failWithData(code: String, msg: String): Response<E> {
  return Response<E>().apply {
    this.code = code
    this.msg = msg
    this.data = null
  }
}

/**
 * 返回成功
 */
fun success(): Response<String> {
  return Response<String>().apply {
    this.code = ErrorCode.SUBMIT_SUCCESS.code
    this.msg = ErrorCode.SUBMIT_SUCCESS.msg
  }
}

/**
 * 返回成功
 */
fun success(errorCode: ErrorCode): Response<String> {
  return Response<String>().apply {
    this.code = errorCode.code
    this.msg = errorCode.msg
  }
}

/**
 * 带数据的成功返回
 */
fun <E> successWithData(data: E): Response<E> {
  return Response<E>().apply {
    this.code = ErrorCode.SUBMIT_SUCCESS.code
    this.msg = ErrorCode.SUBMIT_SUCCESS.msg
    this.data = data
  }
}

/**
 * 响应请求数据
 */
fun <E> responseData(response: HttpServletResponse, data: Response<E>) {
  response.writer.write(JsonUtils.toJsonStr(data))
}