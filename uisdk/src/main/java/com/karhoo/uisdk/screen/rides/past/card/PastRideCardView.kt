package com.karhoo.uisdk.screen.rides.past.card

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.categoryToLocalisedString
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class PastRideCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), PastRideCardMVP.View {
    private var presenter: PastRideCardMVP.Presenter? = null

    private lateinit var khTermsAndConditionsText: TextView
    private lateinit var carText: TextView
    private lateinit var dateTimeText: TextView
    private lateinit var dropOffLabel: TextView
    private lateinit var logoImage: ImageView
    private lateinit var pickupLabel: TextView
    private lateinit var pickupTypeText: TextView
    private lateinit var priceText: TextView
    private lateinit var stateIcon: ImageView
    private lateinit var stateText: TextView

    init {
        inflate(context, R.layout.uisdk_view_past_ride_card, this)

        khTermsAndConditionsText = findViewById(R.id.khTermsAndConditionsText)
        carText = findViewById(R.id.carText)
        dateTimeText = findViewById(R.id.dateTimeText)
        dropOffLabel = findViewById(R.id.dropOffLabel)
        logoImage = findViewById(R.id.logoImage)
        pickupLabel = findViewById(R.id.pickupLabel)
        pickupTypeText = findViewById(R.id.pickupTypeText)
        priceText = findViewById(R.id.priceText)
        stateIcon = findViewById(R.id.stateIcon)
        stateText = findViewById(R.id.stateText)
    }

    fun bind(trip: TripInfo) {

        presenter = PastRideCardPresenter(this, ScheduledDateViewBinder(), trip)

        loadFleetLogo(trip)

        khTermsAndConditionsText.text = trip.fleetInfo?.name
        pickupLabel.text = trip.origin?.displayAddress
        dropOffLabel.text = trip.destination?.displayAddress

        handleCarVisibility(trip)

        trip.meetingPoint?.pickupType?.let {
            bindPickupType(it)
        }

        setOnClickListener { presenter?.selectDetails() }
        presenter?.apply {
            bindState()
            bindPrice()
            bindDate()
        }
    }

    private fun loadFleetLogo(trip: TripInfo) {
        if (trip.fleetInfo?.logoUrl.isNullOrBlank()) {
            Picasso.get()
                    .load(R.drawable.uisdk_ic_quotes_logo_empty)
                    .into(logoImage)
        } else {
            Picasso.get()
                    .load(trip.fleetInfo?.logoUrl)
                    .into(logoImage)
        }
    }

    private fun bindPickupType(pickupType: PickupType) {
        when (pickupType) {
            PickupType.DEFAULT,
            PickupType.NOT_SET -> {
                pickupTypeText.visibility = View.GONE
            }
            else -> {
                pickupTypeText.visibility = View.VISIBLE
                pickupTypeText.text = pickupType.toLocalisedString(context.applicationContext)
            }
        }
    }

    private fun handleCarVisibility(trip: TripInfo) =
            if (trip.vehicle?.vehicleLicencePlate.isNullOrBlank()) {
                carText.visibility = View.GONE
            } else {
                carText.visibility = View.VISIBLE
                carText.text = "${trip.vehicle?.categoryToLocalisedString(this.context)}: ${trip.vehicle?.vehicleLicencePlate}"
            }

    override fun goToDetails(trip: TripInfo) {
        val intent = RideDetailActivity.Builder.newBuilder()
                .trip(trip)
                .build(context)
        context.startActivity(intent)
    }

    override fun displayDate(date: DateTime) {
        dateTimeText.text = DateUtil.getDateAndTimeFormat(context, date)
    }

    override fun displayNoDateAvailable() {
        dateTimeText.setText(R.string.kh_uisdk_pending)
    }

    override fun displayState(@DrawableRes icon: Int, @StringRes state: Int, @ColorRes color: Int) {
        stateIcon.setImageResource(icon)
        this.stateText.setTextColor(ContextCompat.getColor(context, color))
        this.stateText.setText(state)
    }

    override fun displayPricePending() {
        priceText.setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_label))
        priceText.setText(R.string.kh_uisdk_cancelled)
    }

    override fun displayPrice(price: String) {
        this.priceText.setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_text))
        this.priceText.text = price
    }

}
