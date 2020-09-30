package com.karhoo.karhootraveller.util

import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.BraintreeSDKToken
import com.karhoo.sdk.api.model.CancellationReason
import com.karhoo.sdk.api.model.Driver
import com.karhoo.sdk.api.model.DriverTrackingInfo
import com.karhoo.sdk.api.model.Fare
import com.karhoo.sdk.api.model.FareBreakdown
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.MeetingPoint
import com.karhoo.sdk.api.model.Organisation
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Poi
import com.karhoo.sdk.api.model.PoiDetails
import com.karhoo.sdk.api.model.Position
import com.karhoo.sdk.api.model.Price
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteId
import com.karhoo.sdk.api.model.QuoteList
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuotesSearch
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripState
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.network.request.Passengers
import com.karhoo.sdk.api.network.request.PlaceSearch
import com.karhoo.sdk.api.network.request.TripBooking
import com.karhoo.sdk.api.network.request.TripCancellation
import com.karhoo.sdk.api.network.request.TripSearch
import com.karhoo.sdk.api.network.request.UserLogin
import com.karhoo.sdk.api.network.request.UserRegistration
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.DEFAULT_CURRENCY
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_DESTINATION
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_ORIGIN
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class TestData {

    companion object {

        //Requests
        val USER_LOGIN = UserLogin(
                email = "name@email.com",
                password = "1234567890")

        val PLACE_SEARCH = PlaceSearch(
                position = Position(
                        latitude = 0.0,
                        longitude = 0.0),
                query = "",
                sessionToken = "1234567890")

        val USER_REGISTRATION = UserRegistration(
                firstName = "John",
                lastName = "Smith",
                email = "name@email.com",
                password = "password",
                phoneNumber = "1234567890",
                locale = "en-GB")

        const val BOOKING_ID = "BK123"

        val BOOK_TRIP = TripBooking(
                quoteId = "1234567890",
                passengers = Passengers(
                        additionalPassengers = 1,
                        passengerDetails = listOf()))

        val TRIP_SEARCH = TripSearch()

        val CANCEL = TripCancellation(tripIdentifier = "1234", reason = CancellationReason
                .OTHER_USER_REASON)

        val QUOTE_SEARCH = QuotesSearch(origin = LocationInfo(placeId = "123",
                                                              position = Position(1.0, -1.0),
                                                              address = Address()),
                                        destination = LocationInfo(placeId = "321",
                                                                   position = Position(-1.0, 1.0),
                                                                   address = Address()),
                                        dateScheduled = null)

        //Responses
        const val LATITUDE = 51.5166744
        const val LONGITUDE = -0.1769328

        val ADDRESS = Address(displayAddress = "Paddington Station",
                              lineOne = "",
                              lineTwo = "",
                              buildingNumber = "",
                              streetName = "Praed St",
                              city = "London",
                              postalCode = "W2 1HQ",
                              region = "Greater London",
                              countryCode = "UK")

        val LOCATION_INFO = LocationInfo(position = Position(LATITUDE, LONGITUDE),
                                         placeId = "123",
                                         poiType = Poi.REGULATED,
                                         address = ADDRESS,
                                         timezone = "UK",
                                         details = PoiDetails(iata = "iata",
                                                              terminal = "terminal",
                                                              type = null),
                                         meetingPoint = MeetingPoint(position = Position(
                                                 latitude = 51.5062894,
                                                 longitude = -0.0859324),
                                                                     instructions = "I am near by",
                                                                     pickupType = PickupType.CURBSIDE))

        val USER_PHONE_CODE = "+44"
        val USER_PHONE_NUMBER = "1234567890"

        val USER = UserInfo(
                firstName = "John",
                lastName = "Smith",
                email = "name@email.com",
                userId = "1234",
                countryCode = USER_PHONE_CODE,
                phoneNumber = USER_PHONE_NUMBER,
                locale = "en-GB",
                organisations = listOf(Organisation(
                        id = "0987",
                        name = "Karhoo",
                        roles = listOf("TRIP_ADMIN")))
                           )

        val USER_CARD_REGISTERED = UserInfo(
                firstName = "John",
                lastName = "Smith",
                email = "name@email.com",
                userId = "1234",
                phoneNumber = USER_PHONE_CODE + USER_PHONE_NUMBER,
                locale = "en-GB",
                organisations = listOf(Organisation(
                        id = "0987",
                        name = "Karhoo",
                        roles = listOf("TRIP_ADMIN")))
                                           )

        val USER_UPDATED_PHONE_CODE = "+44"
        val USER_UPDATED_PHONE_NUMBER = "7900000000"

        val USER_UPDATED = UserInfo(
                firstName = "Jeremy",
                lastName = "Peter",
                email = "name@email.com",
                userId = "1234",
                phoneNumber = USER_UPDATED_PHONE_NUMBER,
                locale = "en-GB",
                organisations = listOf(Organisation(
                        id = "0987",
                        name = "Karhoo",
                        roles = listOf("TRIP_ADMIN")))
                                   )

        val DRIVER_TRACKING_INFO = DriverTrackingInfo(position = Position(LATITUDE, LONGITUDE),
                                                      originEta = 5,
                                                      destinationEta = 10)

        val TRIP_INFO_BLANK = TripInfo()

        val POSITION = Position(
                latitude = LATITUDE,
                longitude = LONGITUDE)

        val TRIP_POSITION_PICKUP = Position(
                latitude = 51.523766,
                longitude = -0.1375291)

        val TRIP_POSITION_DROPOFF = Position(
                latitude = 51.514432,
                longitude = -0.1585557)

        val TRIP_LOCATION_INFO_PICKUP = TripLocationInfo(
                displayAddress = ADDRESS_ORIGIN,
                position = TRIP_POSITION_PICKUP,
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                timezone = "Europe/London",
                poiType = Poi.NOT_SET)

        val TRIP_LOCATION_INFO_DROPOFF = TripLocationInfo(
                displayAddress = ADDRESS_DESTINATION,
                position = TRIP_POSITION_DROPOFF,
                placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM",
                timezone = "Europe/London",
                poiType = Poi.NOT_SET)
        val PRICE = Price(
                total = 3550,
                currency = DEFAULT_CURRENCY)

        val FLEET_INFO = FleetInfo(
                fleetId = "FleetID123",
                name = "KarhooTestFleet",
                phoneNumber = "01234567890",
                description = "Karhoo's test fleet",
                logoUrl = "some logo url",
                termsConditionsUrl = "some terms and conditions")

        val DRIVER = Driver(
                firstName = "Michael",
                lastName = "Higgins",
                phoneNumber = "+441111111111",
                licenceNumber = "ZXZ151YTY",
                photoUrl = "https://image.shutterstock.com/image-photo/truck-driver-man-sitting-cabin-450w-1086793355.jpg")

        val VEHICLE = Vehicle(
                vehicleClass = "MPV",
                description = "Renault Scenic (Black)",
                driver = DRIVER,
                vehicleLicencePlate = "123 XYZ")

        val TRIP_MEETING_POINT = Position(
                longitude = -0.1375291,
                latitude = 51.5086692)

        val MEETING_POINT = MeetingPoint(
                position = TRIP_MEETING_POINT,
                pickupType = null,
                instructions = "string")

        val SCHEDULED_DATE = getDate("2019-07-31T12:35:00Z")

        val TRIP = TripInfo(
                tripId = "b6a5f9dc-9066-4252-9013-be85dfa563bc",
                origin = TRIP_LOCATION_INFO_PICKUP,
                destination = TRIP_LOCATION_INFO_DROPOFF,
                dateScheduled = SCHEDULED_DATE,
                //   flightNumber = "null",
                tripState = TripStatus.REQUESTED,
                quote = PRICE,
                fleetInfo = FLEET_INFO,
                comments = "They are waiting by the green door at  Street 100",
                vehicle = VEHICLE,
                displayTripId = "A5TH-R27D",
                meetingPoint = MEETING_POINT)

        val FARE = Fare(
                state = "COMPLETED",
                breakdown = FareBreakdown(
                        total = 0,
                        currency = DEFAULT_CURRENCY))

        val TRIP_COMPLETED = TRIP.copy(
                origin = TRIP_LOCATION_INFO_PICKUP.copy(displayAddress = ADDRESS_ORIGIN),
                destination = TRIP_LOCATION_INFO_DROPOFF.copy(displayAddress = ADDRESS_DESTINATION),
                tripState = TripStatus.COMPLETED)

        val TRIP_CANCELLED_BY_DRIVER = TRIP.copy(
                origin = TRIP_LOCATION_INFO_PICKUP.copy(displayAddress = ADDRESS_ORIGIN),
                destination = TRIP_LOCATION_INFO_DROPOFF.copy(displayAddress = ADDRESS_DESTINATION),
                tripState = TripStatus.CANCELLED_BY_DISPATCH)

        val TRIP_DER = TRIP.copy(
                origin = TRIP_LOCATION_INFO_PICKUP.copy(displayAddress = ADDRESS_ORIGIN),
                destination = TRIP_LOCATION_INFO_DROPOFF.copy(displayAddress = ADDRESS_DESTINATION))

        val TRIP_ARRIVED = TRIP.copy(
                origin = TRIP_LOCATION_INFO_PICKUP.copy(displayAddress = ADDRESS_ORIGIN),
                destination = TRIP_LOCATION_INFO_DROPOFF.copy(displayAddress = ADDRESS_DESTINATION))

        val TRIP_POB = TRIP.copy(
                origin = TRIP_LOCATION_INFO_PICKUP.copy(displayAddress = ADDRESS_ORIGIN),
                destination = TRIP_LOCATION_INFO_DROPOFF.copy(displayAddress = ADDRESS_DESTINATION))

        val TRIP_HISTORY = listOf(TRIP)

        val BLANK_TRIP_HISTORY = listOf<TripInfo>()

        val PAYMENT_TOKEN = BraintreeSDKToken(
                token = "njfdeilnvbflinvbiurnceernnvbrgtuverosa")

        val BLANK_PAYMENT_TOKEN = BraintreeSDKToken()

        val TRIP_STATE = TripState(TripStatus.NO_DRIVERS)

        const val QUOTE_LIST_ID = "129e51a-bc10-11e8-a821-0a580a0414db"

        const val REVERSE_GEO_DISPLAY_ADDRESS = "12 Grimmauld Place, OFTP HQ"

        val QUOTE = Quote(availabilityId = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
                          categoryName = "Exec",
                          currencyCode = DEFAULT_CURRENCY,
                          fleetId = "someFleetId",
                          supplierName = "someFleetName",
                          highPrice = 779,
                          phoneNumber = "+123",
                          qta = 2,
                          quoteId = "someQuoteId",
                          quoteType = QuoteType.ESTIMATED,
                          logoUrl = "someLogoUrl",
                          termsAndConditions = "someTermsUrl",
                          vehicleClass = "saloon",
                          pickupType = PickupType.CURBSIDE)

        val QUOTE_LIST = QuoteList(
                id = QuoteId(QUOTE_LIST_ID),
                categories = mapOf(
                        Pair("Saloon", emptyList()),
                        Pair("Taxi", emptyList()),
                        Pair("MPV", emptyList()),
                        Pair("Exec", listOf(QUOTE)),
                        Pair("Electric", emptyList()),
                        Pair("Moto", emptyList())))

        val QUOTE_LIST_EMPTY = QuoteList(
                id = QuoteId(QUOTE_LIST_ID),
                categories = mapOf(
                        Pair("Saloon", emptyList()),
                        Pair("Taxi", emptyList()),
                        Pair("MPV", emptyList()),
                        Pair("Exec", emptyList()),
                        Pair("Electric", emptyList()),
                        Pair("Moto", emptyList())))

        fun getDate(dateScheduled: String): Date {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateScheduled)
        }

    }
}