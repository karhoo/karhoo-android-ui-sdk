package com.karhoo.uisdk.screen.trip.bookingstatus

import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.CollapsiblePanelView
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.trip.bookingstatus.contact.ContactOptionsActions
import com.karhoo.uisdk.screen.trip.bookingstatus.contact.ContactOptionsView
import com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo.TripInfoActions
import com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo.TripInfoView

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

    private lateinit var tripInfoCollapsibleLayout: CollapsiblePanelView
    private lateinit var tripInfoWidget: TripInfoView
    private lateinit var contactOptionsWidget: ContactOptionsView

    init {
        inflate(context, R.layout.uisdk_view_booking_status, this)

        tripInfoCollapsibleLayout = findViewById(R.id.tripInfoCollapsibleLayout)
        tripInfoWidget = findViewById(R.id.tripInfoWidget)
        contactOptionsWidget = findViewById(R.id.contactOptionsWidget)

        if (!isInEditMode) {
            tripInfoWidget.apply {
                observeTripStatus(presenter)
                actions = this@BookingStatusView
            }

            tripInfoCollapsibleLayout.apply {
                setHeights(0F,
                           resources.getDimension(R.dimen.kh_uisdk_collapsible_panel_collapsed_trip_info))
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

    override fun updateStatus(@StringRes status: Int, quote: String) {
        var text = resources.getString(status)
        if (text.contains("%s")) {
            text = String.format(text, quote)
        }
        actions?.showTopBarNotification(text)
    }

    override fun showTemporaryError(error: String, karhooError: KarhooError?) {
        actions?.showSnackbar(SnackbarConfig(text = error, karhooError = karhooError))
    }

    override fun tripInfoVisibility(isVisible: Boolean) {
        if (isVisible && tripInfoCollapsibleLayout.height <= 0) {
            tripInfoCollapsibleLayout.togglePanelState()
        }
    }

    override fun goToNextScreen() {
        actions?.goToCleanBooking()
    }

    override fun showCancellationDialog(tripDetails: TripInfo) {
        val message = String.format(context.getString(R.string.kh_uisdk_dispatch_cancelled),
                                    if (tripDetails.fleetInfo == null) context.getString(R.string.kh_uisdk_quote) else tripDetails.fleetInfo?.name)
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_title_dispatch_cancelled,
                message = message,
                cancellable = false,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok,
                                                         DialogInterface.OnClickListener { _, _ -> actions?.goToCleanBooking() }),
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_alternative,
                                                         DialogInterface.OnClickListener { _, _ -> actions?.goToPrefilledBooking(tripDetails) }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

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
