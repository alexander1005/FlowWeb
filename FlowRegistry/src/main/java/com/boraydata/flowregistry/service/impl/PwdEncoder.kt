package com.boraydata.flowregistry.service.impl

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


/**
 * 密码器
 *
 */
@Service
class PwdEncoder: PasswordEncoder {

  var encoder = BCryptPasswordEncoder()

  override fun encode(cs: CharSequence): String {
    return encoder.encode(cs)
  }

  override fun matches(rawPassword: CharSequence, encodedPassword: String): Boolean {
    println()
    println(encodedPassword)
    print(rawPassword)
    println()
    return encoder.matches(rawPassword, encodedPassword)
  }
}