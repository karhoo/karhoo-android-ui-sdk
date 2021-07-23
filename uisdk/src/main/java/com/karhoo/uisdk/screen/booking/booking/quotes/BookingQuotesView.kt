package com.karhoo.uisdk.screen.booking.booking.quotes

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.PicassoLoader
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.*
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.capacityWidget

class BookingQuotesView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), BookingQuotesMVP.View {

    private val presenter: BookingQuotesMVP.Presenter = BookingQuotesPresenter(this)

    init {
        inflate(context, R.layout.uisdk_view_booking_quotes, this)
    }

    fun bindViews(url: String?, quoteName: String, category: String, serviceCancellation: ServiceCancellation?, isPrebook: Boolean) {
        quoteNameText.text = quoteName
        presenter.capitalizeCategory(category)
        presenter.checkCancellationSLAMinutes(context, serviceCancellation, isPrebook)
        loadImage(url)
    }

    override fun setCancellationText(text: String) {
        bookingQuoteCancellationText.text = text
    }

    override fun showCancellationText(show: Boolean) = if (show) {
        bookingQuoteCancellationText.visibility = VISIBLE
    } else {
        bookingQuoteCancellationText.visibility = GONE
    }

    override fun setCategoryText(text: String) {
        categoryText.text = text
    }

    private fun loadImage(url: String?) {
        PicassoLoader.loadImage(context,
                logoImage,
                url,
                R.drawable.uisdk_ic_quotes_logo_empty,
                R.dimen.logo_size,
                R.integer.logo_radius)
    }

    override fun setCapacity(luggage: Int, people: Int) {
        capacityWidget.setCapacity(luggage, people)
    }
}
