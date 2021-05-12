package com.karhoo.uisdk.util

import android.content.Context
import com.karhoo.uisdk.R

object TimeUtil {
    fun roundMinutesInHours(minutes: Int): Int {
        return minutes / HOUR_IN_MINUTES
    }

    fun getLeftOverMinutesFromHours(minutes: Int): Int {
        return minutes % HOUR_IN_MINUTES
    }

    fun getHourAndMinutesFormattedText(context: Context, minutes: Int, hours: Int): String {
        var text = ""

        if (hours > 0) text += context.resources.getQuantityString(R.plurals.kh_uisdk_hours_plural, hours, hours) + " "
        if (hours > 0 && minutes > 0) text += context.getString(R.string.kh_uisdk_quote_cancellation_and_keyword) + " "
        if (minutes > 0) text += context.resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, minutes, minutes) + " "

        return text
    }

    private const val HOUR_IN_MINUTES = 60
}
