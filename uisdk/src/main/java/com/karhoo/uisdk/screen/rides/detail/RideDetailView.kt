package com.karhoo.uisdk.screen.rides.detail

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.booking.basefare.BaseFareView
import com.karhoo.uisdk.screen.rides.feedback.FeedbackCompletedTripsStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.IntentUtils
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.baseFareIcon
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.bookingTermsText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.cancelRideButton
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.carText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.cardLogoImage
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.cardNumberText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.commentsLayout
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.commentsText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.contactFleetButton
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.dateTimeText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.dropOffLabel
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.flightDetailsLayout
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.flightNumberText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.karhooId
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.logoImage
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.meetingPointText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.pickupLabel
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.pickupTypeText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.priceText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.priceTypeText
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.ratingDivider
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.rebookRideButton
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.reportIssueButton
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.starRatingWidget
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.stateIcon
import kotlinx.android.synthetic.main.uisdk_view_ride_detail.view.stateText
import org.joda.time.DateTime

class RideDetailView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), RideDetailMVP.View, LifecycleObserver {

    private var presenter: RideDetailPresenter? = null
    private var progressDialog: ProgressDialog? = null
    private var cancellationDialog: AlertDialog? = null
    var rideDetailActions: RideDetailMVP.View.Actions? = null

    init {
        View.inflate(context, R.layout.uisdk_view_ride_detail, this)
    }

    fun bind(trip: TripInfo) {
        presenter = RideDetailPresenter(this, trip, KarhooApi.tripService, ScheduledDateViewBinder(), KarhooUISDK.analytics, FeedbackCompletedTripsStore(context))

        loadLogo(trip)
        displayText(trip)
        setListeners(trip)
        bindPickupType(trip.meetingPoint?.pickupType)
        setTripRating(trip)

        presenter?.apply {
            bindFlightDetails()
            bindPrice()
            bindState()
            bindCard()
            bindButtons()
            bindVehicle()
            bindDate()
        }
    }

    private fun setTripRating(trip: TripInfo) {
        if (trip.tripState == TripStatus.COMPLETED) {
            starRatingWidget.visibility = View.VISIBLE
            ratingDivider.visibility = View.VISIBLE
            starRatingWidget.trip = trip
        }
    }

    private fun setListeners(trip: TripInfo) {
        reportIssueButton.setOnClickListener { rideDetailActions?.showCustomerSupport(trip.displayTripId) }
        rebookRideButton.setOnClickListener { startBookingActivityWithTrip(trip) }
        cancelRideButton.setOnClickListener { displayCancellationConfirmationDialog() }
        contactFleetButton.setOnClickListener { presenter?.contactFleet() }
        baseFareIcon.setOnClickListener { presenter?.baseFarePressed() }
    }

    private fun loadLogo(trip: TripInfo) {
        val fleetInfo: FleetInfo? = trip.fleetInfo
        if (!fleetInfo?.logoUrl.isNullOrEmpty()) {
            Picasso.with(context)
                    .load(trip.fleetInfo?.logoUrl)
                    .into(logoImage)
        }
    }

    private fun displayText(trip: TripInfo) {
        trip.fleetInfo?.let { bookingTermsText.text = it.name }
        trip.origin?.let { pickupLabel.text = it.displayAddress }
        trip.destination?.let { dropOffLabel.text = it.displayAddress }
        karhooId.text = trip.displayTripId
    }

    override fun displayDate(date: DateTime) {
        val date = DateUtil.getDateAndTimeFormat(context, date)
        dateTimeText.text = date
        rideDetailActions?.externalDateTime = date
    }

    override fun displayNoDateAvailable() {
        dateTimeText.setText(R.string.pending)
        rideDetailActions?.externalDateTime = resources.getString(R.string.pending)
    }

    private fun displayCancellationConfirmationDialog() {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.cancel_your_ride,
                messageResId = R.string.cancellation_fee,
                positiveButton = KarhooAlertDialogAction(R.string.cancel,
                                                         DialogInterface.OnClickListener { _, _ -> presenter?.cancelTrip() }),
                negativeButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        cancellationDialog = KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun displayVehicle(licensePlate: String) {
        carText.visibility = View.VISIBLE
        carText.text = licensePlate
    }

    override fun displayState(@DrawableRes icon: Int, @StringRes state: Int, @ColorRes color: Int) {
        stateIcon.setImageResource(icon)
        this.stateText.apply {
            setTextColor(ContextCompat.getColor(context, color))
            setText(state)
        }
    }

    override fun displayPricePending() {
        this.priceText.setText(R.string.cancelled)
    }

    override fun displayPrice(price: String) {
        this.priceTypeText.text = resources.getText(R.string.price)
        this.baseFareIcon.visibility = GONE
        this.priceText.text = price
    }

    override fun displayBasePrice(price: String) {
        this.priceText.text = price
    }

    override fun displayCard(@DrawableRes logo: Int, number: String) {
        cardLogoImage.setImageResource(logo)
        cardNumberText.text = number
    }

    private fun startBookingActivityWithTrip(trip: TripInfo) {
        val intent = BookingActivity.Builder.builder
                .tripDetails(trip)
                .build(context)
        context.startActivity(intent)
    }

    override fun displayRebookButton() {
        rebookRideButton.visibility = View.VISIBLE
    }

    override fun displayReportIssueButton() {
        reportIssueButton.visibility = View.VISIBLE
    }

    override fun displayCancelRideButton() {
        cancelRideButton.visibility = View.VISIBLE
    }

    override fun displayContactFleetButton() {
        contactFleetButton.visibility = View.VISIBLE
    }

    override fun hideRebookButton() {
        rebookRideButton.visibility = View.GONE
    }

    override fun hideReportIssueButton() {
        reportIssueButton.visibility = View.GONE
    }

    override fun hideCancelRideButton() {
        if (cancellationDialog?.isShowing == true) {
            cancellationDialog?.dismiss()
        }
        cancelRideButton.visibility = View.GONE
    }

    override fun hideContactFleetButton() {
        contactFleetButton.visibility = View.GONE
    }

    override fun makeCall(number: String) {
        IntentUtils.dialIntent(number)?.let { context.startActivity(it) }
    }

    override fun displayLoadingDialog() {
        progressDialog = ProgressDialog(context).apply {
            setTitle(R.string.cancelling_ride)
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            isIndeterminate = true
            setCancelable(false)
            setProgressNumberFormat(null)
            setProgressPercentFormat(null)
            show()
        }
    }

    override fun hideLoadingDialog() {
        progressDialog?.cancel()
    }

    override fun displayTripCancelledDialog() {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.cancel_ride_successful,
                messageResId = R.string.cancel_ride_successful_message,
                positiveButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { _, _ -> rideDetailActions?.finishActivity() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

    }

    override fun displayError(errorMessage: Int, karhooError: KarhooError?) {
        rideDetailActions?.showSnackbar(SnackbarConfig(text = resources.getString(errorMessage), karhooError = karhooError))
    }

    override fun displayCallToCancelDialog(number: String, quote: String) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.difficulties_cancelling_title,
                messageResId = R.string.difficulties_cancelling_message,
                positiveButton = KarhooAlertDialogAction(R.string.call,
                                                         DialogInterface.OnClickListener { _, _ -> makeCall(number) }),
                negativeButton = KarhooAlertDialogAction(R.string.dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

    }

    override fun displayFlightDetails(flightNumber: String, meetingPoint: String) {
        if (flightNumber.isNotBlank()) {
            flightDetailsLayout.visibility = View.VISIBLE
            flightNumberText.text = flightNumber
            meetingPointText.text = meetingPoint
        }
    }

    override fun displayComments(comments: String) {
        commentsLayout.visibility = View.VISIBLE
        commentsText.text = comments
    }

    override fun hideComments() {
        commentsLayout.visibility = View.GONE
    }

    override fun hideFlightDetails() {
        flightDetailsLayout.visibility = View.GONE
    }

    override fun displayBaseFareDialog() {
        val config = KarhooAlertDialogConfig(
                view = BaseFareView(context),
                positiveButton = KarhooAlertDialogAction(R.string.got_it,
                                                         DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.dismiss() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

    }

    private fun bindPickupType(pickupType: PickupType?) {
        when (pickupType) {
            PickupType.DEFAULT,
            PickupType.NOT_SET -> {
                pickupTypeText.visibility = View.GONE
            }
            else -> {
                pickupTypeText.visibility = View.VISIBLE
                pickupTypeText.text = pickupType?.toLocalisedString(context.applicationContext)
            }
        }
    }

    override fun showFeedbackSubmitted() {
        starRatingWidget.showFeedbackSubmitted()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        presenter?.onResume()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        presenter?.onPause()
    }

}
