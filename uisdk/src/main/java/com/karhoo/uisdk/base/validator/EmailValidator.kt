package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

class EmailValidator : SelfValidatingTextLayout.Validator {

    override val errorTextResId = R.string.kh_uisdk_invalid_email

    override fun validate(field: String): Boolean = android.util.Patterns
        .EMAIL_ADDRESS.matcher(field).matches()

}
