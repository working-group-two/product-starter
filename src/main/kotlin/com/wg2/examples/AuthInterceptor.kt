package com.wg2.examples

import com.wgtwo.auth.WgtwoAuth
import io.grpc.*

// This class is used to intercept gRPC calls and add an OAuth2 token to the call.
class AuthInterceptor(private val isSandbox: Boolean, clientId: String, clientSecret: String) : ClientInterceptor {

    private val tokenSource = WgtwoAuth.builder(clientId, clientSecret).build()
        .clientCredentials.newTokenSource("sms.text:send_from_subscriber sms.text:send_to_subscriber")

    override fun <ReqT, RespT> interceptCall(method: MethodDescriptor<ReqT, RespT>, callOptions: CallOptions, next: Channel): ClientCall<ReqT, RespT> {
        if (isSandbox) return next.newCall(method, callOptions) // Don't add token for sandbox
        return next.newCall(method, callOptions.withCallCredentials(tokenSource.callCredentials()))
    }
}
