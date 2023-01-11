package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.intToRangedPrice
import java.util.*

class BottomPriceViewPresenter : BookingPriceViewContract.BottomViewPresenter {
    private lateinit var view: BookingPriceViewContract.BottomView

    override fun attachView(view: BookingPriceViewContract.BottomView) {
        this.view = view
    }

    override fun formatPriceText(quote: Quote, currency: Currency, locale: Locale) {
        val quotePrice = when (quote.quoteSource) {
            QuoteSource.FLEET -> {
                currency.formatted(quote.price.highPrice, locale = locale)
            }
            QuoteSource.MARKET -> {
                currency.intToRangedPrice(
                    quote.price.lowPrice,
                    quote.price.highPrice,
                    locale = locale
                )
            }
        }

        view.setPriceText(quotePrice)
    }

    override fun formatPriceType(quote: Quote, context: Context) {
        view.setPriceType(quote.quoteType.toLocalisedString(context).uppercase(Locale.getDefault()))
    }
}
