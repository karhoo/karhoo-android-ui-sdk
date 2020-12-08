package com.karhoo.uisdk.screen.booking.domain.support

import android.content.Intent
import com.karhoo.sdk.api.model.TripInfo

interface FeedbackEmailComposer {
    fun showFeedbackMail(): Intent?
    fun reportIssueWith(trip: TripInfo): Intent?
    fun showNoCoverageEmail(): Intent?
}
