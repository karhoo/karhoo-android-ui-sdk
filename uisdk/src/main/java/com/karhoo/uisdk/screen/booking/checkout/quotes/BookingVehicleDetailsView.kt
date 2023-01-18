package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.PicassoLoader
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.uisdk_view_vehicle_details.view.*
import java.util.*

class BookingVehicleDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BookingVehicleDetailsMVP.View {

    private val presenter: BookingVehicleDetailsMVP.Presenter = BookingVehicleDetailsPresenter(this)
    private var isExpandedSectionShown = false

    init {
        inflate(context, R.layout.uisdk_view_vehicle_details, this)
    }

    fun bindViews(
        url: String?,
        fleetName: String,
        type: String?,
        serviceCancellation: ServiceCancellation?,
        vehicleFleetLogoUrl: String?,
        tags: List<String>,
        isPrebook: Boolean
    ) {
        vehicleType.text = if (tags.contains(EXECUTIVE_TAG)) {
            resources.getString(R.string.kh_uisdk_filter_executive)
        } else if (tags.contains(LUXURY_TAG)) {
            resources.getString(R.string.kh_uisdk_filter_luxury)
        } else {
            type?.capitalize(Locale.ROOT)
        }

        presenter.checkCancellationSLAMinutes(context, serviceCancellation, isPrebook)
        loadImage(url)

        vehicleFleet.text = fleetName

        Picasso.get().load(vehicleFleetLogoUrl).into(vehicleLogo)

        quoteLearnMoreContainer.setOnClickListener {
            isExpandedSectionShown = !isExpandedSectionShown

            val arrowIcon = if (isExpandedSectionShown)
                getDrawableResource(R.drawable.kh_uisdk_ic_arrow_up_small)
            else
                getDrawableResource(R.drawable.kh_uisdk_ic_arrow_down_small)

            quoteLearnMoreIcon.setImageDrawable(arrowIcon)
        }
    }

    override fun setCancellationText(text: String) {
        bookingQuoteCancellationText.text = text
    }

    override fun showCancellationText(show: Boolean) = if (show) {
        bookingQuoteCancellationText.visibility = VISIBLE
    } else {
        bookingQuoteCancellationText.visibility = GONE
    }

    private fun loadImage(url: String?) {
        PicassoLoader.loadImage(
            context,
            logoImage,
            url,
            R.drawable.uisdk_ic_quotes_logo_empty,
            R.dimen.kh_uisdk_logo_size,
            R.integer.kh_uisdk_logo_radius
        )
    }

    override fun setCapacity(luggage: Int?, people: Int?, capabilitiesCount: Int) {
        capacityWidget.setCapacity(luggage, people, capabilitiesCount)
    }

    override fun getDrawableResource(id: Int): Drawable? {
        return ResourcesCompat.getDrawable(resources, id, null)
    }

    companion object {
        private const val LUXURY_TAG = "luxury"
        private const val EXECUTIVE_TAG = "executive"
    }
}
