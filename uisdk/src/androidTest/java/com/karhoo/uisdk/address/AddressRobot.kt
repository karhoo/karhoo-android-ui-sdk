package com.karhoo.uisdk.address

import com.google.gson.Gson
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot
import com.karhoo.uisdk.common.Launch
import com.karhoo.uisdk.common.preferences
import com.karhoo.uisdk.util.TestData
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS_EXTRA_RESULT
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS_RESULT

fun address(func: AddressRobot.() -> Unit) = AddressRobot().apply { func() }

fun address(launch: Launch, func: AddressRobot.() -> Unit) = AddressRobot().apply {
    launch.launch()
    func()
}

class AddressRobot : BaseTestRobot() {

    fun search(searchTerm: String) {
        fillEditText(
                resId = R.id.searchInput,
                text = searchTerm)
    }

    fun pressBackButtonToolbar() {
        clickButtonByContentDescription(R.string.abc_action_bar_up_description)
    }

    fun clearAddressSearchList() {
        clickButton(R.id.clearSearchButtonIcon)
    }

    fun setDefaultRecents() {
        preferences {
            setStringPreference("SharedPreferencesLocationStore.LocationsKey", Gson().toJson(listOf(TestData.LOCATION_INFO)))
        }
    }

    fun setAirportRecent() {
        preferences {
            setStringPreference("SharedPreferencesLocationStore.LocationsKey", Gson().toJson(listOf(TestData.LOCATION_INFO_AIRPORT)))
        }
    }

    fun clickSetPinOnMapButton() {
        clickButton(R.id.setOnMap)
    }

    fun clickBakerStreetResult() {
        clickButtonByText(SEARCH_ADDRESS_RESULT)
    }

    fun clickOxfordStreetResult() {
        clickButtonByText(SEARCH_ADDRESS_EXTRA_RESULT)
    }

    fun clickGetCurrentLocation() {
        clickButton(R.id.currentLocation)
    }

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }
}

class ResultRobot : BaseTestRobot() {

    fun areAddressesAvailable() {
        checkListSizeFromParent(R.id.addressResultListWidget, R.id.recycler, 5)
    }

    fun areRecentAddressesAvailable() {
        checkListSizeFromParent(R.id.recentsListWidget, R.id.recycler, 1)
        preferences {
            clear()
        }
    }

    fun noAddressesFound() {
        checkListSizeFromParent(R.id.addressResultListWidget, R.id.recycler, 0)
    }

    fun poweredByGoogleFound() {
        viewIsVisible(R.id.poweredByGoogleIcon)
    }

    fun noRecentFound() {
        checkTextViewIfMultiMatches(R.id.emptyText, R.string.recents_empty)
    }

    fun checkErrorIsShown(expectedText: Int) {
        checkSnackbarWithText(expectedText)
    }

    fun checkAddressPickUpPageIsShown() {
        hintIsVisible(R.id.searchInput, R.string.enter_pickup)
    }

    fun checkAddressDestinationPageIsShown() {
        hintIsVisible(R.id.searchInput, R.string.enter_destination)
    }

    fun enterPickupHintIsVisible() {
        hintIsVisible(R.id.searchInput, R.string.enter_pickup)
    }

    fun enterDestinationHintIsVisible() {
        hintIsVisible(R.id.searchInput, R.string.enter_destination)
    }

    fun clearAddressButtonIsNotClickable() {
        buttonIsNotClickable(R.id.clearSearchButtonIcon)
    }

    fun checkSetPinOnMapIsVisible() {
        viewIsVisible(R.id.setOnMap)
    }

    fun checkGetLocationIsVisible() {
        viewIsVisible(R.id.currentLocation)
    }

    fun setLocationOnMapButtonIsEnabled() {
        buttonIsEnabled(R.id.setOnMap)
    }

    fun getCurrentLocationIsEnabled() {
        buttonIsEnabled(R.id.currentLocation)
    }

    fun fullCheckSetAddressOnMapPickup() {
        addressSelectButtonIsEnabled()
        enterPickupHintIsVisible()
        pickUpPinIsVisible()
        correctAddressIsDisplayedInField(address = TestData.REVERSE_GEO_DISPLAY_ADDRESS)
    }

    fun fullCheckSetAddressOnMapDestination() {
        addressSelectButtonIsEnabled()
        enterDestinationHintIsVisible()
        dropOffPinIsVisible()
        correctAddressIsDisplayedInField(address = TestData.REVERSE_GEO_DISPLAY_ADDRESS)
    }

    fun addressSelectButtonIsEnabled() {
        buttonIsEnabled(R.id.addressSelectButton)
    }

    fun pickUpPinIsVisible() {
        drawableIsVisible(R.id.pickupPinIcon, R.drawable.uisdk_ic_pickup_pin)
    }

    fun dropOffPinIsVisible() {
        drawableIsVisible(R.id.pickupPinIcon, R.drawable.uisdk_ic_dropoff_pin)
    }

    fun correctAddressIsDisplayedInField(address: String) {
        stringIsVisibleIsDescendant(address, R.id.addressResultTextView)
    }

    fun airportSymbolIsVisible() {
        drawableIsVisible(R.id.itemIcon, R.drawable.uisdk_ic_airport)
    }

    fun checkAddressScreenFromPickupGuestCheckout() {
        checkAddressPickUpPageIsShown()
        viewIsNotVisible(R.id.setOnMap)
        viewIsNotVisible(R.id.currentLocation)
        viewIsVisible(R.id.poweredByGoogleIcon)
    }

    fun checkAddressScreenFromDestinationGuestCheckout() {
        checkAddressDestinationPageIsShown()
        viewIsNotVisible(R.id.setOnMap)
        viewIsNotVisible(R.id.currentLocation)
        viewIsVisible(R.id.poweredByGoogleIcon)
    }

}
