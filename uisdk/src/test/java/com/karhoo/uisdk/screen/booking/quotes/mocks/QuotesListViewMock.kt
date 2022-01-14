package com.karhoo.uisdk.screen.booking.quotes.mocks

import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.QuotesListMVP

class QuotesListViewMock: QuotesListMVP.View {
    var calledShowNowResults = false

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) { /** do nothing **/ }

    override fun destinationChanged(bookingInfo: BookingInfo) { /** do nothing **/ }

    override fun updateList(quoteList: List<Quote>) { /** do nothing **/ }

    override fun setSortMethod(sortMethod: SortMethod) { /** do nothing **/ }

    override fun togglePanelState() { /** do nothing **/ }

    override fun setChevronState(isExpanded: Boolean) { /** do nothing **/ }

    override fun prebook(isPrebook: Boolean) { /** do nothing **/ }

    override fun showList() { /** do nothing **/ }

    override fun hideList(): Boolean {
        return true
    }

    override fun showNoAvailability() { /** do nothing **/ }

    override fun showNoResultsText(show: Boolean) {
        calledShowNowResults = show
    }

    override fun hideNoAvailability() { /** do nothing **/ }

    override fun showSnackbarError(snackbarConfig: SnackbarConfig) { /** do nothing **/ }
}
