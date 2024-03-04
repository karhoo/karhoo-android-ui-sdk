package com.karhoo.uisdk.screen.rides.detail.rating

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.rides.feedback.FeedbackActivity
class RatingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr) {

    var trip: TripInfo? = null

    private lateinit var additionalFeedbackButton: Button
    private lateinit var ratingBar: RatingBar
    private lateinit var ratingLabel: TextView
    private lateinit var labelLayout: LinearLayout

    init {
        View.inflate(context, R.layout.uisdk_view_star_rating, this)

        additionalFeedbackButton = findViewById(R.id.additionalFeedbackButton)
        ratingBar = findViewById(R.id.ratingBar)
        ratingLabel = findViewById(R.id.ratingLabel)
        labelLayout = findViewById(R.id.labelLayout)

        additionalFeedbackButton.setOnClickListener {
            (context as Activity).startActivity(FeedbackActivity.Builder.newBuilder()
                                                        .trip(trip)
                                                        .build(context))
        }

        ratingBar.setOnRatingBarChangeListener { _: RatingBar, rating: Float, _: Boolean ->
            ratingLabel.visibility = View.VISIBLE
            ratingLabel.setText(R.string.kh_uisdk_rating_submitted)
        }

    }

    fun showFeedbackSubmitted() {
        ratingBar.visibility = View.GONE
        labelLayout.visibility = View.GONE
        additionalFeedbackButton.visibility = View.GONE
        ratingLabel.setText(R.string.kh_uisdk_feedback_submitted)
    }

}
