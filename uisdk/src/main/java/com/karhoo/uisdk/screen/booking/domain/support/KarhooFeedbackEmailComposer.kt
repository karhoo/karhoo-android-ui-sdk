package com.karhoo.uisdk.screen.booking.domain.support

import android.app.Activity
import android.content.Intent
import android.net.Uri.parse
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.VersionUtil
import com.karhoo.uisdk.util.VersionUtilContact
import java.lang.ref.WeakReference

class KarhooFeedbackEmailComposer(activity: Activity, private val userStore: UserStore =
        KarhooApi.userStore, private val versionUtil: VersionUtilContact = VersionUtil) :
        FeedbackEmailComposer {

    private val activity: WeakReference<Activity> = WeakReference(activity)

    override fun showFeedbackMail(): Boolean {
        activity.get()?.let {
            it.startActivity(createFeedbackEmail())
            return true
        } ?: run {
            return false
        }
    }

    override fun reportIssueWith(trip: TripInfo): Boolean {
        activity.get()?.let {
            it.startActivity(createSupportForTripEmail(trip))
            return true
        } ?: run {
            return false
        }
    }

    override fun showNoCoverageEmail(): Boolean {
        activity.get()?.let {
            it.startActivity(createSupplierEmail())
            return true
        } ?: run {
            return false
        }
    }

    fun createFeedbackEmail(): Intent {
        val emailTo = "mo@mo.copm"//activity.get()?.getString(R.string.feedback_email)
        val emailSubject = "subject" //activity.get()?.getString(R.string.feedback)

        val headline = "headline"//activity.get()?.getString(R.string.email_info).orEmpty()
        val emailFooter = mailMetaInfo(headline)

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$emailFooter" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        val chooserTitle = "title"//activity.get()?.getString(R.string
//                                                           .title_activity_intent_chooser_send_email)
        return Intent.createChooser(sendEmailIntent, chooserTitle)
    }

    private fun createSupportForTripEmail(tripInfo: TripInfo?): Intent {
        val headline = activity.get()?.getString(R.string.email_info).orEmpty()

        var emailFooter = getTripDetails(tripInfo)
        emailFooter += mailMetaInfo(headline)

        val emailTo = activity.get()?.getString(R.string.support_email)
        val emailSubject = "${activity.get()?.getString(R.string.support_report_issue)}: " +
                "${tripInfo?.displayTripId}"

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$emailFooter" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        return Intent.createChooser(sendEmailIntent,
                                    activity.get()?.getString(R.string.title_activity_intent_chooser_send_email))
    }

    private fun createSupplierEmail(): Intent {
        val headline = activity.get()?.getString(R.string.fleet_recommendation_body).orEmpty()

        val emailTo = activity.get()?.getString(R.string.supplier_email)
        val emailSubject = activity.get()?.getString(R.string.fleet_recommendation_subject)

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$headline" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        return Intent.createChooser(sendEmailIntent,
                                    activity.get()?.getString(R.string.title_activity_intent_chooser_send_email))
    }

    private fun getTripDetails(tripInfo: TripInfo?): String {
        var text = activity.get()?.getString(R.string.email_report_issue_message) ?: ""
        val lastTripId = tripInfo?.displayTripId

        text += lastTripId.let {
            "\n-------------------------------------\n" +
                    "\"Trip: $lastTripId " +
                    "\n-------------------------------------\n"
        }
        return text
    }

    private fun mailMetaInfo(headline: String): String {
        var details = ""
        val user = userStore.currentUser

        activity.get()?.let {
            details = "\n\n\n\n\n\n\n$headline" +
                    "\n-------------------------------------\n" +
                    "Application: ${versionUtil.getAppNameString(it)} v ${versionUtil
                            .createBuildVersionString(it)} \n"
            details += "${versionUtil.appAndDeviceInfo()}\n"
            details += "Locale: ${user.locale}\n"
            details += userInfo()
        }
        return details
    }

    private fun userInfo(): String {
        val user = userStore.currentUser

        return if (user.firstName.isNotEmpty()) {
            "Email: ${user.email}\nMobile phone: ${user.phoneNumber}\nFirst name: ${user
                    .firstName}\nLast name: " +
                    "${user.lastName}\n "
        } else {
            ""
        }
    }
}
