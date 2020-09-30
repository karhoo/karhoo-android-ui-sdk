package com.karhoo.uisdk.common

import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.givenThat
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.stubbing.Scenario
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.Availability
import com.karhoo.sdk.api.model.AvailabilityVehicle
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Credentials
import com.karhoo.sdk.api.model.Driver
import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.Fare
import com.karhoo.sdk.api.model.FareBreakdown
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.MeetingPoint
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PaymentProvider
import com.karhoo.sdk.api.model.PaymentsNonce
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Place
import com.karhoo.sdk.api.model.Places
import com.karhoo.sdk.api.model.Poi
import com.karhoo.sdk.api.model.PoiDetails
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.Provider
import com.karhoo.sdk.api.model.QuoteId
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteV2
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripList
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripState
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.model.VehicleAttributes
import com.karhoo.sdk.api.model.VehiclesV2
import com.karhoo.sdk.api.network.client.APITemplate
import com.karhoo.sdk.api.network.client.DateTypeAdapter
import com.karhoo.sdk.api.network.request.QuoteQTA
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.TRIP
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.Date

fun serverRobot(func: ServerRobot.() -> Unit) = ServerRobot().apply { func() }

class ServerRobot {

    private val gson: Gson = GsonBuilder().registerTypeAdapter(Date::class.java, DateTypeAdapter()).create()

    fun successfulToken() {
        mockPostResponse(
                code = 200,
                response = TOKEN,
                endpoint = APITemplate.TOKEN_METHOD
                        )

        mockPostResponse(
                code = 200,
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
                endpoint = APITemplate.USER_PROFILE_UPDATE_METHOD.replace("{id}", TestData.USER.userId),
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
            .QUOTES_V2_REQUEST_METHOD, delayInMillis: Int = 0) {
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
                endpoint = APITemplate.QUOTES_V2_METHOD.replace("{${APITemplate.IDENTIFIER_ID}}", quoteId),
                delayInMillis = delayInMillis
                       )
    }

    fun bookingResponse(code: Int, response: Any, delayInMillis: Int = 0) {
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

    fun mockTripSuccessResponse(status: Any, tracking: Any, details: TripInfo) {
        bookingStatusResponse(code = HttpURLConnection.HTTP_OK, response = status, trip = TestData.TRIP.tripId)
        driverTrackingResponse(code = HttpURLConnection.HTTP_OK, response = tracking, trip = TestData.TRIP.tripId)
        bookingDetailsResponse(code = HttpURLConnection.HTTP_OK, response = details, trip = TestData.TRIP.tripId)
    }

    private fun mockPostResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0) {
        givenThat(post(urlEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          fileName = gson.toJson(response),
                                          useJson = true,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockGetResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0) {
        givenThat(get(urlPathEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          fileName = gson.toJson(response),
                                          useJson = true,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockPutResponse(code: Int, response: Any, endpoint: String, delayInMillis: Int = 0) {
        givenThat(put(urlEqualTo(endpoint))
                          .willReturn(
                                  ResponseUtils(
                                          httpCode = code,
                                          fileName = gson.toJson(response),
                                          useJson = true,
                                          delayInMillis = delayInMillis)
                                          .createResponse()))
    }

    private fun mockGetChainResponsesSuccess(codeFirst: Int, responseFirst: Any,
                                             codeSecond: Int, responseSecond: Any,
                                             endpoint: String, delayInMillis: Int = 0) {
        val scenario = "scenario1"
        val stageTwo = "stage2"
        givenThat(get(urlEqualTo(endpoint))
                          .inScenario(scenario)
                          .whenScenarioStateIs(Scenario.STARTED)
                          .willSetStateTo(stageTwo)
                          .willReturn(ResponseUtils(
                                  httpCode = codeFirst, fileName = gson.toJson(responseFirst),
                                  useJson = true, delayInMillis = delayInMillis)
                                              .createResponse()))
        givenThat(get(urlEqualTo(endpoint))
                          .inScenario(scenario)
                          .whenScenarioStateIs(stageTwo)
                          .willReturn(ResponseUtils(
                                  httpCode = codeSecond, fileName = gson.toJson(responseSecond),
                                  useJson = true, delayInMillis = delayInMillis)
                                              .createResponse()))
    }

    companion object {

        /**
         * Address Payloads
         */
        val PLACE_SEARCH = Places(
                locations = listOf(
                        Place(
                                placeId = "ChIJ3QDsadsaPI0DjT6SU",
                                displayAddress = "Terminal 1, Longford, Hounslow, UK"),
                        Place(
                                placeId = "ChIJ3QDsadsaPI0DjT6SU",
                                displayAddress = "Terminal 2, Longford, Hounslow, UK"),
                        Place(
                                placeId = "ChIJ3QDsadsaPI0DjT6SU",
                                displayAddress = "Terminal 3, Longford, Hounslow, UK"),
                        Place(
                                placeId = "ChIJ3QDsadsaPI0DjT6SU",
                                displayAddress = "Terminal 4, Longford, Hounslow, UK"),
                        Place(
                                placeId = "ChIJ3QDsadsaPI0DjT6SU",
                                displayAddress = "Terminal 5, Longford, Hounslow, UK")
                                  )
                                 )

        val PLACE_SEARCH_RESULT = Places(
                locations = listOf(
                        Place(
                                displayAddress = "221B Baker Street, London, UK",
                                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ"

                             )
                                  )
                                        )

        val PLACE_SEARCH_RESULT_EXTRA = Places(
                locations = listOf(
                        Place(
                                displayAddress = "368 Oxford Street, London, UK",
                                placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM"
                             )
                                  )
                                              )

        val REVERSE_GEO_SUCCESS = LocationInfo(
                address = Address(
                        displayAddress = "12 Grimmauld Place, OFTP HQ",
                        buildingNumber = "12",
                        city = "Grimmauld Place",
                        countryCode = "GB",
                        lineOne = "12,Grimmauld Place",
                        lineTwo = "HP05OFP",
                        postalCode = "HP05OFP",
                        postalCodeExt = "",
                        region = "England",
                        streetName = "Grimmauld Place"),
                details = PoiDetails(
                        iata = "",
                        terminal = "",
                        type = PoiType.NOT_SET
                                    ),
                placeId = "ChIJzfg7uaWMdUgRe0RpO1Y_oXc",
                meetingPoint = MeetingPoint(
                        position = Position(
                                latitude = 0.0,
                                longitude = 0.0
                                           ),
                        instructions = "",
                        pickupType = PickupType.NOT_SET
                                           ),
                position = Position(
                        longitude = 51.51313379690047,
                        latitude = -0.131489597260952
                                   ),
                poiType = Poi.NOT_SET,
                timezone = "Europe/London"
                                              )

        val REVERSE_GEO_SUCCESS_ALTERNATIVE = REVERSE_GEO_SUCCESS.copy(
                address = Address(
                        displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK"),
                position = Position(
                        latitude = 51.523766,
                        longitude = -0.1585557
                                   ),
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                poiType = Poi.NOT_SET,
                timezone = "Europe/London"
                                                                      )

        val PLACE_SEARCH_AIRPORT = Places(
                locations = listOf(
                        Place(
                                placeId = "ChIJ3QDsadsaPI0DjT6SU",
                                displayAddress = "Terminal 1, Heathrow Airport, UK",
                                type = PoiType.AIRPORT)
                                  )
                                         )

        val PLACE_DETAILS = LocationInfo(
                address = Address(
                        displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                        buildingNumber = "221",
                        city = "London",
                        countryCode = "GB",
                        lineOne = "221B Baker St, 221B, Baker Street",
                        lineTwo = "Marylebone",
                        postalCode = "NW1 6XE",
                        postalCodeExt = "",
                        region = "England",
                        streetName = "Baker Street"),
                details = PoiDetails(
                        iata = "",
                        terminal = "",
                        type = PoiType.NOT_SET
                                    ),
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                meetingPoint = MeetingPoint(
                        position = Position(
                                latitude = 0.0,
                                longitude = 0.0
                                           ),
                        instructions = "",
                        pickupType = PickupType.NOT_SET
                                           ),
                position = Position(
                        longitude = 51.523767,
                        latitude = -0.1585557
                                   ),
                poiType = Poi.NOT_SET,
                timezone = "Europe/London"
                                        )

        val PLACE_DETAILS_EXTRA = LocationInfo(
                address = Address(
                        displayAddress = "368 Oxford St, London W1D 1LU, UK",
                        buildingNumber = "368",
                        city = "London",
                        countryCode = "GB",
                        lineOne = "368 Oxford St, London",
                        lineTwo = "UK",
                        postalCode = "W1D 1LU",
                        postalCodeExt = "",
                        region = "England",
                        streetName = "Oxford Street"),
                details = PoiDetails(
                        iata = "",
                        terminal = "",
                        type = PoiType.NOT_SET
                                    ),
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                meetingPoint = MeetingPoint(
                        position = Position(
                                latitude = 0.0,
                                longitude = 0.0
                                           ),
                        instructions = "",
                        pickupType = PickupType.NOT_SET
                                           ),
                position = Position(
                        longitude = 51.5155617,
                        latitude = -0.1746889
                                   ),
                poiType = Poi.NOT_SET,
                timezone = "Europe/London"
                                              )
        /**
         *
         * Ride Details
         *
         */
        val FARE_COMPLETE = Fare(
                state = "COMPLETED",
                breakdown = FareBreakdown(
                        total = 3550,
                        currency = DEFAULT_CURRENCY
                                         ))

        val FARE_CANCELLED = Fare(
                state = "CANCELLED",
                breakdown = FareBreakdown(
                        total = 3550,
                        currency = ""
                                         )
                                 )

        /**
         *
         * Trip
         *
         */
        val TRIP_DER = TripInfo(
                tripId = "b6a5f9dc-9066-4252-9013-be85dfa563bc",
                tripState = TripStatus.DRIVER_EN_ROUTE,
                fleetInfo = FleetInfo(
                        name = "Antelope [Zoo]",
                        description = "Zoo Test Fleet 5",
                        fleetId = "d4e6e7df-76ac-46dd-89c9-5968949ed10a",
                        logoUrl = "https://cdn.karhoo.com/d/images/logos/cc775eda-950d-4a77-aa83-172d487a4cbf.png",
                        phoneNumber = "+447760222331",
                        termsConditionsUrl = "https://karhoo.com/fleettcs/d4e6e7df-76ac-46dd-89c9-5968949ed10a"
                                     ),
                meetingPoint = MeetingPoint(pickupType = PickupType.NOT_SET),
                quote = Price(
                        total = 500,
                        currency = DEFAULT_CURRENCY,
                        quoteType = QuoteType.METERED
                             ),
                vehicle = Vehicle(
                        vehicleClass = "saloon",
                        description = "Black Prius Toyota",
                        driver = Driver(
                                firstName = "John",
                                lastName = "Nowhas Picture",
                                phoneNumber = "447234765098",
                                licenceNumber = "55555",
                                photoUrl = "https://cdn.karhoo.net/d/images/driver-photos/b0f859f345ce3eeac98d227439d26a91.jpg"
                                       ),
                        vehicleLicencePlate = "ZATLOW"
                                 ),
                origin = TripLocationInfo(
                        displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                        position = Position(
                                latitude = 51.523766,
                                longitude = -0.1585557
                                           ),
                        placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                        poiType = Poi.NOT_SET,
                        timezone = "Europe/London"
                                         ),
                destination = TripLocationInfo(
                        displayAddress = "368 Oxford St, London W1D 1LU, UK",
                        placeId = "k_476dae9d-78b3-11e9-a919-0a580a040d71",
                        poiType = Poi.NOT_SET,
                        timezone = "Europe/London",
                        position = Position(
                                latitude = 51.539127,
                                longitude = -0.142267
                                           )
                                              ),
                dateScheduled = Date(),
                displayTripId = "23SHUD")

        val TRIP_FIXED_DATE = TRIP_DER.copy(
                tripState = TripStatus.CONFIRMED,
                vehicle = Vehicle(
                        vehicleClass = "",
                        description = "",
                        driver = Driver(
                                firstName = "",
                                lastName = "",
                                phoneNumber = "",
                                licenceNumber = "",
                                photoUrl = ""
                                       ), vehicleLicencePlate = ""
                                 ),
                dateScheduled = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse("2021-12-2T20:00:00Z"))

        val TRIP_DER_NO_DRIVER_DETAILS = TRIP_DER.copy(
                vehicle = Vehicle(
                        vehicleClass = "saloon",
                        description = "Black Prius Toyota",
                        driver = Driver(
                                firstName = "",
                                lastName = "",
                                phoneNumber = "",
                                licenceNumber = "",
                                photoUrl = ""
                                       ), vehicleLicencePlate = "ZATLOW"
                                 )
                                                      )

        val TRIP_DER_NO_VEHICLE_DETAILS = TRIP_DER.copy(
                vehicle = Vehicle(
                        vehicleClass = "",
                        description = "",
                        driver = Driver(
                                firstName = "John",
                                lastName = "Everyday",
                                phoneNumber = "123456789",
                                licenceNumber = "55555",
                                photoUrl = "https://cdn.karhoo.net/d/images/driver-photos/b0f859f345ce3eeac98d227439d26a91.jpg"
                                       ), vehicleLicencePlate = "BLKJVS"
                                 )
                                                       )

        val TRIP_DER_NO_VEHICLE_AND_DRIVER_DETAILS = TRIP_DER.copy(
                vehicle = Vehicle(
                        vehicleClass = "",
                        description = "",
                        driver = Driver(
                                firstName = "",
                                lastName = "",
                                phoneNumber = "",
                                licenceNumber = "",
                                photoUrl = ""
                                       ), vehicleLicencePlate = "BLKJVS"
                                 )
                                                                  )

        val TRIP_DER_NO_VEHICLE_NUMBER_PLATE_AND_DRIVER_DETAILS = TRIP_DER.copy(
                vehicle = Vehicle(
                        vehicleClass = "",
                        description = "",
                        driver = Driver(
                                firstName = "",
                                lastName = "",
                                phoneNumber = "",
                                licenceNumber = "",
                                photoUrl = ""
                                       ), vehicleLicencePlate = ""
                                 )
                                                                               )

        val TRIP_DER_NO_NUMBER_PLATE = TRIP_DER.copy(
                vehicle = Vehicle(
                        vehicleClass = "saloon",
                        description = "Black Prius Toyota",
                        driver = Driver(
                                firstName = "John",
                                lastName = "Everyday",
                                phoneNumber = "123456789",
                                licenceNumber = "55555",
                                photoUrl = "https://cdn.karhoo.net/d/images/driver-photos/b0f859f345ce3eeac98d227439d26a91.jpg"
                                       ), vehicleLicencePlate = ""
                                 )
                                                    )

        val TRIP_REQUESTED_DETAILS = TRIP_DER.copy(tripState = TripStatus.REQUESTED)

        val TRIP_CONFIRMED_DETAILS = TRIP_DER.copy(tripState = TripStatus.CONFIRMED)

        val TRIP_REQUEST_INCOMPLETE = TRIP_DER.copy(tripState = TripStatus.INCOMPLETE)

        /**
         *
         * Trip History
         *
         */
        val RIDE_SCREEN_CONFIRMED = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.CONFIRMED
                                                                            )))

        val RIDE_SCREEN_PREBOOKED: TripList
            get() = TripList(bookings = listOf(TRIP_FIXED_DATE))

        val RIDE_SCREEN_PREBOOKED_CANCELLED_BY_FLEET = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.CANCELLED_BY_DISPATCH,
                vehicle = Vehicle(
                        vehicleClass = "",
                        description = "",
                        driver = Driver(
                                firstName = "",
                                lastName = "",
                                phoneNumber = "",
                                licenceNumber = "",
                                photoUrl = ""
                                       ), vehicleLicencePlate = ""
                                 )
                                                                                               )))

        val RIDE_SCREEN_PREBOOKED_CANCELLED_BY_USER = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.CANCELLED_BY_USER,
                vehicle = Vehicle(
                        vehicleClass = "",
                        description = "",
                        driver = Driver(
                                firstName = "",
                                lastName = "",
                                phoneNumber = "",
                                licenceNumber = "",
                                photoUrl = ""
                                       ), vehicleLicencePlate = ""
                                 )
                                                                                              )))

        val TRIP_HISTORY_EMPTY = TripList(bookings = listOf())

        val RIDE_SCREEN_COMPLETED = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.COMPLETED
                                                                            )))

        val RIDE_SCREEN_DER = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE
                                                                      )))

        val RIDE_SCREEN_COMPLETED_AIRPORT_PICKUP = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.COMPLETED,
                meetingPoint = MeetingPoint(pickupType = PickupType.MEET_AND_GREET)
                                                                                           )))

        val RIDE_SCREEN_INCOMPLETE = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.INCOMPLETE
                                                                             )))

        val RIDE_SCREEN_CANCELLED_USER = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.CANCELLED_BY_USER
                                                                                 )))

        val RIDE_SCREEN_CANCELLED_KARHOO = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.CANCELLED_BY_KARHOO
                                                                                   )))

        val RIDE_SCREEN_CANCELLED_DRIVER = TripList(bookings = listOf(TRIP_DER.copy(
                tripState = TripStatus.CANCELLED_BY_DISPATCH
                                                                                   )))

        val RIDE_SCREEN_DER_AIRPORT_PICKUP = TripList(bookings = listOf(TRIP_DER.copy(
                meetingPoint = MeetingPoint(pickupType = PickupType.MEET_AND_GREET)
                                                                                     )))

        val TRIP_ARRIVED = TRIP_DER.copy(tripState = TripStatus.ARRIVED)

        val TRIP_POB = TRIP_DER.copy(tripState = TripStatus.PASSENGER_ON_BOARD)

        val TRIP_COMPLETED = TRIP.copy(tripState = TripStatus.COMPLETED)

        val TRIP_CANCELLED_BY_FLEET = TRIP_DER.copy(tripState = TripStatus.CANCELLED_BY_DISPATCH)

        val TRIP_ALLOCATING = TRIP_DER.copy(tripState = TripStatus.REQUESTED)

        val TRIP_STATUS_POB = TripState(TripStatus.PASSENGER_ON_BOARD)

        val TRIP_STATUS_DER = TripState(TripStatus.DRIVER_EN_ROUTE)

        val TRIP_STATUS_CANCELLED_BY_USER = TripState(TripStatus.CANCELLED_BY_USER)

        val TRIP_STATUS_REQUESTED = TripState(TripStatus.REQUESTED)

        val TRIP_STATUS_ARRIVED = TripState(TripStatus.ARRIVED)

        val TRIP_STATUS_CANCELLED_BY_FLEET = TripState(TripStatus.CANCELLED_BY_DISPATCH)

        val TRIP_STATUS_COMPLETED = TripState(TripStatus.COMPLETED)

        /**
         *
         * Driver Tracking
         *
         */
        val DRIVER_TRACKING = DriverTrackingInfo(
                position = Position(
                        latitude = 51.5166744,
                        longitude = -0.1769328
                                   ),
                destinationEta = 10,
                originEta = 5
                                                )

        /**
         *
         * Quotes
         *
         */
        val QUOTE_LIST_ID_ASAP = QuoteId(quoteId = "eb00db4d-44bb-11e9-bdab-0a580a04005f")

        val QUOTE_PRICE = QuotePrice(currencyCode = "DEFAULT_CURRENCY",
                                     highPrice = 577,
                                     lowPrice = 577)

        val QUOTE_FLEET = FleetInfo(fleetId = "52123bd9-cc98-4b8d-a98a-122446d69e79",
                                    name = "iCabbi [Sandbox]",
                                    logoUrl = "https://cdn.karhoo.com/d/images/logos/52123bd9-cc98-4b8d-a98a-122446d69e79.png",
                                    description = "Some fleet description",
                                    phoneNumber = "+447904839920",
                                    termsConditionsUrl = "http://www.google.com")

        val QUOTE_VEHICLE = QuoteVehicle(vehicleClass = "Electric",
                                         vehicleQta = QuoteQTA(highMinutes = 30, lowMinutes = 1))

        val VEHICLE_ATTRIBUTES = VehicleAttributes(passengerCapacity = 4,
                                                   luggageCapacity = 5)

        val QUOTE_V2 = QuoteV2(id = "eb00db4d-44bb-11e9-bdab-0a580a04005f:NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O2VsZWN0cmlj",
                               quoteType = QuoteType.ESTIMATED,
                               quoteSource = QuoteSource.FLEET,
                               price = QUOTE_PRICE,
                               fleet = QUOTE_FLEET,
                               pickupType = PickupType.NOT_SET,
                               vehicle = QUOTE_VEHICLE,
                               vehicleAttributes = VEHICLE_ATTRIBUTES)

        val AVAILABILITY = Availability(vehicles = AvailabilityVehicle(classes = listOf("Saloon", "Taxi", "MPV", "Exec", "Electric", "Moto")))

        val VEHICLES_V2_ASAP = VehiclesV2(
                status = "PROGRESSING",
                id = QUOTE_LIST_ID_ASAP.quoteId,
                availability = AVAILABILITY,
                quotes = listOf(
                        QUOTE_V2,
                        QUOTE_V2.copy(
                                id = "eb00db4d-44bb-11e9-bdab-0a580a04005f:NGY1OTZlM2YtYzYzOC00MjIxLTllODgtYjI0YmM3YjRkZWE1O3RheGk=",
                                quoteSource = QuoteSource.FLEET,
                                quoteType = QuoteType.METERED,
                                price = QUOTE_PRICE.copy(highPrice = 841, lowPrice = 841,
                                                         currencyCode = DEFAULT_CURRENCY),
                                fleet = QUOTE_FLEET.copy(fleetId = "4f596e3f-c638-4221-9e88-b24bc7b4dea5",
                                                         name = "QA_base_ex_com_ex_tax_metered",
                                                         logoUrl = "https://cdn.karhoo.com/d/images/logos/cc775eda-950d-4a77-aa83-172d487a4cbf.png",
                                                         description = "Some fleet description",
                                                         phoneNumber = "+447715364890",
                                                         termsConditionsUrl = "https://karhoo.com/fleettcs/cdda3d54-2926-451f-b839-4201c9adc9f5"),
                                pickupType = PickupType.NOT_SET,
                                vehicle = QUOTE_VEHICLE.copy(vehicleClass = "Taxi",
                                                             vehicleQta = QuoteQTA(highMinutes = 5, lowMinutes = 5)),
                                vehicleAttributes = VEHICLE_ATTRIBUTES),
                        QUOTE_V2.copy(
                                id = "eb00db4d-44bb-11e9-bdab-0a580a04005f:NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
                                quoteSource = QuoteSource.FLEET,
                                quoteType = QuoteType.ESTIMATED,
                                price = QUOTE_PRICE.copy(highPrice = 841, lowPrice = 841,
                                                         currencyCode = DEFAULT_CURRENCY),
                                fleet = QUOTE_FLEET.copy(fleetId = "52123bd9-cc98-4b8d-a98a-122446d69e79",
                                                         name = "iCabbi [Sandbox]",
                                                         logoUrl = "https://cdn.karhoo.com/d/images/logos/cc775eda-950d-4a77-aa83-172d487a4cbf.png",
                                                         description = "Some fleet description",
                                                         phoneNumber = "+447904839920",
                                                         termsConditionsUrl = "https://karhoo.com/fleettcs/cdda3d54-2926-451f-b839-4201c9adc9f5"),
                                pickupType = PickupType.NOT_SET,
                                vehicle = QUOTE_VEHICLE.copy(vehicleClass = "Saloon",
                                                             vehicleQta = QuoteQTA(highMinutes = 30,
                                                                                   lowMinutes = 30)),
                                vehicleAttributes = VEHICLE_ATTRIBUTES),
                        QUOTE_V2.copy(
                                id = "eb00db4d-44bb-11e9-bdab-0a580a04005f:NTlhMTVkYTctOGUyMy00NTRiLTliNDMtNzBlMmRmZDMwN2ZjO2V4ZWN1dGl2ZQ==",
                                quoteSource = QuoteSource.FLEET,
                                quoteType = QuoteType.ESTIMATED,
                                price = QUOTE_PRICE.copy(highPrice = 2380, lowPrice = 2380,
                                                         currencyCode = DEFAULT_CURRENCY),
                                fleet = QUOTE_FLEET.copy(fleetId = "52123bd9-cc98-4b8d-a98a-122446d69e79",
                                                         name = "Third Fleet",
                                                         logoUrl = "https://cdn.karhoo.com/d/images/logos/52123bd9-cc98-4b8d-a98a-122446d69e79.png",
                                                         description = "Some fleet description",
                                                         phoneNumber = "+442999999",
                                                         termsConditionsUrl = "https://karhoo.com/fleettcs/cdda3d54-2926-451f-b839-4201c9adc9f5"),
                                pickupType = PickupType.NOT_SET,
                                vehicle = QUOTE_VEHICLE.copy(vehicleClass = "Exec",
                                                             vehicleQta = QuoteQTA(highMinutes = 20,
                                                                                   lowMinutes =
                                                                                   20)),
                                vehicleAttributes = VEHICLE_ATTRIBUTES),
                        QUOTE_V2.copy(
                                id = "eb00db4d-44bb-11e9-bdab-0a580a04005f:NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O21wdg==",
                                quoteSource = QuoteSource.FLEET,
                                quoteType = QuoteType.ESTIMATED,
                                price = QUOTE_PRICE.copy(highPrice = 2380, lowPrice = 2380,
                                                         currencyCode = DEFAULT_CURRENCY),
                                fleet = QUOTE_FLEET.copy(fleetId = "52123bd9-cc98-4b8d-a98a-122446d69e79",
                                                         name = "Ivcardo",
                                                         logoUrl = "https://cdn.karhoo.com/d/images/logos/52123bd9-cc98-4b8d-a98a-122446d69e79.png",
                                                         description = "Some fleet description",
                                                         phoneNumber = "+447904839920",
                                                         termsConditionsUrl = "https://karhoo.com/fleettcs/cdda3d54-2926-451f-b839-4201c9adc9f5"),
                                pickupType = PickupType.NOT_SET,
                                vehicle = QUOTE_VEHICLE.copy(vehicleClass = "MPV",
                                                             vehicleQta = QuoteQTA(highMinutes = 30,
                                                                                   lowMinutes =
                                                                                   30)),
                                vehicleAttributes = VEHICLE_ATTRIBUTES),
                        QUOTE_V2.copy(
                                id = "eb00db4d-44bb-11e9-bdab-0a580a04005f:OWI3ZTNhZTktNDhkMC00MmYyLTkxMzAtZDk5YzViZWM0MzFjO3NhbG9vbg==",
                                quoteSource = QuoteSource.FLEET,
                                quoteType = QuoteType.ESTIMATED,
                                price = QUOTE_PRICE.copy(highPrice = 2380, lowPrice = 2380,
                                                         currencyCode = DEFAULT_CURRENCY),
                                fleet = QUOTE_FLEET.copy(fleetId = "52123bd9-cc98-4b8d-a98a-122446d69e79",
                                                         name = "A Taxi Fleet",
                                                         logoUrl = "https://cdn.karhoo.com/d/images/logos/9b7e3ae9-48d0-42f2-9130-d99c5bec431c.png",
                                                         description = "Some fleet description",
                                                         phoneNumber = "+447760222331",
                                                         termsConditionsUrl = "https://karhoo.com/fleettcs/cdda3d54-2926-451f-b839-4201c9adc9f5"),
                                pickupType = PickupType.NOT_SET,
                                vehicle = QUOTE_VEHICLE.copy(vehicleClass = "Saloon",
                                                             vehicleQta = QuoteQTA(highMinutes = 4,
                                                                                   lowMinutes = 4)),
                                vehicleAttributes = VEHICLE_ATTRIBUTES))
                                         )

        /**
         *
         * Payments
         *
         */
        val PAYMENTS_TOKEN = PaymentsNonce(
                nonce = "njfdeilnvbflinvbiurnceernnvbrgtuverosa",
                lastFour = "1234",
                cardType = CardType.VISA)

        val PAYMENTS_TOKEN_NO_CARD_REGISTERED = PaymentsNonce(
                nonce = "6e4a4cdf-5a77-0105-791a-7c112a521f92",
                lastFour = "",
                cardType = CardType.NOT_SET
                                                             )

        val BRAINTREE_TOKEN = BraintreeSDKToken(token = "duidchjbwe36874cbaskj3")

        val BRAINTREE_PROVIDER = PaymentProvider(Provider(id = "Braintree"))

        /**
         *
         * User
         *
         */
        val USER_INFO = UserInfo(
                userId = "1234",
                email = "name@email.com",
                firstName = "John",
                lastName = "Smith",
                phoneNumber = "1234567890",
                locale = "en-GB",
                primaryOrganisationId = "Karhoo",
                organisations = listOf(Organisation(
                        id = "0987",
                        name = "Karhoo",
                        roles = listOf("TRIP_ADMIN")
                                                   ))
                                )

        val USER_UPDATED_INFO = USER_INFO.copy(
                firstName = "Jeremy",
                lastName = "Peter",
                phoneNumber = "7900000000"
                                              )

        val PASSWORD_RESET_SUCCESS = ""

        /**
         *
         * Tokens
         *
         */
        val TOKEN = Credentials(
                accessToken = "eyJz93ak4laUWw",
                expiresIn = 86400,
                refreshToken = "sajkqoweioiuoiuoqwe")

        /**
         *
         * Errors
         *
         */
        val NO_ADDRESS_FOUND = KarhooInternalError(code = "K2002")

        val GENERAL_ERROR = KarhooInternalError(code = "K0001")

        val NO_COVERAGE = KarhooInternalError(code = "K3002")

        val NO_AVAILABILITY = KarhooInternalError(code = "K5002")

        val COULD_NOT_CANCEL_TRIP = KarhooInternalError(code = "K4007")

        val BOOKING_EXPIRED_QUOTE = KarhooInternalError(code = "K4004")

        val INVALID_TOKEN = KarhooInternalError(code = "K6001")

        val EMAIL_ALREADY_IN_USE = KarhooInternalError(code = "K1001")

        val INVALID_PHONE_NUMBER = KarhooInternalError(code = "K1004")

        val REGISTRATION_FAILED = KarhooInternalError(code = "K1001")

        val ADDRESSES_IDENTICAL = KarhooInternalError(code = "Q0001")

        data class KarhooInternalError(@SerializedName("code") val code: String)

        const val TIMEOUT = 30000
    }
}
