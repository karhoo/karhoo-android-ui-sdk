package com.karhoo.uisdk.screen.booking.domain.support

import android.content.Context
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.EmailClient
import com.karhoo.uisdk.util.VersionUtilContact
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class KarhooFeedbackEmailComposerTest {

    private lateinit var composer: FeedbackEmailComposer
    private var context: Context = mock()
    private val userStore: UserStore = mock()
    private val versionUtil: VersionUtilContact = mock()
    private val emailClient: EmailClient = mock()

    private var intentCapture = argumentCaptor<String>()

    private val currentUser = UserInfo(firstName = "First name", lastName = "last name", phoneNumber =
    "0123", locale = "gb", email = "email")
    private val tripInfo = TripInfo(displayTripId = "TRIP ID")

    private val feedbackEmailDataUri = """mailto:?subject=Feedback&body=






Please do not delete this information
-------------------------------------
Application: getAppNameString v createBuildVersionString 
appAndDeviceInfo
Locale: gb
Email: email
Mobile phone: 0123
First name: First name
Last name: last name
 &to=feedback@karhoo.com"""

    private val reportIssueEmailDataUri = """mailto:?subject=Report Issue: TRIP ID&body=Please use the space below to report
-------------------------------------
"Trip: TRIP ID 
-------------------------------------







Please do not delete this information
-------------------------------------
Application: getAppNameString v createBuildVersionString 
appAndDeviceInfo
Locale: gb
Email: email
Mobile phone: 0123
First name: First name
Last name: last name
 &to=support@karhoo.com"""

    private val reportNoCoverageEmailDataUri = "mailto:?subject=Fleet recommendation in my area&body=Thank you for recommending a fleet in your area&to=siupplier@karhoo.com"

    @Before
    fun setUp() {
        composer = KarhooFeedbackEmailComposer(context = context, userStore = userStore,
                                               versionUtil = versionUtil, emailClient = emailClient)
        whenever(userStore.currentUser).thenReturn(currentUser)

        prepareStrings()

        prepareVersionUtil()
    }

    private fun prepareVersionUtil() {
        whenever(versionUtil.getAppNameString(any())).thenReturn("getAppNameString")
        whenever(versionUtil.createBuildVersionString(any())).thenReturn("createBuildVersionString")
        whenever(versionUtil.appAndDeviceInfo()).thenReturn("appAndDeviceInfo")
    }

    private fun prepareStrings() {
        whenever(context.getString(R.string.kh_uisdk_feedback_email)).thenReturn("feedback@karhoo.com")
        whenever(context.getString(R.string.kh_uisdk_feedback)).thenReturn("Feedback")
        whenever(context.getString(R.string.kh_uisdk_email_info)).thenReturn("Please do not delete this information")
        whenever(context.getString(R.string.kh_uisdk_support_email)).thenReturn("support@karhoo.com")
        whenever(context.getString(R.string.kh_uisdk_support_report_issue)).thenReturn("Report Issue")
        whenever(context.getString(R.string.kh_uisdk_email_report_issue_message)).thenReturn("Please use the space below to report")
        whenever(context.getString(R.string.kh_uisdk_supplier_email)).thenReturn("siupplier@karhoo.com")
        whenever(context.getString(R.string.kh_uisdk_fleet_recommendation_subject)).thenReturn("Fleet recommendation in my area")
        whenever(context.getString(R.string.kh_uisdk_fleet_recommendation_body)).thenReturn("Thank you for recommending a fleet in your area")
    }

    @Test
    fun showFeedbackMail() {
        composer.showFeedbackMail()
        verify(emailClient).getSendEmailIntent(any(), intentCapture.capture())
        assertEquals(feedbackEmailDataUri, intentCapture.firstValue)
    }

    @Test
    fun reportIssueWith() {
        composer.reportIssueWith(tripInfo)
        verify(emailClient).getSendEmailIntent(any(), intentCapture.capture())

        assertEquals(reportIssueEmailDataUri, intentCapture.firstValue)
    }

    @Test
    fun showNoCoverageEmail() {
        composer.showNoCoverageEmail()
        verify(emailClient).getSendEmailIntent(any(), intentCapture.capture())
        assertEquals(reportNoCoverageEmailDataUri, intentCapture.firstValue)
    }
}
