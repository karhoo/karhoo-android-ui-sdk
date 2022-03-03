package com.karhoo.uisdk.screen.booking.address.timedatepicker

import androidx.lifecycle.Observer
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import org.joda.time.DateTime

interface TimeDatePickerMVP {

    interface Presenter {

        fun subscribeToJourneyDetails(journeyDetailsStateViewModel: JourneyDetailsStateViewModel): Observer<JourneyDetails>

        fun dateSelected(selectedYear: Int, selectedMonth: Int, dayOfMonth: Int)

        fun timeSelected(setHour: Int, setMinute: Int)

        fun datePickerClicked()

        fun clearScheduledTimeClicked()

        fun getPreviousSelectedDateTime(): DateTime?
    }

    interface View {

        fun displayDatePicker(minDate: Long, maxDate: Long, timeZone: String)

        fun displayTimePicker(hour: Int, minute: Int, timeZone: String)

        fun displayPrebookTime(time: DateTime)

        fun hideDateViews()

    }

}
