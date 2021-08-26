package com.karhoo.uisdk.util

import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import java.util.regex.Pattern

class PersonNameValidator : SelfValidatingTextLayout.Validator {

    override fun validate(field: String): Boolean = Pattern.compile("^[^!@#\$%^&*~()_+=\\|/<>]+\$")
                                                                        .matcher(field).matches()

}

