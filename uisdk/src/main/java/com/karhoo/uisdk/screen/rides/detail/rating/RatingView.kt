package com.karhoo.uisdk.screen.rides.detail.rating

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.RatingBar
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.rides.feedback.FeedbackActivity
import com.karhoo.uisdk.util.extension.configure
import kotlinx.android.synthetic.main.uisdk_view_star_rating.view.additionalFeedbackButton
import kotlinx.android.synthetic.main.uisdk_view_star_rating.view.labelLayout
import kotlinx.android.synthetic.main.uisdk_view_star_rating.view.ratingBar
import kotlinx.android.synthetic.main.uisdk_view_star_rating.view.ratingLabel

class RatingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var trip: TripInfo? = null

    init {
        View.inflate(context, R.layout.uisdk_view_star_rating, this)
        configure()

        additionalFeedbackButton.setOnClickListener {
            (context as Activity).startActivity(FeedbackActivity.Builder.newBuilder()
                                                        .trip(trip)
                                                        .build(context))
        }

        ratingBar.setOnRatingBarChangeListener { _: RatingBar, rating: Float, _: Boolean ->
            ratingLabel.visibility = View.VISIBLE
            additionalFeedbackButton.configure()
            ratingLabel.setText(R.string.rating_submitted)
            KarhooUISDK.analytics?.submitRating(tripId = trip?.tripId.orEmpty(), rating = rating)
        }

    }

    fun showFeedbackSubmitted() {
        ratingBar.visibility = View.GONE
        labelLayout.visibility = View.GONE
        additionalFeedbackButton.visibility = View.GONE
        ratingLabel.setText(R.string.feedback_submitted)
    }

}