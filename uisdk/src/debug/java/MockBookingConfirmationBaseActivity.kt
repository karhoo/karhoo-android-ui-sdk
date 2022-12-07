package com.karhoo.uisdk.booking.checkout.bookingconfirmation

import com.karhoo.sdk.api.model.*
import com.karhoo.sdk.api.network.request.QuoteQTA
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.checkout.bookingconfirmation.BookingConfirmationView
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyMode
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import org.joda.time.DateTime
import java.text.SimpleDateFormat
import java.util.*

class MockBookingConfirmationBaseActivity : BaseActivity() {
    override val layout: Int
        get() = com.karhoo.uisdk.R.layout.uisdk_mock_booking_confirmation_layout

    override fun handleExtras() {
        val bookingConfirmationView = BookingConfirmationView(
            JourneyDetails(
                TRIP_LOCATION_INFO_PICKUP,
                TRIP_LOCATION_INFO_DROPOFF,
                DateTime(SCHEDULED_DATE)
            ),
            QUOTE,
            null,
            null
        )

        bookingConfirmationView.setLoyaltyProperties(
            true,
            LoyaltyMode.BURN,
            10
        )

        bookingConfirmationView.actions = object : CheckoutViewContract.BookingConfirmationActions {
            override fun openRideDetails() {
                val activity = this@MockBookingConfirmationBaseActivity
                activity.finish()
            }

            override fun dismissedPrebookDialog() {
                val activity = this@MockBookingConfirmationBaseActivity
                activity.finish()
            }
        }

        this.supportFragmentManager.let {
            bookingConfirmationView.show(it, BookingConfirmationView.TAG)
        }
    }

    companion object {
        val TRIP_POSITION_PICKUP = Position(
            latitude = 51.523766,
            longitude = -0.1375291
        )

        val TRIP_POSITION_DROPOFF = Position(
            latitude = 51.514432,
            longitude = -0.1585557
        )

        val TRIP_LOCATION_INFO_PICKUP = LocationInfo(
            address = Address(
                displayAddress = "221B Baker St, Marylebone, London NW1 6XE, UK",
                buildingNumber = "221B",
                streetName = "Baker St",
                city = "Marleybone",
                postalCode = "NW1 6XE",
                region = "London",
                countryCode = "UK"
            ),
            position = TRIP_POSITION_PICKUP,
            placeId = "ChIJEYJiM88adkgR4SKDqHd2XUQ",
            timezone = "Europe/London",
            poiType = Poi.NOT_SET
        )

        val TRIP_LOCATION_INFO_DROPOFF = LocationInfo(
            address =  Address(
                displayAddress = "368 Oxford St, London W1D 1LU, UK",
                buildingNumber = "368",
                streetName = "Oxford St",
                city = "London",
                postalCode = "W1D 1LU",
                countryCode = "UK"
            ),
            position = TRIP_POSITION_DROPOFF,
            placeId = "ChIJyWu2IisbdkgRHIRWuD0ANfM",
            timezone = "Europe/London",
            poiType = Poi.NOT_SET
        )
        val QUOTE_PRICE = QuotePrice(
            currencyCode = "GBP",
            highPrice = 577,
            lowPrice = 577
        )

        val QUOTE_FLEET = Fleet(
            id = "52123bd9-cc98-4b8d-a98a-122446d69e79",
            name = "iCabbi [Sandbox]",
            logoUrl = "https://cdn.karhoo.com/d/images/logos/52123bd9-cc98-4b8d-a98a-122446d69e79.png",
            description = "Some fleet description",
            phoneNumber = "+447904839920",
            termsConditionsUrl = "http://www.google.com"
        )

        val QUOTE_VEHICLE = QuoteVehicle(
            vehicleClass = "Electric",
            vehicleQta = QuoteQTA(highMinutes = 30, lowMinutes = 1),
            luggageCapacity = 2,
            vehicleType = "standard",
            vehicleTags = arrayListOf("taxi, hybrid"),
            passengerCapacity = 2
        )

        val QUOTE = Quote(
            id = "NTIxMjNiZDktY2M5OC00YjhkLWE5OGEtMTIyNDQ2ZDY5ZTc5O3NhbG9vbg==",
            quoteType = QuoteType.ESTIMATED,
            quoteSource = QuoteSource.FLEET,
            price = QUOTE_PRICE,
            fleet = QUOTE_FLEET,
            pickupType = PickupType.CURBSIDE,
            vehicle = QUOTE_VEHICLE
        )

        fun getDate(dateScheduled: String): Date {
            val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm").apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateScheduled)
        }

        val SCHEDULED_DATE = getDate("2019-07-31T12:35:00Z")

    }
}