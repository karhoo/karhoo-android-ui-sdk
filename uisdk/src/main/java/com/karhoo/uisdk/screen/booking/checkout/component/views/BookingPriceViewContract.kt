package com.karhoo.uisdk.screen.booking.checkout.component.views

import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import java.util.Currency
import java.util.Locale

interface BookingPriceViewContract {
    interface View {
        fun setPriceText(price: String)
        fun setPickUpType(pickUpType: PickupType?)
        fun setQuoteTypeDetails(quoteType: QuoteType)
        fun getString(id: Int): String
    }

    interface Presenter {
        fun attachView(view: View)
        fun formatPriceText(quote: Quote, currency: Currency, locale: Locale = Locale.getDefault())
        fun formatPickUpType(quote: Quote)
        fun formatQuoteType(quote: Quote)
    }
}
