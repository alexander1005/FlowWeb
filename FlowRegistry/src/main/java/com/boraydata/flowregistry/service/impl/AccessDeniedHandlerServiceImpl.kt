package com.boraydata.flowregistry.service.impl

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowregistry.service.IAccessDeniedHandlerService
import com.boraydata.flowauth.utils.JsonUtils
import com.boraydata.flowregistry.utils.fail
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 授权异常处理接口实现类
 */
@Service
class AccessDeniedHandlerServiceImpl: IAccessDeniedHandlerService {

  override fun handle(request: HttpServletRequest, response: HttpServletResponse,
                      accessDeniedException: AccessDeniedException) {
    response.writer.write(JsonUtils.toJsonStr(fail(ErrorCode.NO_AUTHORIZE.code, ErrorCode.NO_AUTHORIZE.msg)))
  }
}
    