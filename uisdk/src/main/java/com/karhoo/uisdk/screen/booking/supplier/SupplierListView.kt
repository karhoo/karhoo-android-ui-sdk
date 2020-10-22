package com.karhoo.uisdk.screen.booking.supplier

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
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewContract
import com.karhoo.uisdk.screen.booking.booking.supplier.BookingSupplierViewModel
import com.karhoo.uisdk.screen.booking.booking.supplier.QuoteListStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.supplier.AvailabilityProvider
import com.karhoo.uisdk.screen.booking.domain.supplier.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.supplier.LiveFleetsViewModel
import com.karhoo.uisdk.screen.booking.domain.supplier.SortMethod
import com.karhoo.uisdk.screen.booking.domain.support.ContactSupplier
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import kotlinx.android.synthetic.main.uisdk_view_supplier.view.collapsiblePanelView
import kotlinx.android.synthetic.main.uisdk_view_supplier_list.view.categorySelectorWidget
import kotlinx.android.synthetic.main.uisdk_view_supplier_list.view.chevronIcon
import kotlinx.android.synthetic.main.uisdk_view_supplier_list.view.supplierRecyclerView
import kotlinx.android.synthetic.main.uisdk_view_supplier_list.view.supplierSortWidget

class SupplierListView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : CollapsiblePanelView(context, attrs, defStyleAttr), SupplierSortView.Listener,
      SupplierListMVP.View, BookingSupplierViewContract.BookingSupplierWidget {

    private val categoriesViewModel: CategoriesViewModel = CategoriesViewModel()
    private val liveFleetsViewModel: LiveFleetsViewModel = LiveFleetsViewModel()
    private var bookingSupplierViewModel: BookingSupplierViewModel? = null
    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null
    private var availabilityProvider: AvailabilityProvider? = null

    private var presenter = SupplierListPresenter(this, KarhooUISDK.analytics)

    private var isSupplierListVisible = false
        private set

    init {
        inflate(context, R.layout.uisdk_view_supplier, this)

        collapsiblePanelView.enable()
        hideListInitially()

        supplierSortWidget.setListener(this)
        chevronIcon.setOnClickListener { presenter.showMore() }
    }

    override fun setChevronState(isExpanded: Boolean) {
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isExpanded) 1 else -1)
        chevronIcon.setImageState(stateSet, true)
    }

    override fun togglePanelState() {
        collapsiblePanelView.togglePanelState()
        if (collapsiblePanelView.panelState == CollapsiblePanelView.PanelState.EXPANDED) {
            bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent.SupplierListExpanded)
        } else {
            bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent
                                                      .SupplierListCollapsed)
        }
    }

    override fun setSortMethod(sortMethod: SortMethod) {
        supplierRecyclerView.setSortMethod(sortMethod)
    }

    override fun onUserChangedSortMethod(sortMethod: SortMethod) {
        presenter.sortMethodChanged(sortMethod)

    }

    override fun sortChoiceRequiresDestination() {
        bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent
                                                  .Error(SnackbarConfig(text = resources
                                                          .getString(R.string
                                                                             .destination_price_error))))
    }

    override fun bindViewToData(lifecycleOwner: LifecycleOwner,
                                bookingStatusStateViewModel: BookingStatusStateViewModel,
                                bookingSupplierViewModel: BookingSupplierViewModel) {
        liveFleetsViewModel.liveFleets.observe(lifecycleOwner, presenter.watchVehicles())
        this.bookingStatusStateViewModel = bookingStatusStateViewModel
        bookingStatusStateViewModel.viewStates().observe(lifecycleOwner, presenter.watchBookingStatus())
        categorySelectorWidget.bindViewToData(lifecycleOwner, categoriesViewModel, bookingStatusStateViewModel)
        supplierRecyclerView.watchCategories(lifecycleOwner, categoriesViewModel)
        supplierRecyclerView.watchQuoteListStatus(lifecycleOwner, bookingSupplierViewModel)

        this.bookingSupplierViewModel = bookingSupplierViewModel
        bookingSupplierViewModel.viewStates().observe(lifecycleOwner, watchBookingSupplierStatus())
    }

    override fun cleanup() {
        availabilityProvider?.cleanup()
    }

    private fun watchBookingSupplierStatus(): Observer<in QuoteListStatus> {
        return Observer { quoteListStatus ->
            quoteListStatus?.let {
                it.selectedQuote
            }
        }
    }

    override fun destinationChanged(bookingStatus: BookingStatus) {
        supplierSortWidget.destinationChanged(bookingStatus)
    }

    override fun updateList(quoteList: List<Quote>) {
        supplierRecyclerView.updateList(quoteList)
    }

    override fun setListVisibility(pickup: LocationInfo?, destination: LocationInfo?) {
        supplierRecyclerView.setListVisibility(pickup != null && destination != null)
    }

    override fun prebook(isPrebook: Boolean) {
        supplierRecyclerView.prebook(isPrebook)
        supplierSortWidget.prebookChanged(isPrebook)
    }

    override fun showList() {
        if (!isSupplierListVisible) {
            animate()
                    .translationY(0F)
                    .setDuration(resources.getInteger(R.integer.animation_duration_slide_out_or_in_suppliers).toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withStartAction {
                        isSupplierListVisible = true
                        bookingSupplierViewModel?.process(
                                BookingSupplierViewContract.BookingSupplierEvent
                                        .SupplierListVisibilityChanged(isVisible = true, panelState = collapsiblePanelView.panelState))
                    }
        }
    }

    override fun hideList() {
        if (isSupplierListVisible) {

            val translation = when (collapsiblePanelView.panelState) {
                PanelState.COLLAPSED -> resources.getDimension(R.dimen.quote_list_height)
                PanelState.EXPANDED -> resources.getDimension(R.dimen.collapsible_pane_expanded_height)
            }

            animate()
                    .translationY(translation)
                    .setDuration(resources.getInteger(R.integer.animation_duration_slide_out_or_in_suppliers).toLong())
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .withStartAction {
                        bookingSupplierViewModel?.process(
                                BookingSupplierViewContract.BookingSupplierEvent
                                        .SupplierListVisibilityChanged(isVisible = false, panelState =
                                        collapsiblePanelView.panelState))
                    }
                    .withEndAction {
                        isSupplierListVisible = false
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
        bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent
                                                  .Error(snackbarConfig))

    }

    override fun hideNoAvailability() {
        bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent
                                                  .SupplierListVisibilityChanged(false, panelState = collapsiblePanelView.panelState))
    }

    override fun showSnackbarError(snackbarConfig: SnackbarConfig) {
        bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent
                                                  .Error(snackbarConfig))
    }

    override fun setSupplierListVisibility() {
        bookingSupplierViewModel?.process(
                BookingSupplierViewContract.BookingSupplierEvent
                        .SupplierListVisibilityChanged(isVisible = isVisible, panelState = collapsiblePanelView.panelState))
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
