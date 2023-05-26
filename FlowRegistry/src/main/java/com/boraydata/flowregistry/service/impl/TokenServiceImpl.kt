package com.boraydata.flowregistry.service.impl

import com.boraydata.flowauth.constants.SymbolConstants
import com.boraydata.flowauth.constants.SymbolConstants.LINE
import com.boraydata.flowauth.enums.ExpireTimeConstants
import com.boraydata.flowauth.utils.JsonUtils
import com.boraydata.flowregistry.entity.Token
import com.boraydata.flowregistry.entity.TokenResult
import com.boraydata.flowregistry.entity.User
import com.boraydata.flowregistry.service.ITokenService
import com.boraydata.flowregistry.utils.HttpUtils.getRequest
import com.boraydata.flowregistry.utils.IpConfiguration
import com.boraydata.flowregistry.utils.PropertiesUtils
import com.boraydata.flowregistry.utils.TokenUtils
import com.boraydata.flowregistry.utils.TokenUtils.decode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.InetAddress
import java.util.*

/**
 * token业务接口实现类
 */
@Service
class TokenServiceImpl : ITokenService {

    private val logger = LoggerFactory.getLogger(TokenServiceImpl::class.java)

//    @Value("\${registry.nodes}")
//    private lateinit var registrys: String

    @Value("\${server.idle.timeout}")
    private lateinit var idleTimeout: String

    fun generateKey(): String {
        val uuid = UUID.randomUUID().toString()
        return uuid.replace("-", "")
    }

    // token-1
    override fun generateAccessToken(user: User): TokenResult {
        val token = "${generateKey()}$LINE${getRegistry()}$LINE${System.currentTimeMillis()}"
        val reflashToken = generateKey()

        TokenUtils.token[token] = Token(JsonUtils.toJsonStr(user), System.currentTimeMillis())
//        logger.info(getRegistry())
//        logger.info(InetAddress.getLocalHost().hostAddress)
        return TokenResult().apply {
            this.token = token
            this.expire = ExpireTimeConstants.LOGIN_TOKEN_EXPIRE
            this.reflashToken = reflashToken
        }
    }

    override fun receiveAccessTokenData(accessToken: String?): String? {
        if (accessToken == null)
            return null
        val tokenAndHost = accessToken.split(LINE)
        if (tokenAndHost.size < 3)
            return null
        val hostStr = tokenAndHost[1]
        val timeStr = tokenAndHost[2].toLong()
        if ((System.currentTimeMillis() - timeStr) > idleTimeout.toLong()) {
            cancelAccessToken(accessToken)
            return null
        }
        val token = TokenUtils.token[accessToken] ?: return handleOtherToken(accessToken, hostStr)
        val time = System.currentTimeMillis() - token.current
        if (time > PropertiesUtils.EXPIRE)
            return null
        return token.user
    }

    override fun cancelAccessToken(accessToken: String) {
        TokenUtils.token.remove(accessToken)
    }

    private fun handleOtherToken(accessToken: String, host: String): String? {
        logger.info("connect ${decode(host)}")
        val auth = "http://${decode(host)}/api/login/user"
        return getRequest(auth, accessToken)
    }

    fun getRegistry(): String? {
        val hostName = InetAddress.getLocalHost().hostName
        val host = "$hostName${SymbolConstants.COLON}${IpConfiguration.serverPort}"
        logger.info("connect $host")
        return TokenUtils.encode(host)
    }
}