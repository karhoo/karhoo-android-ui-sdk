package com.karhoo.uisdk.screen.booking.checkout.passengerdetails

import android.content.res.Resources
import com.karhoo.sdk.api.network.request.PassengerDetails

interface PassengerDetailsMVP {
    interface View {
        fun setPassengerDetails(passengerDetails: PassengerDetails)

        fun getPassengerDetails(): PassengerDetails

        fun bindPassengerDetails(passengerDetails: PassengerDetails)

        fun bindEditMode(isEditing: Boolean)

        fun allFieldsValid(): Boolean

        fun findAndfocusFirstInvalid(): Boolean
    }

    interface Presenter {
        var isEditingMode: Boolean

        fun bindViews(passengerDetails: PassengerDetails)

        fun getCountryCodeFromPhoneNumber(number: String?, resources: Resources): String

        fun passengerDetailsValue(): PassengerDetails

        fun prefillForPassengerDetails(passengerDetails: PassengerDetails)

        fun removeCountryCodeFromPhoneNumber(number: String?, resources: Resources): String

        fun updatePassengerDetails(firstName: String, lastName: String, email: String,
                                   mobilePhoneNumber: String)

        fun validateMobileNumber(code: String, number: String): String
    }
}
