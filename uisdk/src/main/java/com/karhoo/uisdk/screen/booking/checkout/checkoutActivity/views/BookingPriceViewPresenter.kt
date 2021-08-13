package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
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
        view.setPickUpType(quote.pickupType)
    }

    override fun formatQuoteType(quote: Quote) {
        view.setQuoteTypeDetails(quote.quoteType)
    }

}
