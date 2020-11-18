package com.karhoo.uisdk.screen.booking.quotes

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.AttrRes
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
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
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesViewContract
import com.karhoo.uisdk.screen.booking.booking.quotes.BookingQuotesViewModel
import com.karhoo.uisdk.screen.booking.booking.quotes.QuoteListStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.quotes.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.domain.quotes.SortMethod
import com.karhoo.uisdk.screen.booking.domain.support.ContactSupplier
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import kotlinx.android.synthetic.main.uisdk_view_quotes.view.collapsiblePanelView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.categorySelectorWidget
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.chevronIcon
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.quotesRecyclerView
import kotlinx.android.synthetic.main.uisdk_view_quotes_list.view.quotesSortWidget

class QuotesListView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : CollapsiblePanelView(context, attrs, defStyleAttr), QuotesSortView.Listener,
      QuotesListMVP.View, BookingQuotesViewContract.BookingQuotesWidget {

    private val categoriesViewModel: CategoriesViewModel = CategoriesViewModel()
    private val liveFleetsViewModel: LiveFleetsViewModel = LiveFleetsViewModel()
    private var bookingQuotesViewModel: BookingQuotesViewModel? = null
    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null
    private var availabilityProvider: AvailabilityProvider? = null

    private var presenter = QuotesListPresenter(this, KarhooUISDK.analytics)

    private var isQuotesListVisible = false
        private set

    init {
        inflate(context, R.layout.uisdk_view_quotes, this)

        collapsiblePanelView.enable()
        hideListInitially()

        quotesSortWidget.setListener(this)
        chevronIcon.setOnClickListener { presenter.showMore() }
    }

    override fun setChevronState(isExpanded: Boolean) {
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isExpanded) 1 else -1)
        chevronIcon.setImageState(stateSet, true)
    }

    override fun togglePanelState() {
        collapsiblePanelView.togglePanelState()
        if (collapsiblePanelView.panelState == CollapsiblePanelView.PanelState.EXPANDED) {
            bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent.QuotesListExpanded)
        } else {
            bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                      .QuotesListCollapsed)
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
                                                                             .destination_price_error))))
    }

    override fun bindViewToData(lifecycleOwner: LifecycleOwner,
                                bookingStatusStateViewModel: BookingStatusStateViewModel,
                                bookingQuotesViewModel: BookingQuotesViewModel) {
        liveFleetsViewModel.liveFleets.observe(lifecycleOwner, presenter.watchVehicles())
        this.bookingStatusStateViewModel = bookingStatusStateViewModel
        bookingStatusStateViewModel.viewStates().observe(lifecycleOwner, presenter.watchBookingStatus())
        categorySelectorWidget.bindViewToData(lifecycleOwner, categoriesViewModel, bookingStatusStateViewModel)
        quotesRecyclerView.watchCategories(lifecycleOwner, categoriesViewModel)
        quotesRecyclerView.watchQuoteListStatus(lifecycleOwner, bookingQuotesViewModel)

        this.bookingQuotesViewModel = bookingQuotesViewModel
        bookingQuotesViewModel.viewStates().observe(lifecycleOwner, watchBookingQuotesStatus())
    }

    override fun cleanup() {
        availabilityProvider?.cleanup()
    }

    private fun watchBookingQuotesStatus(): Observer<in QuoteListStatus> {
        return Observer { quoteListStatus ->
            quoteListStatus?.let {
                it.selectedQuote
            }
        }
    }

    override fun destinationChanged(bookingStatus: BookingStatus) {
        quotesSortWidget.destinationChanged(bookingStatus)
    }

    override fun updateList(quoteList: List<Quote>) {
        quotesRecyclerView.updateList(quoteList)
    }

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
        quotesRecyclerView.setListVisibility(pickup != null && destination != null)
    }

    override fun prebook(isPrebook: Boolean) {
        quotesRecyclerView.prebook(isPrebook)
        quotesSortWidget.prebookChanged(isPrebook)
    }

    override fun showList() {
        if (!isQuotesListVisible) {
            animate()
                    .translationY(0F)
                    .setDuration(resources.getInteger(R.integer
                                                              .animation_duration_slide_out_or_in_quotes).toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withStartAction {
                        isQuotesListVisible = true
                        bookingQuotesViewModel?.process(
                                BookingQuotesViewContract.BookingQuotesEvent
                                        .QuotesListVisibilityChanged(isVisible = true, panelState = collapsiblePanelView.panelState))
                    }
        }
    }

    override fun hideList() {
        if (isQuotesListVisible) {

            val translation = when (collapsiblePanelView.panelState) {
                PanelState.COLLAPSED -> resources.getDimension(R.dimen.quote_list_height)
                PanelState.EXPANDED -> resources.getDimension(R.dimen.collapsible_pane_expanded_height)
            }

            animate()
                    .translationY(translation)
                    .setDuration(resources.getInteger(R.integer.animation_duration_slide_out_or_in_quotes).toLong())
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

        }
    }

    private fun hideListInitially() {
        animate().translationY(resources.getDimension(R.dimen.quote_list_height)).duration = 0
    }

    override fun showNoAvailability() {
        val supplierFeedback = ContactSupplier(context as Activity, KarhooPreferenceStore.getInstance(context.applicationContext))

        val snackbarConfig = SnackbarConfig(type = SnackbarType.BLOCKING_DISMISSIBLE,
                                            priority = SnackbarPriority.NORMAL,
                                            action = SnackbarAction(resources.getString(R.string.contact)) { (context as Activity).startActivity(supplierFeedback.createEmail()) },
                                            text = resources.getString(R.string.no_availability))
        bookingQuotesViewModel?.process(BookingQuotesViewContract.BookingQuotesEvent
                                                  .Error(snackbarConfig))

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
        bookingStatusStateViewModel?.let {
            availabilityProvider = KarhooAvailability(KarhooApi.quotesService,
                                                      KarhooUISDK.analytics, categoriesViewModel, liveFleetsViewModel,
                                                      it, lifecycleOwner).apply {
                setAllCategory(resources.getString(R.string.all_category))
                setAvailabilityHandler(presenter)
                categorySelectorWidget.bindAvailability(this)
            }
        }
    }
}
