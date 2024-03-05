package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.quotes.capacity.CapacityView
import com.karhoo.uisdk.util.PicassoLoader
import com.squareup.picasso.Picasso
import java.util.*

class BookingVehicleDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BookingVehicleDetailsMVP.View {

    private val presenter: BookingVehicleDetailsMVP.Presenter = BookingVehicleDetailsPresenter(this)
    private var isExpandedSectionShown = false

    private lateinit var vehicleType: TextView
    private lateinit var logoImage: ImageView
    private lateinit var vehicleLogo: ImageView
    private lateinit var quoteLearnMoreIcon: ImageView
    private lateinit var vehicleFleet: TextView
    private lateinit var bookingQuoteCancellationText: TextView
    private lateinit var quoteLearnMoreContainer: LinearLayout
    private lateinit var capacityWidget: CapacityView

    init {
        inflate(context, R.layout.uisdk_view_vehicle_details, this)

        vehicleType = findViewById(R.id.vehicleType)
        logoImage = findViewById(R.id.logoImage)
        vehicleLogo = findViewById(R.id.vehicleLogo)
        quoteLearnMoreIcon = findViewById(R.id.quoteLearnMoreIcon)
        vehicleFleet = findViewById(R.id.vehicleFleet)
        bookingQuoteCancellationText = findViewById(R.id.bookingQuoteCancellationText)
        quoteLearnMoreContainer = findViewById(R.id.quoteLearnMoreContainer)
        capacityWidget = findViewById(R.id.capacityWidget)
    }
    @Suppress("LongParameterList")
    fun bindViews(
        url: String?,
        vehicleImageContentDescription: String?,
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
        logoImage.contentDescription = vehicleImageContentDescription

        vehicleFleet.text = fleetName
        vehicleFleet.contentDescription = resources.getString(R.string.kh_uisdk_accessibility_label_fleet_name) + " " + fleetName

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
