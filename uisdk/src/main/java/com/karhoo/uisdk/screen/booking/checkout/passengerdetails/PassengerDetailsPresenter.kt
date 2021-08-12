package com.karhoo.uisdk.screen.booking.checkout.passengerdetails

import android.content.res.Resources
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.uisdk.util.getCodeFromMobileNumber
import com.karhoo.uisdk.util.getMobileNumberWithoutCode

class PassengerDetailsPresenter(view: PassengerDetailsMVP.View) : BasePresenter<PassengerDetailsMVP.View>(), PassengerDetailsMVP.Presenter {
    var passengerDetails: PassengerDetails? = null

    override var isEditingMode = true
        set(value) {
            field = value
            view?.bindEditMode(field)
        }

    init {
        attachView(view)
    }

    override fun passengerDetailsValue(): PassengerDetails? {
        return passengerDetails
    }

    override fun prefillForPassengerDetails(passengerDetails: PassengerDetails) {
        this.passengerDetails = passengerDetails
        bindViews(passengerDetails)
    }

    override fun bindViews(passengerDetails: PassengerDetails) {
        view?.bindPassengerDetails(passengerDetails)
        view?.bindEditMode(isEditingMode)
    }

    override fun getCountryCodeFromPhoneNumber(number: String?, resources: Resources): String {
        return number?.let { getCodeFromMobileNumber(it, resources) } ?: ""
    }

    override fun removeCountryCodeFromPhoneNumber(number: String?, resources: Resources): String {
        return number?.let { getMobileNumberWithoutCode(it, resources) } ?: ""
    }

    override fun validateMobileNumber(code: String, number: String): String {
        return formatMobileNumber(code, number)
    }

    override fun updatePassengerDetails(firstName: String, lastName: String, email: String,
                                        mobilePhoneNumber: String) {
        if (passengerDetails == null) {
            passengerDetails = PassengerDetails()
        }
        passengerDetails = passengerDetails?.copy(firstName = firstName, lastName = lastName,
                                                  email = email, phoneNumber = mobilePhoneNumber)

        passengerDetails?.let { view?.storePassenger(it) }
    }
}
