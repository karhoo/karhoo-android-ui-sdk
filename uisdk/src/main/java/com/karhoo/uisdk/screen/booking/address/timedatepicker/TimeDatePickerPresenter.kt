package com.karhoo.uisdk.screen.booking.address.timedatepicker

import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.karhoo.uisdk.R
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.address.TimePickerTitleView
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class TimeDatePickerPresenter(view: TimeDatePickerMVP.View,
                              val analytics: Analytics?)
    : BasePresenter<TimeDatePickerMVP.View>(), TimeDatePickerMVP.Presenter, OnDateSetListener, TimePickerDialog.OnTimeSetListener  {

    private var journeyDetailsStateViewModel: JourneyDetailsStateViewModel? = null

    private val timezone: DateTimeZone
    get() {
        return try {
            DateTimeZone.forID(journeyDetailsStateViewModel?.currentState?.pickup?.timezone.orEmpty())
        } catch(e: IllegalArgumentException) {
            DateTimeZone.forID(TIMEZONE_DEFAULT_EUROPE)
        }
    }

    private val nowPlusOneHour: DateTime
        get() = DateTime.now(timezone).plusMinutes(MAX_MINUTES)

    private val nowDateTime: DateTime
        get() = DateTime.now(timezone)

    init {
        attachView(view)
    }

    override fun datePickerClicked() {
        journeyDetailsStateViewModel?.currentState?.pickup?.let {
            analytics?.prebookOpened()
            val nowPlusOneYear = DateTime.now(timezone).plusYears(1)
            displayDatePicker(
                    minDate = nowPlusOneHour.millis,
                    maxDate = nowPlusOneYear.millis,
                    timeZone = getTimezoneDisplayName(it.timezone))
        }
    }

    private fun getTimezoneDisplayName(timezone: String): String {
        val timeZone = TimeZone.getTimeZone(timezone)
        return timeZone.getDisplayName(false, TimeZone.SHORT)
    }

    override fun dateSelected(selectedYear: Int, selectedMonth: Int, dayOfMonth: Int) {
        journeyDetailsStateViewModel?.currentState?.pickup?.let {
            val localisedTimeZone = getTimezoneDisplayName(it.timezone)

            val calendarMinute = nowDateTime.minuteOfHour
            val remainderMinutes = calendarMinute % TIME_ROUND_TO_FIVE
            var minutesRoundedToNearestFive = minutesToTheNearestFive(remainderMinutes, calendarMinute)

            var minuteRoundingHourExtra = 0
            if (minutesRoundedToNearestFive > MAX_MINUTES) {
                minutesRoundedToNearestFive = 0
                minuteRoundingHourExtra = 1
            }

            /*
            Due to the date picker being compatible with calender values 0-11 for months we add 1
            to the month for use with Joda time.
             */
            year = selectedYear
            month = selectedMonth + 1
            day = dayOfMonth

            val oneHourAheadOfCurrentHour = oneHourAhead(minuteRoundingHourExtra)
            displayTimePicker(oneHourAheadOfCurrentHour, minutesRoundedToNearestFive, localisedTimeZone, (year == nowDateTime.year && month == nowDateTime.monthOfYear && day == nowDateTime.dayOfMonth))
        }
    }

    var forUnitTests: Boolean = false
    fun displayDatePicker(minDate: Long, maxDate: Long, timeZone: String) {
        val previousSelectedDate = getPreviousSelectedDateTime()
        view?.let {
            val title = view?.getContext()?.getString(R.string.kh_uisdk_prebook_timezone_title) + " " + timeZone

            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setStart(minDate)
                    .setEnd(maxDate)
                    .build()

            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText(if(!forUnitTests) title else "")
                    .setSelection(previousSelectedDate?.millis ?: MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder)
                    .setTheme(R.style.MaterialCalendarTheme)
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                // Respond to positive button click.
                val calendar = Calendar.getInstance(TimeZone.getTimeZone(timeZone))
                calendar.timeInMillis = it
                dateSelected(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
            }

            if(!forUnitTests){
                val activity = view?.getContext() as AppCompatActivity
                val fragmentManager = activity.supportFragmentManager
                datePicker.show(fragmentManager, "tag")
            }
        }
    }

    fun displayTimePicker(hour: Int, minute: Int, timeZone: String, isCurrentDay: Boolean = false) {
        val previousSelectedDate = getPreviousSelectedDateTime()
        view?.let {
            val dialog = RangeTimePickerDialog(it.getContext(),
                R.style.DialogTheme,
                this,
                previousSelectedDate?.hourOfDay ?: hour,
                previousSelectedDate?.minuteOfHour ?: minute,
                DateFormat.is24HourFormat(it.getContext()))
            if(!forUnitTests)
                dialog.setCustomTitle(TimePickerTitleView(it.getContext()).setTitle(R.string.kh_uisdk_prebook_timezone_title, timeZone))

            if(isCurrentDay)
                dialog.setMin(nowPlusOneHour.hourOfDay, nowPlusOneHour.minuteOfHour)
            dialog.show()
        }
    }

    private fun minutesToTheNearestFive(remainderMinutes: Int, calendarMinute: Int): Int {
        return if (remainderMinutes == 0 && calendarMinute < TIME_ROUND_TO_FIVE && calendarMinute > 0) {
            TIME_ROUND_TO_FIVE
        } else if (remainderMinutes == 0) {
            calendarMinute
        } else {
            calendarMinute + (TIME_ROUND_TO_FIVE - remainderMinutes)
        }
    }

    private fun oneHourAhead(minuteRoundingHourExtra: Int): Int {
        val currentHour = nowDateTime.hourOfDay
        var oneHourAheadOfCurrentHour = minuteRoundingHourExtra + currentHour + HOURS_TO_BE_ADDED
        if (oneHourAheadOfCurrentHour > OURS_IN_DAY_MINUS_ONE) {
            oneHourAheadOfCurrentHour -= OURS_IN_DAY
        }
        return oneHourAheadOfCurrentHour
    }

    override fun timeSelected(setHour: Int, setMinute: Int) {
        val updatedTime = dateFromCalendar(setHour = setHour, setMinute = setMinute)
        if (updatedTime.toLocalDateTime().isBefore(nowPlusOneHour.toLocalDateTime())) {
            displayPrebookTime(nowPlusOneHour)
        } else {
            displayPrebookTime(updatedTime)
        }
    }

    private fun displayPrebookTime(prebookDate: DateTime) {
        setDate(prebookDate)
        view?.displayPrebookTime(prebookDate)
    }

    private fun setDate(prebookDate: DateTime) {
        journeyDetailsStateViewModel?.currentState?.let {
            analytics?.prebookSet(Date(prebookDate.millis), it.pickup?.timezone.orEmpty())
            journeyDetailsStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                         .BookingDateEvent(prebookDate))
        }
    }

    private fun dateFromCalendar(setYear: Int = year, setMonth: Int = month, setDay: Int = day,
                                 setHour: Int = hour, setMinute: Int = minute): DateTime {

        val localDateTime = LocalDateTime(timezone).withYear(setYear).withMonthOfYear(setMonth).withDayOfMonth(setDay).withHourOfDay(setHour).withMinuteOfHour(setMinute)

        // dateFromCalendar now adds an hour. This is because Jodatime cannot handle Daylight saving by itself and has issues with conversion.
        // This ensures it does the conversion with the extra hour it needs added onto it for when the hours go forwards. This works for all timezones.
        return if(timezone.isLocalDateTimeGap(localDateTime)){
            localDateTime.plusHours(1).toDateTime(timezone)
        } else localDateTime.toDateTime(timezone)
    }

    override fun subscribeToJourneyDetails(journeyDetailsStateViewModel: JourneyDetailsStateViewModel): Observer<JourneyDetails> {
        setCurrentJourneyDetails(journeyDetailsStateViewModel)
        return Observer {
            it?.let {
                it.date ?: view?.hideDateViews()
            }
        }
    }

    private fun setCurrentJourneyDetails(journeyDetailsStateViewModel: JourneyDetailsStateViewModel) {
        this.journeyDetailsStateViewModel = journeyDetailsStateViewModel
    }

    private fun clearScheduledTimeInView() {
        journeyDetailsStateViewModel?.process(AddressBarViewContract.AddressBarEvent.BookingDateEvent(null))
    }

    override fun clearScheduledTimeClicked() {
        clearScheduledTimeInView()
        view?.hideDateViews()
    }

    override fun getPreviousSelectedDateTime(): DateTime? {
        return journeyDetailsStateViewModel?.currentState?.date
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        dateSelected(year, month, dayOfMonth)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        timeSelected(hourOfDay, minute)
    }
    private companion object {

        var year: Int = 0
        var month: Int = 0
        var day: Int = 0
        var hour: Int = 0
        var minute: Int = 0

        const val MAX_MINUTES = 59
        const val TIME_ROUND_TO_FIVE = 5
        const val OURS_IN_DAY = 24
        const val OURS_IN_DAY_MINUS_ONE = OURS_IN_DAY - 1
        const val TIMEZONE_DEFAULT_EUROPE = "Europe/Paris"
        const val HOURS_TO_BE_ADDED = 1
    }

}
