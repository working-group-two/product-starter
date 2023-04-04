package com.wg2.examples

import com.wgtwo.api.v1.consent.ConsentEventServiceGrpc
import com.wgtwo.api.v1.consent.ConsentEventsProto
import com.wgtwo.api.v1.events.EventsProto
import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.util.concurrent.TimeUnit
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TargetCase.NUMBER
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TargetCase.TENANT
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TypeCase.ADDED
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TypeCase.UPDATED
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TypeCase.REVOKED
import com.wgtwo.auth.ClientCredentialSource

class ConsentEventClient(apiEndpoint: String, tokenSource: ClientCredentialSource) {

    private val logger = org.slf4j.LoggerFactory.getLogger(ConsentEventClient::class.java.simpleName)

    private fun handleConsentEvent(event: ConsentEventsProto.StreamConsentChangeEventsResponse) {
        when (Pair(event.consentChangeEvent.targetCase!!, event.consentChangeEvent.typeCase!!)) {
            Pair(TENANT, ADDED) -> {
                logger.info("New consent for tenant ${event.consentChangeEvent.tenant}")
                // perform some action here
            }
            Pair(TENANT, UPDATED) -> logger.info("Consent updated for tenant ${event.consentChangeEvent.tenant}")
            Pair(TENANT, REVOKED) -> logger.info("Consent revoked for tenant ${event.consentChangeEvent.tenant}")
            Pair(NUMBER, ADDED) -> {
                logger.info("New consent for subscriber ${event.consentChangeEvent.number.e164}")
                // perform some action here
            }
            Pair(NUMBER, UPDATED) -> logger.info("Consent updated for subscriber ${event.consentChangeEvent.number.e164}")
            Pair(NUMBER, REVOKED) -> logger.info("Consent revoked for subscriber ${event.consentChangeEvent.number.e164}")
            else -> logger.info("Bad consent. Type: ${event.consentChangeEvent.typeCase}, Target: ${event.consentChangeEvent.targetCase}")
        }
    }

    init {
        val channel = ManagedChannelBuilder.forAddress(apiEndpoint, 443)
            .useTransportSecurity()
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(10, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
        val request = ConsentEventsProto.StreamConsentChangeEventsRequest.newBuilder()
            .setStreamConfiguration(EventsProto.StreamConfiguration.newBuilder().setMaxInFlight(1))
            .build()
        Thread {
            while (!channel.isShutdown) {
                try {
                    ConsentEventServiceGrpc.newBlockingStub(channel)
                        .apply { if ("sandbox" !in apiEndpoint) withCallCredentials(tokenSource.callCredentials()) }
                        .streamConsentChangeEvents(request)
                        .forEach { handleConsentEvent(it) }
                } catch (e: StatusRuntimeException) {
                    Thread.sleep(10_000) // restart after 10 seconds
                }
            }
        }.apply { name = "ConsentEvents-0" }.start()
        logger.info("Consent event listener started ...")
    }

}
