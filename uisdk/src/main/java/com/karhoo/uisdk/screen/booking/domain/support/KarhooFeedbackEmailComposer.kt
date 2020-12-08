package com.karhoo.uisdk.screen.booking.domain.support

import android.content.Context
import android.content.Intent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.EmailClient
import com.karhoo.uisdk.util.KarhooEmailClient
import com.karhoo.uisdk.util.VersionUtil
import com.karhoo.uisdk.util.VersionUtilContact
import java.lang.ref.WeakReference

class KarhooFeedbackEmailComposer(context: Context, private val userStore: UserStore =
        KarhooApi.userStore, private val versionUtil: VersionUtilContact = VersionUtil,
                                  private val emailClient: EmailClient = KarhooEmailClient()) :
        FeedbackEmailComposer {

    private val contextWeakRef: WeakReference<Context> = WeakReference(context)

    override fun showFeedbackMail(): Intent? {
        contextWeakRef.get()?.let {
            return createFeedbackEmail()
        } ?: run {
            return null
        }
    }

    override fun reportIssueWith(trip: TripInfo): Intent? {
        contextWeakRef.get()?.let {
            return createSupportForTripEmail(trip)
        } ?: run {
            return null
        }
    }

    override fun showNoCoverageEmail(): Intent? {
        contextWeakRef.get()?.let {
            return createSupplierEmail()
        } ?: run {
            return null
        }
    }

    fun createFeedbackEmail(): Intent? {
        val emailTo = contextWeakRef.get()?.getString(R.string.feedback_email)
        val emailSubject = contextWeakRef.get()?.getString(R.string.feedback)

        val headline = contextWeakRef.get()?.getString(R.string.email_info).orEmpty()
        val emailFooter = mailMetaInfo(headline)

        val data = "mailto:?subject=$emailSubject" +
                "&body=$emailFooter" +
                "&to=$emailTo"
        return sendEmailIntent(data)
    }

    private fun createSupportForTripEmail(tripInfo: TripInfo?): Intent? {
        val headline = contextWeakRef.get()?.getString(R.string.email_info).orEmpty()

        var emailFooter = getTripDetails(tripInfo)
        emailFooter += mailMetaInfo(headline)

        val emailTo = contextWeakRef.get()?.getString(R.string.support_email)
        val emailSubject = "${contextWeakRef.get()?.getString(R.string.support_report_issue)}: " +
                "${tripInfo?.displayTripId}"

        val data = "mailto:?subject=$emailSubject" +
                "&body=$emailFooter" +
                "&to=$emailTo"
        return sendEmailIntent(data)
    }

    private fun createSupplierEmail(): Intent? {
        val headline = contextWeakRef.get()?.getString(R.string.fleet_recommendation_body).orEmpty()

        val emailTo = contextWeakRef.get()?.getString(R.string.supplier_email)
        val emailSubject = contextWeakRef.get()?.getString(R.string.fleet_recommendation_subject)

        val data = "mailto:?subject=$emailSubject" +
                "&body=$headline" +
                "&to=$emailTo"
        return sendEmailIntent(data)
    }

    private fun sendEmailIntent(data: String): Intent? {
        return contextWeakRef.get()?.let {
            emailClient.getSendEmailIntent(context = it, data = data)
        }
    }

    private fun getTripDetails(tripInfo: TripInfo?): String {
        var text = contextWeakRef.get()?.getString(R.string.email_report_issue_message) ?: ""
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

        contextWeakRef.get()?.let {
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
