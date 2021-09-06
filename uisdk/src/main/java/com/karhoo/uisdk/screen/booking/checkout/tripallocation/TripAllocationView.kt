package com.karhoo.uisdk.screen.booking.checkout.tripallocation

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.booking.BookingCodes
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.listener.SimpleAnimationListener
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.screen.web.WebActivity
import com.karhoo.uisdk.util.IntentUtils
import com.karhoo.uisdk.util.ViewsConstants.TRIP_ALLOCATION_ANIMATION_DURATION
import com.karhoo.uisdk.util.ViewsConstants.TRIP_ALLOCATION_ANIMATION_START_DELAY
import com.karhoo.uisdk.util.ViewsConstants.TRIP_ALLOCATION_TEXT_ROTATION_ANIM_DURATION
import com.karhoo.uisdk.util.ViewsConstants.TRIP_ALLOCATION_TEXT_ROTATION_ANIM_INITIAL_DELAY
import com.karhoo.uisdk.util.ViewsConstants.TRIP_ALLOCATION_TEXT_ROTATION_ANIM_START_OFFSET_LONG
import com.karhoo.uisdk.util.ViewsConstants.TRIP_ALLOCATION_TEXT_ROTATION_ANIM_START_OFFSET_SHORT
import com.karhoo.uisdk.util.extension.convertDpToPixels
import com.karhoo.uisdk.util.extension.guestTripTrackingUrl
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.allocationOneLabel
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.allocationTwoLabel
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.cancelButton
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.findingYourTripLabel

class TripAllocationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), TripAllocationContract.View, TripAllocationContract.Widget {

    private var bookingRequestStateViewModel: BookingRequestStateViewModel? = null
    private var presenter: TripAllocationContract.Presenter? = null
    var actions: TripAllocationContract.Actions? = null

    init {
        View.inflate(context, R.layout.uisdk_view_trip_allocation, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val tripInfo = data?.getParcelableExtra<TripInfo>(CheckoutActivity
                                                                  .BOOKING_CHECKOUT_TRIP_INFO_KEY)
        if (tripInfo != null) {
            waitForAllocation(tripInfo)
        }
    }

    private fun waitForAllocation(trip: TripInfo) {
        presenter = TripAllocationPresenter(this, KarhooApi.tripService)
        val animUpwards = resources.getDimension(R.dimen.kh_uisdk_spacing_xsmall).convertDpToPixels().toFloat()
        findingYourTripLabel.animate()
                .translationY(-animUpwards)
                .setStartDelay(TRIP_ALLOCATION_ANIMATION_START_DELAY)
                .setDuration(TRIP_ALLOCATION_ANIMATION_DURATION)
                .start()

        startTextRotationAnimations(TRIP_ALLOCATION_TEXT_ROTATION_ANIM_INITIAL_DELAY)

        visibility = View.VISIBLE
        isClickable = true
        cancelButton.isEnabled = true
        cancelButton.setListener { cancelTrip() }
        presenter?.waitForAllocation(trip)
        if (!KarhooUISDKConfigurationProvider.isGuest()) {
            handler.postDelayed({ presenter?.handleAllocationDelay(trip) }, ALLOCATION_ALERT_DELAY)
        }

    }

    private fun cancelTrip() {
        cancelButton.isEnabled = false
        presenter?.cancelTrip()
    }

    private fun startTextRotationAnimations(initialStartOffset: Long) {
        val animationDuration = TRIP_ALLOCATION_TEXT_ROTATION_ANIM_DURATION
        val startOffsetShort = TRIP_ALLOCATION_TEXT_ROTATION_ANIM_START_OFFSET_SHORT
        val startOffsetLong = TRIP_ALLOCATION_TEXT_ROTATION_ANIM_START_OFFSET_LONG
        val reverseInterpolator = Interpolator { paramFloat -> Math.abs(paramFloat - 1f) }

        //FOURTH ANIMATION
        val text2Out = AnimationUtils.loadAnimation(context, R.anim.uisdk_scale_up_translate_up_fade_in).apply {
            interpolator = reverseInterpolator
            startOffset = startOffsetLong
            duration = animationDuration
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    startTextRotationAnimations(animationDuration)
                }
            })
        }

        //THIRD ANIMATION
        val text2In = AnimationUtils.loadAnimation(context, R.anim.uisdk_scale_up_translate_up_fade_in).apply {
            startOffset = startOffsetShort
            duration = animationDuration
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    allocationTwoLabel.startAnimation(text2Out)
                }
            })
        }

        //SECOND ANIMATION
        val text1Out = AnimationUtils.loadAnimation(context, R.anim.uisdk_scale_up_translate_up_fade_in).apply {
            interpolator = reverseInterpolator
            startOffset = startOffsetLong
            duration = animationDuration
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    allocationTwoLabel.startAnimation(text2In)
                }
            })
        }

        //FIRST ANIMATION
        val text1In = AnimationUtils.loadAnimation(context, R.anim.uisdk_scale_up_translate_up_fade_in).apply {
            startOffset = initialStartOffset
            this.duration = animationDuration
            setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation) {
                    allocationOneLabel.startAnimation(text1Out)
                }
            })
        }

        allocationOneLabel.startAnimation(text1In)
    }

    override fun goToTrip(trip: TripInfo) {
        val activity = context as Activity
        val activityWasStartedForResult = activity.callingActivity != null

        if (activityWasStartedForResult) {
            val data = Intent().apply {
                putExtra(BookingCodes.BOOKED_TRIP, trip)
            }
            activity.setResult(Activity.RESULT_OK, data)
            activity.finish()
        } else {
            val intent = TripActivity.Builder.builder
                    .tripInfo(trip = trip, backToBooking = true)
                    .build(context)
            context.startActivity(intent)
        }
    }

    override fun displayBookingFailed(fleetName: String) {
        visibility = View.INVISIBLE
        isClickable = false

        val message = if (fleetName.isBlank()) {
            resources.getString(R.string.kh_uisdk_booking_failed_body_no_fleet_name)
        } else {
            String.format(resources.getString(R.string.kh_uisdk_booking_failed_body), fleetName)
        }
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_booking_failed,
                message = message,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

        actions?.onBookingCancelledOrFinished()
    }

    override fun displayTripCancelledSuccess() {
        cancelButton.isEnabled = true
        visibility = View.INVISIBLE
        isClickable = false

        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_cancel_ride_successful,
                messageResId = R.string.kh_uisdk_cancel_ride_successful_message,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

        actions?.onBookingCancelledOrFinished()
    }

    override fun displayWebTracking(followCode: String) {
        val environment = KarhooUISDKConfigurationProvider.configuration.environment()
        val trackingUrl = resources.getString(environment.guestTripTrackingUrl(), followCode)
        val trackingWebIntent = WebActivity.Builder.builder
                .url(trackingUrl)
                .build(context)
        context.startActivity(BookingActivity.Builder.builder.build(context))
        context.startActivity(trackingWebIntent)
    }

    override fun showAllocationDelayAlert(trip: TripInfo) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_allocation_delay_title,
                messageResId = R.string.kh_uisdk_allocation_delay_text,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok,
                                                         DialogInterface.OnClickListener { dialog, _ ->
                                                             dialog.cancel()
                                                             presenter?.unsubscribeFromUpdates()
                                                             cancelButton.isEnabled = false
                                                             visibility = View.INVISIBLE
                                                             isClickable = false
                                                             context.startActivity(RideDetailActivity.Builder.newBuilder()
                                                                                           .trip(trip)
                                                                                           .build(context))
                                                             actions?.onBookingCancelledOrFinished()
                                                         })
                                            )
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    override fun showCallToCancelDialog(number: String, quote: String, karhooError: KarhooError?) {
        cancelButton.isEnabled = true
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_difficulties_cancelling_title,
                messageResId = R.string.kh_uisdk_difficulties_cancelling_message,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_call,
                                                         DialogInterface.OnClickListener { _, _ -> makeCall(number) }),
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_dismiss,
                                                         DialogInterface.OnClickListener { dialog, _ -> dialog.cancel() }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)
    }

    private fun makeCall(number: String) {
        IntentUtils.dialIntent(number)?.let { context.startActivity(it) }
    }

    fun watchBookingRequestStatus(lifecycleOwner: LifecycleOwner, bookingRequestStateViewModel:
    BookingRequestStateViewModel) {
        this.bookingRequestStateViewModel = bookingRequestStateViewModel
        val observer = Observer<BookingRequestStatus> { bookingRequestStatus ->
            bookingRequestStatus?.tripInfo?.let {
                waitForAllocation(it)
            }
        }
        bookingRequestStateViewModel.viewStates().observe(lifecycleOwner, observer)
    }

    companion object {
        private const val ALLOCATION_ALERT_DELAY = 60000L
    }
}
