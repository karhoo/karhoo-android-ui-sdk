package com.karhoo.uisdk.screen.booking.booking

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.widget.TextViewCompat
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.booking.payment.PaymentActions
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.bookingTermsText
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.cancellationText
import kotlinx.android.synthetic.main.uisdk_view_booking_terms.view.termsConditionsText

class BookingTermsView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private var lineTextStyle: Int = R.style.Text_Alternative_Small
    private var linkTextStyle: Int = R.style.Text_Action_Primary_Small

    var actions: PaymentActions? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_terms, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingPriceView,
                                                        defStyleAttr, R.style.KhBookingETAPriceView)
        lineTextStyle = typedArray.getResourceId(R.styleable.BookingTermsView_termsText, R
                .style
                .Text_Alternative_Small)
        linkTextStyle = typedArray.getResourceId(R.styleable.BookingTermsView_termsLinkText, R
                .style
                .Text_Action_Primary_Small)
        TextViewCompat.setTextAppearance(bookingTermsText, lineTextStyle)
        TextViewCompat.setTextAppearance(termsConditionsText, linkTextStyle)
        TextViewCompat.setTextAppearance(cancellationText, linkTextStyle)
    }

    fun bindViews(vehicle: Quote) {
        bookingTermsText.text = String.format(resources.getString(R.string.booking_terms),
                                              vehicle.fleet.name)
        termsConditionsText.setOnClickListener {
            actions?.showWebView(vehicle.fleet.termsConditionsUrl)
        }
        cancellationText.setOnClickListener {
            actions?.showWebView(vehicle.fleet.termsConditionsUrl)
        }
    }
}
