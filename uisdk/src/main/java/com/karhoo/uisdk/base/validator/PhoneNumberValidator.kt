package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.uisdk.util.isValidNumber

class PhoneNumberValidator : SelfValidatingTextLayout.Validator {
    override val errorTextResId = R.string.kh_uisdk_invalid_phone_number

    fun validate(field: String, secondField: String): Boolean {
        return if (field.isNotBlank()) {
            secondField?.let {
                val formattedNumber = formatMobileNumber(it, field)
                isValidNumber(formattedNumber, secondField)
            }
        } else {
            false
        }
    }

    fun validatePhoneNumber(field: String, countryCode: String): Boolean {
        return isValidNumber(field, countryCode)
    }

    override fun validate(field: String): Boolean {
        return true
    }
}
