package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views

import com.karhoo.sdk.api.model.Quote
import java.util.Currency

interface BookingPriceViewContract {
    interface View {
        fun setInfoText(text: String)
        fun setPriceText(price: String)
        fun setPickUpType(pickUpType: String?)
        fun setPricingType(pricingType: String)
        fun getString(id: Int): String
    }

    interface Presenter {
        fun attachView(view: View)
        fun formatPriceText(quote: Quote, currency: Currency)
        fun formatPickUpType(quote: Quote)
        fun formatPricingType(quote: Quote)
    }
}
