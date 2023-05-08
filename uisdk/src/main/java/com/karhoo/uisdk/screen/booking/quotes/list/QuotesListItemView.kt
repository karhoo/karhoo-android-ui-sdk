package com.karhoo.uisdk.screen.booking.quotes.list

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.FleetRating
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseRecyclerAdapter
import com.karhoo.uisdk.screen.booking.domain.quotes.VehicleMappingsProvider
import com.karhoo.uisdk.screen.booking.quotes.filterview.VehicleClassFilter
import com.karhoo.uisdk.util.PicassoLoader
import com.karhoo.uisdk.util.extension.getCorrespondingLogoMapping
import com.karhoo.uisdk.util.extension.logoImageTag
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.intToRangedPrice
import com.squareup.picasso.Callback
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.capacityWidget
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.categoryText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.etaText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.fareTypeText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.loadingIcon
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.logoImage
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.priceText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteNameText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteProgressBar
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.detailsButton
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteFleetRating
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.quoteFleetRatingLayout
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.logoImageSmall
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.driverArrivalText
import kotlinx.android.synthetic.main.uisdk_view_quotes_item.view.logoBadgeImage
import java.util.Currency

class QuotesListItemView @JvmOverloads constructor(
    context: Context,
    private var attrs: AttributeSet? = null,
    private var defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var itemLayout: Int = R.layout.uisdk_view_quotes_item
    private var isPrebook: Boolean = false

    private fun getListItemLayout(context: Context, attr: AttributeSet?, defStyleAttr: Int): Int {
        val typedArray = context.obtainStyledAttributes(
            attr, R.styleable.QuotesListItem,
            defStyleAttr, R.style.KhQuoteListItemView
        )
        val layout = if (!isPrebook) typedArray.getResourceId(
            R.styleable.QuotesListItem_layout, R
                .layout.uisdk_view_quotes_item
        ) else R.layout.uisdk_view_quotes_item_prebook
        typedArray.recycle()
        return layout
    }

    fun bind(
        listPosition: Int,
        vehicleDetails: Quote,
        isPrebook: Boolean,
        itemClickListener: BaseRecyclerAdapter.OnRecyclerItemClickListener<Quote>
    ) {

        this.isPrebook = isPrebook
        itemLayout = getListItemLayout(context, attrs, defStyleAttr)
        View.inflate(context, itemLayout, this)

        startLoading()
        quoteNameText.text = vehicleDetails.fleet.name

        val logoImageRule =  VehicleMappingsProvider.getVehicleMappings()?.let {
            vehicleDetails.vehicle.getCorrespondingLogoMapping(it)
        }

        val logoImageUrl = logoImageRule?.vehicleImagePNG ?: vehicleDetails.fleet.logoUrl
        loadImages(logoImageUrl , vehicleDetails.fleet.logoUrl)

        val logoImageTag = vehicleDetails.vehicle.logoImageTag(context, logoImageRule)
        logoImage.contentDescription = resources.getString(R.string.kh_uisdk_accessibility_label_quote_card) + " " + logoImageTag

        val vehicleTags = vehicleDetails.vehicle.vehicleTags.map { it.lowercase() }
        val badgeDrawableRes = when {
            vehicleTags.contains("electric") -> R.drawable.kh_uisdk_electric
            vehicleTags.contains("hybrid") -> R.drawable.kh_uisdk_hybrid
            vehicleTags.contains("economy") -> R.drawable.kh_uisdk_economy
            else -> null // Handle the case when no badge needs to be shown
        }

        if (badgeDrawableRes != null) {
            logoBadgeImage.setImageDrawable(ContextCompat.getDrawable(context, badgeDrawableRes))
        }

        setCategoryText(vehicleDetails.vehicle)
        setPrice(vehicleDetails)
        setDriverArrival(isPrebook)
        setEta(vehicleDetails.vehicle.vehicleQta.highMinutes, isPrebook)
        setCapacity(vehicleDetails.vehicle)
        setRating(vehicleDetails.fleet.rating)
        detailsButton?.visibility = View.INVISIBLE

        tag = vehicleDetails

        setOnClickListener { v ->
            itemClickListener.onRecyclerItemClicked(
                v,
                listPosition,
                vehicleDetails
            )
        }
    }

    private fun setDriverArrival(isPrebook: Boolean) {
        driverArrivalText?.visibility = if (isPrebook) View.GONE else View.VISIBLE
    }

    private fun setRating(rating: FleetRating?) {
        quoteFleetRating.text = " ${rating?.score}/5(${rating?.count})"
        quoteFleetRatingLayout.visibility = View.GONE
    }

    private fun setCategoryText(vehicle: QuoteVehicle) {
        var textToDisplay = if (vehicle.vehicleTags.contains(VehicleClassFilter.EXECUTIVE)) {
            resources.getString(R.string.kh_uisdk_filter_executive)
        } else if (vehicle.vehicleTags.contains(VehicleClassFilter.LUXURY)) {
            resources.getString(R.string.kh_uisdk_filter_luxury)
        } else if (vehicle.vehicleTags.contains(VehicleClassFilter.NORMAL)) {
            resources.getString(R.string.kh_uisdk_filter_standard)
        } else {
            "${vehicle.vehicleType}"
        }
        categoryText.text = textToDisplay.replaceFirstChar { it.uppercase() }
    }

    private fun loadImages(url: String?, fleetUrl: String?) {
        PicassoLoader.loadImage(context,
            logoImage,
            url,
            R.drawable.uisdk_ic_quotes_logo_empty,
            R.dimen.kh_uisdk_driver_photo_size,
            R.integer.kh_uisdk_logo_radius,
            object : Callback {
                override fun onSuccess() {
                    stopLoading()
                }

                override fun onError(e: java.lang.Exception?) {
                    //Do Nothing
                }
            })

        PicassoLoader.loadImage(context,
            logoImageSmall,
            fleetUrl,
            R.drawable.uisdk_ic_quotes_logo_empty,
            R.dimen.kh_uisdk_spacing_small,
            R.integer.kh_uisdk_logo_radius,
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
                            QuoteSource.FLEET -> priceText.text =
                                currency.formatted(vehicleDetails.price.highPrice)
                            QuoteSource.MARKET -> priceText.text = currency.intToRangedPrice(
                                lowPrice = vehicleDetails.price.lowPrice,
                                highPrice = vehicleDetails.price.highPrice
                            )
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
                context.getString(R.string.kh_uisdk_fixed_fare)
            }
            QuoteType.METERED -> {
                context.getString(R.string.kh_uisdk_metered)
            }
            QuoteType.ESTIMATED -> {
                context.getString(R.string.kh_uisdk_estimated_fare)
            }
            else -> context.getString(R.string.kh_uisdk_estimated_fare)
        }
    }

    private fun setEta(etaTime: Int?, isPrebook: Boolean) {
        etaText?.visibility = if (isPrebook) View.GONE else View.VISIBLE
        val etaTimeString = etaTime?.toString() ?: "~"
        etaText?.text =
            String.format("%s %s", etaTimeString, context.getString(R.string.kh_uisdk_min))
    }

    private fun setCapacity(vehicle: QuoteVehicle) {
        capacityWidget.setCapacity(
            luggage = vehicle.luggageCapacity,
            people = vehicle.passengerCapacity,
            otherCapabilities = null
        )
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
