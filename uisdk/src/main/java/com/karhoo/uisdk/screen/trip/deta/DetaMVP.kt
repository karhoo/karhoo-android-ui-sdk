package com.karhoo.uisdk.screen.trip.deta

interface DetaMVP {

    interface View {

        fun showDeta(deta: Int, offsetMilliseconds: Int)

        fun hideDeta()
    }

    interface Presenter {

        fun monitorDeta(tripId: String, timeZone: String)

        fun onStop()

        fun onDestroy()

    }

}
