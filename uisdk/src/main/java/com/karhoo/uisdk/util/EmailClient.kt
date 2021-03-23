package com.karhoo.uisdk.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.karhoo.uisdk.R

interface EmailClient {
    fun getSendEmailIntent(context: Context, data: String?): Intent
}

class KarhooEmailClient : EmailClient {
    override fun getSendEmailIntent(context: Context, data: String?): Intent {
        val sendEmailIntent = Intent(Intent.ACTION_VIEW)
        sendEmailIntent.data = Uri.parse(data)
        return Intent.createChooser(sendEmailIntent,
                                    context.getString(R.string.kh_uisdk_title_activity_intent_chooser_send_email))

    }
}
