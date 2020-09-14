package com.karhoo.uisdk.screen.booking.domain.support

import android.app.Activity
import android.content.Intent
import android.net.Uri.parse
import com.karhoo.uisdk.R
import com.karhoo.uisdk.service.preference.PreferenceStore
import java.lang.ref.WeakReference

class ContactSupplier(activity: Activity, private val preferenceStore: PreferenceStore) {

    private val activity: WeakReference<Activity> = WeakReference(activity)
    private var emailFooterText: String = ""

    fun createEmail(): Intent {
        getUserDetails()
        val emailTo = activity.get()?.getString(R.string.supplier_email)
        val emailSubject = activity.get()?.getString(R.string.fleet_recommendation_subject)

        val data = parse("mailto:?subject=$emailSubject" +
                                 "&body=$emailFooterText" +
                                 "&to=$emailTo")
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = data
        return Intent.createChooser(sendEmailIntent,
                                    activity.get()?.getString(R.string.title_activity_intent_chooser_send_email))
    }

    private fun getUserDetails() {
        activity.get()?.let {
            emailFooterText = activity.get()?.getString(R.string.fleet_recommendation_body).toString()
        }
    }
}
