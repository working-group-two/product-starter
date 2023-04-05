package com.wg2.examples

import io.grpc.ManagedChannelBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

// This is the main application class.
fun main() {

    val organizationName = "NOT_SET" // TODO: Set your organization name from Developer Portal
    val productName = "NOT_SET" // TODO: Set your product name from Developer Portal
    val smsPrefix = "$organizationName - $productName\n"
    val productSenderId = "NOT_SET" // TODO: Set your product sender ID from Developer Portal
    val productClientId = "NOT_SET" // TODO: Set your product client ID from Developer Portal
    val productClientSecret = "NOT_SET" // TODO: Set your product client ID from Developer Portal
    val apiEndpoint = "sandbox.api.wgtwo.com" // TODO: remove "sandbox." to use production API
    val jorunfaSmsForwardingSlackChannelNumber = "+46724452895" // see #jorunfa-sms-forwarding in Slack

    val logger = LoggerFactory.getLogger("ProductStarterApp")

    val channel = ManagedChannelBuilder.forAddress(apiEndpoint, 443)
        .useTransportSecurity()
        .keepAliveTime(30, TimeUnit.SECONDS)
        .keepAliveTimeout(10, TimeUnit.SECONDS)
        .keepAliveWithoutCalls(true)
        .build()

    val authInterceptor = AuthInterceptor(isSandbox = "sandbox" in apiEndpoint, productClientId, productClientSecret)

    val smsClient = SmsClient(channel, authInterceptor)

    ConsentEventClient(
        channel,
        authInterceptor,
        onSubscriberConsent = { event ->
            smsClient.sendFromSubscriber( // send FROM subscriber
                from = event.consentChangeEvent.number.e164, // we are sending from the subscriber that consented
                to = jorunfaSmsForwardingSlackChannelNumber,
                content = smsPrefix + "I, '${event.consentChangeEvent.number.e164}', have consented to use $productName"
            )
        },
        onTenantConsent = { event ->
            smsClient.sendToSubscriber( // send TO subscriber
                from = productSenderId, // we are sending from the product sender ID
                to = jorunfaSmsForwardingSlackChannelNumber, // this msisdn belongs to Wotel, so we have consent to send to it
                content = smsPrefix + "Tenant '${event.consentChangeEvent.tenant}' has enabled $productName for all their subscribers"
            )
        },
    )

    logger.info("Application started successfully!")

}
