package com.karhoo.uisdk.screen.rides.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.domain.support.KarhooFeedbackEmailComposer
import com.karhoo.uisdk.screen.rides.feedback.FeedbackActivity.Companion.EXTRA_TRIP
import kotlinx.android.synthetic.main.uisdk_activity_ride_detail.rideDetailWidget
import kotlinx.android.synthetic.main.uisdk_activity_ride_detail.toolbar

class RideDetailActivity : BaseActivity(), RideDetailMVP.View.Actions {

    private var trip: TripInfo? = null

    override val layout = R.layout.uisdk_activity_ride_detail

    override var externalDateTime: String
        get() = supportActionBar?.title.toString()
        set(value) {
            supportActionBar?.title = value
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        rideDetailWidget.rideDetailActions = this
        lifecycle.addObserver(rideDetailWidget)
    }

    override fun handleExtras() {
        trip = extras?.getParcelable(EXTRA_TRIP)
    }

    override fun initialiseViews() {
        // Do nothing
    }

    override fun initialiseViewListeners() {
        // Do nothing
    }

    override fun finishActivity() {
        finish()
    }

    override fun bindViews() {
        super.bindViews()
        trip?.let { rideDetailWidget.bind(it) }
    }

    override fun showCustomerSupport(tripId: String) {
        trip?.let {
            val emailComposer = KarhooFeedbackEmailComposer(this)
            emailComposer.reportIssueWith(trip = it)
        }
    }

    /**
     * Intent Builder
     */
    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        /**
         * The activity will display the details from the [trip]
         * and use its id to continue to poll for updates if it is in
         * a live state (not cancelled, failed or complete)
         */
        fun trip(trip: TripInfo): Builder {
            extras.putParcelable(EXTRA_TRIP, trip)
            return this
        }

        /**
         * Returns a launchable Intent to the configured ride detail activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent {
            val intent = Intent(context, KarhooUISDK.Routing.rideDetail)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtras(extras)
            return intent
        }

        companion object {

            const val EXTRA_TRIP = "extra::trip"

            fun newBuilder(): Builder {
                return Builder()
            }
        }
    }

}
