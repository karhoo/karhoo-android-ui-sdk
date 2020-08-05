package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.uisdk.util.isValidNumber

class PhoneNumberValidator : SelfValidatingTextLayout.Validator {

    private var countryCode: String? = null

    fun setCountryCode(countryCode: String) {
        this.countryCode = countryCode
    }

    override fun validate(field: String): Boolean {
        return if (field.isNotBlank()) {
            countryCode?.let {
                val formattedNumber = formatMobileNumber(it, field)
                isValidNumber(formattedNumber)
            } ?: false
        } else {
            false
        }
    }
}
