package com.karhoo.karhootraveller.presentation.register.registration

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.StringRes
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.base.listener.SimpleTextWatcher
import com.karhoo.karhootraveller.presentation.web.KarhooWebActivity
import com.karhoo.karhootraveller.service.analytics.KarhooAnalytics
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.validator.EmailValidator
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.validator.PasswordValidator
import com.karhoo.uisdk.base.validator.PhoneNumberValidator
import kotlinx.android.synthetic.main.view_registration_details.view.countryCodeSpinner
import kotlinx.android.synthetic.main.view_registration_details.view.emailInput
import kotlinx.android.synthetic.main.view_registration_details.view.emailLayout
import kotlinx.android.synthetic.main.view_registration_details.view.firstNameInput
import kotlinx.android.synthetic.main.view_registration_details.view.firstNameLayout
import kotlinx.android.synthetic.main.view_registration_details.view.lastNameInput
import kotlinx.android.synthetic.main.view_registration_details.view.lastNameLayout
import kotlinx.android.synthetic.main.view_registration_details.view.mobileNumberInput
import kotlinx.android.synthetic.main.view_registration_details.view.mobileNumberLayout
import kotlinx.android.synthetic.main.view_registration_details.view.passwordInput
import kotlinx.android.synthetic.main.view_registration_details.view.passwordLayout
import kotlinx.android.synthetic.main.view_registration_details.view.privacyLabel
import kotlinx.android.synthetic.main.view_registration_details.view.registerButton
import kotlinx.android.synthetic.main.view_registration_details.view.termsAndConditionsLabel

class RegistrationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), RegistrationMVP.View, View.OnFocusChangeListener {

    private var presenter: RegistrationMVP.Presenter? = RegistrationPresenter(this, KarhooAnalytics.INSTANCE)
    var actions: Actions? = null

    private val locale: String
        @TargetApi(Build.VERSION_CODES.N)
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0).toString()
        } else {
            resources.configuration.locale.toString()
        }

    private var passwordTextWatcher: SimpleTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            passwordLayout.enableColoredError()
            presenter?.setRegistrationMode(allFieldsValid())
        }
    }

    private val allFieldsValidTextWatcher: SimpleTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            presenter?.setRegistrationMode(allFieldsValid())
        }
    }

    private val phoneNumber: String
        get() = presenter?.validateMobileNumber(countryCodeSpinner.selectedItem.toString(),
                                                mobileNumberInput.text.toString()).orEmpty()

    init {
        inflate(context, R.layout.view_registration_details, this)

        initialiseListeners()
        addValidators()
        setErrors()

        if (!isInEditMode) {
            presenter?.registrationStarted()
        }
    }

    private fun setErrors() {
        emailLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_email)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
        mobileNumberLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_phone_number)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
        firstNameLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_empty_field)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
        lastNameLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_empty_field)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
        passwordLayout.apply {
            errorColorEnabled = true
            setErrorMsg(R.string.kh_uisdk_invalid_password)
            setHelper(true)
        }
    }

    private fun addValidators() {
        emailLayout.setValidator(EmailValidator())
        passwordLayout.setValidator(PasswordValidator())
        mobileNumberLayout.setValidator(getPhoneNumberValidator())
        firstNameLayout.setValidator(EmptyFieldValidator())
        lastNameLayout.setValidator(EmptyFieldValidator())
    }

    private fun getPhoneNumberValidator(): PhoneNumberValidator {
        val validator = PhoneNumberValidator()
        countryCodeSpinner?.selectedItem?.let {
            validator.setCountryCode(it.toString())
        }
        return validator
    }

    private fun initialiseListeners() {
        passwordInput.setOnFocusChangeListener { _, hasFocus -> passwordLayout.setFocus(hasFocus) }
        emailInput.setOnFocusChangeListener { _, hasFocus ->
            emailLayout.setFocus(hasFocus)
            emailLayout.enableColoredError()
        }
        mobileNumberInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                mobileNumberLayout.setValidator(getPhoneNumberValidator())
            }
            mobileNumberLayout.setFocus(hasFocus)
        }
        firstNameInput.setOnFocusChangeListener { _, hasFocus -> firstNameLayout.setFocus(hasFocus) }
        lastNameInput.setOnFocusChangeListener { _, hasFocus -> lastNameLayout.setFocus(hasFocus) }

        registerButton.setOnClickListener { presenter?.registerUser() }
        termsAndConditionsLabel.setOnClickListener { presenter?.goToTerms() }
        privacyLabel.setOnClickListener { presenter?.goToPrivacy() }

        passwordLayout.editText?.addTextChangedListener(passwordTextWatcher)

        emailLayout.editText?.addTextChangedListener(allFieldsValidTextWatcher)
        mobileNumberLayout.editText?.addTextChangedListener(allFieldsValidTextWatcher)
        firstNameLayout.editText?.addTextChangedListener(allFieldsValidTextWatcher)
        lastNameLayout.editText?.addTextChangedListener(allFieldsValidTextWatcher)
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        presenter?.setRegistrationMode(allFieldsValid())
    }

    override fun registrationPossible(isRegistrationPossible: Boolean) {
        registerButton.isEnabled = isRegistrationPossible
        actions?.userRegistrationFailed()
    }

    override fun allFieldsValid(): Boolean {
        return emailLayout.isValid && passwordLayout.isValid &&
                mobileNumberLayout.isValid && firstNameLayout.isValid &&
                lastNameLayout.isValid
    }

    override fun userRegistered(userInfo: UserInfo) {
        actions?.userRegistered(userInfo)
    }

    override fun userRegistering() {
        completeRegistration()
    }

    override fun completeRegistration() {
        presenter?.completeRegistration(UserInfo(
                firstName = firstNameInput.text.toString(),
                lastName = lastNameInput.text.toString(),
                email = emailInput.text.toString(),
                phoneNumber = phoneNumber,
                locale = locale), passwordInput.text.toString(), KarhooApi.userService)
    }

    override fun goToTerms() {
        val webIntent = KarhooWebActivity.Builder.builder
                .setScrollable(true)
                .url(context.getString(R.string.kh_uisdk_link_t_n_c_terms))
                .build(context)
        context.startActivity(webIntent)
    }

    override fun goToPrivacy() {
        val webIntent = KarhooWebActivity.Builder.builder
                .setScrollable(true)
                .url(context.getString(R.string.kh_uisdk_link_t_n_c_privacy))
                .build(context)
        context.startActivity(webIntent)
    }

    override fun showError(@StringRes errorMessage: Int, karhooError: KarhooError?) {
        actions?.userRegistrationFailed()
        actions?.showSnackbar(SnackbarConfig(text = null, messageResId = errorMessage))
    }

    interface Actions {

        fun userRegistrationFailed()

        fun userRegistered(userInfo: UserInfo)

        fun showSnackbar(snackbarConfig: SnackbarConfig)

    }
}
