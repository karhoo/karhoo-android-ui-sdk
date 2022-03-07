package com.karhoo.uisdk.screen.booking.quotes.mocks

import android.content.res.Resources
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.quotes.fragment.QuoteListViewDataModel
import com.karhoo.uisdk.screen.booking.quotes.fragment.QuotesFragmentContract
import com.nhaarman.mockitokotlin2.mock

class QuotesListViewMock: QuotesFragmentContract.View {
    var calledShowNowResults = false
    var resourcesMock: Resources = mock()

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) { /** do nothing **/ }

    override fun destinationChanged(bookingInfo: BookingInfo) { /** do nothing **/ }

    override fun updateList(quoteList: List<Quote>) { /** do nothing **/ }

    override fun setSortMethod(sortMethod: SortMethod) { /** do nothing **/ }

    override fun setChevronState(isExpanded: Boolean) { /** do nothing **/ }

    override fun prebook(isPrebook: Boolean) { /** do nothing **/ }

    override fun showNoAvailability() { /** do nothing **/ }

    override fun showNoResultsText(show: Boolean) {
        calledShowNowResults = show
    }

    override fun initAvailability() {
        /** do nothing **/
    }

    override fun provideResources(): Resources {
        return resourcesMock
    }

    override fun setViewDelegate(quoteListDelegate: QuotesFragmentContract.QuoteListDelegate) {
        /** do nothing **/
    }

    override fun setup(data: QuoteListViewDataModel) {
        /** do nothing **/
    }

    override fun showList(show: Boolean) {
        /** do nothing **/
    }
    override fun showSnackbarError(snackbarConfig: SnackbarConfig) { /** do nothing **/ }
}
