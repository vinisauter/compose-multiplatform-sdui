package me.next.serverdriven.apollo

import com.apollographql.apollo3.api.http.ByteStringHttpBody
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain
import okio.Buffer

class LogInterceptor(val logDelegate: (String) -> Unit = { println(it) }) : HttpInterceptor {
    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain,
    ): HttpResponse {

        val logHeaders = true
        val logBody = true
        val log = StringBuilder()
        log.appendLine("----------------------")
        log.appendLine("${request.method.name} ${request.url}")

        if (logHeaders) {
            request.headers.forEach {
                log.appendLine("${it.name}: ${it.value}")
            }
            log.appendLine("[end of headers]")
        }

        val requestBody = request.body
        val newRequest = if (!logBody || requestBody == null) {
            request
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val bodyByteString = buffer.readByteString()
            log.appendLine(bodyByteString.utf8())
            request.newBuilder()
                .body(ByteStringHttpBody(contentType = requestBody.contentType, bodyByteString))
                .build()
        }

        log.appendLine("")
        val httpResponse = chain.proceed(newRequest)

        log.appendLine("HTTP: ${httpResponse.statusCode}")

        if (logHeaders) {
            httpResponse.headers.forEach {
                log.appendLine("${it.name}: ${it.value}")
            }
            log.appendLine("[end of headers]")
        }

        val responseBody = httpResponse.body
        return if (!logBody || responseBody == null) {
            log.appendLine("----------------------")
            logDelegate(log.toString())
            httpResponse
        } else {
            val bodyByteString = responseBody.readByteString()
            log.appendLine(bodyByteString.utf8())
            log.appendLine("----------------------")
            logDelegate(log.toString())
            @Suppress("DEPRECATION")
            HttpResponse.Builder(statusCode = httpResponse.statusCode)
                .body(bodyByteString)
                .addHeaders(httpResponse.headers)
                .build()
        }

    }
}
