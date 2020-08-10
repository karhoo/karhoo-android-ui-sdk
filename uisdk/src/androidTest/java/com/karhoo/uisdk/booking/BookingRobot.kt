package com.karhoo.uisdk.booking

import android.content.Intent
import com.karhoo.uisdk.BuildConfig
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.ADDRESS_ORIGIN
import org.junit.Assert

fun booking(func: BookingRobot.() -> Unit) = BookingRobot().apply { func() }

fun booking(launch: Launch, intent: Intent? = null, func: BookingRobot.() -> Unit) = BookingRobot().apply {
    launch.launch(intent)
    tapTurnOnGpsBtn()
    func()
}

class BookingRobot : BaseTestRobot() {

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }

    fun pressRidesButton() {
        clickButton(R.id.booking_action_rides)
    }

    fun pressPrebookButton() {
        clickButton(R.id.scheduledIcon)
    }

    fun clickPickUpAddressField() {
        clickButton(R.id.pickupLabel)
    }

    fun clickDestinationAddressField() {
        clickButton(R.id.dropoffLayout)
    }

    fun pressMenuButton() {
        clickButtonByContentDescription(R.string.drawer_open)
    }

    fun longClickCancelAllocationScreen() {
        longClickButton(R.id.cancellationButton)
    }

    fun pressCancelPrebookWindow() {
        clickButtonByText("CANCEL")
    }

    fun pressSwapAddressesButton() {
        clickButton(R.id.flipButtonIcon)
    }

    fun pressExpandListButton() {
        clickButton(R.id.chevronIcon)
    }

    fun pressOKPrebookWindow() {
        clickButtonByString(R.string.ok)
    }

    fun pressOutOfSideMenu() {
        clickButton(R.id.scheduledIcon)
    }

    fun pressFirstQuote() {
        pressItemInList(R.id.supplierListRecycler, 0)
    }

    fun pressBookRideButton() {
        clickButton(R.id.bookingRequestButton)
    }

    fun clickOnProfileButton() {
        clickButtonByString(R.string.profile)
    }

    fun clickOnLocateMeButton() {
        clickButton(R.id.locateMeButton)
    }

    fun pressCloseGuestDetailsPage() {
        clickButton(R.id.cancelButton)
    }

    fun fillCorrectInfoGuestDetails() {
        fillGuestDetailsFirstName()
        fillGuestDetailsLastName()
        fillGuestDetailsEmail()
        fillGuestDetailsPhoneNumber()
        fillGuestDetailsComment()
    }

    fun fillGuestDetailsFirstName() {
        clickButton(R.id.firstNameInput)
        fillText(
                resId = R.id.firstNameInput,
                text = TestData.USER.firstName
                )
    }

    fun fillGuestDetailsLastName() {
        clickButton(R.id.lastNameInput)
        fillText(
                resId = R.id.lastNameInput,
                text = TestData.USER.lastName
                )
    }

    fun fillGuestDetailsEmail() {
        clickButton(R.id.emailInput)
        fillText(
                resId = R.id.emailInput,
                text = TestData.USER.email
                )
    }

    fun fillGuestDetailsPhoneNumber() {
        clickButton(R.id.mobileNumberInput)
        fillText(
                resId = R.id.mobileNumberInput,
                text = TestData.USER_PHONE_NUMBER
                )
    }

    fun fillGuestDetailsComment() {
        scrollUp(R.id.bookingRequestPassengerDetailsWidget)
        clickButton(R.id.bookingCommentsInput)
        fillTextIsDescendant(
                id = R.id.bookingCommentsInput,
                resId = R.id.bookingRequestCommentsWidget,
                text = "Any comment"
                            )
    }

    fun enterCardDetails() {
        clickOnAddCard()
    }

    fun clickOnAddCard() {
        clickButton(R.id.paymentLayout)
    }

    fun pressCloseBookARideScreen() {
        clickButtonIsDescendant(R.id.cancelButton, R.id.bookingRequestLayout)
    }
}

class ResultRobot : BaseTestRobot() {

    fun checkErrorIsShown(expectedText: Int) {
        checkSnackbarWithText(expectedText)
    }

    fun checkBookingScreenIsShown() {
        ridesButtonIsEnabled()
        bothAddressFieldsAreVisible()
        locateMeButtonIsEnabled()
    }

    fun ridesButtonIsEnabled() {
        buttonIsEnabled(R.id.booking_action_rides)
    }

    fun bothAddressFieldsAreVisible() {
        viewIsVisible(R.id.pickupLabel)
        viewIsVisible(R.id.dropOffLabel)
    }

    fun locateMeButtonIsEnabled() {
        buttonIsEnabled(R.id.locateMeButton)
    }

    fun localTimeMessageIsDisplayed() {
        textIsVisibleIsDescendant(R.string.prebook_timezone_title, R.id.action_mode_bar_stub)
    }

    fun checkSideMenuIsShown() {
        sideMenuKarhooLogoIsVisible()
        sideMenuProfileButtonIsVisible(R.string.profile)
        sideMenuRidesButtonIsVisible()
        sideMenuFeedbackButtonIsVisible(R.string.feedback)
        sideMenuHelpButtonIsVisible(R.string.help)
        sideMenuAboutButtonIsVisible(R.string.about)
    }

    fun sideMenuKarhooLogoIsVisible() {
        viewIsVisible(R.id.navigationHeaderIcon)
    }

    fun sideMenuProfileButtonIsVisible(expectedText: Int) {
        textIsVisible(expectedText)
    }

    fun sideMenuButtonIsNotVisiible(expectedText: Int) {
        textIsNotVisible(expectedText)
    }

    fun sideMenuRidesButtonIsVisible() {
        textIsVisibleIsDescendant(R.string.rides, R.id.design_navigation_view)
    }

    fun sideMenuFeedbackButtonIsVisible(expectedText: Int) {
        textIsVisible(expectedText)
    }

    fun sideMenuHelpButtonIsVisible(expectedText: Int) {
        textIsVisible(expectedText)
    }

    fun sideMenuAboutButtonIsVisible(expectedText: Int) {
        textIsVisible(expectedText)
    }

    fun cancellationConfirmation() {
        textIsVisible(R.string.cancel_ride_successful)
    }

    fun prebookWindowNotVisible() {
        viewIsNotVisible(R.id.action_mode_bar_stub)
    }

    fun reverseGeoAddressVisiblePickUp(address: String) {
        Assert.assertEquals(address, getStringFromTextView(R.id.pickupLabel))
    }

    fun reverseGeoAddressVisibleDropOff(address: String) {
        Assert.assertEquals(address, getStringFromTextView(R.id.dropOffLabel))
    }

    fun selectedPickupAddressIsVisible(address: String) {
        Assert.assertEquals(address, getStringFromTextView(R.id.pickupLabel))
    }

    fun selectedDestinationAddressIsVisible(address: String) {
        Assert.assertEquals(address, getStringFromTextView(R.id.dropOffLabel))
    }

    fun pickUpisNowDropoff(dropoffAddress: String) {
        Assert.assertEquals(dropoffAddress, getStringFromTextView(R.id.dropOffLabel))
    }

    fun dropoffIsNowPickUp(pickupAddress: String) {
        Assert.assertEquals(pickupAddress, getStringFromTextView(R.id.pickupLabel))
    }

    fun contactButtonSnackbarIsEnabled() {
        checkSnackbarButtonIsEnabled(R.string.contact)
    }

    fun prebookButtonIsEnabled() {
        buttonIsEnabled(R.id.scheduledIcon)
    }

    fun fullASAPQuotesListCheck() {
        expandChevronIsVisibleAndEnabled()
        tabsAreVisibleAndButtonsEnabled()
        fleetLogoIsVisible()
        supplierNameVisible()
        ETATextVisible()
        categoryTextVisible()
        priceTextVisible()
        fareTypeVisible()
        allCategoriesAreVisible()
    }

    fun expandChevronIsVisibleAndEnabled() {
        viewIsVisible(R.id.chevronIcon)
        buttonIsEnabled(R.id.chevronIcon)
    }

    fun tabsAreVisibleAndButtonsEnabled() {
        viewIsVisible(R.id.etaLabel)
        viewIsVisible(R.id.priceLabel)
        buttonIsEnabled(R.id.etaLabel)
        buttonIsEnabled(R.id.priceLabel)
    }

    fun fleetLogoIsVisible() {
        viewIsVisibleIsDescendant(R.id.logoImage, R.id.supplierListRecycler)
    }

    fun fleetLogoIsVisibleGuestDetails() {
        viewIsVisibleIsDescendant(R.id.logoImage, R.id.bookingRequestSupplierWidget)
    }

    fun supplierNameVisible() {
        viewIsVisibleIsDescendant(R.id.supplierNameText, R.id.supplierListRecycler)
    }

    fun supplierNameVisibleGuestDetails() {
        viewIsVisibleIsDescendant(R.id.supplierNameText, R.id.bookingRequestSupplierWidget)
    }

    fun ETATextVisible() {
        viewIsVisibleIsDescendant(R.id.etaText, R.id.supplierListRecycler)
    }

    fun capacityChecksGuestDetails() {
        viewIsVisibleIsDescendant(R.id.luggageImage, R.id.bookingRequestSupplierWidget)
        viewIsVisibleIsDescendant(R.id.luggageCapacityText, R.id.bookingRequestSupplierWidget)
        viewIsVisibleIsDescendant(R.id.peopleImage, R.id.bookingRequestSupplierWidget)
        viewIsVisibleIsDescendant(R.id.peopleCapacityText, R.id.bookingRequestSupplierWidget)
    }

    fun categoryTextVisible() {
        viewIsVisibleIsDescendant(R.id.categoryText, R.id.supplierListRecycler)
    }

    fun categoryTextVisibleGuestDetails() {
        viewIsVisibleIsDescendant(R.id.categoryText, R.id.bookingRequestSupplierWidget)
    }

    fun priceTextVisible() {
        viewIsVisibleIsDescendant(R.id.priceText, R.id.supplierListRecycler)
    }

    fun fareTypeVisible() {
        viewIsVisibleIsDescendant(R.id.fareTypeText, R.id.supplierListRecycler)
    }

    fun allCategoriesAreVisible() {
        textIsVisibleIsDescendant(R.string.taxi, R.id.categorySelectorWidget)
        textIsVisibleIsDescendant(R.string.mpv, R.id.categorySelectorWidget)
        textIsVisibleIsDescendant(R.string.electric, R.id.categorySelectorWidget)
        textIsVisibleIsDescendant(R.string.moto, R.id.categorySelectorWidget)
        textIsVisibleIsDescendant(R.string.all_category, R.id.categorySelectorWidget)
    }

    fun sideMenuIsNotVisible() {
        viewIsNotVisible(R.id.navigationHeaderIcon)
    }

    fun okButtonIsEnabled() {
        buttonIsEnabled(android.R.id.button1)
    }

    fun closeButtonIsEnabledGuestDetails() {
        buttonIsEnabledIsDescendant(R.id.cancelButton, R.id.bookingRequestWidget)
    }

    fun prebookButtonIsNotVisible() {
        viewIsNotVisible(R.id.scheduledIcon)
    }

    fun guestCheckoutEmptyFullCheck() {
        locateMeButtonIsNotVisible()
        pickupFieldCheck()
        destinationFieldCheck()
        ridesButtonIsNotVisible()
        prebookButtonIsNotVisible()
        menuButtonIsEnabled()
    }

    fun locateMeButtonIsNotVisible() {
        viewIsNotVisible(R.id.locateMeButton)
    }

    fun pickupFieldCheck() {
        viewIsVisible(R.id.pickupDot)
        textIsVisibleIsDescendant(R.string.address_picker_add_pickup, R.id.pickupLabel)
    }

    fun destinationFieldCheck() {
        viewIsVisible(R.id.addDestinationDot)
        textIsVisibleIsDescendant(R.string.address_picker_dropoff_booking, R.id.dropOffLabel)
    }

    fun ridesButtonIsNotVisible() {
        viewDoesNotExist(R.id.booking_action_rides)
    }

    fun menuButtonIsEnabled() {
        buttonByContentDescriptionIsEnabled(R.string.drawer_open)
    }

    fun bothSelectedAddressesAreVisible() {
        selectedDestinationAddressIsVisible(address = TestData.SELECTED_DESTINATION_ADDRESS)
        selectedPickupAddressIsVisible(address = TestData.SELECTED_ADDRESS)
    }

    fun checkSideMenuGuestCheckoutIsShown() {
        sideMenuKarhooLogoIsVisible()
        sideMenuFeedbackButtonIsVisible(R.string.feedback)
        sideMenuHelpButtonIsVisible(R.string.help)
        sideMenuAboutButtonIsVisible(R.string.about)
        sideMenuButtonIsNotVisiible(R.string.profile)
    }

    fun samePickUpAndDestinationErrorIsDisplayed() {
        checkErrorIsShown(R.string.Q0001)
    }

    fun fullASAPQuotesListCheckGuest() {
        fullASAPQuotesListCheck()
        locateMeButtonIsNotVisible()
    }

    fun pickupPinIsVisible() {
        viewIsVisible(R.id.pickupPinIcon)
    }

    fun flowBookingPickupBookingCheck() {
        selectedPickupAddressIsVisible(ADDRESS_ORIGIN)
        //      pickupPinIsVisible()
        prebookButtonIsEnabled()
        destinationFieldCheck()
    }

    fun checkGuestDetailsPageIsShown() {
        viewIsVisible(R.id.bookingRequestPassengerDetailsWidget)
    }

    fun fullCheckEmptyGuestDetailsPage() {
        guestDetailsPageFleetCheck()
        closeButtonIsEnabledGuestDetails()
        guestDetailsPagePriceCheck()
        passengerDetailsCheckGuestDetails()
        paymentEmptyDetailsCheckGuestDetails()
        termsGuestDetailsCheck()
        checkoutAsGuestButtonIsDisabled()
    }

    fun guestBookingCheckCardDetails() {
        paymentCardDetailsCheck()
    }

    fun guestDetailsPageFleetCheck() {
        fleetLogoIsVisibleGuestDetails()
        supplierNameVisibleGuestDetails()
        capacityChecksGuestDetails()
        categoryTextVisibleGuestDetails()
    }

    fun guestDetailsPagePriceCheck() {
        ETATextIsVisibleGuestDetails(ETA = TestData.QUOTE.qta)
        estimatedPriceTextIsVisibleGuestDetails(price = TestData.QUOTE.highPrice)
    }

    fun ETATextIsVisibleGuestDetails(ETA: Int?) {
        viewIsVisibleIsDescendant(R.id.etaTypeText, R.id.bookingRequestPriceWidget)
        textIsVisibleIsDescendant(R.string.estimated_arrival_time, R.id.bookingRequestPriceWidget)
        if (ETA != null) {
            textIsVisibleIsDescendant(ETA, R.id.bookingRequestPriceWidget)
        }
    }

    fun estimatedPriceTextIsVisibleGuestDetails(price: Int) {
        viewIsVisibleIsDescendant(R.id.priceTypeText, R.id.bookingRequestPriceWidget)
        textIsVisibleIsDescendant(R.string.estimated_price, R.id.bookingRequestPriceWidget)
        textIsVisibleIsDescendant(price, R.id.bookingRequestPriceWidget)
    }

    fun passengerDetailsCheckGuestDetails() {
        passengerDetailsTitleIsVisible()
        passengerDetailsFieldsCheck()
    }

    fun passengerDetailsTitleIsVisible() {
        textIsVisibleIsDescendant(R.string.passenger_details, R.id.bookingRequestWidget)
    }

    fun passengerDetailsFieldsCheck() {
        addCardButtonEnabled()
        viewIsVisibleIsDescendant(R.id.bookingRequestPassengerDetailsWidget, R.id.bookingRequestWidget)
        viewIsVisibleIsDescendant(R.id.firstNameInput, R.id.bookingRequestPassengerDetailsWidget)
        viewIsVisibleIsDescendant(R.id.lastNameInput, R.id.bookingRequestPassengerDetailsWidget)
        viewIsVisibleIsDescendant(R.id.emailInput, R.id.bookingRequestPassengerDetailsWidget)
        viewIsVisibleIsDescendant(R.id.countryCodeSpinner, R.id.bookingRequestPassengerDetailsWidget)
        viewIsVisibleIsDescendant(R.id.mobileNumberInput, R.id.bookingRequestPassengerDetailsWidget)
        viewIsVisibleIsDescendant(R.id.bookingCommentsInput, R.id.bookingRequestPassengerDetailsWidget)
    }

    fun addCardButtonEnabled() {
        buttonIsEnabledIsDescendant(R.id.paymentLayout, R.id.bookingRequestWidget)
    }

    fun paymentEmptyDetailsCheckGuestDetails() {
        viewIsVisibleIsDescendant(R.id.bookingRequestPaymentDetailsWidget, R.id.bookingRequestWidget)
        textIsVisibleIsDescendant(R.string.payment_details, R.id.bookingRequestWidget)
        viewIsVisibleIsDescendant(R.id.cardLogoImage, R.id.bookingRequestPaymentDetailsWidget)
        textIsVisibleIsDescendant(R.string.add_payment, R.id.cardNumberText)
    }

    fun paymentCardDetailsCheck() {
        viewIsVisibleIsDescendant(R.id.bookingRequestPaymentDetailsWidget, R.id.bookingRequestWidget)
        textIsVisibleIsDescendant(R.string.payment_details, R.id.bookingRequestWidget)
        viewIsVisibleIsDescendant(R.id.cardLogoImage, R.id.bookingRequestPaymentDetailsWidget)
        textStringIsVisibleIsDescendant(TestData.CARD_ENDING, R.id.cardNumberText)
    }

    fun termsGuestDetailsCheck() {
        viewIsVisibleIsDescendant(R.id.bookingRequestTermsWidget, R.id.bookingRequestWidget)
        textIsVisibleIsDescendant(R.string.booking_terms, R.id.bookingRequestTermsWidget)
        textIsVisibleIsDescendant(R.string.label_terms_and_conditions, R.id.bookingRequestTermsWidget)
        textIsVisibleIsDescendant(R.string.label_cancellation_policy, R.id.bookingRequestTermsWidget)
    }

    fun checkoutAsGuestButtonIsDisabled() {
        bookButtonTextIsCheckoutGuest()
        buttonIsDisabled(R.id.bookingRequestButton)
    }

    fun bookButtonTextIsCheckoutGuest() {
        textIsVisibleIsDescendant(R.string.checkout_as_guest, R.id.bookingRequestButton)
    }

    fun fullCheckFilledGuestDetailsPage() {
        checkoutAsGuestButtonIsEnabled()
    }

    fun checkoutAsGuestButtonIsEnabled() {
        bookButtonTextIsCheckoutGuest()
        buttonIsEnabled(R.id.bookingRequestButton)
    }

    fun bookARideScreenIsVisible() {
        viewIsVisible(R.id.bookingRequestLayout)
    }

    fun bookARideScreenIsNotVisible() {
        viewIsNotVisible(R.id.bookingRequestLayout)
    }

    fun fullCheckBookARideScreenASAP() {
        fleetDetailsAreVisible(fleetName = TestData.QUOTE.supplierName)
        vehicleDetailsAreVisible(vehicle = TestData.QUOTE.vehicleClass)
        ETAIsVisible(ETA = TestData.QUOTE.qta)
        priceDetailsVisible(price = TestData.QUOTE.highPrice)
        paymentFieldIsEnabled()
        termsCheck()
        bookButtonIsEnabled()
    }

    fun fleetDetailsAreVisible(fleetName: String) {
        viewIsVisibleIsDescendant(R.id.logoImage, R.id.bookingRequestLayout)
        viewIsVisibleIsDescendant(R.id.supplierNameText, R.id.bookingRequestLayout)
        textStringIsVisibleIsDescendant(fleetName, R.id.bookingRequestLayout)
    }

    fun vehicleDetailsAreVisible(vehicle: String?) {
        viewIsVisibleIsDescendant(R.id.categoryText, R.id.bookingRequestLayout)
        if (vehicle != null) {
            textStringIsVisibleIsDescendant(vehicle, R.id.categoryText)
        }
        viewIsVisibleIsDescendant(R.id.luggageImage, R.id.bookingRequestLayout)
        viewIsVisibleIsDescendant(R.id.luggageCapacityText, R.id.bookingRequestLayout)
        viewIsVisibleIsDescendant(R.id.peopleImage, R.id.bookingRequestLayout)
        viewIsVisibleIsDescendant(R.id.peopleCapacityText, R.id.bookingRequestLayout)
    }

    fun ETAIsVisible(ETA: Int?) {
        textIsVisibleIsDescendant(R.string.estimated_arrival_time, R.id.etaTypeText)
        viewIsVisibleIsDescendant(R.id.etaText, R.id.bookingRequestLayout)
        if (ETA != null) {
            textIsVisibleIsDescendant(ETA, R.id.etaText)
        }
    }

    fun priceDetailsVisible(price: Int) {
        textIsVisibleIsDescendant(R.string.estimated_price, R.id.priceTypeText)
        viewIsVisibleIsDescendant(R.id.priceText, R.id.bookingRequestLayout)
        if (price != null) {
            textIsVisibleIsDescendant(price, R.id.priceText)
        }
    }

    fun paymentFieldIsEnabled() {
        buttonIsEnabled(R.id.bookingRequestPaymentDetailsWidget)
    }

    fun termsCheck() {
        viewIsVisibleIsDescendant(R.id.bookingRequestTermsWidget, R.id.bookingRequestLayout)
        textIsVisibleIsDescendant(R.string.booking_terms, R.id.bookingRequestTermsWidget)
        textIsVisibleIsDescendant(R.string.label_terms_and_conditions, R.id.bookingRequestTermsWidget)
        textIsVisibleIsDescendant(R.string.label_cancellation_policy, R.id.bookingRequestTermsWidget)
    }

    fun bookButtonIsEnabled() {
        buttonIsEnabled(R.id.bookingRequestButton)
    }

    fun quotesListIsExpanded(fleetName: String) {
        stringIsVisible(fleetName)
    }

    fun quotesListNotExpanded(fleetName: String) {
        stringIsNotDisplayed(fleetName)
    }

    fun prebookLogoNotVisible() {
        viewIsNotVisible(R.id.scheduledIcon)
    }

    fun checkDriverDetails() {
        viewIsVisible(R.id.rideOptionsLabel)
    }

    fun checkWebViewDisplayed() {
        viewIsVisible(R.id.activityWebView)
    }
}
