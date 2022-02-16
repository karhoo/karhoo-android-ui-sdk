package com.karhoo.uisdk.screen.rides.feedback

import com.karhoo.uisdk.analytics.Analytics

class FeedbackPresenter(private val view: FeedbackMVP.View,
                        private val tripId: String,
                        private val analytics: Analytics?,
                        private val feedbackCompletedTrips: FeedbackCompletedTripsStore) : FeedbackMVP.Presenter {

    override fun submit(answers: List<FeedbackAnswer>) {
        feedbackCompletedTrips.addTrip(tripId)
        view.finish()
    }

}
