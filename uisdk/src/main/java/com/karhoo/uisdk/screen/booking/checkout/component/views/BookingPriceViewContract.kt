package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
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

    interface BottomView {
        fun bindViews(vehicle: Quote,
                      currency: Currency)
        fun setPriceText(price: String)
        fun setPriceType(priceType: String)
        fun getString(id: Int): String
    }

    interface BottomViewPresenter {
        fun attachView(view: BottomView)
        fun formatPriceType(quote: Quote, context: Context)
        fun formatPriceText(quote: Quote, currency: Currency, locale: Locale = Locale.getDefault())
    }
}
