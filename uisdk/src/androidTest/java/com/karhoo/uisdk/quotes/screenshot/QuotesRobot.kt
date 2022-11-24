package com.karhoo.uisdk.quotes.screenshot

import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.BaseTestRobot
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS_EXTRA_RESULT
import com.karhoo.uisdk.util.TestData.Companion.SEARCH_ADDRESS_RESULT

fun quotes(func: QuotesRobot.() -> Unit) = QuotesRobot().apply { func() }

class QuotesRobot : BaseTestRobot() {

    fun search(searchTerm: String) {
        fillEditText(
                resId = R.id.searchInput,
                text = searchTerm)
    }

    fun checkPriceTextVisible() {
        viewIsVisible(R.id.quotesSortByPrice)
    }

    fun checkDriverArrivalTextVisible() {
        viewIsVisible(R.id.quotesSortByDriverArrival)
    }

    fun checkPriceCheckboxIsSelected() {
        checkboxIsChecked(R.id.quotesSortByPrice)
    }

    fun checkDrivelArrivalCheckboxIsSelected() {
        checkboxIsChecked(R.id.quotesSortByDriverArrival)
    }

    fun clickOnDriverArrivalCheckbox() {
        clickButton(R.id.quotesSortByDriverArrival)
    }

    fun checkFixedPriceTextVisible() {
        textIsVisible(R.string.kh_uisdk_fixed_fare)
    }

    fun checkEstimatedPriceTextVisible() {
        textIsVisible(R.string.kh_uisdk_estimated_fare)
    }

    fun checkFreeWaitingTimeTextVisible() {
        textIsVisible(R.string.kh_uisdk_filter_free_waiting_time)
    }

    fun checkFreeCancellationTextVisible() {
        textIsVisible(R.string.kh_uisdk_filter_free_cancellation)
    }

    fun clickBakerStreetResult() {
        clickButtonByText(SEARCH_ADDRESS_RESULT)
    }

    fun clickOxfordStreetResult() {
        clickButtonByText(SEARCH_ADDRESS_EXTRA_RESULT)
    }

    fun clickXButton() {
        clickButton(R.id.masterBottomSheetCloseDialog)
    }

    fun clickSortBySaveButton(){
        clickButton(R.id.quotesSortBySave)
    }

    fun checkBottomSheetSortByTitle() {
        viewIsVisible(R.id.masterBottomSheetTitle)
    }

    fun checkBottomSheetSortByTitleIsNotVisible() {
        viewDoesNotExist(R.id.masterBottomSheetTitle)
    }

    fun checkBottomSheetFilterTitle() {
        viewIsVisible(R.id.masterBottomSheetTitle)
    }

    fun checkBottomSheetFilterTitleIsNotVisible() {
        viewDoesNotExist(R.id.masterBottomSheetTitle)
    }

    fun checkFilterButtonIsVisible() {
        viewIsVisible(R.id.quotesFilterByButton)
    }

    fun clickFilterButton() {
        clickButton(R.id.quotesFilterByButton)
    }

    fun clickSortByButton() {
        clickButton(R.id.quotesSortByButton)
    }

    fun clickPickUpAddressField() {
        clickButton(R.id.pickupLabel)
    }

    fun clickDestinationAddressField() {
        clickButton(R.id.dropOffLabel)
    }

    fun scrollUpFilterScreen() {
        scrollUp(R.id.filterScrollView)
    }

    fun scrollDownFilterScreen() {
        scrollDown(R.id.filterScrollView)
    }

    fun checkFirstItemOfQuoteListSortedByDriverArrival() {
        checkItemInList(R.id.quotesRecyclerView, 0, "A Taxi Fleet")
    }

    fun taxesAndFeesVisible() {
        viewIsVisible(R.id.quotesTaxesAndFeesLabel)
    }

    fun quotesCount() {
        checkListSize(R.id.quotesListRecycler, 6)
    }

    fun quoteTitleIsCorrect(title: String, position: Int) {
        checkItemInList(R.id.quotesRecyclerView, position, title)
    }

    fun quoteTitleIsCorrect(title: String) {
        checkItemInList(R.id.quotesRecyclerView, title)
    }

    fun scrollDownQuotesList() {
        scrollUp(R.id.quotesRecyclerView)
    }

    fun quotePriceIsCorrect(price: String, position: Int) {
        checkItemInList(R.id.quotesRecyclerView, position, price)
    }

    fun quoteETAIsCorrect(eta: String, position: Int) {
        checkItemInList(R.id.quotesRecyclerView, position, eta)
    }

    fun luggagesVisible() {
        viewIsVisibleInDescendant(R.id.quotesRecyclerView, R.id.luggageCapacityText)
    }

    fun passengersVisible() {
        viewIsVisibleInDescendant(R.id.quotesRecyclerView, R.id.peopleCapacityText)
    }

    fun pressFlightTrackingFilter() {
        clickItemWithText(R.string.kh_uisdk_flight_tracking)
    }

    fun pressVehicleDetails() {
        clickItemWithText(R.string.kh_uisdk_filter_vehicle_details)
    }

    fun pressFilterByButton() {
        clickButton(R.id.quotesFilterSave)
    }

    fun quotesCountFlightTracking() {
        checkListSize(R.id.quotesListRecycler, 1)
    }

    fun noResultsAfterFilteringVisible() {
        viewIsVisibleWithText(R.string.kh_uisdk_quotes_error_no_results_after_filter_title)
    }

    fun clearDestinationAddress() {
        clickButton(R.id.clearDestinationButtonIcon)
    }

    fun noResultsAfterFilteringLabelIsVisible() {
        viewIsVisibleWithText(R.string.kh_uisdk_quotes_error_missing_addresses_title)
    }
}
