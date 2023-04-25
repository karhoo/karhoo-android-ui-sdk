package com.karhoo.uisdk.screen.booking.address.timedatepicker

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import java.text.DateFormat
import java.util.*

class RangeTimePickerDialog(
    context: Context?,
    themeResId: Int,
    callBack: OnTimeSetListener?,
    private var currentHour: Int,
    private var currentMinute: Int,
    is24HourView: Boolean
) :
    TimePickerDialog(context, themeResId, callBack, currentHour, currentMinute, is24HourView) {
    private var minHour = DEFAULT_MIN_HOUR
    private var minMinute = DEFAULT_MIN_HOUR
    private var maxHour = DEFAULT_MAX_HOUR
    private var maxMinute = DEFAULT_MAX_HOUR
    private val calendar = Calendar.getInstance()
    private val dateFormat: DateFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    fun setMin(hour: Int, minute: Int) {
        minHour = hour
        minMinute = minute
    }

    fun setMax(hour: Int, minute: Int) {
        maxHour = hour
        maxMinute = minute
    }

    override fun onTimeChanged(view: TimePicker, hourOfDay: Int, minute: Int) {
        var validTime = true
        if (hourOfDay < minHour || (hourOfDay == minHour && minute < minMinute)) {
            validTime = false
        }
        if (hourOfDay > maxHour || (hourOfDay == maxHour && minute > maxMinute)) {
            validTime = false
        }
        if (validTime) {
            currentHour = hourOfDay
            currentMinute = minute
        }
        updateTime(currentHour, currentMinute)
        updateDialogTitle(currentHour, currentMinute)
    }

    private fun updateDialogTitle(hourOfDay: Int, minute: Int) {
        calendar[Calendar.HOUR_OF_DAY] = hourOfDay
        calendar[Calendar.MINUTE] = minute
        val title = dateFormat.format(calendar.time)
        setTitle(title)
    }

    private companion object {
        private const val DEFAULT_MIN_HOUR = -1
        private const val DEFAULT_MAX_HOUR = 25
    }
}
