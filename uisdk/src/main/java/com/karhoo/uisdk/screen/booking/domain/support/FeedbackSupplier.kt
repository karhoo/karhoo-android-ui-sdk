package com.karhoo.uisdk.screen.booking.domain.support

import android.app.Activity
import android.content.Intent
import android.net.Uri.parse
import android.os.Build
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.R
import com.karhoo.uisdk.service.preference.PreferenceStore
import com.karhoo.uisdk.util.VersionUtil
import java.lang.ref.WeakReference

class FeedbackSupplier(activity: Activity, private val preferenceStore: PreferenceStore) {

    private val activity: WeakReference<Activity> = WeakReference(activity)
    private var emailFooterText: String = ""

    private val deviceInfoString: String
        get() {
            val apiLevel = Build.VERSION.SDK_INT
            val model = Build.MODEL
            val manufacturer = Build.MANUFACTURER

            return "Device: " + manufacturer.toUpperCase() + " " + model.toUpperCase() + " (Android OS: " + apiLevel + ")\n"
        }

    fun createEmail(): Intent {
        getUserDetails()
        val emailTo = activity.get()?.getString(R.string.support_email)
        val emailSubject = activity.get()?.getString(R.string.feedback)

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$emailFooterText" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        return Intent.createChooser(sendEmailIntent,
                                    activity.get()?.getString(R.string.title_activity_intent_chooser_send_email))
    }

    private fun getUserDetails() {
        val userStore = KarhooApi.userStore

        val lastTripId = preferenceStore.lastTrip?.displayTripId.orEmpty()

        val pleaseLeaveInfo = activity.get()?.getString(R.string.email_info)
        activity.get()?.let {
            emailFooterText = "\n\n\n\n\n$pleaseLeaveInfo" +
                    "\n-------------------------------------\n" +
                    "Application: Karhoo v" +
                    VersionUtil.createBuildVersionString(it) +
                    "\n" +
                    deviceInfoString +
                    "Email: " +
                    userStore.currentUser.email +
                    "\n" +
                    "Last Trip ID: " +
                    lastTripId +
                    "\n-------------------------------------\n"
        }
    }
}