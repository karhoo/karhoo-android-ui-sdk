package com.karhoo.uisdk.screen.booking.checkout.passengerdetails

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.validator.EmailValidator
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.validator.PhoneNumberValidator
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

class PassengerDetailsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), PassengerDetailsMVP.View {

    private val presenter: PassengerDetailsMVP.Presenter = PassengerDetailsPresenter(this)

    private val phoneNumber: String
        get() = presenter.validateMobileNumber(
            code = countryCodeSpinner.selectedItem.toString(),
            number = mobileNumberInput.text.toString()
        )

    init {
        View.inflate(context, R.layout.uisdk_view_booking_passenger_details, this)
        initialiseFieldListeners()
        retrievePassenger()
    }

    private fun initialiseFieldListeners() {
        firstNameInput.addTextChangedListener {
            if (!EmptyFieldValidator().validate(firstNameInput.text.toString())) {
                firstNameLayout.error = resources.getString(R.string.kh_uisdk_invalid_empty_field)
            } else {
                firstNameLayout.error = null
            }
        }
        lastNameInput.addTextChangedListener {
            if (!EmptyFieldValidator().validate(lastNameInput.text.toString())) {
                lastNameLayout.error = resources.getString(R.string.kh_uisdk_invalid_empty_field)
            } else {
                lastNameLayout.error = null
            }
        }

        emailInput.addTextChangedListener {
            if (!EmailValidator().validate(emailInput.text.toString())) {
                emailLayout.error = resources.getString(R.string.kh_uisdk_invalid_email)
            } else {
                emailLayout.error = null
            }
        }

        mobileNumberInput.addTextChangedListener {
            if (!PhoneNumberValidator().validate(mobileNumberInput.text.toString())) {
                mobileNumberLayout.error =
                    resources.getString(R.string.kh_uisdk_invalid_phone_number)
            } else {
                mobileNumberLayout.error = null
            }
        }

        firstNameInput.onFocusChangeListener = createFocusChangeListener()
        lastNameInput.onFocusChangeListener = createFocusChangeListener()
        emailInput.onFocusChangeListener = createFocusChangeListener()
        mobileNumberInput.onFocusChangeListener = createFocusChangeListener()
    }

    /**
     *  Method used to check if any input text has focus, if not then we close the keyboard
     *  @param view The view that last had the focus and can close the keyboard
     */
    private fun hideKeyboardIfNothingFocus(view: View) {
        if (!nameHasFocus()
            && !emailInput.hasFocus()
            && !mobileNumberInput.hasFocus()
        ) {
            view.hideSoftKeyboard()
        }
    }

    private fun nameHasFocus(): Boolean {
        return firstNameInput.hasFocus()
                || lastNameInput.hasFocus()
    }

    private fun createFocusChangeListener(): OnFocusChangeListener {
        return OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                v.requestFocus()
            } else {
                v.clearFocus()
                hideKeyboardIfNothingFocus(v)
                presenter.updatePassengerDetails(
                    firstName = firstNameInput.text.toString(),
                    lastName = lastNameInput.text.toString(),
                    email = emailInput.text.toString(),
                    mobilePhoneNumber = phoneNumber
                )
            }
        }

    }

    override fun areFieldsValid(): Boolean {
        var isValid = false
        if (!EmptyFieldValidator().validate(firstNameInput.text.toString())) {
            isValid = true
        } else if (!EmptyFieldValidator().validate(lastNameInput.text.toString())) {
            isValid = true
        } else if (!EmailValidator().validate(emailInput.text.toString())) {
            isValid = true
        } else if (!PhoneNumberValidator().validate(mobileNumberInput.text.toString())) {
            isValid = true
        }
        return isValid
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

    override fun getPassengerDetails(): PassengerDetails? {
        return presenter.passengerDetailsValue()
    }

    override fun bindPassengerDetails(passengerDetails: PassengerDetails) {
        firstNameInput.setText(passengerDetails.firstName)
        lastNameInput.setText(passengerDetails.lastName)
        emailInput.setText(passengerDetails.email)
        countryCodeSpinner.setCountryCode(
            presenter.getCountryCodeFromPhoneNumber(
                passengerDetails.phoneNumber,
                resources
            )
        )
        mobileNumberInput.setText(
            presenter.removeCountryCodeFromPhoneNumber(
                passengerDetails.phoneNumber,
                resources
            )
        )
    }

    override fun bindEditMode(isEditing: Boolean) {
        updatePassengerDetailsMask.visibility = if (isEditing) View.GONE else View.VISIBLE
    }

    private fun getFirstInvalid(): TextInputLayout? {
        var invalidTextLayout: TextInputLayout? = null
        if (mobileNumberLayout.error != null) invalidTextLayout = mobileNumberLayout
        if (emailLayout.error != null) invalidTextLayout = emailLayout
        if (lastNameLayout.error != null) invalidTextLayout = lastNameLayout
        if (firstNameLayout.error != null) invalidTextLayout = firstNameLayout
        return invalidTextLayout
    }

    override fun allFieldsValid(): Boolean {
        return emailLayout.error != null
                && mobileNumberLayout.error != null
                && firstNameLayout.error != null
                && lastNameLayout.error != null
    }

    override fun storePassenger(passengerDetails: PassengerDetails) {
        val pd = Gson().toJson(passengerDetails)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPrefs.edit().putString(PASSENGER_DETAILS_SHARED_PREFS, pd).apply()
    }

    override fun retrievePassenger(): PassengerDetails? {
        return try {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val json = sharedPrefs.getString(PASSENGER_DETAILS_SHARED_PREFS, null)
            Gson().fromJson(json, PassengerDetails::class.java)
        } catch (e: Exception) {
            null
        }
    }

    companion object {
        private const val PASSENGER_DETAILS_SHARED_PREFS = "PASSENGER_DETAILS_SHARED_PREFS"
    }
}
