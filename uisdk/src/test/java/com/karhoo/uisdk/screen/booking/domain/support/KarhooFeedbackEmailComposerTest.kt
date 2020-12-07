package com.karhoo.uisdk.screen.booking.domain.support

import android.app.Activity
import android.content.Intent
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.util.VersionUtilContact
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
class KarhooFeedbackEmailComposerTest {

    private lateinit var composer: FeedbackEmailComposer
    private var activity: Activity = mock()
    private val userStore: UserStore = mock()
    private val versionUtil: VersionUtilContact = mock()
    private val currentUser = UserInfo()
    private val intent: Intent = Intent()

    private var intentCapture = argumentCaptor<Intent>()

    @Before
    fun setUp() {
        composer = KarhooFeedbackEmailComposer(activity = activity, userStore = userStore, versionUtil = versionUtil)
        activity.startActivity(intentCapture.capture())
        whenever(userStore.currentUser).thenReturn(currentUser)
    }

    @Test
    fun showFeedbackMail() {
        composer.showFeedbackMail()

        Assertions.assertThat(intentCapture.firstValue.action).isEqualTo(Intent.ACTION_VIEW)
    }

    @Test
    fun reportIssueWith() {

    }

    @Test
    fun showNoCoverageEmail() {
    }
}
