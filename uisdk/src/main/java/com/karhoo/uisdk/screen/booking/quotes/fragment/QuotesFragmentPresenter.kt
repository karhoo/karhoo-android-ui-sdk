package com.karhoo.uisdk.screen.booking.quotes.fragment

import android.graphics.Insets
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.view.WindowManager
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityHandler
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import androidx.lifecycle.Observer

internal class QuotesFragmentPresenter(view: QuotesFragmentContract.View, private val analytics: Analytics?) :
    BasePresenter<QuotesFragmentContract.View>(),
    QuotesFragmentContract.Presenter, AvailabilityHandler {

    private var isExpanded: Boolean = false
    private var isPrebook: Boolean = false
    private var hasDestination: Boolean = false
    private var dataModel: QuoteListViewDataModel? = null

    override var hasAvailability: Boolean = false
        set(value) {
            field = value
            shouldShowQuotesList()
        }
    override var hasNoResults: Boolean = false
        set(value) {
            field = value
            view?.showNoResultsText(hasNoResults)
        }

    init {
        attachView(view)
    }

    override fun handleAvailabilityError(snackbarConfig: SnackbarConfig) {
        view?.showSnackbarError(snackbarConfig)
    }

    override fun setData(data: QuoteListViewDataModel) {
        this.dataModel = data

        checkBookingInfo()
    }

    override fun showMore() {
        isExpanded = !isExpanded
        view?.setChevronState(isExpanded)
    }

    override fun sortMethodChanged(sortMethod: SortMethod) {
        view?.setSortMethod(sortMethod)
        updateList()
    }

    private fun updateList() {
        dataModel?.quotes?.let { quotes ->
            if (isPrebook) {
                view?.updateList(quotes)
            } else {
                val nearVehicles = quotes.filter {
                    it.vehicle.vehicleQta.highMinutes <= MAX_ACCEPTABLE_QTA
                }
                view?.updateList(nearVehicles)
            }
        }

    }

    override fun vehiclesShown(quoteId: String, isExpanded: Boolean) {
        analytics?.fleetsShown(quoteId, if (isExpanded) 4 else 2)
    }

    private fun checkBookingInfo() {
        dataModel?.bookingInfo?.let {
            isPrebook = it.date != null
            hasDestination = it.destination != null

            view?.apply {
                prebook(isPrebook)
                setListVisibility(it.pickup, it.destination)
                destinationChanged(it)
            }

            if (!hasDestination) {
                shouldShowQuotesList()
            }
            updateList()
        }
    }

    private fun shouldShowQuotesList() {
        when {
            !hasDestination -> view?.apply {
                if (isExpanded) {
                    showMore()
                }
                view?.showList(false)
            }
            hasAvailability -> view?.apply {
                view?.showList(true)
            }
            else -> view?.apply {
                view?.showList(false)
                showNoAvailability()
            }
        }
    }

    override fun watchQuotes() = Observer<List<Quote>> { quotes ->
        quotes?.let {
            dataModel?.quotes = it
            updateList()
        }
    }

    override fun calculateListHeight(windowManager: WindowManager, percentage: Int): Int {
        val maxHeight = view?.provideResources()?.getInteger(
            R.integer.kh_uisdk_query_list_view_max_screen_percentage
        ) ?: 0

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            ((windowMetrics.bounds.height() - insets.left - insets.right) * (percentage.toFloat() / maxHeight)).toInt()
        } else {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            (displayMetrics.heightPixels * (percentage.toFloat() / maxHeight)).toInt()
        }
    }

    companion object {
        private const val MAX_ACCEPTABLE_QTA = 20
    }

}
