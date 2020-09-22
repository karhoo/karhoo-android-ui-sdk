package com.karhoo.uisdk.ridedetail

import android.content.Intent
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_DESTINATION
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_ORIGIN
import com.karhoo.uisdk.util.TestData.Companion.HELP_TEXT
import com.karhoo.uisdk.util.TestData.Companion.KARHOO_ID
import com.karhoo.uisdk.util.TestData.Companion.PAST_DATE_TIME
import com.karhoo.uisdk.util.TestData.Companion.PREBOOK_TIME_DATE
import com.karhoo.uisdk.util.TestData.Companion.PRICE_TOTAL
import com.karhoo.uisdk.util.TestData.Companion.TEST_FLEET
import com.karhoo.uisdk.util.TestData.Companion.TRIP_ID
import com.karhoo.uisdk.util.TestData.Companion.VEHICLE_DETAILS

fun rideDetail(func: RideDetailRobot.() -> Unit) = RideDetailRobot().apply { func() }

fun rideDetail(launch: Launch, intent: Intent? = null, func: RideDetailRobot.() -> Unit) = RideDetailRobot().apply {
    launch.launch(intent)
    func()
}

class RideDetailRobot : BaseTestRobot() {

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }

    fun clickCancelRideDetails() {
        clickButton(R.id.cancelRideButton)
    }

    fun confirmCancellationRideDetails() {
        clickOnButtonInAlertDialog(AlertDialogButton.POSITIVE)
    }

    fun clickOnCancel() {
        dialogClickButtonByText(R.string.cancel)
    }

    fun clickOnDismiss() {
        dialogClickButtonByText(R.string.dismiss)
    }

    fun clickOnReportIssue() {
        clickButton(R.id.reportIssueButton)
    }
}

class ResultRobot : BaseTestRobot() {

    fun couldNotCancelMessageIsDisplayed() {
        dialogTextIsVisible(R.string.difficulties_cancelling_message)
    }

    fun checkErrorIsShown(expectedText: Int) {
        checkSnackbarWithText(expectedText)
    }

    fun completedRideFullCheck() {
        pastRideFleetNameVisible(TEST_FLEET)
        pastRideFleetLogoVisible()
        pastRideDateAndTimeVisible(PAST_DATE_TIME)
        pastRidePickUpAddressVisible(ADDRESS_ORIGIN)
        pastRideDropOffAddressVisible(ADDRESS_DESTINATION)
        pastRideVehicleDetailsVisible(VEHICLE_DETAILS)
        pastRideStateIconVisible()
        pastRideStatusLabelVisible()
        pastRideStatusVisible(R.string.completed)
        pastRidePriceLabelVisible()
        pastRidePriceIsVisible(PRICE_TOTAL)
        pastRideKarhooIdLabelVisible()
        pastRideKarhooIdVisible(TRIP_ID)
        pastTripRatingFieldsVisible()
        pastTripRebookButtonVisibleAndEnabled()
        pastTripReportIssueButtonVisibleAndEnabled()
    }

    fun completedRideFullCheckFromTrip() {
        pastRideFleetNameVisible(TEST_FLEET)
        pastRideFleetLogoVisible()
        pastRideDateAndTimeVisible(PAST_DATE_TIME)
        pastRidePickUpAddressVisible(ADDRESS_ORIGIN)
        pastRideDropOffAddressVisible(ADDRESS_DESTINATION)
        pastRideVehicleDetailsVisible(VEHICLE_DETAILS)
        pastRideStateIconVisible()
        pastRideStatusLabelVisible()
        pastRideStatusVisible(R.string.completed)
        pastRideBaseFareLabelIsVisible()
        pastRideKarhooIdLabelVisible()
        pastRideKarhooIdVisible(TRIP_ID)
        pastTripRatingFieldsVisible()
        pastTripRebookButtonVisibleAndEnabled()
        pastTripReportIssueButtonVisibleAndEnabled()
    }

    fun pastRideFleetNameVisible(expectedText: String) {
        stringIsVisibleIsDescendant(expectedText, R.id.bookingTermsText)
    }

    fun pastRideFleetLogoVisible() {
        viewIsVisible(R.id.logoImage)
    }

    fun pastRideDateAndTimeVisible(expectedText: String) {
        stringIsVisibleIsDescendantIgnoreCase(expectedText, R.id.dateTimeText)
    }

    fun prebookedRideDateAndTimeIsVisible(date: String) {
        stringIsVisibleIsDescendantIgnoreCase(date, R.id.dateTimeText)
    }

    fun pastRidePickUpAddressVisible(expectedText: String) {
        stringIsVisibleIsDescendant(expectedText, R.id.pickupLabel)
    }

    fun pastRideDropOffAddressVisible(expectedText: String) {
        stringIsVisibleIsDescendant(expectedText, R.id.dropOffLabel)
    }

    fun pastRideVehicleDetailsVisible(expectedText: String) {
        stringIsVisibleIsDescendant(expectedText, R.id.carText)
    }

    fun pastRideStateIconVisible() {
        viewIsVisible(R.id.stateIcon)
    }

    fun pastRideStatusLabelVisible() {
        stringIsVisible("Status")
    }

    fun pastRideStatusVisible(expectedText: Int) {
        textIsVisibleIsDescendant(expectedText, R.id.stateText)
    }

    fun pastRidePriceLabelVisible() {
        textIsVisible(R.string.price)
    }

    fun baseFareLabelIsVisible() {
        textIsVisible(R.string.faretype_info_base)
    }

    fun pastRideBaseFareLabelIsVisible() {
        textIsVisible(R.string.base)
    }

    fun pastRidePriceIsVisible(expectedText: String) {
        stringIsVisibleIsDescendant(expectedText, R.id.priceText)
    }

    fun pastRideBaseFareShowsAsPending(expectedText: Int) {
        textIsVisibleIsDescendant(expectedText, R.id.priceText)
    }

    fun pastRideKarhooIdLabelVisible() {
        stringIsVisible(KARHOO_ID)
    }

    fun pastRideKarhooIdVisible(expectedText: String) {
        stringIsVisibleIsDescendant(expectedText, R.id.karhooId)
    }

    fun pastTripRatingFieldsVisible() {
        viewIsVisible(R.id.ratingLabel)
        viewIsVisible(R.id.ratingBar)
    }

    fun pastTripRebookButtonVisibleAndEnabled() {
        buttonIsEnabled(R.id.rebookRideButton)
    }

    fun pastTripReportIssueButtonVisibleAndEnabled() {
        buttonIsEnabled(R.id.reportIssueButton)
    }

    fun checkForPendingState() {
        textIsVisible(R.string.pending)
    }

    fun cancelledByUserRideFullCheck() {
        pastRideFleetNameVisible(TEST_FLEET)
        pastRideFleetLogoVisible()
        pastRideDateAndTimeVisible(PAST_DATE_TIME)
        pastRidePickUpAddressVisible(ADDRESS_ORIGIN)
        pastRideDropOffAddressVisible(ADDRESS_DESTINATION)
        pastRideVehicleDetailsVisible(VEHICLE_DETAILS)
        pastRideStateIconVisible()
        pastRideStatusLabelVisible()
        pastRideStatusVisible(R.string.cancelled)
        pastRideBaseFareLabelIsVisible()
        pastRideBaseFareShowsAsPending(R.string.cancelled)
        pastRideKarhooIdLabelVisible()
        pastRideKarhooIdVisible(TRIP_ID)
        pastTripRebookButtonVisibleAndEnabled()
        pastTripReportIssueButtonVisibleAndEnabled()
    }

    fun confirmedRideFullCheck() {
        upcomingTripFleetNameVisible(TEST_FLEET)
        upcomingRideFleetLogoVisible()
        pickUpAndDropOffIconVisible()
        pickUpAddressVisible(address = ADDRESS_ORIGIN)
        dropOffAddressVisible(address = ADDRESS_DESTINATION)
        rideStatusVisible(status = R.string.confirmed)
        upcomingTripPriceChecks(fare = PRICE_TOTAL)
        upcomingTripIDVisible(trip = TRIP_ID)
        upcomingTripcCancelRideButtonIsEnabled()
        upcomingTripCallFleetButtonIsEnabled()
    }

    fun cancelledByDriverRideFullCheck() {
        cancelledByUserRideFullCheck()
    }

    fun cancelledByKarhooRideFullCheck() {
        cancelledByUserRideFullCheck()
    }

    fun upcomingDriverEnRouteRideFullCheck() {
        upcomingTripFleetNameVisible(TEST_FLEET)
        upcomingRideFleetLogoVisible()
        pickUpAndDropOffIconVisible()
        upcomingVehicleDetailsVisible(VEHICLE_DETAILS)
        pickUpAddressVisible(address = ADDRESS_ORIGIN)
        dropOffAddressVisible(address = ADDRESS_DESTINATION)
        rideStatusVisible(status = R.string.confirmed)
        upcomingTripPriceChecks(fare = PRICE_TOTAL)
        upcomingTripIDVisible(trip = TRIP_ID)
        upcomingTripcCancelRideButtonIsEnabled()
        upcomingTripCallFleetButtonIsEnabled()
    }

    fun upcomingTripFleetNameVisible(fleet: String) {
        stringIsVisibleIsDescendant(fleet, R.id.bookingTermsText)
    }

    fun upcomingRideFleetLogoVisible() {
        viewIsVisible(R.id.logoImage)
    }

    fun pickUpAndDropOffIconVisible() {
        viewIsVisible(R.id.pickupBallIcon)
        viewIsVisible(R.id.dropoffBallIcon)
    }

    fun pickUpAddressVisible(address: String) {
        stringIsVisibleIsDescendant(address, R.id.pickupLabel)
    }

    fun dropOffAddressVisible(address: String) {
        stringIsVisibleIsDescendant(address, R.id.dropOffLabel)
    }

    fun rideStatusVisible(status: Int) {
        viewIsVisible(R.id.stateIcon)
        textIsVisibleIsDescendant(status, R.id.stateText)
    }

    fun upcomingTripPriceChecks(fare: String) {
        viewIsVisible(R.id.priceTypeText)
        stringIsVisibleIsDescendant(fare, R.id.priceText)
    }

    fun upcomingTripIDVisible(trip: String) {
        stringIsVisible(KARHOO_ID)
        stringIsVisibleIsDescendant(trip, R.id.karhooId)
    }

    fun upcomingTripcCancelRideButtonIsEnabled() {
        buttonIsEnabled(R.id.cancelRideButton)
    }

    fun upcomingTripCallFleetButtonIsEnabled() {
        buttonIsEnabled(R.id.contactFleetButton)
    }

    fun upcomingVehicleDetailsVisible(vehicle: String) {
        stringIsVisibleIsDescendant(vehicle, R.id.carText)
    }

    fun upcomingTripcCancelRideButtonIsNotVisible() {
        viewIsNotVisible(R.id.cancelRideButton)
    }

    fun upcomingPassengerOnBoardRideDetailsFullCheck() {
        upcomingTripFleetNameVisible(TEST_FLEET)
        upcomingRideFleetLogoVisible()
        pickUpAndDropOffIconVisible()
        upcomingVehicleDetailsVisible(VEHICLE_DETAILS)
        pickUpAddressVisible(address = ADDRESS_ORIGIN)
        dropOffAddressVisible(address = ADDRESS_DESTINATION)
        rideStatusVisible(status = R.string.pass_on_board)
        upcomingTripPriceChecks(fare = PRICE_TOTAL)
        upcomingTripIDVisible(trip = TRIP_ID)
        upcomingTripcCancelRideButtonIsNotVisible()
        upcomingTripCallFleetButtonIsEnabled()
    }

    fun upcomingPrebookedRideDetailsFullCheck() {
        confirmedRideFullCheck()
        prebookedRideDateAndTimeIsVisible(PREBOOK_TIME_DATE)
    }

    fun cancelDialogIsNotVisible() {
        textIsNotVisible(R.string.cancel_your_ride)
    }

    fun cancelConfirmationIsVisible() {
        dialogTextIsVisible(R.string.cancel_ride_successful)
        dialogTextIsVisible(R.string.cancel_ride_successful_message)
    }

    fun helpTextIsVisibleReportIssue() {
        stringIsVisibleIsDescendantWeb(HELP_TEXT, R.id.khWebView)
    }

    fun contactDetailFieldsReportIssue() {
        stringIsVisibleIsDescendantWeb("Contact Details", R.id.khWebView)
        stringIsVisibleIsDescendantWeb("Full Name", R.id.khWebView)
        stringIsVisibleIsDescendantWeb("Email", R.id.khWebView)
        stringIsVisibleIsDescendantWeb("Phone", R.id.khWebView)
        stringIsVisibleIsDescendantWeb("Subject", R.id.khWebView)
        stringIsVisibleIsDescendantWeb("Description", R.id.khWebView)
    }

    fun correctDetailsFilledReportIssue() {
        stringIsVisibleIsDescendantWeb(TestData.USER.firstName, R.id.khWebView)
        stringIsVisibleIsDescendantWeb(TestData.USER.lastName, R.id.khWebView)
        stringIsVisibleIsDescendantWeb(TestData.USER.email, R.id.khWebView)
        stringIsVisibleIsDescendantWeb(TestData.USER.phoneNumber, R.id.khWebView)
        stringIsVisibleIsDescendantWeb("Trip ID", R.id.khWebView)
        stringIsVisibleIsDescendantWeb(TestData.TRIP.displayTripId, R.id.khWebView)
    }

    fun reportIssueWithRideElementsChecks() {
        helpTextIsVisibleReportIssue()
        contactDetailFieldsReportIssue()
        correctDetailsFilledReportIssue()
    }
}