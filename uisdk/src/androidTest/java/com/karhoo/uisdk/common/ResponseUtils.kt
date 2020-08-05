package com.karhoo.uisdk.common

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock

class ResponseUtils(val httpCode: Int, val fileName: String, val useJson: Boolean = false, val delayInMillis: Int = 0) {

    fun createResponse(): ResponseDefinitionBuilder {
        val json = if (!useJson) {
            getJsonFromFile()
        } else {
            fileName
        }
        return WireMock.aResponse()
                .withStatus(httpCode)
                .withHeader("Content-Type", "json/application")
                .withBody(json)
                .withFixedDelay(delayInMillis)
    }

    private fun getJsonFromFile(): String {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val inputStream = ctx.resources.assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun createDelayedResponse(delayInMillis: Int): ResponseDefinitionBuilder {
        return createResponse().withFixedDelay(delayInMillis)
    }
}