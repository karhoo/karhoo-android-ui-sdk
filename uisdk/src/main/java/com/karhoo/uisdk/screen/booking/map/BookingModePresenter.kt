package com.karhoo.uisdk.screen.booking.map

import com.karhoo.uisdk.base.BasePresenter

class BookingModePresenter(view: BookingModeMVP.View): BasePresenter<BookingModeMVP.View>(), BookingModeMVP.Presenter {

    init {
        attachView(view)
    }

    override var isAllowedToBook: Boolean = false

}
