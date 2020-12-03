package com.karhoo.uisdk.screen.booking.domain.support

import android.app.Activity
import android.content.Intent
import android.net.Uri.parse
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.VersionUtil
import java.lang.ref.WeakReference

class ContactEmailProvider(activity: Activity) {

    private val activity: WeakReference<Activity> = WeakReference(activity)

    fun createFeedbackEmail(): Intent {
        val emailTo = activity.get()?.getString(R.string.feedback_email)
        val emailSubject = activity.get()?.getString(R.string.feedback)

        val headline = activity.get()?.getString(R.string.email_info).orEmpty()
        val emailFooter = getUserDetails(headline)

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$emailFooter" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        return Intent.createChooser(sendEmailIntent,
                                    activity.get()?.getString(R.string.title_activity_intent_chooser_send_email))
    }

    fun createSupportForTripEmail(tripInfo: TripInfo?): Intent {
        val headline = activity.get()?.getString(R.string.email_info).orEmpty()

        var emailFooter = getUserDetails(headline)
        emailFooter += getTripDetails(tripInfo)

        val emailTo = activity.get()?.getString(R.string.support_email)
        val emailSubject = activity.get()?.getString(R.string.support)

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$emailFooter" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        return Intent.createChooser(sendEmailIntent,
                                    activity.get()?.getString(R.string.title_activity_intent_chooser_send_email))
    }

    fun createSupplierEmail(): Intent {
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
        var text = ""
        val lastTripId = tripInfo?.displayTripId

        text += lastTripId.let {
            "\nTrip ID: $lastTripId \n-------------------------------------\n"
        }
        return text
    }

    private fun getUserDetails(headline: String): String {
        var details = ""
        val userStore = KarhooApi.userStore

        activity.get()?.let {
            details = "\n\n\n\n\n$headline" +
                    "\n-------------------------------------\n" +
                    "Application: ${VersionUtil.getAppNameString(it)} v ${VersionUtil
                            .createBuildVersionString(it)} \n"
            details += "${VersionUtil.appAndDeviceInfo()}\n"
            details += "Email: ${userStore.currentUser.email}\n"

        }
        return details
    }
}
