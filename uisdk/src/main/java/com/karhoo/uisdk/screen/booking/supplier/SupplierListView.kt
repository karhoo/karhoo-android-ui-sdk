package com.karhoo.uisdk.screen.booking.supplier

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.AttrRes
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
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

    private var bookingSupplierViewModel: BookingSupplierViewModel? = null

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
        if (collapsiblePanelView.panelState == PanelState.EXPANDED) {
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

    override fun bindViewToData(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel:
    BookingStatusStateViewModel,
                                categoriesViewModel: CategoriesViewModel, vehicles:
                                LiveFleetsViewModel, bookingSupplierViewModel: BookingSupplierViewModel) {
        vehicles.liveFleets.observe(lifecycleOwner, presenter.watchVehicles())
        bookingStatusStateViewModel.viewStates().observe(lifecycleOwner, presenter.watchBookingStatus())
        categorySelectorWidget.bindViewToData(lifecycleOwner, categoriesViewModel, bookingStatusStateViewModel)
        supplierRecyclerView.watchCategories(lifecycleOwner, categoriesViewModel)
        supplierRecyclerView.watchQuoteListStatus(lifecycleOwner, bookingSupplierViewModel)

        this.bookingSupplierViewModel = bookingSupplierViewModel
        bookingSupplierViewModel.viewStates().observe(lifecycleOwner, watchBookingSupplierStatus())
    }

    private fun watchBookingSupplierStatus(): Observer<in QuoteListStatus> {
        return Observer { quoteListStatus ->
            quoteListStatus?.let {
                it.selectedQuote
            }
        }
    }

    override fun bindAvailability(availabilityProvider: AvailabilityProvider) {
        availabilityProvider.setAvailabilityHandler(presenter)
        categorySelectorWidget.bindAvailability(availabilityProvider)
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
            Log.d("PD36", "showList")
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
            Log.d("PD36", "hideList")

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
        Log.d("PD36", "hideNoAvailability")
        bookingSupplierViewModel?.process(BookingSupplierViewContract.BookingSupplierEvent
                                                  .SupplierListVisibilityChanged(false, panelState = collapsiblePanelView.panelState))
    }

    override fun setSupplierListVisibility() {
        Log.d("PD36", "setSupplierListVisibility")
        bookingSupplierViewModel?.process(
                BookingSupplierViewContract.BookingSupplierEvent
                        .SupplierListVisibilityChanged(isVisible = isVisible, panelState = collapsiblePanelView.panelState))
    }

}
