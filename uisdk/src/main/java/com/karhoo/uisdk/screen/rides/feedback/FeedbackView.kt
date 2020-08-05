package com.karhoo.uisdk.screen.rides.feedback

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_feedback.view.questions
import kotlinx.android.synthetic.main.uisdk_view_feedback.view.submitButton
import kotlinx.android.synthetic.main.uisdk_view_feedback.view.tripIdEditText

/**
 * Widget that contains a form made up of multiple RatedQuestionView widgets.
 * To add an additional question to the form, all that needs to be done is to add
 * another RatedQuestionView into the layout xml and configure it properly from XML.
 */
class FeedbackView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), FeedbackMVP.View, RatedQuestionsMVP.Actions {

    var tripId: String = ""
        set(value) {
            field = value
            tripIdEditText.setText(value)
            presenter = FeedbackPresenter(this, value, KarhooUISDK.analytics, FeedbackCompletedTripsStore(context))
        }
    private var presenter: FeedbackMVP.Presenter? = null

    private var ratedQuestionViewCount = 0
    private var completedRatedQuestionViewCount = 0

    init {
        View.inflate(context, R.layout.uisdk_view_feedback, this)
        setActionsOnRatedQuestionViews()
    }

    private fun setActionsOnRatedQuestionViews() {
        for (i in 0..questions.childCount) {
            val v = questions.getChildAt(i)
            if (v is RatedQuestionView) {
                v.actions = this
                ratedQuestionViewCount++
            }
        }
    }

    override fun onRatedQuestionViewComplete() {
        completedRatedQuestionViewCount++

        if (completedRatedQuestionViewCount == ratedQuestionViewCount) {
            submitButton.isEnabled = true
            submitButton.setOnClickListener { submit() }
        }
    }

    override fun onRatedQuestionInvalidated() {
        completedRatedQuestionViewCount--

        if (completedRatedQuestionViewCount < ratedQuestionViewCount) {
            submitButton.isEnabled = false
            submitButton.setOnClickListener { }
        }
    }

    private fun submit() {
        val answers = mutableListOf<FeedbackAnswer>()
        for (i in 0..questions.childCount) {
            val v = questions.getChildAt(i)
            if (v is RatedQuestionView) {
                val feedbackAnswer = FeedbackAnswer(v.fieldId, v.rating, v.additionalComments)
                answers.add(feedbackAnswer)
            }
        }

        presenter?.submit(answers)
    }

    override fun finish() {
        (context as Activity).finish()
    }

}
