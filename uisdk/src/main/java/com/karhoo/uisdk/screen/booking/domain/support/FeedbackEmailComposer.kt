package com.karhoo.uisdk.screen.booking.domain.support

import com.karhoo.sdk.api.model.TripInfo

interface FeedbackEmailComposer {
    fun showFeedbackMail(): Boolean
    fun reportIssueWith(trip: TripInfo): Boolean
    fun showNoCoverageEmail(): Boolean
}