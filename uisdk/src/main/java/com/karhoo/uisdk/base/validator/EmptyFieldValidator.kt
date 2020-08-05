package com.karhoo.uisdk.base.validator

import com.karhoo.uisdk.base.view.SelfValidatingTextLayout

class EmptyFieldValidator : SelfValidatingTextLayout.Validator {

    override fun validate(field: String): Boolean = field.isNotEmpty()

}
