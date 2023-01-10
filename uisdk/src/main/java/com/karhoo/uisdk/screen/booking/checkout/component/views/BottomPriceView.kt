package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.quotes.sortview.QuotesSortView
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceText
import kotlinx.android.synthetic.main.uisdk_view_booking_time_price.view.priceTypeText
import java.util.*

class BottomPriceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), BookingPriceViewContract.BottomView {
    private var presenter: BookingPriceViewContract.BottomViewPresenter

    init {
        inflate(context, R.layout.uisdk_view_bottom_price_view, this)

        presenter = BottomPriceViewPresenter()
        presenter.attachView(this)
    }


    override fun getString(id: Int): String {
        return context.getString(id)
    }

    override fun bindViews(
        quote: Quote,
        currency: Currency
    ) {
        presenter.formatPriceText(quote, currency)
        presenter.formatPriceType(quote, context)

        priceLayout?.setOnClickListener {
            (context as FragmentActivity).supportFragmentManager.let {
                val bottomPriceViewSheet = BottomPriceViewSheet(quote.quoteType)
                bottomPriceViewSheet.show(it, QuotesSortView.TAG)
            }
        }
    }

    override fun setPriceText(price: String) {
        priceText.text = price
    }

    override fun setPriceType(priceType: String) {
        priceTypeText.text = priceType
    }
}
