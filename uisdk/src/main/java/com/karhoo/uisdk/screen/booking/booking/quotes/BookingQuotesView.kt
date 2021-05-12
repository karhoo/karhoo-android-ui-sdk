package com.karhoo.uisdk.screen.booking.booking.quotes

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.TextViewCompat
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.PicassoLoader
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.*
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.capacityWidget

class BookingQuotesView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), BookingQuotesMVP.View {

    private var headerTextStyle: Int = R.style.Text_Black_Medium_Bold
    private var detailsTextStyle: Int = R.style.Text_Alternative_XSmall
    private val presenter: BookingQuotesMVP.Presenter = BookingQuotesPresenter(this)

    init {
        inflate(context, R.layout.uisdk_view_booking_quotes, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingQuotesView,
                defStyleAttr, R.style.KhBookingQuotesView)
        headerTextStyle = typedArray.getResourceId(R.styleable.BookingQuotesView_headerText,
                R.style.Text_Black_Medium_Bold)
        detailsTextStyle = typedArray.getResourceId(R.styleable.BookingQuotesView_detailsText,
                R.style.Text_Alternative_XSmall)
        TextViewCompat.setTextAppearance(quoteNameText, headerTextStyle)
        TextViewCompat.setTextAppearance(categoryText, detailsTextStyle)
    }

    fun bindViews(url: String?, quoteName: String, category: String, serviceCancellation: ServiceCancellation?) {
        quoteNameText.text = quoteName
        presenter.capitalizeCategory(category)
        presenter.checkCancellationSLAMinutes(serviceCancellation, context)
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
