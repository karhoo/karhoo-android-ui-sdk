package com.karhoo.uisdk.screen.booking.booking.quotes

import android.content.Context
import com.karhoo.uisdk.R
import java.util.*

class BookingQuotesPresenter(val view: BookingQuotesMVP.View) : BookingQuotesMVP.Presenter {
    override fun checkCancellationSLAMinutes(minutes: Int?, context: Context) {
        if (minutes != null && minutes > 0) {
            view.setCancellationText(String.format(context.getString(R.string.uisdk_quote_cancellation_minutes), minutes))
            view.showCancellationText(true)
        } else {
            view.showCancellationText(false)
        }
    }

    override fun capitalizeCategory(category: String) {
        view.setCategoryText(category.capitalize(Locale.getDefault()))
    }
}
