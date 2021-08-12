package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.intToRangedPrice
import java.util.Currency

class BookingPriceViewPresenter : BookingPriceViewContract.Presenter {
    private lateinit var view: BookingPriceViewContract.View

    override fun attachView(view: BookingPriceViewContract.View) {
        this.view = view
    }

    override fun formatPriceText(quote: Quote, currency: Currency) {
        val quotePrice = when (quote.quoteSource) {
            QuoteSource.FLEET -> {
                currency.formatted(quote.price.highPrice)
            }
            QuoteSource.MARKET -> {
                currency.intToRangedPrice(quote.price.lowPrice, quote.price.highPrice)
            }
        }

        view.setPriceText(quotePrice)
    }

    override fun formatPickUpType(quote: Quote) {
        val pickUpType = when (quote.pickupType) {
            PickupType.MEET_AND_GREET -> {
                view.getString(R.string.kh_uisdk_price_meet_and_greet)
            }
            PickupType.CURBSIDE,
            PickupType.STANDBY,
            PickupType.NOT_SET,
            PickupType.DEFAULT -> null
            else -> null
        }

        view.setPickUpType(pickUpType)
    }

    override fun formatPricingType(quote: Quote) {
        val infoText: String
        val pricingType: String

        when (quote.quoteType) {
            QuoteType.ESTIMATED -> {
                pricingType = view.getString(R.string.kh_uisdk_estimated_fare)
                infoText = view.getString(R.string.kh_uisdk_price_info_text_estimated)
            }
            QuoteType.FIXED -> {
                pricingType = view.getString(R.string.kh_uisdk_fixed_fare)
                infoText = view.getString(R.string.kh_uisdk_price_info_text_fixed)
            }
            QuoteType.METERED -> {
                pricingType = view.getString(R.string.kh_uisdk_metered)
                infoText = view.getString(R.string.kh_uisdk_price_info_text_metered)
            }
        }

        view.setPricingType(pricingType)
        view.setInfoText(infoText)
    }

}
