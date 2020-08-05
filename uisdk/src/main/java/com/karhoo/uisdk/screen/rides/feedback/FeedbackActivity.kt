package com.karhoo.uisdk.screen.rides.feedback

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import kotlinx.android.synthetic.main.uisdk_activity_feedback.feedbackWidget
import kotlinx.android.synthetic.main.uisdk_activity_feedback.toolbar

class FeedbackActivity : BaseActivity() {

    override val layout: Int = R.layout.uisdk_activity_feedback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        toolbar.setNavigationOnClickListener { finish() }
    }

    override fun handleExtras() {
        extras?.getParcelable<TripInfo>(EXTRA_TRIP)?.let {
            feedbackWidget.tripId = it.tripId
            toolbar?.title = it.fleetInfo?.name.orEmpty()
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
        fun trip(trip: TripInfo?): Builder {
            extras.putParcelable(EXTRA_TRIP, trip)
            return this
        }

        /**
         * Returns a launchable Intent to the configured feedback activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent {
            val intent = Intent(context, KarhooUISDK.Routing.feedback)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtras(extras)
            return intent
        }

        companion object {

            fun newBuilder(): Builder {
                return Builder()
            }
        }
    }

    companion object {
        const val EXTRA_TRIP = "extra::trip"
    }

}