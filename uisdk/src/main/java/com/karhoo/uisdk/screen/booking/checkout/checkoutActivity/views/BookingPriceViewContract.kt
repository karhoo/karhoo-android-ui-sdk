package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import java.util.Currency

interface BookingPriceViewContract {
    interface View {
        fun setPriceText(price: String)
        fun setPickUpType(pickUpType: PickupType?)
        fun setQuoteTypeDetails(quoteType: QuoteType)
        fun getString(id: Int): String
    }

    interface Presenter {
        fun attachView(view: View)
        fun formatPriceText(quote: Quote, currency: Currency)
        fun formatPickUpType(quote: Quote)
        fun formatQuoteType(quote: Quote)
    }
}
