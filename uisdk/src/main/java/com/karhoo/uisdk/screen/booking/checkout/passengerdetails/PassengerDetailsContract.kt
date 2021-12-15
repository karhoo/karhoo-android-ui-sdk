package com.karhoo.uisdk.screen.booking.checkout.passengerdetails

import android.content.Context
import android.content.res.Resources
import com.google.android.material.textfield.TextInputLayout
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

interface PassengerDetailsContract {
    interface View {
        fun setPassengerDetails(passengerDetails: PassengerDetails)

        fun getPassengerDetails(): PassengerDetails?

        fun bindPassengerDetails(passengerDetails: PassengerDetails)

        fun bindEditMode(isEditing: Boolean)

        fun allFieldsValid(): Boolean

        fun findAndfocusFirstInvalid(): Boolean

        fun areFieldsValid(): Boolean

        fun storePassenger(passengerDetails: PassengerDetails)

        fun retrievePassenger(): PassengerDetails?

        fun clickOnSaveButton()

        fun setCountryFlag(countryCode: String, dialingCode: String, validateField: Boolean)

        fun setErrorOnField(field: TextInputLayout, errorId: Int)

        fun retrievePassengerFromSharedPrefs(): PassengerDetails?

        fun retrieveCountryCodeFromSharedPrefs(): String?

        fun setFocusOnPhoneNumber()
    }

    interface Presenter {
        var isEditingMode: Boolean

        fun bindViews(passengerDetails: PassengerDetails)

        fun getCountryDialingCodeFromNumber(number: String?, resources: Resources): String

        fun passengerDetailsValue(): PassengerDetails?

        fun prefillForPassengerDetails(passengerDetails: PassengerDetails)

        fun removeCountryCodeFromPhoneNumber(number: String?, resources: Resources): String

        fun updatePassengerDetails(firstName: String, lastName: String, email: String,
                                   mobilePhoneNumber: String)

        fun validateMobileNumber(code: String, number: String): String

        fun getCountryCode(context: Context): String

        fun getDialingCode(context: Context): String

        fun validateField(
                layout: TextInputLayout,
                showError: Boolean,
                validator: SelfValidatingTextLayout.Validator)

        fun formatPhoneNumber(phoneNumber: String, countryCode: String): String

        fun setCountryCode(countryCode: String)

        fun setDialingCode(dialingCode: String)

        fun getCountryCode(): String
    }

    interface Validator {
        fun onFieldsValidated(validated: Boolean)
    }
}
