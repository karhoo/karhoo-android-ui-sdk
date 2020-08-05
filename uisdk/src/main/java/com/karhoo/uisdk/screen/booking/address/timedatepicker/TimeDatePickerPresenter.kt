package com.karhoo.uisdk.screen.booking.address.timedatepicker

import androidx.lifecycle.Observer
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import java.util.Date
import java.util.TimeZone

class TimeDatePickerPresenter(view: TimeDatePickerMVP.View,
                              private val analytics: Analytics?)
    : BasePresenter<TimeDatePickerMVP.View>(), TimeDatePickerMVP.Presenter {

    private var bookingStatusStateViewModel: BookingStatusStateViewModel? = null

    private val timezone: DateTimeZone
        get() = DateTimeZone.forID(bookingStatusStateViewModel?.currentState?.pickup?.timezone.orEmpty())

    private val nowPlusOneHour: DateTime
        get() = DateTime.now(timezone).plusMinutes(MAX_MINUTES)

    private val nowDateTime: DateTime
        get() = DateTime.now(timezone)

    init {
        attachView(view)
    }

    override fun datePickerClicked() {
        bookingStatusStateViewModel?.currentState?.pickup?.let {
            analytics?.prebookOpened()
            val nowPlusOneYear = DateTime.now(timezone).plusYears(1)
            view?.displayDatePicker(
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
        bookingStatusStateViewModel?.currentState?.pickup?.let {
            val localisedTimeZone = getTimezoneDisplayName(it.timezone)

            val calendarMinute = nowDateTime.minuteOfHour
            val remainderMinutes = calendarMinute % 5
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
            view?.displayTimePicker(oneHourAheadOfCurrentHour, minutesRoundedToNearestFive, localisedTimeZone)
        }
    }

    private fun minutesToTheNearestFive(remainderMinutes: Int, calendarMinute: Int): Int {
        return if (remainderMinutes == 0 && calendarMinute < 5 && calendarMinute > 0) {
            5
        } else if (remainderMinutes == 0) {
            calendarMinute
        } else {
            calendarMinute + (5 - remainderMinutes)
        }
    }

    private fun oneHourAhead(minuteRoundingHourExtra: Int): Int {
        val currentHour = nowDateTime.hourOfDay
        var oneHourAheadOfCurrentHour = minuteRoundingHourExtra + currentHour + 1
        if (oneHourAheadOfCurrentHour > 23) {
            oneHourAheadOfCurrentHour -= 24
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
        bookingStatusStateViewModel?.currentState?.let {
            analytics?.prebookSet(Date(prebookDate.millis), it.pickup?.timezone.orEmpty())
            bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent
                                                         .BookingDateEvent(prebookDate))
        }
    }

    private fun dateFromCalendar(setYear: Int = year, setMonth: Int = month, setDay: Int = day,
                                 setHour: Int = hour, setMinute: Int = minute): DateTime {
        return DateTime(setYear, setMonth, setDay, setHour, setMinute, timezone)
    }

    override fun subscribeToBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel): Observer<BookingStatus> {
        setCurrentBookingStatus(bookingStatusStateViewModel)
        return Observer {
            it?.let { bookingStatus ->
                it.date ?: view?.hideDateViews()
            }
        }
    }

    private fun setCurrentBookingStatus(bookingStatusStateViewModel: BookingStatusStateViewModel) {
        this.bookingStatusStateViewModel = bookingStatusStateViewModel
    }

    private fun clearScheduledTimeInView() {
        bookingStatusStateViewModel?.process(AddressBarViewContract.AddressBarEvent.BookingDateEvent(null))
    }

    override fun clearScheduledTimeClicked() {
        clearScheduledTimeInView()
        view?.hideDateViews()
    }

    private companion object {

        var year: Int = 0
        var month: Int = 0
        var day: Int = 0
        var hour: Int = 0
        var minute: Int = 0

        const val MAX_MINUTES = 59
    }

}