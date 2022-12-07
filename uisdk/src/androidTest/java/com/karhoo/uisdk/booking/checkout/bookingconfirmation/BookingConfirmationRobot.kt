package com.karhoo.uisdk.booking.checkout.bookingconfirmation

import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot


fun bookingConfirmation(func: BookingConfirmationRobot.() -> Unit) = BookingConfirmationRobot().apply { func() }

class BookingConfirmationRobot: BaseTestRobot() {

    fun scrollUpBookingConfirmation() {
        scrollUp(R.id.masterBottomLayout)
    }

    fun checkBookingPickupPrimaryAddress() {
        textIsVisibleInDescendant("221B Baker St", R.id.pickupAddressTextPrimary)
    }

    fun checkBookingPickupSecondaryAddress() {
        textIsVisibleInDescendant("Marleybone, NW1 6XE, UK", R.id.pickupAddressTextSecondary)
    }

    fun checkBookingDestinationPrimaryAddress() {
        textIsVisibleInDescendant("368 Oxford St", R.id.destinationAddressTextPrimary)
    }

    fun checkBookingDestinationSecondaryAddress() {
        textIsVisibleInDescendant("London, W1D 1LU, UK", R.id.destinationAddressTextPrimary)
    }

    fun checkBookingTimeText() {
        textIsVisibleInDescendant("3:35 PM", R.id.bookingTimeText)
    }

    fun checkBookingDateText() {
        textIsVisibleInDescendant("Wed, 31/07/2019 PM", R.id.bookingDateText)
    }

    fun checkBookingPriceText() {
        textIsVisibleInDescendant("£5.37", R.id.bookingDateText)
    }

    fun checkBookingEstimatedFareTypeText() {
        textIsVisibleInDescendant("Estimated price", R.id.bookingDateText)
    }

    fun checkBookingConfirmationTitle() {
        textIsVisible(R.string.kh_uisdk_booking_confirmation)
    }

    fun checkLoyaltyVisibility() {
        viewIsVisible(R.id.loyaltyStaticDetails)
    }
}