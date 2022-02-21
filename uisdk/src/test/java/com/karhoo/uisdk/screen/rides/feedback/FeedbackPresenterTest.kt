package com.karhoo.uisdk.screen.rides.feedback

import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FeedbackPresenterTest {

    private val view: FeedbackMVP.View = mock()
    private val tripId = "someTripid"
    private val analytics: Analytics = mock()
    private val feedbackCompletedTripsStore: FeedbackCompletedTripsStore = mock()

    private val presenter: FeedbackMVP.Presenter = FeedbackPresenter(view, tripId, analytics, feedbackCompletedTripsStore)

    private val feedback = listOf(FeedbackAnswer("1", 1, "yes"),
                                  FeedbackAnswer("2", 2, "no"))


    @Test
    fun `tripId added to FeedbackCompletedTripStore on submit`() {
        presenter.submit(feedback)

        verify(feedbackCompletedTripsStore).addTrip(tripId)
    }

}
