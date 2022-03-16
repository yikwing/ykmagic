package com.yk.yknetwork


/**
 * api 错误码异常
 * */
class ApiException(val code: Int, override val message: String?, override val cause: Throwable? = null) : RuntimeException(message, cause)