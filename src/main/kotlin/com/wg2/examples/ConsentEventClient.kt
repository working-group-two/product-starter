package com.wg2.examples

import com.wgtwo.api.v1.consent.ConsentEventServiceGrpc
import com.wgtwo.api.v1.consent.ConsentEventsProto
import com.wgtwo.api.v1.events.EventsProto
import io.grpc.StatusRuntimeException
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TargetCase.NUMBER
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TargetCase.TENANT
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TypeCase.ADDED
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TypeCase.UPDATED
import com.wgtwo.api.v1.consent.ConsentEventsProto.ConsentChangeEvent.TypeCase.REVOKED
import io.grpc.ManagedChannel
import org.slf4j.LoggerFactory

class ConsentEventClient(
    private val channel: ManagedChannel,
    private val authInterceptor: AuthInterceptor,
    private val onTenantConsent: (ConsentEventsProto.StreamConsentChangeEventsResponse) -> Unit,
    private val onSubscriberConsent: (ConsentEventsProto.StreamConsentChangeEventsResponse) -> Unit,
) {

    private val logger = LoggerFactory.getLogger(ConsentEventClient::class.java.simpleName)

    private fun handleConsentEvent(event: ConsentEventsProto.StreamConsentChangeEventsResponse) {
        when (Pair(event.consentChangeEvent.targetCase!!, event.consentChangeEvent.typeCase!!)) {
            Pair(TENANT, ADDED) -> {
                logger.info("New consent for tenant ${event.consentChangeEvent.tenant}")
                onTenantConsent(event)
            }
            Pair(TENANT, UPDATED) -> logger.info("Consent updated for tenant ${event.consentChangeEvent.tenant}")
            Pair(TENANT, REVOKED) -> logger.info("Consent revoked for tenant ${event.consentChangeEvent.tenant}")
            Pair(NUMBER, ADDED) -> {
                logger.info("New consent for subscriber ${event.consentChangeEvent.number.e164}")
                onSubscriberConsent(event)
            }
            Pair(NUMBER, UPDATED) -> logger.info("Consent updated for subscriber ${event.consentChangeEvent.number.e164}")
            Pair(NUMBER, REVOKED) -> logger.info("Consent revoked for subscriber ${event.consentChangeEvent.number.e164}")
            else -> logger.info("Bad consent :/")
        }
    }

    init {
        val request = ConsentEventsProto.StreamConsentChangeEventsRequest.newBuilder()
            .setStreamConfiguration(EventsProto.StreamConfiguration.newBuilder().setMaxInFlight(1))
            .build()
        Thread {
            while (!channel.isShutdown) {
                try {
                    ConsentEventServiceGrpc.newBlockingStub(channel)
                        .withInterceptors(authInterceptor)
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
