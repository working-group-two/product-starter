package com.wg2.examples

import com.wgtwo.auth.WgtwoAuth
import io.grpc.*

class AuthInterceptor(private val isSandbox: Boolean, clientId: String, clientSecret: String) : ClientInterceptor {

    private val tokenSource = WgtwoAuth.builder(clientId, clientSecret).build()
        .clientCredentials.newTokenSource("sms.text:send_from_subscriber")

    override fun <ReqT, RespT> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel): ClientCall<ReqT, RespT> {
        val newCallOptions = callOptions.apply { if (!isSandbox) this.withCallCredentials(tokenSource.callCredentials()) }
        return next.newCall(method, newCallOptions)
    }
}
