package com.karhoo.uisdk.screen.booking.quotes.capacity

interface CapacityMVP {

    interface View {

        fun setCapacity(luggage: Int, people: Int, otherCapabilities: Int?)
        fun showCapacities(show: Boolean)
    }

}
