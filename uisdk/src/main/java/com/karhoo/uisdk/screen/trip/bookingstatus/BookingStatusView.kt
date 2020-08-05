package com.karhoo.uisdk.screen.trip.bookingstatus

import android.app.AlertDialog
import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.trip.bookingstatus.contact.ContactOptionsActions
import com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo.TripInfoActions
import kotlinx.android.synthetic.main.uisdk_view_booking_status.view.tripInfoCollapsibleLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_status.view.tripInfoWidget
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.contactOptionsWidget

class BookingStatusView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr), BookingStatusMVP.View, ContactOptionsActions,
      TripInfoActions, LifecycleObserver {

    private var presenter: BookingStatusMVP.Presenter = BookingStatusPresenter(this, KarhooApi.tripService,
                                                                               KarhooUISDK.analytics)

    internal var actions: BookingStatusActions? = null

    private var viewIsVisible = false

    init {
        inflate(context, R.layout.uisdk_view_booking_status, this)

        if (!isInEditMode) {
            tripInfoWidget.apply {
                observeTripStatus(presenter)
                actions = this@BookingStatusView
            }

            tripInfoCollapsibleLayout.apply {
                setHeights(0F,
                           resources.getDimension(R.dimen.collapsible_panel_collapsed_trip_info))
                isEnabled = true
            }

            contactOptionsWidget.actions = this
            contactOptionsWidget.observeTripStatus(presenter)
        }
    }

    override fun monitorTrip(tripIdentifier: String) {
        presenter.monitorTrip(tripIdentifier)
    }

    override fun setCancelEnabled(enabled: Boolean) {
        if (enabled) {
            contactOptionsWidget.enableCancelButton()
        } else {
            contactOptionsWidget.disableCancelButton()
        }
    }

    override fun updateStatus(@StringRes status: Int, supplier: String) {
        var text = resources.getString(status)
        if (text.contains("%s")) {
            text = String.format(text, supplier)
        }
        actions?.showTopBarNotification(text)
    }

    override fun showTemporaryError(error: String) {
        actions?.showSnackbar(SnackbarConfig(text = error))
    }

    override fun tripInfoVisibility(isVisible: Boolean) {
        if (isVisible && tripInfoCollapsibleLayout.height <= 0) {
            tripInfoCollapsibleLayout.togglePanelState()
        }
    }

    override fun goToCleanBooking() {
        actions?.goToCleanBooking()
    }

    override fun showCancellationDialog(tripDetails: TripInfo) {
        AlertDialog.Builder(context, R.style.AlertDialog)
                .setTitle(R.string.title_dispatch_cancelled)
                .setMessage(String.format(context
                                                  .getString(R.string.dispatch_cancelled),
                                          if (tripDetails.fleetInfo == null) context.getString(R.string.supplier) else tripDetails.fleetInfo?.name))
                .setPositiveButton(R.string.ok) { _, _ -> actions?.goToCleanBooking() }
                .setNegativeButton(R.string.alternative) { _, _ -> actions?.goToPrefilledBooking(tripDetails) }
                .setCancelable(false)
                .show()
    }

    override fun tripComplete(tripDetails: TripInfo) {
        if (viewIsVisible) {
            actions?.gotoRideDetails(tripDetails)
        } else {
            KarhooUISDK.karhooNotification?.let {
                it.notifyRideEnded(context, tripDetails)
            }
            actions?.updateRideDetails(tripDetails)
        }
    }

    override fun tripCanceled(tripDetails: TripInfo) {
        if (!viewIsVisible) {
            KarhooUISDK.karhooNotification?.let {
                it.notifyRideEnded(context, tripDetails)
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        viewIsVisible = true
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        viewIsVisible = false
    }
}
