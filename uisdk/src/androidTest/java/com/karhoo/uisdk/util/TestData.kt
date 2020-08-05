package com.karhoo.uisdk.util

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
import com.karhoo.sdk.api.model.PoiType
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.system.measureTimeMillis

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
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

        val LOCATION_INFO_AIRPORT = LOCATION_INFO.copy(
                details = PoiDetails(
                        iata = "iata",
                        terminal = "terminal",
                        type = PoiType.AIRPORT
                                    )
                                                      )

        val USER_PHONE_CODE = "+44"
        val USER_PHONE_NUMBER = "1234567890"

        val USER = UserInfo(
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
        val USER_UPDATED_PHONE_NUMBER = "999999999"

        val USER_UPDATED = UserInfo(
                firstName = "Jeremy",
                lastName = "Peter",
                email = "name@email.com",
                userId = "1234",
                phoneNumber = USER_UPDATED_PHONE_CODE + USER_UPDATED_PHONE_NUMBER,
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
                displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                position = TRIP_POSITION_PICKUP,
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                timezone = "Europe/London",
                poiType = Poi.NOT_SET)

        val TRIP_LOCATION_INFO_DROPOFF = TripLocationInfo(
                displayAddress = "368 Oxford St, London W1D 1LU, UK",
                position = TRIP_POSITION_DROPOFF,
                placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM",
                timezone = "Europe/London",
                poiType = Poi.NOT_SET)

        val PRICE = Price(
                total = 3550,
                currency = "GBP")

        val FLEET_INFO = FleetInfo(
                fleetId = "FleetID123",
                name = "KarhooTestFleet",
                phoneNumber = "01234567890",
                description = "Karhoo's test fleet",
                logoUrl = "some logo url",
                termsConditionsUrl = "some terms and conditions")

        val FLEET_INFO_ALT = FLEET_INFO.copy(
                name = "Third Fleet"
                                            )

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

        val MEETING_POINT_UNSET = MEETING_POINT.copy(pickupType = PickupType.NOT_SET)

        val SCHEDULED_DATE = getDate("2019-07-31T12:35:00Z")

        val TRIP = TripInfo(
                tripId = "b6a5f9dc-9066-4252-9013-be85dfa563bc",
                origin = TRIP_LOCATION_INFO_PICKUP,
                destination = TRIP_LOCATION_INFO_DROPOFF,
                dateScheduled = SCHEDULED_DATE,
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
                        currency = "GBP"))

        val TRIP_COMPLETED = TRIP.copy(
                tripState = TripStatus.COMPLETED)

        val TRIP_COMPLETED_AIRPORT_PICKUP = TRIP_COMPLETED.copy(
                meetingPoint = MEETING_POINT.copy(
                        pickupType = PickupType.MEET_AND_GREET
                                                 )
                                                               )

        val TRIP_INCOMPLETE = TRIP.copy(
                tripState = TripStatus.INCOMPLETE
                                       )

        val TRIP_CANCELLED_BY_DRIVER_MEETING_POINT_UNSET = TRIP.copy(
                tripState = TripStatus.CANCELLED_BY_DISPATCH,
                meetingPoint = MEETING_POINT_UNSET)

        val TRIP_CANCELLED_BY_USER_MEETING_POINT_UNSET = TRIP.copy(
                tripState = TripStatus.CANCELLED_BY_USER,
                meetingPoint = MEETING_POINT_UNSET)

        val TRIP_CANCELLED_BY_KARHOO_MEETING_POINT_UNSET = TRIP.copy(
                tripState = TripStatus.CANCELLED_BY_KARHOO,
                meetingPoint = MEETING_POINT_UNSET)

        val TRIP_DER = TRIP.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE)

        val TRIP_ARRIVED = TRIP.copy(
                tripState = TripStatus.ARRIVED)

        val TRIP_POB = TRIP.copy(
                tripState = TripStatus.PASSENGER_ON_BOARD)

        val TRIP_CONFIRMED = TRIP.copy(
                tripState = TripStatus.CONFIRMED)

        val TRIP_PREBOOKED = TRIP_CONFIRMED.copy(
                dateScheduled = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse("2021-12-2T20:00:00Z"))

        val TRIP_CONFIRMED_MEETING_POINT_UNSET = TRIP_CONFIRMED.copy(
                meetingPoint = MEETING_POINT_UNSET
                                                                    )

        val TRIP_CONFIRMED_AIRPORT_PICKUP = TRIP_CONFIRMED.copy(
                meetingPoint = MEETING_POINT.copy(
                        pickupType = PickupType.MEET_AND_GREET)
                                                               )

        val TRIP_DRIVER_EN_ROUTE_POINT_UNSET = TRIP.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE,
                meetingPoint = MEETING_POINT_UNSET)

        val TRIP_HISTORY = listOf(TRIP)

        val BLANK_TRIP_HISTORY = listOf<TripInfo>()

        val PAYMENT_TOKEN = BraintreeSDKToken(
                token = "njfdeilnvbflinvbiurnceernnvbrgtuverosa")

        val BLANK_PAYMENT_TOKEN = BraintreeSDKToken()

        val TRIP_STATE = TripState(TripStatus.NO_DRIVERS)

        const val QUOTE_LIST_ID = "129e51a-bc10-11e8-a821-0a580a0414db"

        const val REVERSE_GEO_DISPLAY_ADDRESS = "12 Grimmauld Place, OFTP HQ"

        const val SELECTED_DESTINATION_ADDRESS = "368 Oxford St, London W1D 1LU, UK"

        const val SELECTED_ADDRESS = "221B Baker St, Marylebone, London NW1 6XE, UK"

        const val SEARCH_ADDRESS = "221B Baker Street"

        const val SEARCH_ADDRESS_RESULT = "221B Baker Street, London, UK"

        const val SEARCH_ADDRESS_EXTRA = "368 Oxford St"

        const val SEARCH_ADDRESS_EXTRA_RESULT = "368 Oxford Street, London, UK"

        const val SEARCH_AIRPORT_ADDRESS = "Heathrow"

        const val SEARCH_GENERAL_ADDRESS = "London"

        const val SEARCH_INCORRECT_ADDRESS = "zzzzzzzzxxxxxxx"

        const val ADDRESS_ORIGIN = "221B Baker St, Marylebone, London NW1 6XE, UK"

        const val ADDRESS_DESTINATION = "368 Oxford St, London W1D 1LU, UK"

        const val FILL_EMAIL = "name@email"

        const val HELP_TEXT = "How can we help?"

        const val KARHOO_TCS = "Karhoo Platform Terms and Conditions"

        const val KARHOO_PRIVACY = "Karhoo Privacy Policy"

        const val KARHOO_LIBRARIES ="Notice for libraries"

        const val CARD_ENDING = "•••• 1234"

        const val SHOW_PASSWORD = "Show password"

        const val INVALID_PHONE = "Invalid phone number"

        const val ZOO_TEST_FLEET = "Antelope [Zoo]"

        const val THIRD_FLEET = "Third Fleet"

        const val TEST_FLEET = "KarhooTestFleet"

        const val PAST_DATE_TIME = "31 Jul 2019, 1:35PM"

        const val VEHICLE_DETAILS = "Mpv: 123 XYZ"

        const val CAR_DETAILS = "Saloon: ZATLOW"

        const val REG_PLATE = "BLKJVS"

        const val PRICE_TOTAL = "£35.50"

        const val TRIP_ID = "A5TH-R27D"

        const val KARHOO_ID = "Karhoo ID"

        const val PREBOOK_TIME_DATE = "2 Dec 2021, 8:00pm"

        val ORIGIN_TRIP = TripLocationInfo(
                displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                position = Position(latitude = LATITUDE, longitude = LONGITUDE))

        val DESTINATION_TRIP = TripLocationInfo(
                displayAddress = "368 Oxford St, London W1D 1LU, UK",
                placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM",
                position = Position(latitude = 51.5166744, longitude = LONGITUDE))

        val QUOTE = Quote(availabilityId = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
                          categoryName = "Exec",
                          currencyCode = "GBP",
                          fleetId = "A Taxi Id",
                          supplierName = "A Taxi Fleet",
                          highPrice = 577,
                          phoneNumber = "+123",
                          qta = 4,
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

        val MEDIUM = measureTimeMillis { 1000 }

        val LONG = measureTimeMillis { 3000 }

    }
}