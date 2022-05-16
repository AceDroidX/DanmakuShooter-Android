package io.github.acedroidx.danmaku.model

import okhttp3.Headers

data class HttpHeaders(
    val headers: MutableList<String>
) {
    fun build(): Headers {
        return Headers.Builder().apply {
            headers.forEach {
                add(it)
            }
        }.build()
    }
}
