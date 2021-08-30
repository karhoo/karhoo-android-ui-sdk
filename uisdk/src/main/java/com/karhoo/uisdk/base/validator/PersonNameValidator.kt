package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

class PersonNameValidator : SelfValidatingTextLayout.Validator {

    override val errorTextResId = R.string.kh_uisdk_invalid_input_value

    override fun validate(field: String): Boolean =
        field.all { it.isLetter() || it.isWhitespace() || it.toString() == ("-") }

}
