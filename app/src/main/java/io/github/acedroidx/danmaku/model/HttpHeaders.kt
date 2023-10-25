package io.github.acedroidx.danmaku.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import okhttp3.Headers

@Parcelize
data class HttpHeaders(
    val headers: MutableList<String>
) : Parcelable {
    fun build(): Headers {
        return Headers.Builder().apply {
            headers.forEach {
                add(it)
            }
        }.build()
    }
}
