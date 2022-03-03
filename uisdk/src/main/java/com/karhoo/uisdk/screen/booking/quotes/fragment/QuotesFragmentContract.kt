package com.karhoo.uisdk.screen.booking.quotes.fragment

import android.content.res.Resources
import android.view.WindowManager
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.base.listener.ErrorView
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import androidx.lifecycle.Observer

interface QuotesFragmentContract {

    interface View {

        fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?)

        fun destinationChanged(bookingInfo: BookingInfo)

        fun updateList(quoteList: List<Quote>)

        fun setSortMethod(sortMethod: SortMethod)

        fun setChevronState(isExpanded: Boolean)

        fun prebook(isPrebook: Boolean)

        fun showNoAvailability()

        fun showNoResultsText(show: Boolean)

        fun showSnackbarError(snackbarConfig: SnackbarConfig)

        fun provideResources(): Resources

        fun setViewDelegate(quoteListDelegate: QuoteListDelegate)

        fun setup(data: QuoteListViewDataModel)

        fun showList(show: Boolean)

        fun initAvailability()
    }

    interface Presenter {

        fun showMore()

        fun vehiclesShown(quoteId: String, isExpanded: Boolean)

        fun sortMethodChanged(sortMethod: SortMethod)

        fun calculateListHeight(windowManager: WindowManager, percentage: Int): Int

        fun setData(data: QuoteListViewDataModel)

        fun watchQuotes(): Observer<List<Quote>>
    }

    interface QuoteListDelegate {
        fun onQuoteSelected()
        // These will disappear once we transition to a full-screen implementation
        fun onListExpanded()
        fun onListCollapsed()
        fun onError(snackBar: SnackbarConfig)
    }

    interface Actions : ErrorView

    interface QuoteValidityListener {
        fun isValidUntil(timestamp: Long)
    }
}
