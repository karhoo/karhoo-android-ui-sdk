package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.extension.toLocalisedInfoString
import com.karhoo.uisdk.util.extension.toLocalisedString
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.etaText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.etaTypeText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.pickUpTypeText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceInfoLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceInfoText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.pricingTypeText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.pricingTypeTextLayout
import java.util.Currency

class BookingPriceView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), BookingPriceViewContract.View {

    private var presenter: BookingPriceViewContract.Presenter

    init {
        inflate(context, R.layout.uisdk_view_booking_time_price, this)

        presenter = BookingPriceViewPresenter()
        presenter.attachView(this)
    }

    fun bindViews(vehicle: Quote,
                  typeEta: String,
                  currency: Currency) {
        etaText.text = String.format("%s %s", vehicle.vehicle.vehicleQta.highMinutes, context.getString(R.string.kh_uisdk_min))
        bindRemainingViews(vehicle, typeEta, currency)
    }

    fun bindPrebook(vehicle: Quote, time: String,
                    typeEta: String,
                    currency: Currency) {
        etaText.text = time
        bindRemainingViews(vehicle, typeEta, currency)
    }

    private fun bindRemainingViews(quote: Quote, typeEta: String, currency: Currency) {
        presenter.formatPriceText(quote, currency)
        presenter.formatQuoteType(quote)
        presenter.formatPickUpType(quote)

        etaTypeText.text = typeEta
        setContainerVisibility(R.dimen.spacing_medium, VISIBLE)
        pricingTypeTextLayout.setOnClickListener {
            priceInfoLayout.visibility = if (priceInfoLayout.visibility == VISIBLE) GONE else VISIBLE
        }
    }

    fun bindETAOnly(time: Int?, typeEta: String, typePrice: QuoteType) {
        etaText.text = String.format("%s %s", time ?: "~", context.getString(R.string.kh_uisdk_min))
        etaTypeText.text = typeEta
        setContainerVisibility(R.dimen.spacing_none, GONE)
    }

    private fun setContainerVisibility(dimenSize: Int, visibility: Int) {
        priceLayout.visibility = visibility
    }

    override fun getString(id: Int): String {
        return context.getString(id)
    }

    override fun setPickUpType(pickUpType: PickupType?) {
        pickUpType?.let {
            pickUpTypeText.text = pickUpType.toLocalisedString(context)
            pickUpTypeText.visibility = VISIBLE
        } ?: run {
            pickUpTypeText.visibility = GONE
        }
    }

    override fun setPriceText(price: String) {
        priceText.text = price
    }

    override fun setQuoteTypeDetails(quoteType: QuoteType) {
        pricingTypeText.text = quoteType.toLocalisedString(context)
        priceInfoText.text = quoteType.toLocalisedInfoString(context)
    }
}
