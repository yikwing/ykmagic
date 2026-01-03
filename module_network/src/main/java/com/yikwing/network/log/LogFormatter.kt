package com.yikwing.network.log

import okhttp3.Headers

/**
 * HTTP 日志格式化器
 */
object LogFormatter {

    fun format(entry: LogEntry): String = buildString {
        val durationInfo = entry.duration?.let { "${it}ms" } ?: "N/A"

        append("---[ OkHttp ID: #${entry.id} | ${statusEmoji(entry.responseCode)} | duration: $durationInfo ]---------------------------------\n")

        // 请求信息
        append("➡️ Request: ${entry.requestMethod} ${entry.requestUrl}")
        append("\n₍^. .^₎⟆ \n${entry.requestHeaders}")
        if (!entry.requestBody.isNullOrBlank()) {
            append("\nBody:\n").append(entry.requestBody).append("\n")
        }

        // 响应信息
        when {
            entry.responseCode != null -> {
                append("\n⬅️ Response: ${entry.responseCode} ${entry.responseMessage ?: ""}\n")
                if (!entry.responseBody.isNullOrBlank() && isJsonResponse(entry.responseHeaders)) {
                    append(entry.responseBody).append("\n")
                }
            }
            entry.exception != null -> {
                append("\n❗️ Error: ${entry.exception?.message}\n")
            }
            else -> {
                append("... No network response ...\n")
            }
        }

        append("------------------------------------------------------------------\n\n")
    }

    private fun statusEmoji(code: Int?): String = when (code) {
        null -> "⏱️"
        in 200..299 -> "✅"
        in 300..399 -> "➡️"
        in 400..499 -> "⚠️"
        else -> "❌"
    }

    private fun isJsonResponse(headers: Headers?): Boolean =
        headers?.get("Content-Type")?.contains("application/json", ignoreCase = true) == true
}
