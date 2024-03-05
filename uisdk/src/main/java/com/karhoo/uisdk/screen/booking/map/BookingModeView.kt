package com.karhoo.uisdk.screen.booking.map

import android.content.Context
import android.util.AttributeSet
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.lifecycle.LifecycleOwner
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.listener.SimpleAnimationListener
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerMVP
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerPresenter
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import org.joda.time.DateTime

class BookingModeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), BookingModeMVP.View, TimeDatePickerMVP.View {

    private val presenter: BookingModeMVP.Presenter = BookingModePresenter(this)
    private val timeDatePresenter: TimeDatePickerMVP.Presenter =
        TimeDatePickerPresenter(this, KarhooUISDK.analytics)

    var callbackToStartQuoteList: ((isPrebook: Boolean) -> Unit)? = null
    private var animating: Boolean = false

    private lateinit var nowActionButton: LinearLayout
    private lateinit var scheduleActionButton: LinearLayout
    private lateinit var loyaltyInfoLayout: LinearLayout

    init {
        inflate(context, R.layout.uisdk_view_booking_mode, this)

        nowActionButton = findViewById(R.id.nowActionButton)
        scheduleActionButton = findViewById(R.id.scheduleActionButton)
        loyaltyInfoLayout = findViewById(R.id.loyaltyInfoLayout)

        nowActionButton.setOnClickListener {
            callbackToStartQuoteList?.invoke(false)
        }

        scheduleActionButton.setOnClickListener {
            timeDatePresenter.datePickerClicked()
        }

        nowActionButton.alpha = disabledOpacity
        scheduleActionButton.alpha = disabledOpacity
    }

    override fun enableNowButton(enable: Boolean) {
        nowActionButton.alpha = if (enable) 1F else disabledOpacity
        nowActionButton.isEnabled = enable
    }

    override fun showNoCoverageText(hasCoverage: Boolean) {
        loyaltyInfoLayout.visibility = if (hasCoverage) GONE else VISIBLE
    }

    override fun enableScheduleButton(enable: Boolean) {
        scheduleActionButton.alpha = if (enable) 1F else disabledOpacity
        scheduleActionButton.isEnabled = enable
    }

    override fun displayPrebookTime(time: DateTime) {
        callbackToStartQuoteList?.invoke(true)
    }

    override fun hideDateViews() {
        // no need to hide anything
    }

    override fun show(show: Boolean, cb: (() -> Unit)?) {
        if(visibility == VISIBLE && show) {
            return
        }

        if(visibility == INVISIBLE && !show) {
            return
        }

        if(!animating) {
            animating = true
        }

        if (show) {
            this@BookingModeView.visibility = VISIBLE

            val slideUpAnimation =
                AnimationUtils
                    .loadAnimation(context, R.anim.kh_slide_up)
                    .apply {
                        setAnimationListener(object : SimpleAnimationListener() {
                            override fun onAnimationEnd(animation: Animation) {
                                this@BookingModeView.visibility = VISIBLE

                                animating = false

                                cb?.invoke()
                            }
                        })
                    }
            this.startAnimation(slideUpAnimation)
        } else {
            val slideDownAnimation =
                AnimationUtils
                    .loadAnimation(context, R.anim.kh_slide_down)
                    .apply {
                        setAnimationListener(object : SimpleAnimationListener() {
                            override fun onAnimationEnd(animation: Animation) {
                                this@BookingModeView.visibility = INVISIBLE

                                animating = false
                                cb?.invoke()
                            }
                        })
                    }
            this.startAnimation(slideDownAnimation)
        }
    }

    fun watchJourneyDetailsState(
        lifecycleOwner: LifecycleOwner,
        journeyDetailsStateViewModel: JourneyDetailsStateViewModel
    ) {
        journeyDetailsStateViewModel.viewStates().apply {
            observe(
                lifecycleOwner,
                timeDatePresenter.subscribeToJourneyDetails(journeyDetailsStateViewModel)
            )
        }
    }

    companion object {
        private const val disabledOpacity = 0.4F
    }
}
