package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

class EmailValidator : SelfValidatingTextLayout.Validator {

    override fun validate(field: String): Boolean = android.util.Patterns.EMAIL_ADDRESS.matcher(field).matches()

}
