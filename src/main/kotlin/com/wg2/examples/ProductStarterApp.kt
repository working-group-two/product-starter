package com.wg2.examples

import com.wgtwo.auth.WgtwoAuth
import org.slf4j.LoggerFactory

val organizationName = "NOT_SET" // TODO: Set your organization name from Developer Portal
val productName = "NOT_SET" // TODO: Set your product name from Developer Portal
val productClientId = "NOT_SET" // TODO: Set your product client ID from Developer Portal
val productClienetSecret = "NOT_SET" // TODO: Set your product client ID from Developer Portal
val apiEndpoint = "sandbox.api.wgtwo.com" // TODO: remove
// val apiEndpoint = "api.wgtwo.com" // TODO: uncomment

fun main() {
    // Config.validate()
    val logger = LoggerFactory.getLogger("ProductStarterApp")

    val tokenSource = WgtwoAuth.builder(productClientId, productClienetSecret).build()
        .clientCredentials.newTokenSource("sms.text:send_from_subscriber")

    ConsentEventClient(apiEndpoint, tokenSource)

    logger.info("Application started successfully!")

}
