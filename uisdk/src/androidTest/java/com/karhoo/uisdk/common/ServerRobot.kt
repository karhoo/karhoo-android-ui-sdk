package com.karhoo.uisdk.common

import com.github.tomakehurst.wiremock.client.WireMock.delete
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.givenThat
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.client.APITemplate
import com.karhoo.sdk.api.network.client.DateTypeAdapter
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.TestData.Companion.INVALID_TOKEN
import com.karhoo.uisdk.util.TestData.Companion.QUOTE_LIST_ID_ASAP
import com.karhoo.uisdk.util.TestData.Companion.TOKEN
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import com.karhoo.uisdk.util.TestData.Companion.USER
import java.net.HttpURLConnection
import java.util.Date

fun serverRobot(func: ServerRobot.() -> Unit) = ServerRobot().apply { func() }

class ServerRobot {

    private val gson: Gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateTypeAdapter()).create()

    private fun getResponse(useJson: Boolean, response: Any): String {
        return if (useJson) gson.toJson(response) else response as String
    }

    fun successfulToken() {
        mockPostResponse(
                code = 201,
                response = TOKEN,
                endpoint = APITemplate.TOKEN_METHOD
                        )

        mockPostResponse(
                code = 201,
                response = TOKEN,
                endpoint = APITemplate.TOKEN_REFRESH_METHOD
                        )
    }

    fun unsuccessfulToken() {
        mockPostResponse(
                code = 401,
                response = INVALID_TOKEN,
                endpoint = APITemplate.TOKEN_METHOD
                        )
    }

    fun deleteRefreshToken() {
        mockDeleteResponse(
                code = 204,
                response = "",
                endpoint = APITemplate.TOKEN_REFRESH_METHOD
                          )
    }

    fun passwordResetResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.PASSWORD_RESET_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun registerUserResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.REGISTER_USER_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun userProfileResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.USER_PROFILE_METHOD,
                delayInMillis = delayInMillis
                       )
    }

    fun userProfileUpdateResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPutResponse(
                code = code,
                response = response,
                endpoint = APITemplate.USER_PROFILE_UPDATE_METHOD.replace("{id}", USER.userId),
                delayInMillis = delayInMillis
                       )
    }

    fun addressListResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.ADDRESS_AUTOCOMPLETE_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun addressDetails(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.PLACE_DETAILS_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun reverseGeocodeResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.REVERSE_GEO_METHOD,
                delayInMillis = delayInMillis)
    }

    fun quoteIdResponse(code: Int, response: Any, endpoint: String = APITemplate
            .QUOTES_REQUEST_METHOD, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = endpoint,
                delayInMillis = delayInMillis
                        )
    }

    fun quotesResponse(code: Int, response: Any, delayInMillis: Int = 0, quoteId: String = QUOTE_LIST_ID_ASAP.quoteId) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.QUOTES_METHOD.replace("{${APITemplate.IDENTIFIER_ID}}", quoteId),
                delayInMillis = delayInMillis
                       )
    }

    fun bookingWithoutNonceResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun bookingWithNonceResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_WITH_NONCE_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun bookingDetailsResponse(code: Int, response: Any, delayInMillis: Int = 0, trip: String) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_DETAILS_METHOD.replace("{id}", trip),
                delayInMillis = delayInMillis
                       )
    }

    fun guestBookingDetailsResponse(code: Int, response: Any, delayInMillis: Int = 0, trip:
    String) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.GUEST_BOOKING_DETAILS_METHOD.replace("{id}", trip),
                delayInMillis = delayInMillis
                       )
    }

    fun bookingStatusResponse(code: Int, response: Any, delayInMillis: Int = 0, trip: String) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_STATUS_METHOD.replace("{id}", trip),
                delayInMillis = delayInMillis
                       )
    }

    fun driverTrackingResponse(code: Int, response: Any, delayInMillis: Int = 0, trip: String) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.TRACK_DRIVER_METHOD.replace("{id}", trip),
                delayInMillis = delayInMillis
                       )
    }

    fun bookingHistoryResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_HISTORY_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun cancelResponse(code: Int, response: Any, delayInMillis: Int = 0, trip: String) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.CANCEL_BOOKING_METHOD.replace("{id}", trip),
                delayInMillis = delayInMillis
                        )
    }

    fun cancelFeeResponse(code: Int, response: Any, delayInMillis: Int = 0, trip: String) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_CANCEL_FEE.replace("{id}", trip),
                delayInMillis = delayInMillis
                        )
    }

    fun sdkInitResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.SDK_INITIALISER_METHOD +
                        "?organisation_id=organisation_id&currency=$DEFAULT_CURRENCY",
                delayInMillis = delayInMillis
                        )
    }

    fun addCardResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.ADD_CARD_METHOD,
                delayInMillis = delayInMillis

                        )
    }

    fun upcomingRidesResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_HISTORY_METHOD,
                delayInMillis = delayInMillis
                        )
    }

    fun pastRidesResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.BOOKING_HISTORY_METHOD,
                delayInMillis = 0
                        )
    }

    fun cancelRideResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.CANCEL_BOOKING_METHOD,
                delayInMillis = 0
                        )
    }

    fun fareResponse(code: Int, response: Any, delayInMillis: Int = 0, tripId: String) {
        mockGetResponse(
                code = code,
                response = response,
                delayInMillis = delayInMillis,
                endpoint = APITemplate.FARE_DETAILS.replace("{id}", tripId)
                       )
    }

    fun tripStatusChainedResponse(codeFirst: Int, responseFirst: Any,
                                  codeSecond: Int, responseSecond: Any, tripId: String,
                                  delayInMillis: Int = 0) {
        mockGetChainResponsesSuccess(codeFirst = codeFirst, responseFirst = responseFirst,
                                     codeSecond = codeSecond, responseSecond = responseSecond,
                                     endpoint = APITemplate.BOOKING_STATUS_METHOD.replace("{id}", tripId))
    }

    fun paymentsNonceResponse(code: Int, response: Any) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.NONCE_METHOD
                        )
    }

    fun paymentsProviderResponse(code: Int, response: Any) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.PAYMENT_PROVIDERS_METHOD

                       )
    }

    fun adyenPublicKeyResponse(code: Int, response: Any, delayInMillis: Int = 0) {
        mockGetResponse(
                code = code,
                response = response,
                endpoint = APITemplate.ADYEN_PUBLIC_KEY_METHOD,
                delayInMillis = delayInMillis
                       )
    }

    fun adyenPaymentMethods(code: Int, response: Any) {
        mockPostResponse(
                code = code,
                response = response,
                endpoint = APITemplate.ADYEN_PAYMENT_METHODS_METHOD
                        )
    }

    fun mockTripSuccessResponse(status: Any, tracking: Any, details: TripInfo) {
        bookingStatusResponse(code = HttpURLConnection.HTTP_OK, response = status, trip = TRIP.tripId)
        driverTrackingResponse(code = HttpURLConnection.HTTP_OK, response = tracking, trip = TRIP.tripId)
        bookingDetailsResponse(code = HttpURLConnection.HTTP_OK, response = details, trip = TRIP.tripId)
    }

    private fun mockPostResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0, useJson: Boolean = true) {
        givenThat(post(urlEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          response = getResponse(useJson, response),
                                          useJson = useJson,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockGetResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0, useJson: Boolean = true) {
        givenThat(get(urlPathEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          response = getResponse(useJson, response),
                                          useJson = useJson,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockDeleteResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0, useJson: Boolean = true) {
        givenThat(delete(urlEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          response = getResponse(useJson, response),
                                          useJson = useJson,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockPutResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0, useJson: Boolean = true) {
        givenThat(put(urlEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          response = getResponse(useJson, response),
                                          useJson = useJson,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockGetChainResponsesSuccess(codeFirst: Int, responseFirst: Any,
                                             codeSecond: Int, responseSecond: Any,
                                             endpoint: String, delayInMillis: Int = 0,
                                             useJson: Boolean = true) {
        val scenario = "scenario1"
        val stageTwo = "stage2"
        givenThat(get(urlEqualTo(endpoint))
                          .inScenario(scenario)
                          .whenScenarioStateIs(Scenario.STARTED)
                          .willSetStateTo(stageTwo)
                          .willReturn(ResponseUtils(
                                  httpCode = codeFirst, response = getResponse(useJson, responseFirst),
                                  useJson = useJson, delayInMillis = delayInMillis)
                                              .createResponse()))
        givenThat(get(urlEqualTo(endpoint))
                          .inScenario(scenario)
                          .whenScenarioStateIs(stageTwo)
                          .willReturn(ResponseUtils(
                                  httpCode = codeSecond, response = getResponse(useJson, responseSecond),
                                  useJson = useJson, delayInMillis = delayInMillis)
                                              .createResponse()))
    }
}
