package com.karhoo.uisdk.screen.booking.booking.quotes

import android.content.Context
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.util.extension.getCancellationText
import java.util.*

class BookingQuotesPresenter(val view: BookingQuotesMVP.View) : BookingQuotesMVP.Presenter {
    override fun checkCancellationSLAMinutes(serviceCancellation: ServiceCancellation?, context: Context) {
        val text = serviceCancellation?.getCancellationText(context)

        if (text.isNullOrEmpty()) {
            view.showCancellationText(false)
        } else {
            view.setCancellationText(text)
            view.showCancellationText(true)
        }
    }

    override fun capitalizeCategory(category: String) {
        view.setCategoryText(category.capitalize(Locale.getDefault()))
    }
}
