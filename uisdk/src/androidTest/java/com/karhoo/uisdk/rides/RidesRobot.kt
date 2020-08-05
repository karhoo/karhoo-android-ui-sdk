package com.karhoo.uisdk.rides

import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_DESTINATION
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_ORIGIN
import com.karhoo.uisdk.util.TestData.Companion.CAR_DETAILS
import com.karhoo.uisdk.util.TestData.Companion.PREBOOK_TIME_DATE
import com.karhoo.uisdk.util.TestData.Companion.PRICE_TOTAL
import com.karhoo.uisdk.util.TestData.Companion.ZOO_TEST_FLEET

fun rides(func: RidesRobot.() -> Unit) = RidesRobot().apply { func() }

fun rides(launch: Launch, func: RidesRobot.() -> Unit) = RidesRobot().apply {
    launch.launch()
    func()
}

class RidesRobot : BaseTestRobot() {

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }

    fun clickBackButtonRidesScreen() {
        clickBackToolbarButton()
    }

    fun clickBookRideButton() {
        clickButton(R.id.bookRideButton)
    }

    fun clickPastBookingsTabButton() {
        clickButtonByText("PAST")
    }

    fun clickUpcomingBookingsTabButton() {
        clickButtonByText("UPCOMING")
    }

    fun clickOnFirstRide() {
        clickButton(R.id.pickupBallIcon)
    }

    fun clickOnTrackDriver() {
        clickButton(R.id.trackButton)
    }
}

class ResultRobot : BaseTestRobot() {

    fun checkRidesScreenIsShown() {
        checkToolbarTitle(R.string.title_activity_rides)
    }

    fun checkNoUpcomingBookings() {
        textIsVisible(R.string.title_upcoming_rides_empty)
    }

    fun checkNoPastBookings() {
        textIsVisible(R.string.title_past_rides_empty)
    }

    fun checkErrorIsShown(expectedText: Int) {
        textIsVisible(expectedText)
    }

    fun checkErrorMessageIsShown(text: Int) {
        viewIsVisibleWithIndex(text, 0)
    }

    fun pastBookingHasExpectedStatus(expectedText: Int) {
        textIsVisibleIsDescendant(expectedText, R.id.stateText)
    }

    fun pastBookingHasExpectedPrice(expectedText: Int) {
    textIsVisibleIsDescendant(expectedText, R.id.priceText)
    }

    fun upcomingBookingHasExpectedStatus(expectedText: String) {
        stringIsVisible(expectedText)
    }

    fun pickUpTypeVisibleRidesScreen(pickupType: String) {
        stringIsVisibleIsDescendant(pickupType, R.id.pickupTypeText)
    }

    fun pickUpTypeLabelNotVisibleOnDropoffUpcoming() {
        viewIsNotVisible(R.id.pickupTypeText)
    }

    fun pickUpTypeLabelNotVisibleOnDropoffPast() {
        viewIsNotDisplayed(R.id.pickupTypeText)
    }

    fun prebookedTripUpcomingRidesFullCheck() {
        fleetNameIsVisible(ZOO_TEST_FLEET)
        fleetLogoIsVisible()
        timeAndDateFieldIsDisplayed()
        timeAndDateOfBookingIsVisible(PREBOOK_TIME_DATE)
        pickUpIconIsVisible()
        dropOffIconIsVisible()
        pickUpAddressIsVisible(ADDRESS_ORIGIN)
        dropOffAddressIsVisible(ADDRESS_DESTINATION)
        callFleetButtonIsEnabled()
    }

    fun DERTripUpcomingRidesFullCheck() {
        fleetNameIsVisible(ZOO_TEST_FLEET)
        fleetLogoIsVisible()
        timeAndDateFieldIsDisplayed()
        pickUpIconIsVisible()
        dropOffIconIsVisible()
        pickUpAddressIsVisible(ADDRESS_ORIGIN)
        dropOffAddressIsVisible(ADDRESS_DESTINATION)
        callFleetButtonIsEnabled()
        carDetailsAreVisible(CAR_DETAILS)
        trackDriverButtonIsEnabled()
    }

    fun fleetNameIsVisible(fleet: String) {
        stringIsVisibleIsDescendant(fleet, R.id.bookingTermsText)
    }

    fun fleetLogoIsVisible() {
        viewIsVisible(R.id.logoImage)
    }

    fun timeAndDateFieldIsDisplayed() {
        viewIsVisible(R.id.dateTimeText)
    }

    fun timeAndDateOfBookingIsVisible(date: String) {
        stringIsVisibleIsDescendantIgnoreCase(date, R.id.dateTimeText)
    }

    fun pickUpIconIsVisible() {
        viewIsVisible(R.id.pickupBallIcon)
    }

    fun dropOffIconIsVisible() {
        viewIsVisible(R.id.dropoffBallIcon)
    }

    fun pickUpAddressIsVisible(address: String) {
        stringIsVisibleIsDescendant(address, R.id.pickupLabel)
    }

    fun dropOffAddressIsVisible(address: String) {
        stringIsVisibleIsDescendant(address, R.id.dropOffLabel)
    }

    fun callFleetButtonIsEnabled() {
        buttonIsEnabled(R.id.callButton)
    }

    fun trackDriverButtonIsEnabled() {
        buttonIsEnabled(R.id.trackButton)
    }

    fun carDetailsAreVisible(categoryAndPlate: String) {
        stringIsVisibleIsDescendant(categoryAndPlate, R.id.carText)
    }

    fun completedTripPastRidesScreenFullCheck() {
        fleetNameIsVisible(ZOO_TEST_FLEET)
        fleetLogoIsVisible()
        timeAndDateFieldIsDisplayed()
        pickUpIconIsVisible()
        pickUpAddressIsVisible(ADDRESS_ORIGIN)
        dropOffAddressIsVisible(ADDRESS_DESTINATION)
        dropOffIconIsVisible()
        carDetailsAreVisible(CAR_DETAILS)
        pastBookingHasExpectedStatus(R.string.completed)
        priceIsVisible(PRICE_TOTAL)
    }

    fun priceIsVisible(price: String) {
        stringIsVisibleIsDescendant(price, R.id.priceText)
    }

    fun cancelledByFleetPrebookedFullCheck() {
        fleetNameIsVisible(ZOO_TEST_FLEET)
        fleetLogoIsVisible()
        timeAndDateFieldIsDisplayed()
        pickUpIconIsVisible()
        pickUpAddressIsVisible(ADDRESS_ORIGIN)
        dropOffAddressIsVisible(ADDRESS_DESTINATION)
        dropOffIconIsVisible()
        pastBookingHasExpectedStatus(R.string.cancelled)
        pastBookingHasExpectedPrice(R.string.cancelled)
    }

    fun cancelledByUserPrebookedFullCheck() {
        cancelledByFleetPrebookedFullCheck()
    }
}
