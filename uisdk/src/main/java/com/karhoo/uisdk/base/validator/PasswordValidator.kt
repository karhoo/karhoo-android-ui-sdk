package com.karhoo.uisdk.base.validator

import android.annotation.SuppressLint
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

@Suppress("ComplexCondition")
class PasswordValidator : SelfValidatingTextLayout.Validator {

    @SuppressLint("DefaultLocale")
    override fun validate(field: String): Boolean {

        //must be minimum eight characters
        //must contain a lowercase character
        //must contain an uppercase character
        //must contain a numeric digit
        if ((field.length < MIN_PASSWORD_LENGTH)
                || (field.toUpperCase() == field)
                || (field.toLowerCase() == field)
                || (!field.contains(Regex(".*\\d+.*")))) return false

        return true
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
    }

}
