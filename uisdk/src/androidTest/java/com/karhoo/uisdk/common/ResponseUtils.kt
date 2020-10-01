package com.karhoo.uisdk.common

import androidx.test.platform.app.InstrumentationRegistry
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock
import java.io.BufferedReader

class ResponseUtils(val httpCode: Int, val response: String, val useJson: Boolean = false, val
delayInMillis: Int = 0) {

    fun createResponse(): ResponseDefinitionBuilder {
        val json = if (!useJson) {
            getJsonFromFile()
        } else {
            response
        }
        return WireMock.aResponse()
                .withStatus(httpCode)
                .withHeader("Content-Type", "json/application")
                .withBody(json)
                .withFixedDelay(delayInMillis)
    }

    private fun getJsonFromFile(): String {

        val ctx = InstrumentationRegistry.getInstrumentation().context
        return ctx.assets
                .open(response as String)
                .bufferedReader()
                .use(BufferedReader::readText)
    }

    fun createDelayedResponse(delayInMillis: Int): ResponseDefinitionBuilder {
        return createResponse().withFixedDelay(delayInMillis)
    }
}
