package com.karhoo.karhootraveller.presentation.login.signin

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import com.karhoo.karhootraveller.BuildConfig
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.base.listener.SimpleTextWatcher
import com.karhoo.karhootraveller.presentation.splash.domain.KarhooAppVersionValidator
import com.karhoo.karhootraveller.service.analytics.KarhooAnalytics
import com.karhoo.karhootraveller.service.preference.KarhooPreferenceStore
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.uisdk.base.validator.EmailValidator
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import kotlinx.android.synthetic.main.view_login.view.emailInput
import kotlinx.android.synthetic.main.view_login.view.emailLayout
import kotlinx.android.synthetic.main.view_login.view.passwordInput
import kotlinx.android.synthetic.main.view_login.view.passwordLayout
import kotlinx.android.synthetic.main.view_login.view.signInButton

class LoginView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), LoginMVP.View {

    private val passwordEdit: EditText? by lazy { passwordLayout.editText }
    private val emailEdit: EditText? by lazy { emailLayout.editText }
    private val presenter: LoginMVP.Presenter by lazy {
        LoginPresenter(KarhooApi.userService, this,
                       KarhooAnalytics.INSTANCE, KarhooAppVersionValidator(BuildConfig.VERSION_CODE, KarhooPreferenceStore.getInstance(context)))
    }

    internal var actions: Actions? = null

    private var validFieldsTextWatcher: SimpleTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            presenter.setLoginMode(allFieldsValid())
        }
    }

    init {
        inflate(context, R.layout.view_login, this)
        initialiseListeners()
        addValidators()
        setErrors()
    }

    private fun initialiseListeners() {
        signInButton.setOnClickListener { beginSignIn() }
        emailInput.setOnFocusChangeListener { _, hasFocus -> handleFocusChange(emailLayout, hasFocus) }
        passwordInput.setOnFocusChangeListener { _, hasFocus -> handleFocusChange(passwordLayout, hasFocus) }
        emailEdit?.addTextChangedListener(validFieldsTextWatcher)
        passwordEdit?.addTextChangedListener(validFieldsTextWatcher)
    }

    private fun addValidators() {
        emailLayout.setValidator(EmailValidator())
        passwordLayout.setValidator(EmptyFieldValidator())
    }

    private fun setErrors() {
        emailLayout.apply {
            setErrorMsg(R.string.invalid_email)
            setErrorTextAppearance(R.style.Text_Red_Small)

        }
        passwordLayout.apply {
            setErrorMsg(R.string.invalid_password)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
    }

    override fun loginSuccessful() {
        actions?.goToBooking()
    }

    override fun enableLogin(loginAvailable: Boolean) {
        signInButton.isEnabled = loginAvailable
    }

    override fun onError() {
        actions?.hideProgress()
        passwordLayout.error = context.getString(R.string.error_invalid_password)
    }

    private fun handleFocusChange(layout: SelfValidatingTextLayout, hasFocus: Boolean) {
        layout.setFocus(hasFocus)
        presenter.setLoginMode(allFieldsValid())
    }

    private fun allFieldsValid(): Boolean {
        return emailLayout.isValid && passwordLayout.isValid
    }

    private fun beginSignIn() {
        actions?.showProgress()
        passwordLayout.error = ""
        presenter.loginUser(emailEdit?.text.toString(), passwordEdit?.text.toString())
    }

    internal interface Actions {

        fun goToBooking()

        fun showProgress()

        fun hideProgress()

    }
}
