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

// This class is used to listen for consent events. It has two callbacks for tenant and subscriber consent events.
// There are also events for consent revocation and consent update, but they are just logged here.
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
                onTenantConsent(event) // call the callback
            }
            Pair(TENANT, UPDATED) -> logger.info("Consent updated for tenant ${event.consentChangeEvent.tenant}")
            Pair(TENANT, REVOKED) -> logger.info("Consent revoked for tenant ${event.consentChangeEvent.tenant}")
            Pair(NUMBER, ADDED) -> {
                logger.info("New consent for subscriber ${event.consentChangeEvent.number.e164}")
                onSubscriberConsent(event) // call the callback
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
                    val consentEvents = ConsentEventServiceGrpc
                        .newBlockingStub(channel)
                        .withInterceptors(authInterceptor)
                    consentEvents.streamConsentChangeEvents(request).forEach {
                        handleConsentEvent(it)
                        consentEvents.ackConsentChangeEvent( // event will be replayed if we don't ack it
                            ConsentEventsProto.AckConsentChangeEventRequest.newBuilder().setAckInfo(it.metadata.ackInfo).build()
                        )
                    }
                    logger.info("Consent event listener started ...")
                } catch (e: StatusRuntimeException) {
                    logger.info("Exception in consent event stream, restarting in 10 seconds. Message: ${e.message}")
                    Thread.sleep(10_000) // restart after 10 seconds
                }
            }
        }.apply { name = "ConsentEvents-0" }.start()
    }

}
