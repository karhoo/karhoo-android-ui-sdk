package com.karhoo.uisdk.screen.trip.eta

interface EtaMVP {

    interface View {

        fun showEta(eta: Int)

        fun hideEta()

    }

    interface Presenter {

        fun monitorEta(tripIdentifier: String)

        fun onStop()

        fun onDestroy()

    }

}
