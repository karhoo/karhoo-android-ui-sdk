package com.karhoo.uisdk.base.validator

import android.annotation.SuppressLint
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

class PasswordValidator : SelfValidatingTextLayout.Validator {

    @SuppressLint("DefaultLocale")
    override fun validate(field: String): Boolean {

        //must be minimum eight characters
        if (field.length < 8) return false

        //must contain a lowercase character
        if (field.toUpperCase() == field) return false

        //must contain an uppercase character
        if (field.toLowerCase() == field) return false

        //must contain a numeric digit
        if (!field.contains(Regex(".*\\d+.*"))) return false

        return true
    }

}
