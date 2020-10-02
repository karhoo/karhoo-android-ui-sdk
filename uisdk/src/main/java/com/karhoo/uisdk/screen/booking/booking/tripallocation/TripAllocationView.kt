package com.karhoo.uisdk.screen.booking.booking.tripallocation

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
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
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.BuildConfig
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.booking.BookingCodes
import com.karhoo.uisdk.base.listener.SimpleAnimationListener
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStatus
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
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.allocationOneLabel
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.allocationTwoLabel
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.cancelButton
import kotlinx.android.synthetic.main.uisdk_view_trip_allocation.view.findingYourTripLabel

class TripAllocationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), TripAllocationMVP.View {

    private var bookingRequestStateViewModel: BookingRequestStateViewModel? = null
    private var presenter: TripAllocationMVP.Presenter? = null
    var actions: TripAllocationMVP.Actions? = null

    init {
        View.inflate(context, R.layout.uisdk_view_trip_allocation, this)
    }

    private fun waitForAllocation(trip: TripInfo) {
        presenter = TripAllocationPresenter(this, KarhooApi.tripService)
        val animUpwards = resources.getDimension(R.dimen.spacing_xsmall).convertDpToPixels().toFloat()
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
                    .tripInfo(trip)
                    .build(context)
            context.startActivity(intent)
        }
    }

    override fun displayBookingFailed(fleetName: String) {
        visibility = View.INVISIBLE
        isClickable = false
        val alertDialogBuilder = AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.booking_failed)
                .setPositiveButton(R.string.dismiss) { dialog, _ -> dialog.cancel() }
        if (fleetName.isBlank()) {
            alertDialogBuilder.setMessage(resources.getString(R.string.booking_failed_body_no_fleet_name))
        } else {
            alertDialogBuilder.setMessage(String.format(resources.getString(R.string.booking_failed_body), fleetName))
        }
        alertDialogBuilder.show()
        actions?.onBookingCancelledOrFinished()
    }

    override fun displayTripCancelledSuccess() {
        cancelButton.isEnabled = true
        visibility = View.INVISIBLE
        isClickable = false
        AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.cancel_ride_successful)
                .setMessage(R.string.cancel_ride_successful_message)
                .setPositiveButton(R.string.dismiss) { dialog, _ -> dialog.cancel() }
                .show()
        actions?.onBookingCancelledOrFinished()
    }

    override fun showCallToCancelDialog(number: String, supplier: String) {
        cancelButton.isEnabled = true
        AlertDialog.Builder(context, R.style.DialogTheme)
                .setTitle(R.string.difficulties_cancelling_title)
                .setMessage(R.string.difficulties_cancelling_message)
                .setPositiveButton(R.string.call) { _, _ -> makeCall(number) }
                .setNegativeButton(R.string.dismiss) { dialog, _ -> dialog.cancel() }
                .show()
    }

    override fun displayWebTracking(followCode: String) {
        val trackingWebIntent = WebActivity.Builder.builder
                .url(BuildConfig.KARHOO_WEB_TRACKING_URL + followCode)
                .build(context)
        context.startActivity(BookingActivity.Builder.builder.build(context))
        context.startActivity(trackingWebIntent)
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

}
