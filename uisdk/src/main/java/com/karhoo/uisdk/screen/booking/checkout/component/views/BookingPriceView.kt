package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.extension.toLocalisedInfoString
import com.karhoo.uisdk.util.extension.toLocalisedString
import java.util.Currency

class BookingPriceView @JvmOverloads constructor(context: Context,
                                                 attrs: AttributeSet? = null,
                                                 defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), BookingPriceViewContract.View {

    private var presenter: BookingPriceViewContract.Presenter

    private lateinit var etaText: TextView
    private lateinit var etaTypeText: TextView
    private lateinit var pickUpTypeText: TextView
    private lateinit var priceInfoLayout: LinearLayout
    private lateinit var priceInfoText: TextView
    private lateinit var priceLayout: LinearLayout
    private lateinit var priceText: TextView
    private lateinit var pricingTypeText: TextView
    private lateinit var pricingTypeTextLayout: LinearLayout

    init {
        inflate(context, R.layout.uisdk_view_booking_time_price, this)

        etaText = findViewById(R.id.etaText)
        etaTypeText = findViewById(R.id.etaTypeText)
        pickUpTypeText = findViewById(R.id.pickUpTypeText)
        priceInfoLayout = findViewById(R.id.priceInfoLayout)
        priceInfoText = findViewById(R.id.priceInfoText)
        priceLayout = findViewById(R.id.priceLayout)
        priceText = findViewById(R.id.priceText)
        pricingTypeText = findViewById(R.id.pricingTypeText)
        pricingTypeTextLayout = findViewById(R.id.pricingTypeTextLayout)

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
        setContainerVisibility(R.dimen.kh_uisdk_spacing_medium, VISIBLE)
        pricingTypeTextLayout.setOnClickListener {
            priceInfoLayout.visibility = if (priceInfoLayout.visibility == VISIBLE) GONE else VISIBLE
        }
    }

    fun bindETAOnly(time: Int?, typeEta: String, typePrice: QuoteType) {
        etaText.text = String.format("%s %s", time ?: "~", context.getString(R.string.kh_uisdk_min))
        etaTypeText.text = typeEta
        setContainerVisibility(R.dimen.kh_uisdk_spacing_none, GONE)
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
