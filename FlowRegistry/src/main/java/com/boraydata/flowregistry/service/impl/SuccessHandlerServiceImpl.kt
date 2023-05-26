package com.boraydata.flowregistry.service.impl


import com.boraydata.flowregistry.entity.vo.LoginSuccessVO
import com.boraydata.flowregistry.service.ISuccessHandlerService
import com.boraydata.flowregistry.service.ITokenService
import com.boraydata.flowregistry.utils.responseData
import com.boraydata.flowregistry.utils.successWithData
import com.boraydata.flowregistry.entity.User
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 登录成功处理器
 */
@Service
class SuccessHandlerServiceImpl(private val tokenService: ITokenService) : ISuccessHandlerService {

    override fun onAuthenticationSuccess(request: HttpServletRequest, response: HttpServletResponse,
                                         auth: Authentication) {
        val user = auth.principal as User
        val tokenResult = tokenService.generateAccessToken(user)
        responseData(response, successWithData(LoginSuccessVO().apply {
            this.loginTime = Date()
            this.token = tokenResult
            this.user = user
        }))
    }
}