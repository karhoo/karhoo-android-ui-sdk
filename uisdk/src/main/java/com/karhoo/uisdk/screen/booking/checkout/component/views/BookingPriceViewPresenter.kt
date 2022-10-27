package com.karhoo.uisdk.screen.booking.checkout.component.views

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.intToRangedPrice
import java.util.Currency
import java.util.Locale

class BookingPriceViewPresenter : BookingPriceViewContract.Presenter {
    private lateinit var view: BookingPriceViewContract.View

    override fun attachView(view: BookingPriceViewContract.View) {
        this.view = view
    }

    override fun formatPriceText(quote: Quote, currency: Currency, locale: Locale) {
        val quotePrice = when (quote.quoteSource) {
            QuoteSource.FLEET -> {
                currency.formatted(quote.price.highPrice, locale = locale)
            }
            QuoteSource.MARKET -> {
                currency.intToRangedPrice(quote.price.lowPrice, quote.price.highPrice, locale = locale)
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
