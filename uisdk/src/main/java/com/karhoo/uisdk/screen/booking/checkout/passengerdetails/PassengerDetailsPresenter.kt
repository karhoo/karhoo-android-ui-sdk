package com.karhoo.uisdk.screen.booking.checkout.passengerdetails

import android.content.Context
import android.content.res.Resources
import com.google.android.material.textfield.TextInputLayout
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.base.BasePresenter
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.validator.PhoneNumberValidator
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import com.karhoo.uisdk.base.view.countrycodes.CountryPickerActivity.Companion.COUNTRIES_JSON_MAPPINGS
import com.karhoo.uisdk.base.view.countrycodes.CountryUtils
import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.uisdk.util.getCodeFromMobileNumber
import com.karhoo.uisdk.util.getMobileNumberWithoutCode

class PassengerDetailsPresenter(view: PassengerDetailsContract.View) : BasePresenter<PassengerDetailsContract.View>(), PassengerDetailsContract.Presenter {
    var passengerDetails: PassengerDetails? = null
    var selectedCountryCode: String = ""
    var selectedDialingCode: String = ""

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

    override fun getCountryDialingCodeFromNumber(number: String?, resources: Resources): String {
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
    }

    override fun getCountryCode(context: Context): String {
        val phoneNumberWithoutCountryCode = removeCountryCodeFromPhoneNumber(passengerDetails?.phoneNumber, context.resources)
        val storedPhoneNumber = removeCountryCodeFromPhoneNumber(view?.retrievePassengerFromSharedPrefs()?.phoneNumber, context.resources)
        val storedCountryCode = view?.retrieveCountryCodeFromSharedPrefs()

        if (storedPhoneNumber == phoneNumberWithoutCountryCode && storedCountryCode != null) {
            return storedCountryCode
        }

        var countryDialingCode = getCountryDialingCodeFromNumber(passengerDetails?.phoneNumber,
                                                                 context.resources)

        if (countryDialingCode.isNotEmpty() && countryDialingCode.subSequence(0, 1).equals(PLUS_SIGN)) {
            countryDialingCode = countryDialingCode.drop(1)
        }
        val countryMappings = CountryUtils.parseCountries(
                CountryUtils.getCountriesJSON(context, COUNTRIES_JSON_MAPPINGS))

        var countryCode = countryMappings?.find {
            it.dialingCode == countryDialingCode
        }?.isoCode

        if (countryCode.isNullOrEmpty()) {
            countryCode = if (selectedCountryCode.isNotEmpty()) selectedCountryCode else CountryUtils
                    .getDefaultCountryCode(context)
        }
        return countryCode
    }

    override fun getDialingCode(context: Context): String {
        return CountryUtils.getDefaultCountryDialingCode(getCountryCode(context))
    }

    override fun formatPhoneNumber(phoneNumber: String, countryCode: String): String {
        val phoneUtil = PhoneNumberUtil.getInstance()
        return try {
            phoneUtil.format(
                    phoneUtil.parse(phoneNumber, countryCode),
                    PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            ""
        }
    }

    override fun validateField(
            layout: TextInputLayout,
            showError: Boolean,
            validator: SelfValidatingTextLayout.Validator
                              ) {
        if (!EmptyFieldValidator().validate(layout.editText?.text.toString())) {
            layout.isErrorEnabled = true
            if (showError) {
                view?.setErrorOnField(layout, EmptyFieldValidator().errorTextResId)
            }
        } else if (validator is PhoneNumberValidator && !validator.validatePhoneNumber(
                        PLUS_SIGN + selectedDialingCode + layout.editText?.text.toString(),
                        selectedCountryCode)) {
            layout.isErrorEnabled = true
            if (showError) {
                view?.setErrorOnField(layout, validator.errorTextResId)
            }
        } else {
            layout.isErrorEnabled = false
            layout.error = null
        }
    }

    override fun setCountryCode(countryCode: String) {
        selectedCountryCode = countryCode
    }

    override fun setDialingCode(dialingCode: String) {
        selectedDialingCode = dialingCode
    }

    override fun getCountryCode(): String {
        return selectedCountryCode
    }

    companion object {
        const val PLUS_SIGN = "+"
    }
}
