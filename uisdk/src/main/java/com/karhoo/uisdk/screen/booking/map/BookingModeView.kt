package com.karhoo.uisdk.screen.booking.map

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_booking_mode.view.*

class BookingModeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs), BookingModeMVP.View {

    private val presenter: BookingModeMVP.Presenter = BookingModePresenter(this)
    var callbackToStartQuoteList: ((isPrebook: Boolean) -> Unit)? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_mode, this)

        nowActionButton.setOnClickListener {
            callbackToStartQuoteList?.invoke(false)
        }

        scheduleActionButton.setOnClickListener {
            callbackToStartQuoteList?.invoke(true)
        }

        nowActionButton.alpha = disabledOpacity
        scheduleActionButton.alpha = disabledOpacity
    }

    override fun enableNowButton(enable: Boolean) {
        nowActionButton.alpha = if (enable) 1F else disabledOpacity
        nowActionButton.isEnabled = enable

        loyaltyInfoLayout.visibility = if (enable) GONE else VISIBLE
    }

    override fun enableScheduleButton(enable: Boolean) {
        scheduleActionButton.alpha = if (enable) 1F else disabledOpacity
        scheduleActionButton.isEnabled = enable
    }

    companion object {
        private const val disabledOpacity = 0.4F
    }
}
