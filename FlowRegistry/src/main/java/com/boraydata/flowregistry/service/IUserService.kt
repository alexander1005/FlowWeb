package com.boraydata.flowregistry.service

import com.boraydata.flowregistry.entity.vo.RegistryUserInfoVO
import com.boraydata.flowregistry.entity.vo.UserInfoVO
import org.springframework.security.core.userdetails.UserDetailsService

/**
 * 用户管理接口
 */
interface IUserService : UserDetailsService {


  /**
   * 注销登录
   */
  fun logout()

//  /**
//   * 根据accessToken获取用户数据
//   */
//  fun receiveUserFromCache(): User

  fun authentication(username: String): Boolean

  fun listAuthentication(): List<RegistryUserInfoVO>?

  fun verification(username: String): String?

  fun cancel(username: String): String?

  fun getUserInfo(username: String): Any

  fun register(username: String, password: String, email: String?): String?

  fun modify(username: String, password: String)
}
