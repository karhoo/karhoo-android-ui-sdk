package com.karhoo.uisdk.trip

import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.REG_PLATE
import org.junit.Assert

fun trip(func: TripRobot.() -> Unit) = TripRobot().apply { func() }

fun trip(launch: Launch, func: TripRobot.() -> Unit) = TripRobot().apply {
    launch.launch()
    func()
}

class TripRobot : BaseTestRobot() {

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }

    fun clickOnDriverDetails() {
        clickButton(R.id.rideOptionsLabel)
    }

    fun clickOnCancelRide() {
        clickButton(R.id.cancelButton)
    }

    fun clickOnCancelYourRideCancellation() {
        clickOnButtonInAlertDialog(AlertDialogButton.NEGATIVE)
    }

    fun clickConfirmCancellation() {
        dialogClickButtonByText(R.string.kh_uisdk_ok)
    }

    fun clickAlternativeButton() {
        dialogClickButtonByText(R.string.kh_uisdk_alternative)
    }

    fun clickOKOnCancelledConfirmation() {
        dialogClickButtonByText(R.string.kh_uisdk_dismiss)
    }

    fun checkCancellationFeeIsNotShown() {
        dialogTextIsVisibleString(TestData.PROCEED_WITH_CANCELLATION)
    }

    fun checkCancellationFeeIsShown() {
        dialogTextIsVisibleString(TestData.BOOKING_FEE_NOTIFICATION)
    }

}

class ResultRobot : BaseTestRobot() {

    fun checkErrorIsShown(expectedText: Int) {
        checkSnackbarWithText(expectedText)
    }

    fun checkTextInDriverStatusBanner(expectedText: Int) {
        textIsVisible(expectedText)
    }

    fun checkRideInProgress() {
        textView(R.id.driverDetailsLayout)
    }

    fun cancellationConfirmation() {
        textIsVisible(R.string.kh_uisdk_cancel_ride_successful)
    }

    fun tripSummaryPickUpLogoVisible() {
        viewIsVisible(R.id.pickUpFullIcon)
    }

    fun driverPhotoPlaceHolderPresent() {
        viewIsVisible(R.id.driverPhotoImage)
    }

    fun driverNamePresent() {
        viewIsVisible(R.id.driverNameText)
    }

    fun driverNameNotPresent() {
        viewIsNotDisplayed(R.id.driverNameText)
    }

    fun carTypePresent() {
        viewIsVisible(R.id.carTypeText)
    }

    fun carTypeIsBlank(carType: String) {
        stringIsVisibleIsDescendant(carType, R.id.carTypeText)
    }

    fun licenceNumberPresent() {
        viewIsVisible(R.id.licenceNumberText)
    }

    fun licenceNumberNotVisible() {
        viewIsNotDisplayed(R.id.licenceNumberText)
    }

    fun registrationPlatePresent() {
        viewIsVisible(R.id.registrationPlateText)
    }

    fun registrationPlateIsVisible(plate: String) {
        stringIsVisibleIsDescendant(plate, R.id.registrationPlateText)
    }

    fun pickUpAddressField() {
        viewIsVisible(R.id.subtitlePickupLabel)
        viewIsVisible(R.id.pickupLabel)
    }

    fun PickUpAddressVisible(address: String) {
        Assert.assertEquals(address, getStringFromTextView(R.id.pickupLabel))
    }

    fun DropOffAddressVisible(address: String) {
        Assert.assertEquals(address, getStringFromTextView(R.id.dropOffLabel))
    }

    fun ETAWidgetVisible() {
        viewIsVisible(R.id.etaLayout)
    }

    fun locateMeButtonVisible() {
        viewIsVisible(R.id.locateMeButton)
    }

    fun mapViewIsVisible() {
        viewIsVisible(R.id.mapView)
    }

    fun cancelButtonIsEnabled() {
        buttonIsEnabled(R.id.cancelButton)
    }

    fun contactDriverButtonIsEnabled() {
        buttonIsEnabled(R.id.contactDriverButton)
    }

    fun contactFleetButtonIsEnabled() {
        buttonIsEnabled(R.id.contactFleetButton)
    }

    fun contactDriverButtonIsNotVisible() {
        viewIsNotDisplayed(R.id.contactDriverButton)
    }

    fun noETABubbleVisible() {
        viewIsNotVisible(R.id.etaWidget)
    }

    fun noCancelButtonVisible() {
        viewIsNotVisible(R.id.cancelButton)
    }

    fun arrivalTimeBubbleVisible() {
        viewIsVisible(R.id.arrivalLabel)
    }

    fun driverDetailsNoLongerExpanded() {
        viewIsNotVisible(R.id.cancelButton)
    }

    fun alternativeButtonIsEnabled() {
        dialogButtonByTextIsEnabled(R.string.kh_uisdk_alternative)
    }

    fun okCancelledByFleetButtonEnabled() {
        dialogButtonByTextIsEnabled(R.string.kh_uisdk_ok)
    }

    fun pickUpAddressCheck(pickupAddress: String) {
        Assert.assertEquals(pickupAddress, getStringFromTextView(R.id.pickupLabel))
    }

    fun dropOffAddressCheck(dropoffAddress: String) {
        Assert.assertEquals(dropoffAddress, getStringFromTextView(R.id.dropOffLabel))
    }

    fun fleetCancelledAfterDERVisible() {
        dialogTextIsVisible(R.string.kh_uisdk_title_dispatch_cancelled)
        alternativeButtonIsEnabled()
        okCancelledByFleetButtonEnabled()
    }

    fun driverDERDetailsFullCheck() {
        driverPhotoPlaceHolderPresent()
        driverNamePresent()
        carTypePresent()
        licenceNumberPresent()
        registrationPlatePresent()
        cancelButtonIsEnabled()
        contactDriverButtonIsEnabled()
    }

    fun DERFullScreenCheck(pickupText: String, destinationText: String) {
        pickUpAddressField()
        PickUpAddressVisible(pickupText)
        DropOffAddressVisible(destinationText)
        ETAWidgetVisible()
        locateMeButtonVisible()
        mapViewIsVisible()
        // add top notification bar

    }

    fun noDriverDetailsDERCheck() {
        driverPhotoPlaceHolderPresent()
        driverNameNotPresent()
        carTypePresent()
        licenceNumberNotVisible()
        registrationPlatePresent()
        cancelButtonIsEnabled()
        contactDriverButtonIsNotVisible()
        contactFleetButtonIsEnabled()
        cancelButtonIsEnabled()
    }

    fun noVehicleDetailsDERCheck() {
        driverPhotoPlaceHolderPresent()
        driverNamePresent()
        carTypeIsBlank("")
        licenceNumberPresent()
        registrationPlateIsVisible(REG_PLATE)
        cancelButtonIsEnabled()
        contactDriverButtonIsEnabled()
        cancelButtonIsEnabled()
    }

    fun noVehicleAndDriverDetailsDERCheck() {
        driverPhotoPlaceHolderPresent()
        driverNameNotPresent()
        carTypeIsBlank("")
        licenceNumberNotVisible()
        registrationPlateIsVisible(REG_PLATE)
        cancelButtonIsEnabled()
        contactDriverButtonIsNotVisible()
        contactFleetButtonIsEnabled()
        cancelButtonIsEnabled()
    }

    fun noVehicleDetailsNumberPlateAndDriverDetailsDERCheck() {
        driverPhotoPlaceHolderPresent()
        driverNameNotPresent()
        carTypeIsBlank("")
        licenceNumberNotVisible()
        registrationPlateIsVisible("")
        cancelButtonIsEnabled()
        contactDriverButtonIsNotVisible()
        contactFleetButtonIsEnabled()
        cancelButtonIsEnabled()
    }

    fun noNumberPlateDERCheck() {
        driverPhotoPlaceHolderPresent()
        driverNamePresent()
        carTypePresent()
        licenceNumberPresent()
        registrationPlateIsVisible("")
        cancelButtonIsEnabled()
        contactDriverButtonIsEnabled()
        cancelButtonIsEnabled()
    }

    fun ArrivedFullScreenCheck(pickupText: String, destinationText: String) {
        pickUpAddressField()
        PickUpAddressVisible(pickupText)
        DropOffAddressVisible(destinationText)
        locateMeButtonVisible()
        mapViewIsVisible()
        noETABubbleVisible()
        // add top notification bar
    }

    fun driverArrivedDetailsFullCheck() {
        driverPhotoPlaceHolderPresent()
        driverNamePresent()
        carTypePresent()
        licenceNumberPresent()
        registrationPlatePresent()
        cancelButtonIsEnabled()
        contactDriverButtonIsEnabled()
    }

    fun driverDetailsFullCheckPOB() {
        driverPhotoPlaceHolderPresent()
        driverNamePresent()
        carTypePresent()
        licenceNumberPresent()
        registrationPlatePresent()
        noCancelButtonVisible()
        contactDriverButtonIsEnabled()
    }

    fun fullScreenCheckPOB(pickupText: String, destinationText: String) {
        pickUpAddressField()
        PickUpAddressVisible(pickupText)
        DropOffAddressVisible(destinationText)
        locateMeButtonVisible()
        mapViewIsVisible()
        arrivalTimeBubbleVisible()
    }

    fun checkAfterAlternativeSelected(pickupText: String, destinationText: String) {
        pickUpAddressCheck(pickupText)
        dropOffAddressCheck(destinationText)
    }

    fun notificationStringCheck(notification: String) {
        stringIsVisibleIsDescendant(notification, R.id.notificationLabel)
    }

    fun notificationIntCheck(notification: Int) {
        textIsVisibleIsDescendant(notification, R.id.notificationLabel)
    }

}
