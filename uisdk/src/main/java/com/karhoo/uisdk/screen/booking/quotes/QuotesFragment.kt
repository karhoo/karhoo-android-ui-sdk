package com.karhoo.uisdk.screen.booking.quotes

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.lifecycle.LifecycleObserver
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.screen.booking.domain.address.BookingInfo
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.booking.quotes.sortview.QuotesSortView
import kotlinx.android.synthetic.main.uisdk_view_quotes.view.collapsiblePanelView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.chevronIcon
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.quotesRecyclerView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.quotesSortWidget
import java.util.Locale

class QuotesFragment @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : CollapsiblePanelView(context, attrs, defStyleAttr), QuotesSortView.Listener,
      QuotesFragmentContract.View, LifecycleObserver {

    private var quoteListViewDelegate: QuotesFragmentContract.QuoteListDelegate? = null
    private var presenter = QuotesFragmentPresenter(this, KarhooUISDK.analytics)
    private var expandedListHeight: Int = 0
    private var collapsedListHeight: Int = 0
    private var availabilityProvider: AvailabilityProvider? = null

    init {
        inflate(context, R.layout.uisdk_view_quotes, this)

        collapsiblePanelView.enable()
        hideListInitially()

        quotesSortWidget.setListener(this)
        chevronIcon.setOnClickListener { presenter.showMore() }

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.QuotesListView,
            0, 0
        ).apply {

            try {
                val expandedListHeightPercentage = getInteger(
                    R.styleable
                        .QuotesListView_expandedListPercentageOfScreen,
                    resources.getInteger(R.integer.kh_uisdk_query_list_view_default_expanded_screen_percentage)
                )
                val collapsedListHeightPercentage = getInteger(
                    R.styleable.QuotesListView_collapsedListPercentageOfScreen,
                    resources.getInteger(R.integer.kh_uisdk_query_list_view_default_collapsed_screen_percentage)
                )

                expandedListHeight = presenter.calculateListHeight((context as Activity).windowManager, expandedListHeightPercentage)
                collapsedListHeight = presenter.calculateListHeight((context as Activity).windowManager, collapsedListHeightPercentage)
            } finally {
                recycle()
            }
        }
    }

    override fun setViewDelegate(quoteListDelegate: QuotesFragmentContract.QuoteListDelegate) {
        this.quoteListViewDelegate = quoteListDelegate
    }

    override fun provideResources(): Resources {
        return resources
    }

    override fun setChevronState(isExpanded: Boolean) {
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isExpanded) 1 else -1)
        chevronIcon.setImageState(stateSet, true)
    }

    override fun togglePanelState() {
        collapsiblePanelView.togglePanelState()
        if (collapsiblePanelView.panelState == PanelState.EXPANDED) {
            layoutParams.height = expandedListHeight
            quoteListViewDelegate?.onListExpanded()
        } else {
            layoutParams.height = collapsedListHeight
            quoteListViewDelegate?.onListCollapsed()
        }
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        quotesRecyclerView.setSortMethod(sortMethod)
    }

    override fun onUserChangedSortMethod(sortMethod: SortMethod) {
        presenter.sortMethodChanged(sortMethod)

    }

    override fun sortChoiceRequiresDestination() {
        quoteListViewDelegate?.onError(SnackbarConfig(text = resources.getString(R.string.kh_uisdk_destination_price_error)))
    }

    override fun setup(data: QuoteListViewDataModel) {
        presenter.setData(data)
    }

    override fun destinationChanged(bookingInfo: BookingInfo) {
        quotesSortWidget.destinationChanged(bookingInfo)
    }

    override fun updateList(quoteList: List<Quote>) {
        quotesRecyclerView.updateList(quoteList)
    }

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
        quotesRecyclerView.setListVisibility(pickup != null && destination != null)
    }

    override fun prebook(isPrebook: Boolean) {
        quotesRecyclerView.prebook(isPrebook)
        quotesSortWidget.visibility = if (isPrebook) GONE else VISIBLE
    }

    private fun hideListInitially() {
        animate().translationY(resources.getDimension(R.dimen.kh_uisdk_quote_list_height)).duration = 0
    }

    override fun showNoAvailability() {
        val activity = context as Activity
        val emailComposer = KarhooFeedbackEmailComposer(context)

        val snackbarConfig = SnackbarConfig(type = SnackbarType.BLOCKING_DISMISSIBLE,
                                            priority = SnackbarPriority.NORMAL,
                                            action = SnackbarAction(resources.getString(R.string.kh_uisdk_contact)) {
                                                val showNoCoverageEmail = emailComposer.showNoCoverageEmail()
                                                showNoCoverageEmail?.let { intent ->
                                                    activity.startActivity(intent)
                                                }
                                            },
                                            text = resources.getString(R.string.kh_uisdk_no_availability))

        quoteListViewDelegate?.onError(snackbarConfig)
    }

    override fun showNoResultsText(show: Boolean) {
        quotesRecyclerView.showNoResultsText(show)
    }


    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
        quoteListViewDelegate?.onError(snackbarConfig)
    }

    override fun showList(show: Boolean) {
        // will be modified later
    }

    override fun initAvailability() {
        availabilityProvider?.cleanup()
        val locale: Locale? = resources.configuration.locale
//        bookingStatusStateViewModel?.let {
//            availabilityProvider = KarhooAvailability(
//                    KarhooApi.quotesService,
//                    categoriesViewModel, liveFleetsViewModel,
//                    it, lifecycleOwner, locale).apply {
//                setAllCategory(resources.getString(R.string.kh_uisdk_all_category))
//                setAvailabilityHandler(presenter)
//                setAnalytics(KarhooUISDK.analytics)
//                categorySelectorWidget.bindAvailability(this)
//            }
//            (availabilityProvider as KarhooAvailability).quoteListValidityListener = object : QuotesFragmentContract
//                                                                      .QuoteValidityListener {
//                override fun isValidUntil(timestamp: Long) {
//                    bookingQuotesViewModel?.process(
//                            BookingQuotesViewContract
//                                    .BookingQuotesEvent
//                                    .QuoteListValidity(timestamp))
//                }
//            }
//        }
    }
}
