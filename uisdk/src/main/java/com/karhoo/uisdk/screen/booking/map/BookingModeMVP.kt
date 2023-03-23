package com.karhoo.uisdk.screen.booking.map

interface BookingModeMVP {

    interface Presenter {

        var isAllowedToBook: Boolean
    }

    interface View {
        fun enableNowButton(enable: Boolean)
        fun enableScheduleButton(enable: Boolean)

        fun showNoCoverageText(hasCoverage: Boolean)
    }
}
