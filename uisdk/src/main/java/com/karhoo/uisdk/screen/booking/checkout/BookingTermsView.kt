package com.karhoo.uisdk.screen.booking.checkout

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.checkout.payment.PaymentActions
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.bookingTermsText
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.cancellationText
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.termsConditionsText

class BookingTermsView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var actions: PaymentActions? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_terms, this)
    }

    fun bindViews(vehicle: Quote) {
        bookingTermsText.text = String.format(resources.getString(R.string.kh_uisdk_booking_terms),
                                              vehicle.fleet.name)
        termsConditionsText.paintFlags = termsConditionsText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        termsConditionsText.setOnClickListener {
            actions?.showWebView(vehicle.fleet.termsConditionsUrl)
        }
        cancellationText.paintFlags = cancellationText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        cancellationText.setOnClickListener {
            actions?.showWebView(vehicle.fleet.termsConditionsUrl)
        }
    }
}
