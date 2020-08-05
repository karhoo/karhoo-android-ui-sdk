package com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo

import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP

interface TripInfoMVP {

    interface View {

        fun bindViews(driverName: String, carType: String, numberPlate: String,
                      taxiNumber: String, driverPhotoUrl: String)

        fun showDriverDetails()

        fun showTripInfo()

        fun hideDetailsOptions()

        fun showDetailsOptions()

        fun observeTripStatus(bookingStatusPresenter: BookingStatusMVP.Presenter)

    }

    interface Presenter {

        fun observeTripStatus(bookingStatusPresenter: BookingStatusMVP.Presenter)

    }

}
