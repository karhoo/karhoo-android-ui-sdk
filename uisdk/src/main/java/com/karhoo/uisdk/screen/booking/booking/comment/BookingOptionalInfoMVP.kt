package com.karhoo.uisdk.screen.booking.booking.comment

interface BookingOptionalInfoMVP {
    interface View {
        fun setBookingOptionalInfo(comments: String)

        fun getBookingOptionalInfo(): String

        fun bindBookingOptionalInfo(comments: String)

        fun bindEditMode(isEditing: Boolean)

        fun allFieldsValid(): Boolean
    }

    interface Presenter {
        fun bindViews(comments: String)

        fun bookingOptionalInfoValue(): String

        fun prefillForBookingOptionalInfo(comments: String)

        fun updateOptionalInfo(toString: String)
    }
}