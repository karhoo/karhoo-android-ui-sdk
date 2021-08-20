package com.karhoo.uisdk.screen.booking.quotes

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.util.PicassoLoader
import com.karhoo.uisdk.util.extension.getCancellationText
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.intToRangedPrice
import com.squareup.picasso.Callback
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.capacityWidget
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.categoryText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.etaText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.fareTypeText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.loadingIcon
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.logoImage
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.pickupTypeText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.priceText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteNameText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteProgressBar
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteCancellationText
import java.util.Currency

class QuotesListItemView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var itemLayout: Int = R.layout.uisdk_view_quotes_item

    init {
        itemLayout = getListItemLayout(context, attrs, defStyleAttr)
        View.inflate(context, itemLayout, this)
    }

    private fun getListItemLayout(context: Context, attr: AttributeSet?, defStyleAttr: Int): Int {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.QuotesListItem,
                defStyleAttr, R.style.KhQuoteListItemView)
        val layout = typedArray.getResourceId(R.styleable.QuotesListItem_layout, R
                .layout.uisdk_view_quotes_item)
        typedArray.recycle()
        return layout
    }

    fun bind(listPosition: Int,
             vehicleDetails: Quote,
             isPrebook: Boolean,
             itemClickListener: BaseRecyclerAdapter.OnRecyclerItemClickListener<Quote>) {
        startLoading()
        quoteNameText.text = vehicleDetails.fleet.name
        categoryText.text = vehicleDetails.vehicle.vehicleClass?.capitalize()

        loadImage(vehicleDetails.fleet.logoUrl)

        setPrice(vehicleDetails)
        setEta(vehicleDetails.vehicle.vehicleQta.highMinutes, isPrebook)
        setPickupType(vehicleDetails.pickupType)
        setCapacity(vehicleDetails.vehicle)
        setCancellationSLA(vehicleDetails.serviceAgreements?.freeCancellation, isPrebook)

        tag = vehicleDetails

        setOnClickListener { v -> itemClickListener.onRecyclerItemClicked(v, listPosition, vehicleDetails) }
    }

    private fun loadImage(url: String?) {
        PicassoLoader.loadImage(context,
                logoImage,
                url,
                R.drawable.uisdk_ic_quotes_logo_empty,
                R.dimen.logo_size,
                R.integer.logo_radius,
                object : Callback {
                    override fun onSuccess() {
                        stopLoading()
                    }

                    override fun onError(e: java.lang.Exception?) {
                        //Do Nothing
                    }
                })
    }

    @Suppress("NestedBlockDepth")
    private fun setPrice(vehicleDetails: Quote?) {
        vehicleDetails?.let {
            if (it.price.highPrice > 0) {
                if (it.price.currencyCode.isNullOrEmpty()) {
                    priceText.text = ""
                } else {
                    try {
                        val currency = Currency.getInstance(it.price.currencyCode?.trim())
                        priceText.text = currency.formatted(it.price.highPrice)

                        when (vehicleDetails.quoteSource) {
                            QuoteSource.FLEET -> priceText.text = currency.formatted(vehicleDetails.price.highPrice)
                            QuoteSource.MARKET -> priceText.text = currency.intToRangedPrice(
                                    lowPrice = vehicleDetails.price.lowPrice,
                                    highPrice = vehicleDetails.price.highPrice)
                        }
                    } catch (e: Exception) {
                        priceText.text = "??"
                    }
                    fareTypeText.text = getFareType(it.quoteType)
                }
            }
        }
    }

    private fun getFareType(quoteType: QuoteType?): String {
        return when (quoteType ?: QuoteType.ESTIMATED) {
            QuoteType.FIXED -> {
                fareTypeText.setTextColor(ContextCompat.getColor(context, R.color.text_alternative))
                context.getString(R.string.kh_uisdk_fixed_fare)
            }
            QuoteType.METERED -> {
                fareTypeText.setTextColor(ContextCompat.getColor(context, R.color.text_alternative))
                context.getString(R.string.kh_uisdk_metered)
            }
            QuoteType.ESTIMATED -> {
                fareTypeText.setTextColor(ContextCompat.getColor(context, R.color.text_alternative))
                context.getString(R.string.kh_uisdk_estimated_fare)
            }
            else -> context.getString(R.string.kh_uisdk_estimated_fare)
        }
    }

    private fun setEta(etaTime: Int?, isPrebook: Boolean) {
        etaText.visibility = if (isPrebook) View.GONE else View.VISIBLE
        val etaTimeString = etaTime?.toString() ?: "~"
        etaText.text = String.format("%s %s", etaTimeString, context.getString(R.string.kh_uisdk_min))
    }

    private fun setPickupType(pickupType: PickupType?) {
        when (pickupType) {
            PickupType.DEFAULT,
            PickupType.NOT_SET,
            null -> {
                pickupTypeText.visibility = View.GONE
            }
            else -> {
                pickupTypeText.visibility = View.VISIBLE
                pickupTypeText.text = pickupType.toLocalisedString(context.applicationContext)
            }
        }
    }

    private fun setCapacity(vehicle: QuoteVehicle) {
        capacityWidget.setCapacity(
                luggage = vehicle.luggageCapacity,
                people = vehicle.passengerCapacity,
                otherCapabilities = null)
    }

    private fun setCancellationSLA(serviceCancellation: ServiceCancellation?, isPrebook: Boolean) {
        val text = serviceCancellation?.getCancellationText(context, isPrebook)

        if (text.isNullOrEmpty()) {
            quoteCancellationText.visibility = View.GONE
        } else {
            quoteCancellationText.text = text
            quoteCancellationText.visibility = View.VISIBLE
        }
    }

    private fun startLoading() {
        loadingIcon.visibility = View.VISIBLE
        quoteProgressBar.visibility = View.VISIBLE
    }

    private fun stopLoading() {
        loadingIcon.visibility = View.GONE
        quoteProgressBar.visibility = View.GONE
    }
}
