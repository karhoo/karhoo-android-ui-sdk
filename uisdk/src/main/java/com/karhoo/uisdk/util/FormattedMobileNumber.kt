package com.karhoo.uisdk.util

import android.content.res.Resources
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.karhoo.uisdk.R

fun formatMobileNumber(code: String, number: String): String {
    return if (number.startsWith("0")) code + number.substring(1) else code + number
}

fun getCodeFromMobileNumber(number: String, res: Resources): String {
    res.getStringArray(R.array.country_codes).map { countryCode ->
        if (number.contains(countryCode)) {
            return countryCode
        }
    }
    return ""
}

fun getMobileNumberWithoutCode(number: String, res: Resources): String {
    val code = getCodeFromMobileNumber(number, res)
    return number.removePrefix(code)
}

fun isValidNumber(number: String, countryCode: String): Boolean {
    val util = PhoneNumberUtil.getInstance()
    return try {
        val phoneNumber = util.parse(number, countryCode)
        util.isValidNumber(phoneNumber)
    } catch (e: Exception) {
        false
    }
}

fun parsePhoneNumber(number: String, countryCode: String): String {
    val util = PhoneNumberUtil.getInstance()
    try {
        val phoneNumber = util.parse(number, countryCode)
        if(util.isValidNumber(phoneNumber)) {
            return "+" + phoneNumber.countryCode + phoneNumber.nationalNumber
        }

        return ""
    } catch (e: Exception) {
        return ""
    }
}
