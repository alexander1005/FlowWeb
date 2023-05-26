package com.boraydata.flowauth.enums
/**
 * 返回信息常量
 **/
class ResponseConstants {
  /**
   * 错误信息常量
   */
  class ERROR_MSG {
    companion object {
      const val DATA_INTEGRITY_VIOLATION = "This operation violates the relationship of the data agreement. If it is an add operation, confirm whether the reference relationship is correct; if a delete operation, please confirm whether the data still has an association relationship"
      const val DUPLICATE_KEY = "The record already exists in the database"
      const val EXCEPTION = "Unknown exception, please contact the administrator"
      const val NUMBER_FORMAT = "Number conversion error"
      const val POST_DATA_FORMAT = "The format of the submitted data does not meet the requirements"
    }
  }
  /**
   * 成功信息常量
   */
  class SUCCESS_MSG {
    companion object {
      const val SUCCESS = "Successful operation"
    }
  }
}