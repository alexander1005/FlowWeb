package com.boraydata.flowauth.enums

/**
 */
enum class ErrorCode(val code: String, val msg: String) {

    SUBMIT_SUCCESS("30000", "succeed"),
    SUBMIT_FAILED("40000", "failed"),
    USER_DISABLED("40001", "Account is disabled"),
    LOGIN_TIMEOUT("40002", "Login timeout"),
    WRONG_PASSWORD("40003", "wrong password"),
    SYSTEM_BUSY("40004", "The system is busy"),
    UNEXIST_USERNAME("40005", "Username does not exist"),
    LOGIN_TYPE_ERROR("40006", "Please choose the correct login method"),
    NO_AUTHORIZE("40007", "The current account does not have access rights")
}
