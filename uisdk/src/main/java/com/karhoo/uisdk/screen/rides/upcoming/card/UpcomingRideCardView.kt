package com.karhoo.uisdk.screen.rides.upcoming.card

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.screen.trip.TripActivity
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.IntentUtils
import com.karhoo.uisdk.util.extension.classToLocalisedString
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.uisdk_view_upcoming_ride_card.view.*
import org.joda.time.DateTime

class UpcomingRideCardView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), UpcomingRideCardMVP.View {

    private var presenter: UpcomingRideCardMVP.Presenter? = null

    init {
        inflate(context, R.layout.uisdk_view_upcoming_ride_card, this)
    }

    fun bind(trip: TripInfo) {
        presenter = UpcomingRideCardPresenter(
                this,
                trip,
                ScheduledDateViewBinder(),
                KarhooUISDK.analytics,
                context)

        loadFleetLogo(trip)

        bookingTermsText.text = trip.fleetInfo?.name.orEmpty()
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
            carText.text = "${trip.vehicle?.classToLocalisedString()}${trip.vehicle?.vehicleLicencePlate}"
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
