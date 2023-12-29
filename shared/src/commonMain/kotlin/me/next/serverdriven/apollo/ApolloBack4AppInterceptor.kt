package me.next.serverdriven.apollo

import com.apollographql.apollo3.api.http.HttpHeader
import com.apollographql.apollo3.api.http.HttpRequest
import com.apollographql.apollo3.api.http.HttpResponse
import com.apollographql.apollo3.network.http.HttpInterceptor
import com.apollographql.apollo3.network.http.HttpInterceptorChain

class ApolloBack4AppInterceptor(
    parseAppId: String,
    parseClientKey: String,
    var sessionToken: String? = null
) : HttpInterceptor {
    private val extraHeaders = arrayListOf(
        HttpHeader("X-Parse-Application-Id", parseAppId),
        HttpHeader("X-Parse-Client-Key", parseClientKey),
    ).also { headers ->
        sessionToken?.let { headers.add(HttpHeader("X-Parse-Session-Token", it)) }
    }

    override suspend fun intercept(
        request: HttpRequest,
        chain: HttpInterceptorChain
    ): HttpResponse {
        return chain.proceed(request.newBuilder().addHeaders(extraHeaders).build())
    }
}