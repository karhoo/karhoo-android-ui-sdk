package com.karhoo.uisdk.screen.booking.quotes.fragment

import android.content.res.Resources
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.QuoteStatus

interface QuotesFragmentContract {

    interface View {

        fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?)

        fun destinationChanged(journeyDetails: JourneyDetails)

        fun updateList(quoteList: List<Quote>)

        fun setSortMethod(sortMethod: SortMethod)

        fun prebook(isPrebook: Boolean)

        fun showNoCoverageError(show: Boolean)

        fun showNoFleetsError(show: Boolean)

        fun showSameAddressesError(show: Boolean)

        fun showNoAddressesError(show: Boolean)

        fun showNoResultsAfterFilterError()

        fun showSnackbarError(snackbarConfig: SnackbarConfig)

        fun provideResources(): Resources

        fun setViewDelegate(quoteListDelegate: QuoteListDelegate)

        fun setup(data: QuoteListViewDataModel)

        fun showList(show: Boolean)
    }

    interface Presenter {

        fun vehiclesShown(quoteId: String, isExpanded: Boolean)

        fun sortMethodChanged(sortMethod: SortMethod)

        fun setData(data: QuoteListViewDataModel)

        fun watchQuotes(): Observer<List<Quote>>
    }

    interface QuoteListDelegate {
        fun onQuoteSelected()
        fun onError(snackBar: SnackbarConfig)
    }

    interface Actions : ErrorView

    interface QuoteValidityListener {
        fun isValidUntil(timestamp: Long)
    }

    interface QuotePoolingStatusListener {
        fun changedStatus(status: QuoteStatus?)
    }
}
