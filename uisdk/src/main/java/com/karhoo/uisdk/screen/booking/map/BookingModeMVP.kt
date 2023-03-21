package com.karhoo.uisdk.screen.booking.map

interface BookingModeMVP {

    interface Presenter {

        var isAllowedToBook: Boolean
    }

    interface View {

        fun displayDatePicker(minDate: Long, maxDate: Long, timeZone: String)
    }
}
