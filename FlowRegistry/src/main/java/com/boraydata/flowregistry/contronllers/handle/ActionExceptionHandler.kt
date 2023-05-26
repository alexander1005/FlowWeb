package com.boraydata.flowregistry.contronllers.handle

import com.boraydata.flowauth.enums.ResponseConstants
import com.boraydata.flowregistry.utils.Response
import com.boraydata.flowregistry.utils.fail
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import org.springframework.validation.BindException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 统一异常处理类
 */
@RestControllerAdvice
class ActionExceptionHandler {

  private val logger = LoggerFactory.getLogger(ActionExceptionHandler::class.java)

  /**
   * 自定义异常处理
   */
  @ExceptionHandler(ActionException::class)
  fun handleActionException(e: ActionException): Response<String> {
    logger.error(e.msg)
    return fail(e.msg)
  }

  /**
   * 前台提交的数据无法映射到对象
   */
  @ExceptionHandler(BindException::class)
  fun handleBindException(e: BindException): Response<String> {
    logger.error(e.message)
    return fail(ResponseConstants.ERROR_MSG.POST_DATA_FORMAT)
  }

  /**
   * 处理数据库外键关系及非空校验
   */
  @ExceptionHandler(DataIntegrityViolationException::class)
  fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): Response<String> {
    logger.error(e.message)
    return fail(ResponseConstants.ERROR_MSG.DATA_INTEGRITY_VIOLATION)
  }

  /**
   * 处理数据库唯一关系或主键重复
   */
  @ExceptionHandler(DuplicateKeyException::class)
  fun handleDuplicateKeyException(e: DuplicateKeyException): Response<String> {
    logger.error(e.message)
    return fail(ResponseConstants.ERROR_MSG.DUPLICATE_KEY)
  }

  /**
   * 其他异常处理
   */
  @ExceptionHandler(Exception::class)
  fun handleException(e: Exception): Response<String> {
    logger.error(e.message, e)
    return fail(ResponseConstants.ERROR_MSG.EXCEPTION)
  }

  /**
   * 数字转换异常
   */
  @ExceptionHandler(NumberFormatException::class)
  fun handleNumberFormatException(e: NumberFormatException): Response<String> {
    logger.error(e.message)
    return fail(ResponseConstants.ERROR_MSG.NUMBER_FORMAT)
  }

  /**
   * 数字转换异常
   */
  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): Response<String> {
    logger.error(e.bindingResult.allErrors[0].defaultMessage)
    return fail(e.bindingResult.allErrors[0].defaultMessage!!)
  }
}

