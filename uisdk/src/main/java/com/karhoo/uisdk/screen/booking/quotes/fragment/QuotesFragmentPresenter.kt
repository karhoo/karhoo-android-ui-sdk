package com.karhoo.uisdk.screen.booking.quotes.fragment

import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityHandler
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import androidx.lifecycle.Observer

internal class QuotesFragmentPresenter(view: QuotesFragmentContract.View, private val analytics: Analytics?) :
    BasePresenter<QuotesFragmentContract.View>(),
    QuotesFragmentContract.Presenter, AvailabilityHandler {

    private var isPrebook: Boolean = false
    private var hasAddresses: Boolean = false
    private var dataModel: QuoteListViewDataModel? = null

    override var hasAvailability: Boolean = false
        set(value) {
            field = value
            shouldShowQuotesList()
        }
    override var hasNoResults: Boolean = false
        set(value) {
            field = value
            view?.showNoFleetsError(hasNoResults)
        }

    init {
        attachView(view)
    }

    override fun handleSameAddressesError() {
        view?.showSameAddressesError(true)
    }

    override fun handleNoResultsForFiltersError() {
        view?.showNoResultsAfterFilterError()
    }

    override fun handleAvailabilityError(snackbarConfig: SnackbarConfig) {
        view?.showNoCoverageError(true)
    }

    override fun setData(data: QuoteListViewDataModel) {
        this.dataModel = data

        checkBookingInfo()
    }

    override fun sortMethodChanged(sortMethod: SortMethod) {
        view?.setSortMethod(sortMethod)
        dataModel?.quotes?.let { quotes ->
            view?.updateListForSorting(quotes)
        }
    }

    private fun updateList() {
        dataModel?.quotes?.let { quotes ->
            view?.updateList(quotes)
        }
    }

    override fun vehiclesShown(quoteId: String, isExpanded: Boolean) {
        analytics?.fleetsShown(quoteId, if (isExpanded) 4 else 2)
    }

    private fun checkBookingInfo() {
        dataModel?.journeyDetails?.let {
            isPrebook = it.date != null
            hasAddresses = it.destination != null && it.pickup != null

            view?.apply {
                prebook(isPrebook)
                setListVisibility(it.pickup, it.destination)
                destinationChanged(it)
            }

            if (!hasAddresses) {
                shouldShowQuotesList()
            }
            updateList()
        }
    }

    private fun shouldShowQuotesList() {
        when {
            !hasAddresses -> view?.apply {
                showList(false)
                showNoAddressesError(true)
            }
            hasAvailability -> view?.apply {
                if(dataModel?.quotes?.size?.compareTo(0) ?: 0 > 1) {
                    showList(true)
                }
            }
            else -> view?.apply {
                showList(false)
                showNoCoverageError(true)
            }
        }
    }

    override fun watchQuotes() = Observer<List<Quote>> { quotes ->
        quotes?.let {
            dataModel?.quotes = it
            updateList()
        }
    }

}
