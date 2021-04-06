package com.karhoo.uisdk.util

import android.content.Context
import android.text.format.DateFormat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtil {

    fun getDateAndTimeFormat(context: Context, date: Date, timeZone: TimeZone): String {
        return if (DateFormat.is24HourFormat(context)) {
            calculateDateAndTimeFormat("d MMM yyyy, HH:mm", date, timeZone)
        } else {
            calculateDateAndTimeFormat("d MMM yyyy, h:mma", date, timeZone)
        }
    }

    fun getDateAndTimeFormat(context: Context, date: DateTime): String {
        return if (DateFormat.is24HourFormat(context)) {
            date.toLocalDateTime().toString("d MMM yyyy, HH:mm")
        } else {
            date.toLocalDateTime().toString("d MMM yyyy, h:mma")
        }
    }

    private fun calculateDateAndTimeFormat(pattern: String, date: Date, timeZone: TimeZone): String {
        val newDate = date.time + timeZone.getOffset(date.time)
        val sdf = SimpleDateFormat(pattern, Locale.ENGLISH)
        return sdf.format(newDate)
    }

    fun getTimeFormat(context: Context, date: DateTime): String {
        return if (DateFormat.is24HourFormat(context)) {
            date.toString("HH:mm")
        } else {
            date.toString("h:mm a")
        }
    }

    fun getDateFormat(date: DateTime): String {
        return date.toLocalDateTime().toString("d MMM, yyyy")
    }

    fun parseSimpleDate(date: String): DateTime {
        return DateTime.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"))
    }

}
