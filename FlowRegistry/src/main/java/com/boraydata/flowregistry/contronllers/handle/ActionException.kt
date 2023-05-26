package com.boraydata.flowregistry.contronllers.handle

import java.lang.RuntimeException

/**
 * 自定义异常
 */
class ActionException : RuntimeException {

  var msg: String

  constructor(msg: String) : super(msg) {
    this.msg = msg
  }

  constructor(msg: String, e: Throwable) : super(msg, e) {
    this.msg = msg
  }

}

