package com.karhoo.uisdk.screen.trip

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusActions
import com.karhoo.uisdk.screen.trip.map.TripMapMVP
import kotlinx.android.synthetic.main.uisdk_activity_trip.bookingStatusWidget
import kotlinx.android.synthetic.main.uisdk_activity_trip.toolbar
import kotlinx.android.synthetic.main.uisdk_activity_trip.tripAddressWidget
import kotlinx.android.synthetic.main.uisdk_activity_trip.tripMapWidget
import kotlinx.android.synthetic.main.uisdk_view_trip_info.detaWidget
import kotlinx.android.synthetic.main.uisdk_view_trip_info.etaWidget
import kotlinx.android.synthetic.main.uisdk_view_trip_info.locateMeButton

class TripActivity : BaseActivity(), BookingStatusActions, TripMapMVP.Actions {

    private var trip: TripInfo? = null
    private var backToBooking: Boolean = false

    override val layout = R.layout.uisdk_activity_trip

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = null
        }
        lifecycle.apply {
            addObserver(tripMapWidget)
            addObserver(etaWidget)
            addObserver(detaWidget)
            addObserver(bookingStatusWidget)
        }
        toolbar.setNavigationOnClickListener { goToCleanBooking() }
        tripMapWidget.onCreate(savedInstanceState ?: Bundle())
    }

    override fun onResume() {
        super.onResume()
        if (TripStatus.tripEnded(trip?.tripState)) {
            goToCleanBooking()
        } else {
            tripMapWidget.actions = this
            val tripIdentifier = if (KarhooUISDKConfigurationProvider.isGuest()) trip?.followCode
                    .orEmpty() else trip?.tripId.orEmpty()
            etaWidget.monitorEta(tripIdentifier)
            detaWidget.monitorDeta(tripIdentifier, trip?.origin?.timezone.orEmpty())
            bookingStatusWidget.monitorTrip(tripIdentifier)
            tripMapWidget.trackDriver(tripIdentifier)
        }
    }

    override fun onBackPressed() {
        goToCleanBooking()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tripMapWidget.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        tripMapWidget.onLowMemory()
    }

    override fun handleExtras() {
        extras?.let {
            trip = it.getParcelable(Builder.EXTRA_TRIP)
            backToBooking = it.getBoolean(Builder.EXTRA_BACK_TO_BOOKING)
        }
    }

    override fun initialiseViews() {
        // Do nothing
    }

    override fun initialiseViewListeners() {
        locateMeButton?.setOnClickListener { tripMapWidget.locateMe() }
    }

    override fun bindViews() {
        super.bindViews()
        bookingStatusWidget.actions = this
        tripAddressWidget.bindTripPickupAndDropoff(trip)

        val originPosition = trip?.origin?.position
        val destinationPosition = trip?.destination?.position
        if (originPosition != null && destinationPosition != null) {
            tripMapWidget.bindPickupAndDropOffLocations(originPosition, destinationPosition)
        }
    }

    override fun goToCleanBooking() {
        if(backToBooking) {
            startActivity(BookingActivity.Builder.builder.build(this))
        } else {
            finish()
        }
    }

    override fun goToPrefilledBooking(trip: TripInfo) {
        startActivity(BookingActivity.Builder.builder.tripDetails(trip).build(this))
    }

    override fun gotoRideDetails(trip: TripInfo) {
        goToCleanBooking()
        startActivity(RideDetailActivity.Builder.newBuilder().trip(trip).build(this))
    }

    override fun updateRideDetails(trip: TripInfo) {
        this.trip = trip
    }

    /**
     * Intent Builder
     */
    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        /**
         * The activity will take the origin and destination, if available from [tripInfo],
         * use this to prepopulate the addressview and begin fetching quotes
         */
        fun tripInfo(trip: TripInfo, backToBooking: Boolean = false): Builder {
            extras.putParcelable(EXTRA_TRIP, trip)
            extras.putBoolean(EXTRA_BACK_TO_BOOKING, backToBooking)
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, KarhooUISDK.Routing.trip)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtras(extras)
            return intent
        }

        companion object {

            const val EXTRA_TRIP = "extra::trip"
            const val EXTRA_BACK_TO_BOOKING = "extra::backbooking"

            val builder: Builder
                get() = Builder()
        }
    }

}
