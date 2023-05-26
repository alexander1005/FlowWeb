package com.boraydata.flowregistry.contronllers

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowregistry.entity.vo.RegistryUserInfoVO
import com.boraydata.flowregistry.entity.vo.UserInfoVO
import com.boraydata.flowregistry.service.IUserService
import com.boraydata.flowregistry.utils.*
import org.springframework.web.bind.annotation.*

@RequestMapping("/login")
@RestController
class LoginController(private val userService: IUserService) {

//  @PostMapping
//  fun login(@RequestParam("username") username: String?,
//            @RequestParam("password") password: String?) {
//    println("login")
//  }

  @GetMapping("/authentication")
  fun authentication(@RequestParam("username") username: String): Response<Boolean> {
    return try {
      val auth = userService.authentication(username)
      successWithData(auth)
    } catch (e: Throwable) {
      failWithData(ErrorCode.SUBMIT_FAILED.code,"User does not exist")
    }
  }

  @GetMapping("/listAuthentication")
  fun listAuthentication(): Response<List<RegistryUserInfoVO>> {
    val listAuthentication = userService.listAuthentication()
      ?: return failWithData(ErrorCode.SUBMIT_FAILED.code, "user is not `admin`")
    return successWithData(listAuthentication)
  }

  @PostMapping("/verification")
  fun verification(@RequestParam("username") username: String): Response<String> {
    val verification = userService.verification(username)?: return success()
    return failWithData(ErrorCode.SUBMIT_FAILED.code, verification)
  }

  @DeleteMapping("/cancel")
  fun cancel(@RequestParam("username") username: String): Response<String> {
    val cancel = userService.cancel(username)?: return success()
    return failWithData(ErrorCode.SUBMIT_FAILED.code, cancel)
  }

  @GetMapping("/logout")
  fun logout(): Response<String> {
    userService.logout()
    return success()
  }

  @GetMapping("/getUserInfo")
  fun getUserInfo(@RequestParam("username") username: String): Response<Any> {
    return successWithData(userService.getUserInfo(username))
  }

  @PostMapping("modify")
  fun modify(
    @RequestParam("username") username: String,
    @RequestParam("password") password: String
  ): Response<String> {
    userService.modify(username, password)
    return success()
  }

}