package com.karhoo.uisdk.screen.rides.upcoming.card

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.AttrRes
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.IntentUtils
import com.karhoo.uisdk.util.extension.categoryToLocalisedString
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.squareup.picasso.Picasso
import org.joda.time.DateTime

class UpcomingRideCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), UpcomingRideCardMVP.View {

    private var presenter: UpcomingRideCardMVP.Presenter? = null

    private lateinit var khTermsAndConditionsText: TextView
    private lateinit var pickupLabel: TextView
    private lateinit var dropOffLabel: TextView
    private lateinit var callButton: Button
    private lateinit var trackButton: Button
    private lateinit var dateTimeText: TextView
    private lateinit var logoImage: ImageView
    private lateinit var pickupTypeText: TextView
    private lateinit var carText: TextView
    private lateinit var upcomingRideCancellationText: TextView

    init {
        inflate(context, R.layout.uisdk_view_upcoming_ride_card, this)

        khTermsAndConditionsText = findViewById(R.id.khTermsAndConditionsText)
        pickupLabel = findViewById(R.id.pickupLabel)
        dropOffLabel = findViewById(R.id.dropOffLabel)
        callButton = findViewById(R.id.callButton)
        trackButton = findViewById(R.id.trackButton)
        dateTimeText = findViewById(R.id.dateTimeText)
        logoImage = findViewById(R.id.logoImage)
        pickupTypeText = findViewById(R.id.pickupTypeText)
        carText = findViewById(R.id.carText)
        upcomingRideCancellationText = findViewById(R.id.upcomingRideCancellationText)
    }

    fun bind(trip: TripInfo) {
        presenter = UpcomingRideCardPresenter(
                this,
                trip,
                ScheduledDateViewBinder(),
                KarhooUISDK.analytics,
                context)

        loadFleetLogo(trip)

        khTermsAndConditionsText.text = trip.fleetInfo?.name.orEmpty()
        pickupLabel.text = trip.origin?.displayAddress
        dropOffLabel.text = trip.destination?.displayAddress

        handleCarVisibility(trip)

        trip.meetingPoint?.pickupType?.let {
            bindPickupType(it)
        }

        callButton.setOnClickListener { presenter?.call() }
        trackButton.setOnClickListener { presenter?.track() }
        setOnClickListener { presenter?.selectDetails() }

        presenter?.bindDate()

        if(KarhooUISDKConfigurationProvider.configuration.disableCallDriverOrFleetFeature())
            callButton.visibility = View.GONE
    }

    override fun displayDate(date: DateTime) {
        dateTimeText.text = DateUtil.getDateAndTimeFormat(context, date)
    }

    override fun displayNoDateAvailable() {
        dateTimeText.setText(R.string.kh_uisdk_pending)
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

    private fun handleCarVisibility(trip: TripInfo) {
        if (trip.vehicle?.vehicleLicencePlate.isNullOrBlank()) {
            carText.visibility = View.INVISIBLE
        } else {
            carText.visibility = View.VISIBLE
            carText.text = "${trip.vehicle?.categoryToLocalisedString(this.context)}: ${trip.vehicle?.vehicleLicencePlate}"
        }
    }

    override fun callFleet(number: String) {
        IntentUtils.dialIntent(number)?.let {
            context.startActivity(it)
        }
    }

    override fun callText(contactText: Int) {
        callButton.setText(contactText)
    }

    override fun trackTrip(trip: TripInfo) {
        context.startActivity(TripActivity.Builder.builder.tripInfo(trip).build(context))
    }

    override fun goToDetails(trip: TripInfo) {
        val intent = RideDetailActivity.Builder.newBuilder()
                .trip(trip)
                .build(context)
        context.startActivity(intent)
    }

    override fun displayTrackDriverButton() {
        trackButton.visibility = View.VISIBLE
    }

    override fun hideTrackDriverButton() {
        trackButton.visibility = View.GONE
    }

    override fun setCancellationText(text: String) {
        upcomingRideCancellationText.text = text
    }

    override fun showCancellationText(show: Boolean) {
        upcomingRideCancellationText.visibility = if (show) View.VISIBLE else View.GONE
    }
}
