package com.karhoo.uisdk.screen.booking.booking.passengerdetails

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.validator.EmailValidator
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.validator.PhoneNumberValidator
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.showSoftKeyboard
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.countryCodeSpinner
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.emailInput
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.emailLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.firstNameInput
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.firstNameLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.lastNameInput
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.lastNameLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.mobileNumberInput
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.mobileNumberLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.updatePassengerDetailsMask

class PassengerDetailsView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), PassengerDetailsMVP.View {

    private val presenter: PassengerDetailsMVP.Presenter = PassengerDetailsPresenter(this)


    private val locale: String
        @TargetApi(Build.VERSION_CODES.N)
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0).toString()
        } else {
            resources.configuration.locale.toString()
        }

    private val phoneNumber: String
        get() = presenter.validateMobileNumber(
                code = countryCodeSpinner.selectedItem.toString(),
                number = mobileNumberInput.text.toString())

    init {
        View.inflate(context, R.layout.uisdk_view_booking_passenger_details, this)
        initialiseFieldListeners()
        initialiseFieldValidators()
        initialiseFieldErrors()
    }

    private fun initialiseFieldListeners() {
        firstNameInput.setOnFocusChangeListener { view, hasFocus ->
            onFocusChanged(firstNameLayout, hasFocus)
        }

        lastNameInput.setOnFocusChangeListener { _, hasFocus ->
            onFocusChanged(lastNameLayout, hasFocus)
        }

        emailInput.setOnFocusChangeListener { _, hasFocus ->
            onFocusChanged(emailLayout, hasFocus)
            emailLayout.enableColoredError()
        }

        mobileNumberInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                mobileNumberLayout.setValidator(getPhoneNumberValidator())
            }
            onFocusChanged(mobileNumberLayout, hasFocus)
        }
    }

    /**
     *  Method used to check if any input text has focus, if not then we close the keyboard
     *  @param view The view that last had the focus and can close the keyboard
     */
    private fun hideKeyboardIfNothingFocus(view: View) {
        if (!nameHasFocus()
                && !emailInput.hasFocus()
                && !mobileNumberInput.hasFocus()) {
            view.hideSoftKeyboard()
        }
    }

    private fun nameHasFocus(): Boolean {
        return firstNameInput.hasFocus()
                || lastNameInput.hasFocus()
    }

    private fun getPhoneNumberValidator(): PhoneNumberValidator {
        val validator = PhoneNumberValidator()
        countryCodeSpinner?.selectedItem?.let {
            validator.setCountryCode(it.toString())
        }
        return validator
    }

    private fun onFocusChanged(layout: SelfValidatingTextLayout, hasFocus: Boolean) {
        layout.setFocus(hasFocus)
        if (!hasFocus) {
            hideKeyboardIfNothingFocus(layout)
            presenter.updatePassengerDetails(
                    firstName = firstNameInput.text.toString(),
                    lastName = lastNameInput.text.toString(),
                    email = emailInput.text.toString(),
                    mobilePhoneNumber = phoneNumber)
        }
    }

    private fun initialiseFieldValidators() {
        mobileNumberLayout.setValidator(getPhoneNumberValidator())
        firstNameLayout.setValidator(EmptyFieldValidator())
        emailLayout.setValidator(EmailValidator())
        lastNameLayout.setValidator(EmptyFieldValidator())
    }

    private fun initialiseFieldErrors() {
        firstNameLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_empty_field)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }

        lastNameLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_empty_field)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }

        emailLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_email)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }

        mobileNumberLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_phone_number)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
    }

    override fun setPassengerDetails(passengerDetails: PassengerDetails) {
        presenter.prefillForPassengerDetails(passengerDetails)
    }

    /**
     * Checks of any field is invalid, if so we focus on it and open the keyboard
     * @return if we find any invalid field we return `true`
     */
    override fun findAndfocusFirstInvalid(): Boolean {
        getFirstInvalid()?.let {
            it.requestFocus()
            it.editText?.showSoftKeyboard()
            return true
        }
        return false
    }

    override fun getPassengerDetails(): PassengerDetails {
        return PassengerDetails(
                firstName = firstNameInput.text.toString(),
                lastName = lastNameInput.text.toString(),
                email = emailInput.text.toString(),
                phoneNumber = phoneNumber,
                locale = locale)
    }

    override fun bindPassengerDetails(passengerDetails: PassengerDetails) {
        firstNameInput.setText(passengerDetails.firstName)
        lastNameInput.setText(passengerDetails.lastName)
        emailInput.setText(passengerDetails.email)
        countryCodeSpinner.setCountryCode(presenter.getCountryCodeFromPhoneNumber(passengerDetails.phoneNumber, resources))
        mobileNumberInput.setText(presenter.removeCountryCodeFromPhoneNumber(passengerDetails.phoneNumber, resources))
    }

    override fun bindEditMode(isEditing: Boolean) {
        updatePassengerDetailsMask.visibility = if (isEditing) View.GONE else View.VISIBLE
    }

    private fun getFirstInvalid(): SelfValidatingTextLayout? {
        var invalidTextLayout: SelfValidatingTextLayout? = null
        if (!mobileNumberLayout.isValid) invalidTextLayout = mobileNumberLayout
        if (!emailLayout.isValid) invalidTextLayout = emailLayout
        if (!lastNameLayout.isValid) invalidTextLayout = lastNameLayout
        if (!firstNameLayout.isValid) invalidTextLayout = firstNameLayout
        return invalidTextLayout
    }

    override fun allFieldsValid(): Boolean {
        return emailLayout.isValid
                && mobileNumberLayout.isValid
                && firstNameLayout.isValid
                && lastNameLayout.isValid
    }
}
