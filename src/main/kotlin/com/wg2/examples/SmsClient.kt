package com.wg2.examples

import com.wgtwo.api.v1.sms.SmsProto
import com.wgtwo.api.v1.sms.SmsServiceGrpc
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

// This class is used to send SMS messages.
class SmsClient(
    private val channel: ManagedChannel,
    private val authInterceptor: AuthInterceptor
) {

    private val logger = LoggerFactory.getLogger(SmsClient::class.java.simpleName)

    // Send an SMS from a subscriber on our platform to a number (on any platform)
    fun sendFromSubscriber(from: String, to: String, content: String) {
        val request = SmsProto.SendTextFromSubscriberRequest.newBuilder()
            .setFromSubscriber(from)
            .setToAddress(to)
            .setContent(content)
            .build()
        val response = SmsServiceGrpc.newBlockingStub(channel)
            .withInterceptors(authInterceptor)
            .sendTextFromSubscriber(request)
        if (response.status == SmsProto.SendMessageResponse.SendStatus.SEND_STATUS_OK) {
            logger.info("SMS sent: from=${request.fromSubscriber} => to=${request.toAddress}")
        } else {
            logger.warn("SMS failed: from=${request.fromSubscriber} to=${request.toAddress} id=${response.messageId} Status=${response.status} description=${response.description}")
        }
    }

    // Send an SMS to a subscriber on our platform from an address
    fun sendToSubscriber(from: String, to: String, content: String) {
        val request = SmsProto.SendTextToSubscriberRequest.newBuilder()
            .setFromAddress(from)
            .setToSubscriber(to)
            .setContent(content)
            .build()
        val response = SmsServiceGrpc.newBlockingStub(channel)
            .withInterceptors(authInterceptor)
            .sendTextToSubscriber(request)
        if (response.status == SmsProto.SendMessageResponse.SendStatus.SEND_STATUS_OK) {
            logger.info("SMS sent: from=${request.fromAddress} => to=${request.toSubscriber}")
        } else {
            logger.warn("SMS failed: from=${request.fromAddress} to=${request.toSubscriber} id=${response.messageId} Status=${response.status} description=${response.description}")
        }
    }

}
