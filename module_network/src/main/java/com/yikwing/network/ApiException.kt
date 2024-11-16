package com.yikwing.network

const val DEFAULT_ERROR_CODE = -1

/**
 * api 错误码异常
 * */

class ApiException(
    val code: Int,
    override val message: String?,
    override val cause: Throwable? = null,
) : Exception(message, cause) {
    companion object {
        fun createDefault(
            message: String?,
            cause: Throwable? = null,
        ) = ApiException(DEFAULT_ERROR_CODE, message, cause)
    }
}
