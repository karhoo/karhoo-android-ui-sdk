package com.karhoo.uisdk.booking.checkout

import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot

fun checkoutRobot(func: CheckoutRobot.() -> Unit) = CheckoutRobot().apply { func() }

class CheckoutRobot : BaseTestRobot() {

    fun checkPickupPrimaryAddress() {
        textIsVisibleInDescendant("221B Baker St", R.id.pickupAddressTextPrimary)
    }

    fun checkPickupSecondaryAddress() {
        textIsVisibleInDescendant("Marleybone, NW1 6XE, UK", R.id.pickupAddressTextSecondary)
    }

    fun checkDestinationPrimaryAddress() {
        textIsVisibleInDescendant("368 Oxford St", R.id.destinationAddressTextPrimary)
    }

    fun checkDestinationSecondaryAddress() {
        textIsVisibleInDescendant("London, W1D 1LU, UK", R.id.destinationAddressTextPrimary)
    }

    fun checkTimeText() {
        textIsVisibleInDescendant("3:35 PM", R.id.bookingTimeText)
    }

    fun checkDateText() {
        textIsVisibleInDescendant("Wed, 31/07/2019 PM", R.id.bookingDateText)
    }

    fun checkPriceText() {
        textIsVisibleInDescendant("£5.37", R.id.bookingDateText)
    }

    fun checkEstimatedFareTypeText() {
        textIsVisibleInDescendant("Estimated price", R.id.bookingDateText)
    }

    fun checkVehicleWidgetTitleText() {
        textIsVisibleInDescendant("Standard", R.id.vehicleType)
    }

    fun checkVehicleWidgetFleetText() {
        textIsVisibleInDescendant("iCabbi [Sandbox]", R.id.vehicleFleet)
    }

    fun checkVehicleWidgetPassengerCountText() {
        textIsVisibleInDescendant("2", R.id.peopleCapacityText)
    }

    fun checkVehicleWidgetLuggageCountText() {
        textIsVisibleInDescendant("2", R.id.luggageCapacityText)
    }

    fun checkVehicleCancellationText() {
        stringIsVisible("Free cancellation up to 2 minutes before pickup")
    }

    fun checkPassengerViewTitle() {
        stringIsVisible("Passenger")
    }

    fun checkPassengerViewSubtitle() {
        stringIsVisible("Add passenger details")
    }

    fun clickPassengerButton() {
        clickButton(R.id.bookingCheckoutPassengerView)
    }

    fun checkPassengerFirstNameVisible() {
        viewIsVisible(R.id.passengerViewTitle)
    }

    fun clickSavePassengerButton() {
        clickButton(R.id.passengerActionButton)
    }

    fun checkPassengerFirstNameCorrect() {
        stringIsVisible("John")
    }

    fun checkPassengerLastNameCorrect() {
        stringIsVisible("Wick")
    }

    fun checkPassengerPhoneCorrect() {
        textIsVisibleInDescendant("12064512559", R.id.mobileNumberInput)
    }

    fun checkPassengerViewGone() {
        viewIsNotVisible(R.id.mobileNumberInput)
    }

    fun checkPassengerNameVisibleOnCheckout() {
        stringIsVisible("John Wick")
    }

    fun checkPassengerPhoneNumberVisibleOnCheckout() {
        stringIsVisible("+12064512559")
    }

    fun checkCommentViewTitle() {
        stringIsVisible("Comment")
    }

    fun checkCommentViewSubtitle() {
        stringIsVisible("Comment for your driver")
    }

    fun checkAirportViewTitle() {
        stringIsVisible("Flight number")
    }

    fun checkAirportViewSubtitle() {
        stringIsVisible("Add a flight number")
    }

    fun checkAirportViewSubtitleFilled() {
        stringIsVisible("1234")
    }

    fun clickAirportWidget() {
        clickButton(R.id.bookingCheckoutTravelDetailsView)
    }

    fun checkAirportWidgetTitle() {
        stringIsVisible("Add a flight number")
    }

    fun fillFlightInfo() {
        fillText(R.id.checkoutTravelDetailsEditText, "1234")
    }

    fun saveFlightInfo() {
        clickButton(R.id.checkoutTravelDetailsSave)
    }

    fun checkTrainTrackingViewTitle() {
        stringIsVisible("Train number")
    }

    fun clickTrainTrackingWidget() {
        clickButton(R.id.bookingCheckoutTravelDetailsView)
    }

    fun checkTrainTrackingWidgetTitle() {
        stringIsVisible("Add a train number")
    }

    fun fillTrainTrackingInfo() {
        fillText(R.id.checkoutTravelDetailsEditText, "12345")
    }

    fun checkTrainTrackingWidgetSubtitleFilled() {
        stringIsVisible("12345")
    }

    fun saveTrainTrackingInfo() {
        clickButton(R.id.checkoutTravelDetailsSave)
    }

    fun checkPriceDetailsText() {
        stringIsVisible("Price Details")
    }

    fun clickOnPriceDetailsIcon() {
        clickButton(R.id.priceLayout)
    }

    fun checkLegalNoticeText() {
        subStringIsVisible("The data collected is electronically")
    }

    fun checkTermsText() {
        subStringIsVisible("By making a booking you agree")
    }

    fun scrollUpCheckout() {
        scrollUp(R.id.bookingCheckoutViewLayout)
    }

    fun checkLoyaltyVisibility() {
        viewIsVisible(R.id.loyaltyStaticDetails)
    }

    fun checkLoyaltyNotVisible() {
        viewIsNotVisible(R.id.loyaltyStaticDetails)
    }

    fun addToCalendarVisibility() {
        viewIsVisible(R.id.addToCalendar)
    }
}