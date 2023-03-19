package com.karhoo.uisdk.screen.booking.map

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_booking_mode.view.*

class BookingModeView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null)
    : LinearLayout(context, attrs), BookingModeMVP.View {

    private val presenter: BookingModeMVP.Presenter = BookingModePresenter(this)
    var callbackToStartQuoteList: ((isPrebook: Boolean) -> Unit)? = null
    init {
        inflate(context, R.layout.uisdk_view_booking_mode, this)

        nowActionButton.setOnClickListener {
            if(presenter.isAllowedToBook)
                callbackToStartQuoteList?.invoke(false)
        }

        scheduleActionButton.setOnClickListener {
            if(presenter.isAllowedToBook)
                callbackToStartQuoteList?.invoke(true)
        }
    }

    fun setIsAllowedToBook(isAllowedToBook: Boolean){
        presenter.isAllowedToBook = isAllowedToBook
    }
}
