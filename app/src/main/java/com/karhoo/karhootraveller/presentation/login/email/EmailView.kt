package com.karhoo.karhootraveller.presentation.login.email

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.base.listener.SimpleTextWatcher
import com.karhoo.uisdk.base.validator.EmailValidator
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import kotlinx.android.synthetic.main.view_email.view.emailInput
import kotlinx.android.synthetic.main.view_email.view.emailLayout

class EmailView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    var validEmailChangedListener: ValidEmailChangedListener? = null

    private var validFieldsTextWatcher: SimpleTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            validEmailChangedListener?.onEmailValidChanged(emailLayout.isValid)
        }
    }

    init {
        inflate(context, R.layout.view_email, this)

        emailLayout.apply {
            setValidator(EmailValidator())
            editText?.addTextChangedListener(validFieldsTextWatcher)
            setErrorMsg(R.string.invalid_email)
        }

        emailInput.setOnFocusChangeListener { _, hasFocus -> handleFocusChange(emailLayout, hasFocus) }
    }

    fun email(): String = emailInput.text.toString()

    private fun handleFocusChange(layout: SelfValidatingTextLayout, hasFocus: Boolean) {
        layout.setFocus(hasFocus)
    }

}

interface ValidEmailChangedListener {
    fun onEmailValidChanged(isValid: Boolean)
}
