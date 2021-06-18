package com.karhoo.uisdk.screen.booking.quotes.mocks

import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.QuotesListMVP

class QuotesListViewMock: QuotesListMVP.View {
    var calledShowNowResults = false

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
    }

    override fun destinationChanged(bookingStatus: BookingStatus) {
    }

    override fun updateList(quoteList: List<Quote>) {
    }

    override fun setSortMethod(sortMethod: SortMethod) {
    }

    override fun togglePanelState() {
    }

    override fun setChevronState(isExpanded: Boolean) {
    }

    override fun prebook(isPrebook: Boolean) {
    }

    override fun showList() {
    }

    override fun hideList(): Boolean {
        return true
    }

    override fun showNoAvailability() {
    }

    override fun showNoResultsText(show: Boolean) {
        calledShowNowResults = show
    }

    override fun hideNoAvailability() {
    }

    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
    }
}