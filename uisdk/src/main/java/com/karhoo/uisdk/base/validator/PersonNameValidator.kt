package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import java.util.regex.Pattern

class PersonNameValidator : SelfValidatingTextLayout.Validator {

    override val errorTextResId = R.string.kh_uisdk_invalid_input_value

    override fun validate(field: String): Boolean = Pattern.compile("^[^!@#\$%^&*~()_+=\\|/<>]+\$")
                                                                        .matcher(field).matches()

}

