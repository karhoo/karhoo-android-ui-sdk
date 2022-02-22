package com.karhoo.uisdk.screen.booking.quotes

import android.app.Activity
import android.content.Context
import android.graphics.Insets
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.AttrRes
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.snackbar.SnackbarAction
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.snackbar.SnackbarPriority
import com.karhoo.uisdk.base.snackbar.SnackbarType
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewContract
import com.karhoo.uisdk.screen.booking.checkout.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.checkout.quotes.QuoteListStatus
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.quotes.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import kotlinx.android.synthetic.main.uisdk_view_quotes.view.collapsiblePanelView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.categorySelectorWidget
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.chevronIcon
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.quotesRecyclerView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.quotesSortWidget
import android.util.DisplayMetrics
import android.view.WindowInsets
import android.os.Build
import java.util.Locale

class QuotesListView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : CollapsiblePanelView(context, attrs, defStyleAttr), QuotesSortView.Listener,
      QuotesListMVP.View, BookingQuotesViewContract.BookingQuotesWidget, LifecycleObserver {

    private val categoriesViewModel: CategoriesViewModel = CategoriesViewModel()
    private val liveFleetsViewModel: LiveFleetsViewModel = LiveFleetsViewModel()
    private var bookingQuotesViewModel: BookingQuotesViewModel? = null
    private var journeyDetailsStateViewModel: JourneyDetailsStateViewModel? = null
    private var availabilityProvider: AvailabilityProvider? = null

    private var presenter = QuotesListPresenter(this, KarhooUISDK.analytics)

    private var isQuotesListVisible = false
    private var expandedListHeightPercentage = resources.getInteger(R.integer
                                                                            .kh_uisdk_query_list_view_default_expanded_screen_percentage)
    private var collapsedListHeightPercentage = resources.getInteger(R.integer
                                                                             .kh_uisdk_query_list_view_default_collapsed_screen_percentage)

    init {
        inflate(context, R.layout.uisdk_view_quotes, this)

        collapsiblePanelView.enable()
        hideListInitially()

        quotesSortWidget.setListener(this)
        chevronIcon.setOnClickListener { presenter.showMore() }

        context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.QuotesListView,
                0, 0).apply {

            try {
                expandedListHeightPercentage = getInteger(R.styleable
                                                                  .QuotesListView_expandedListPercentageOfScreen, resources.getInteger(R.integer.kh_uisdk_query_list_view_default_expanded_screen_percentage))
                collapsedListHeightPercentage = getInteger(R.styleable
                                                                   .QuotesListView_collapsedListPercentageOfScreen,
                                                           resources.getInteger(R.integer.kh_uisdk_query_list_view_default_collapsed_screen_percentage))
            } finally {
                recycle()
            }
        }
    }

    fun getScreenHeight(activity: Activity, percentage: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = activity.windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            ((windowMetrics.bounds.height() - insets.left - insets.right) * (percentage.toFloat() / resources.getInteger(R.integer.kh_uisdk_query_list_view_max_screen_percentage))).toInt()
        } else {
            val displayMetrics = DisplayMetrics()
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
            (displayMetrics.heightPixels * (percentage.toFloat() / resources.getInteger(R.integer.kh_uisdk_query_list_view_max_screen_percentage))).toInt()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        availabilityProvider?.cleanup()
    }

    override fun setChevronState(isExpanded: Boolean) {
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isExpanded) 1 else -1)
        chevronIcon.setImageState(stateSet, true)
    }

    override fun togglePanelState() {
        collapsiblePanelView.togglePanelState()
        if (collapsiblePanelView.panelState == PanelState.EXPANDED) {
            val desiredHeight = getScreenHeight(context as Activity, expandedListHeightPercentage)
            layoutParams.height = desiredHeight
            bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent.QuotesListExpanded)
        } else {
            bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                    .QuotesListCollapsed)
            val desiredHeight = getScreenHeight(context as Activity, collapsedListHeightPercentage)
            layoutParams.height = desiredHeight
        }
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        quotesRecyclerView.setSortMethod(sortMethod)
    }

    override fun onUserChangedSortMethod(sortMethod: SortMethod) {
        presenter.sortMethodChanged(sortMethod)

    }

    override fun sortChoiceRequiresDestination() {
        bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                .Error(SnackbarConfig(text = resources
                                                        .getString(R.string
                                                                           .kh_uisdk_destination_price_error))))
    }

    override fun bindViewToData(lifecycleOwner: LifecycleOwner,
                                journeyDetailsStateViewModel: JourneyDetailsStateViewModel,
                                bookingQuotesViewModel: BookingQuotesViewModel) {
        lifecycleOwner.lifecycle.addObserver(this)
        liveFleetsViewModel.liveFleets.observe(lifecycleOwner, presenter.watchVehicles())
        this.journeyDetailsStateViewModel = journeyDetailsStateViewModel
        journeyDetailsStateViewModel.viewStates().observe(lifecycleOwner, presenter.watchBookingStatus())
        categorySelectorWidget.bindViewToData(lifecycleOwner, categoriesViewModel, journeyDetailsStateViewModel)
        quotesRecyclerView.watchCategories(lifecycleOwner, categoriesViewModel)
        quotesRecyclerView.watchQuoteListStatus(lifecycleOwner, bookingQuotesViewModel)

        this.bookingQuotesViewModel = bookingQuotesViewModel
        bookingQuotesViewModel.viewStates().observe(lifecycleOwner, watchBookingQuotesStatus())
    }

    private fun watchBookingQuotesStatus(): Observer<in QuoteListStatus> {
        return Observer { quoteListStatus ->
            quoteListStatus?.selectedQuote
        }
    }

    override fun destinationChanged(journeyDetails: JourneyDetails) {
        quotesSortWidget.destinationChanged(journeyDetails)
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

    override fun showList() {
        if (!isQuotesListVisible) {
            val desiredHeight = getScreenHeight(context as Activity, collapsedListHeightPercentage)
            layoutParams.height = desiredHeight
            animate()
                    .translationY(0F)
                    .setDuration(resources.getInteger(R.integer
                                                              .kh_uisdk_animation_duration_slide_out_or_in_quotes).toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withStartAction {
                        isQuotesListVisible = true
                        bookingQuotesViewModel?.process(
                                BookingQuotesViewContract.BookingQuotesEvent
                                        .QuotesListVisibilityChanged(isVisible = true, panelState = collapsiblePanelView.panelState))
                        KarhooUISDK.analytics?.quoteListOpened(journeyDetails = journeyDetailsStateViewModel?.currentState)
                    }
        }
    }

    override fun hideList(): Boolean {
        return if (isQuotesListVisible) {

            val translation = when (collapsiblePanelView.panelState) {
                PanelState.COLLAPSED -> resources.getDimension(R.dimen.kh_uisdk_quote_list_height)
                PanelState.EXPANDED -> resources.getDimension(R.dimen.kh_uisdk_collapsible_pane_expanded_height)
            }

            animate()
                    .translationY(translation)
                    .setDuration(resources.getInteger(R.integer.kh_uisdk_animation_duration_slide_out_or_in_quotes).toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withStartAction {
                        bookingQuotesViewModel?.process(
                                BookingQuotesViewContract.BookingQuotesEvent
                                        .QuotesListVisibilityChanged(isVisible = false, panelState =
                                        collapsiblePanelView.panelState))
                    }
                    .withEndAction {
                        isQuotesListVisible = false
                    }
            true
        } else {
            false
        }
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
        bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                .Error(snackbarConfig))

    }

    override fun showNoResultsText(show: Boolean) {
        quotesRecyclerView.showNoResultsText(show)
    }

    override fun hideNoAvailability() {
        bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                .QuotesListVisibilityChanged(false, panelState = collapsiblePanelView.panelState))
    }

    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
        bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                .Error(snackbarConfig))
    }

    override fun setQuotesListVisibility() {
        bookingQuotesViewModel?.process(
                BookingQuotesViewContract.BookingQuotesEvent
                        .QuotesListVisibilityChanged(isVisible = isVisible, panelState = collapsiblePanelView.panelState))
    }

    override fun initAvailability(lifecycleOwner: LifecycleOwner) {
        availabilityProvider?.cleanup()
        val locale: Locale? = resources.configuration.locale
        journeyDetailsStateViewModel?.let {
            availabilityProvider = KarhooAvailability(
                    KarhooApi.quotesService,
                    categoriesViewModel, liveFleetsViewModel,
                    it, lifecycleOwner, locale).apply {
                setAllCategory(resources.getString(R.string.kh_uisdk_all_category))
                setAvailabilityHandler(presenter)
                setAnalytics(KarhooUISDK.analytics)
                categorySelectorWidget.bindAvailability(this)
            }
            (availabilityProvider as KarhooAvailability).quoteListValidityListener = object : QuotesListMVP
                                                                      .QuoteValidityListener {
                override fun isValidUntil(timestamp: Long) {
                    bookingQuotesViewModel?.process(
                            BookingQuotesViewContract
                                    .BookingQuotesEvent
                                    .QuoteListValidity(timestamp))
                }
            }
        }
    }
}
