package com.karhoo.uisdk.screen.booking.booking.quotes

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.widget.TextViewCompat
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.LogoTransformation
import com.karhoo.uisdk.util.extension.convertDpToPixels
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.categoryText
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.logoImage
import kotlinx.android.synthetic.main.uisdk_view_booking_quotes.view.quoteNameText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.capacityWidget

class BookingQuotesView @JvmOverloads constructor(context: Context,
                                                  attrs: AttributeSet? = null,
                                                  defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), BookingQuotesMVP.View {

    private var headerTextStyle: Int = R.style.Text_Black_Medium_Bold
    private var detailsTextStyle: Int = R.style.Text_Alternative_XSmall

    init {
        inflate(context, R.layout.uisdk_view_booking_quotes, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingQuotesView,
                                                        defStyleAttr, R.style.KhBookingQuotesView)
        headerTextStyle = typedArray.getResourceId(R.styleable
                                                           .BookingQuotesView_headerText, R
                                                           .style
                                                           .Text_Black_Medium_Bold)
        detailsTextStyle = typedArray.getResourceId(R.styleable.BookingQuotesView_detailsText, R
                .style
                .Text_Alternative_XSmall)
        TextViewCompat.setTextAppearance(quoteNameText, headerTextStyle)
        TextViewCompat.setTextAppearance(categoryText, detailsTextStyle)
    }

    fun bindViews(url: String?, quoteName: String, category: String) {
        url?.let { loadImage(it) }
        quoteNameText.text = quoteName
        categoryText.text = String.format("%s%s",
                                          category.substring(0, 1).toUpperCase(),
                                          category.substring(1))
    }

    private fun loadImage(url: String) {
        val logoSize = resources.getDimension(R.dimen.logo_size).convertDpToPixels()

        val picasso = Picasso.with(context)
        val creator: RequestCreator

        creator = if (url.isNotBlank()) {
            picasso.load(url)
        } else {
            picasso.load(R.drawable.uisdk_ic_quotes_logo_empty)
        }

        creator.placeholder(R.drawable.uisdk_ic_quotes_logo_empty)
                .resize(logoSize, logoSize)
                .transform(LogoTransformation(resources.getInteger(R.integer.logo_radius)))
                .into(logoImage)
    }

    override fun setCapacity(luggage: Int, people: Int) {
        capacityWidget.setCapacity(luggage, people)
    }
}
