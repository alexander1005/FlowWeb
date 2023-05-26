package com.boraydata.flowregistry.service.impl

import com.boraydata.flowauth.enums.ErrorCode
import com.boraydata.flowauth.utils.ValidateUtils
import com.boraydata.flowregistry.contronllers.handle.ActionException
import com.boraydata.flowregistry.dao.UserDAO
import com.boraydata.flowregistry.entity.User
import com.boraydata.flowregistry.entity.vo.RegistryUserInfoVO
import com.boraydata.flowregistry.entity.vo.UserInfoVO
import com.boraydata.flowregistry.service.IUserService
import com.boraydata.flowregistry.utils.HttpUtils
import com.boraydata.flowregistry.utils.UserUtils.currentUser
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

/**
 * 用户服务实现类
 */
@Service
class UserServiceImpl(private val userDAO: UserDAO,
                      private val pwdEncoder: PwdEncoder,
                      private val tokenServiceImpl: TokenServiceImpl
): IUserService {

  val USERNAME = "username"

  override fun loadUserByUsername(username: String): UserDetails? {
    val request = HttpUtils.request()
    val userName = request.getParameter(USERNAME)
    val user = userDAO.findByUsername(userName) ?: throw ActionException(ErrorCode.UNEXIST_USERNAME.msg)
    return user
  }

//  override fun receiveUserFromCache(): User {
//    val accessToken = HttpUtils.getAccessToken()
//    val keys = redisService.getPattern(LOGIN_TOKEN + accessToken + SymbolConstants.COLON + SymbolConstants.ASTERISK)
//    return JsonUtils.parse(redisService.get(keys.first())!!, User::class.java)
//  }
  override fun authentication(username: String): Boolean {
    val user = userDAO.findByUsername(username)
    return user.approval
  }

  override fun listAuthentication(): List<RegistryUserInfoVO>? {
    if (!currentUser().username.equals("admin"))
      return null
    return userDAO.findAll().map {
      val registryUserInfo = RegistryUserInfoVO()
      registryUserInfo.username = it.username
      registryUserInfo.accountValid = it.approval
      registryUserInfo
    }.toList()
  }

  override fun verification(username: String): String? {
    if (!currentUser().username.equals("admin"))
      return "user is not `admin`"
    val user = userDAO.findByUsername(username)
    user.approval = true
    userDAO.save(user)
    return null
  }

  override fun cancel(username: String): String? {
    if (!currentUser().username.equals("admin"))
      return "user is not `admin`"
    userDAO.deleteByUsername(username)
    return null
  }

  override fun getUserInfo(username: String): UserInfoVO {
    val user = userDAO.findByUsername(username)
    val userInfo = UserInfoVO()
    userInfo.username = user.username
    if (user.clientPfx!=null && ((System.currentTimeMillis() - user.certCreateTime.time) <= user.certDays * 86400000)) {
      userInfo.certCreateTime = user.certCreateTime
      userInfo.certDays = user.certDays
      userInfo.certValid = true
    } else {
      userInfo.certValid = false
    }
    return userInfo
  }

  override fun register(username: String, password: String, email: String?): String? {
    if (!ValidateUtils.validateName(username)) {
      return "streamName only English characters, numbers, and symbols (_,-) are allowed"
    }
    if (userDAO.findByUsername(username)!=null) {
      return "streamName existing"
    }
    val user = User()
    user.username = username
    user.password = pwdEncoder.encode(password)
    user.approval = false
    if (email!=null)
      user.email = email
    userDAO.save(user)
    return null
  }

  override fun modify(username: String, password: String) {
    val user = userDAO.findByUsername(username)
    user.password = pwdEncoder.encode(password)
    userDAO.save(user)
  }

//  override fun logout() {
//    val accessToken = HttpUtils.getAccessToken()
//    accessToken?.let {
//      val keys = redisService.getPattern( LOGIN_TOKEN + it + SymbolConstants.COLON + SymbolConstants.ASTERISK)
//      if (keys.isNotEmpty()) {
//        keys.forEach { key ->
//          redisService.delete(key)
//        }
//      }
//    }
//  }

  override fun logout() {
    val accessToken = HttpUtils.getAccessToken()
    accessToken?.let {

      tokenServiceImpl.cancelAccessToken(accessToken)

//      val keys = redisService.getPattern( LOGIN_TOKEN + it + SymbolConstants.COLON + SymbolConstants.ASTERISK)
//      if (keys.isNotEmpty()) {
//        keys.forEach { key ->
//          redisService.delete(key)
//        }
//      }
    }
  }

}
