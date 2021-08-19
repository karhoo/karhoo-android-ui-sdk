package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.uisdk.util.isValidNumber

class PhoneNumberValidator {
    fun validate(field: String, countryCode: String?): Boolean {
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
