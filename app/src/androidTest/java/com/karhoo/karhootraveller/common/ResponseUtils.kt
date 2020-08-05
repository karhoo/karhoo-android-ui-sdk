package com.karhoo.karhootraveller.common

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock

class ResponseUtils(val httpCode: Int, val fileName: String) {

    fun createResponse(): ResponseDefinitionBuilder {
        val json = getJsonFromFile()
        return WireMock.aResponse()
                .withStatus(httpCode)
                .withHeader("Content-Type", "json/application")
                .withBody(json)
    }

    private fun getJsonFromFile(): String {
        val context = InstrumentationRegistry.getInstrumentation().context
        val inputStream = context.resources.assets.open(fileName)
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun createDelayedResponse(delayInMillis: Int): ResponseDefinitionBuilder {
        return createResponse().withFixedDelay(delayInMillis)
    }
}