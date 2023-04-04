package com.wg2.examples

import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object Config {

    val logger = LoggerFactory.getLogger(Config::class.java.simpleName)

    fun String.exitIfNotSet(message: String) {
        if (this == "NOT_SET") {
            logger.error(message)
            exitProcess(1)
        }
    }

    fun validate() {
        organizationName.exitIfNotSet("Please set your organization name")
        productName.exitIfNotSet("Please set your product name")
        productClientId.exitIfNotSet("Please set your organization name")
        productClienetSecret.exitIfNotSet("Please set your product name")
    }

}
