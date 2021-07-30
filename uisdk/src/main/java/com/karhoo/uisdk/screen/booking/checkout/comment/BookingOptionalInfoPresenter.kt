package com.karhoo.uisdk.screen.booking.checkout.comment

import com.karhoo.uisdk.base.BasePresenter

class BookingOptionalInfoPresenter(view: BookingOptionalInfoMVP.View) : BasePresenter<BookingOptionalInfoMVP
.View>(), BookingOptionalInfoMVP.Presenter {
    var comments: String? = null
    var isEditingMode = true
        set(value) {
            field = value
            view?.bindEditMode(field)
        }

    init {
        attachView(view)
    }

    override fun bookingOptionalInfoValue(): String {
        return comments.orEmpty()
    }

    override fun prefillForBookingOptionalInfo(comments: String) {
        this.comments = comments
        bindViews(comments)
    }

    override fun bindViews(comments: String) {
        view?.bindBookingOptionalInfo(comments)
        view?.bindEditMode(isEditingMode)
    }

    override fun updateOptionalInfo(comments: String) {
        this.comments = comments
    }
}
