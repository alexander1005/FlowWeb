package com.boraydata.flowregistry.service

import com.boraydata.flowregistry.entity.TokenResult
import com.boraydata.flowregistry.entity.User

/**
 * token登录令牌管理接口
 */
interface ITokenService {

    /**
     * 生成token
     */
    fun generateAccessToken(user: User): TokenResult

//  /**
//   * 刷新token
//   */
//  fun reflashAccessToken(reflashToken: String, expireTime: Long)

    /**
     * 根据accessToken获取redis中的数据，并刷新过时时间
     */
    fun receiveAccessTokenData(accessToken: String?): String?

    fun cancelAccessToken(accessToken: String)

}
