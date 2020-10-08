package com.karhoo.uisdk.util

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.google.gson.Gson
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
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteId
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripList
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.sdk.api.model.TripState
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.model.Vehicle
import com.karhoo.sdk.api.model.VehicleAttributes
import com.karhoo.sdk.api.model.Vehicles
import com.karhoo.sdk.api.model.adyen.AdyenPublicKey
import com.karhoo.sdk.api.network.request.QuoteQTA
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.system.measureTimeMillis

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class TestData {

    companion object {

        const val ADYEN = "ADYEN"

        const val BRAINTREE = "Braintree"

        const val REVERSE_GEO_DISPLAY_ADDRESS = "12 Grimmauld Place, OFTP HQ"

        const val SELECTED_ADDRESS_EXTRA = "368 Oxford St, London W1D 1LU, UK"

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

        const val KARHOO_LIBRARIES = "Notice for libraries"

        const val CARD_ENDING = "•••• 1234"

        const val SHOW_PASSWORD = "Show password"

        const val INVALID_PHONE = "Invalid phone number"

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

        const val LATITUDE = 51.5166744

        const val LONGITUDE = -0.1769328

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
                currency = DEFAULT_CURRENCY)

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

        val FARE = Fare(
                state = "COMPLETED",
                breakdown = FareBreakdown(
                        total = 0,
                        currency = DEFAULT_CURRENCY))

        val TRIP = TripInfo(
                tripId = "b6a5f9dc-9066-4252-9013-be85dfa563bc",
                followCode = "b6a5f9dc-9066-4252-9013-be85dfa563bc",
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

        val TRIP_ARRIVED = TRIP.copy(tripState = TripStatus.ARRIVED)

        val TRIP_DER = TRIP.copy(tripState = TripStatus.DRIVER_EN_ROUTE)

        val TRIP_POB = TRIP.copy(tripState = TripStatus.PASSENGER_ON_BOARD)

        val TRIP_COMPLETED = TRIP.copy(tripState = TripStatus.COMPLETED)

        val TRIP_CANCELLED_BY_FLEET = TRIP.copy(tripState = TripStatus.CANCELLED_BY_DISPATCH)

        val TRIP_ALLOCATING = TRIP.copy(tripState = TripStatus.REQUESTED)

        val TRIP_STATUS_POB = TripState(TripStatus.PASSENGER_ON_BOARD)

        val TRIP_STATUS_DER = TripState(TripStatus.DRIVER_EN_ROUTE)

        val TRIP_STATUS_CANCELLED_BY_USER = TripState(TripStatus.CANCELLED_BY_USER)

        val TRIP_STATUS_REQUESTED = TripState(TripStatus.REQUESTED)

        val TRIP_STATUS_ARRIVED = TripState(TripStatus.ARRIVED)

        val TRIP_STATUS_CANCELLED_BY_FLEET = TripState(TripStatus.CANCELLED_BY_DISPATCH)

        val TRIP_STATUS_COMPLETED = TripState(TripStatus.COMPLETED)

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

        val AVAILABILITY = Availability(vehicles = AvailabilityVehicle(classes = listOf("Saloon", "Taxi", "MPV", "Exec", "Electric", "Moto")))

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

        val TRIP_CONFIRMED = TRIP.copy(
                tripState = TripStatus.CONFIRMED)

        val TRIP_PREBOOKED = TRIP_CONFIRMED.copy(
                dateScheduled = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse("2021-12-2T20:00:00Z"))

        val TRIP_CONFIRMED_MEETING_POINT_UNSET = TRIP_CONFIRMED.copy(
                meetingPoint = MEETING_POINT_UNSET
                                                                    )

        val TRIP_DRIVER_EN_ROUTE_POINT_UNSET = TRIP.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE,
                meetingPoint = MEETING_POINT_UNSET)

        val ORIGIN_TRIP = TripLocationInfo(
                displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
                position = Position(latitude = LATITUDE, longitude = LONGITUDE))

        val DESTINATION_TRIP = TripLocationInfo(
                displayAddress = "368 Oxford St, London W1D 1LU, UK",
                placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM",
                position = Position(latitude = 51.5166744, longitude = LONGITUDE))

        val QUOTE = Quote(id = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
                          quoteType = QuoteType.ESTIMATED,
                          quoteSource = QuoteSource.FLEET,
                          price = QUOTE_PRICE,
                          fleet = QUOTE_FLEET,
                          pickupType = PickupType.CURBSIDE,
                          vehicle = QUOTE_VEHICLE,
                          vehicleAttributes = VEHICLE_ATTRIBUTES)

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
        val TRIP_FIXED_DATE = TRIP.copy(
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

        val TRIP_DER_NO_DRIVER_DETAILS = TRIP.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE,
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

        val TRIP_DER_NO_VEHICLE_DETAILS = TRIP.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE,
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

        val TRIP_REQUESTED_DETAILS = TRIP.copy(tripState = TripStatus.REQUESTED)

        val TRIP_CONFIRMED_DETAILS = TRIP.copy(tripState = TripStatus.CONFIRMED)

        val TRIP_REQUEST_INCOMPLETE = TRIP.copy(tripState = TripStatus.INCOMPLETE)

        /**
         *
         * Trip History
         *
         */
        val RIDE_SCREEN_CONFIRMED = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.CONFIRMED
                                                                        )))

        val RIDE_SCREEN_PREBOOKED: TripList
            get() = TripList(bookings = listOf(TRIP_FIXED_DATE))

        val RIDE_SCREEN_PREBOOKED_CANCELLED_BY_FLEET = TripList(bookings = listOf(TRIP.copy(
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

        val RIDE_SCREEN_PREBOOKED_CANCELLED_BY_USER = TripList(bookings = listOf(TRIP.copy(
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

        val RIDE_SCREEN_COMPLETED = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.COMPLETED
                                                                        )))

        val RIDE_SCREEN_DER = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.DRIVER_EN_ROUTE
                                                                  )))

        val RIDE_SCREEN_COMPLETED_AIRPORT_PICKUP = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.COMPLETED,
                meetingPoint = MeetingPoint(pickupType = PickupType.MEET_AND_GREET)
                                                                                       )))

        val RIDE_SCREEN_INCOMPLETE = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.INCOMPLETE
                                                                         )))

        val RIDE_SCREEN_CANCELLED_USER = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.CANCELLED_BY_USER
                                                                             )))

        val RIDE_SCREEN_CANCELLED_KARHOO = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.CANCELLED_BY_KARHOO
                                                                               )))

        val RIDE_SCREEN_CANCELLED_DRIVER = TripList(bookings = listOf(TRIP.copy(
                tripState = TripStatus.CANCELLED_BY_DISPATCH
                                                                               )))

        val RIDE_SCREEN_DER_AIRPORT_PICKUP = TripList(bookings = listOf(TRIP.copy(
                meetingPoint = MeetingPoint(pickupType = PickupType.MEET_AND_GREET)
                                                                                 )))

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

        val VEHICLES_ASAP = Vehicles(
                status = "PROGRESSING",
                id = QUOTE_LIST_ID_ASAP.quoteId,
                availability = AVAILABILITY,
                quotes = listOf(
                        QUOTE,
                        QUOTE.copy(
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
                        QUOTE.copy(
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
                        QUOTE.copy(
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
                        QUOTE.copy(
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
                        QUOTE.copy(
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

        val ADYEN_PROVIDER = PaymentProvider(Provider(id = "Adyen"))

        val ADYEN_PUBLIC_KEY = AdyenPublicKey("12345678")

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
                phoneNumber = "7777111111",
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
                phoneNumber = "7910000000"
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
        val USER_PHONE_NUMBER = "7777111111"

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
        val USER_UPDATED_PHONE_NUMBER = "7910000000"

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

        fun getDate(dateScheduled: String): Date {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateScheduled)
        }

        val SHORT = measureTimeMillis { 350 }

        val MEDIUM = measureTimeMillis { 1000 }

        val LONG = measureTimeMillis { 5000 }

        fun setUserInfo(provider: String) {
            val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            sharedPreferences.edit()
                    .putString("first_name", "John")
                    .putString("last_name", "Smith")
                    .putString("email", "test@test.test")
                    .putString("mobile_number", "123")
                    .putString("user_id", "1234")
                    .putString("organisations", Gson().toJson(
                            listOf(Organisation(id = "organisation_id",
                                                name = "B2C DefaultOrgForKarhooAppUsers",
                                                roles = emptyList()))))
                    .putString("locale", "en-GB")
                    .putString("payment_provider_id", provider)
                    .putString("last_four", "1234")
                    .putString("card_type", CardType.MASTERCARD.value)
                    .apply()
            editor.commit()
        }
    }
}