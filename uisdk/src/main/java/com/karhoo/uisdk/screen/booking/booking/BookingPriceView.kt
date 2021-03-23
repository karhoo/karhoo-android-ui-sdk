package com.karhoo.uisdk.screen.booking.booking

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.widget.TextViewCompat
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.CurrencyUtils
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.etaText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.etaTypeText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceTypeText
import java.util.Currency

class BookingPriceView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    private var headerTextStyle: Int = R.style.Text_White_Small
    private var detailsTextStyle: Int = R.style.Text_White_XXLarge_Bold

    init {
        inflate(context, R.layout.uisdk_view_booking_time_price, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingPriceView,
                                                        defStyleAttr, R.style.KhBookingETAPriceView)
        headerTextStyle = typedArray.getResourceId(R.styleable.BookingPriceView_quotesHeaderText, R
                .style
                .Text_White_Small)
        detailsTextStyle = typedArray.getResourceId(R.styleable.BookingPriceView_quotesDetailsText, R
                .style
                .Text_White_XXLarge_Bold)
        TextViewCompat.setTextAppearance(etaTypeText, headerTextStyle)
        TextViewCompat.setTextAppearance(etaText, detailsTextStyle)
        TextViewCompat.setTextAppearance(priceTypeText, headerTextStyle)
        TextViewCompat.setTextAppearance(priceText, detailsTextStyle)
    }

    fun bindViews(vehicle: Quote,
                  typeEta: String,
                  currency: Currency) {
        etaText.text = String.format("%s %s", vehicle.vehicle.vehicleQta.highMinutes, context
                .getString(R.string
        .kh_uisdk_min))
        bindRemainingViews(vehicle, typeEta, currency)
    }

    fun bindPrebook(vehicle: Quote, time: String,
                    typeEta: String,
                    currency: Currency) {
        etaText.text = time
        bindRemainingViews(vehicle, typeEta, currency)
    }

    private fun bindRemainingViews(vehicle: Quote, typeEta: String, currency: Currency) {
        when (vehicle.quoteSource) {
            QuoteSource.FLEET -> {
                priceText.text = CurrencyUtils.intToPrice(currency, vehicle.price.highPrice)
            }
            QuoteSource.MARKET -> {
                priceText.text = CurrencyUtils.intToRangedPrice(currency, vehicle.price.lowPrice,
                                                                vehicle.price.highPrice)
            }
        }
        etaTypeText.text = typeEta
        setContainerVisibility(R.dimen.spacing_medium, VISIBLE)
    }

    fun bindETAOnly(time: Int?, typeEta: String, typePrice: QuoteType) {
        etaText.text = String.format("%s %s", time ?: "~", context.getString(R.string.kh_uisdk_min))
        etaTypeText.text = typeEta
        setContainerVisibility(R.dimen.spacing_none, GONE)
    }

    private fun setContainerVisibility(dimenSize: Int, visibility: Int) {
        priceLayout.visibility = visibility
    }
}
