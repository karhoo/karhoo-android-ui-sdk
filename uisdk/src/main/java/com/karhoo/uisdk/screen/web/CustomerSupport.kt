package com.karhoo.uisdk.screen.web

import android.content.Context
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.R
import java.io.IOException
import java.nio.charset.Charset

@Deprecated("Use ContactEmailProvider instead")
internal fun loadHTMLFromAsset(context: Context): String? {
    var html: String?
    try {
        val input = context.resources.openRawResource(R.raw.support)
        val size = input.available()
        val buffer = ByteArray(size)
        input.read(buffer)
        input.close()
        html = String(buffer, Charset.forName("UTF-8"))

    } catch (ex: IOException) {
        ex.printStackTrace()
        return null
    }

    return html
}

fun prepopulateForUser(userDetails: UserInfo, details: String, context: Context): String {
    var update = loadHTMLFromAsset(context) ?: ""
    update.let {
        update = it.replace("$[NAME]", userDetails.firstName + " " + userDetails.lastName)
        update = update.replace("$[EMAIL]", userDetails.email)
        update = update.replace("$[PHONE]", userDetails.phoneNumber)
        update = update.replace("$[LAST_TRIP_INFO]", details)
    }
    return update
}
