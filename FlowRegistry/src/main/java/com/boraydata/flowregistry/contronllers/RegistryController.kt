package com.boraydata.flowregistry.contronllers

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowregistry.service.IUserService
import com.boraydata.flowregistry.utils.Response
import com.boraydata.flowregistry.utils.failWithData
import com.boraydata.flowregistry.utils.success
import org.springframework.web.bind.annotation.*

@RequestMapping("/login")
@RestController
class RegistryController(private val userService: IUserService) {
    @PostMapping("registry")
    fun registry(
        @RequestParam("username") username: String?,
        @RequestParam("password") password: String?,
        @RequestParam("email") email: String?
    ): Response<String> {
        if (username == null)
            return failWithData(ErrorCode.SUBMIT_FAILED.code, "username can not be null")
        if (password == null)
            return failWithData(ErrorCode.SUBMIT_FAILED.code, "password can not be null")
        val result = userService.register(username, password, email)
        if (result != null)
            return failWithData(ErrorCode.SUBMIT_FAILED.code, result)
        return success()
    }

}