package com.karhoo.uisdk.screen.booking.checkout.passengerdetails

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.addTextChangedListener
import androidx.preference.PreferenceManager
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.heetch.countrypicker.Utils
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.validator.EmailValidator
import com.karhoo.uisdk.base.validator.PhoneNumberValidator
import com.karhoo.uisdk.base.validator.PersonNameValidator
import com.karhoo.uisdk.base.view.countrycodes.CountryPickerActivity
import com.karhoo.uisdk.screen.booking.checkout.passengerdetails.PassengerDetailsPresenter.Companion.PLUS_SIGN
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.showSoftKeyboard
import com.karhoo.uisdk.util.parsePhoneNumber
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.countryFlagImageView
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.countryFlagLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_passenger_details.view.countryPrefixCodeText
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
                                                    ) :
        ConstraintLayout(context, attrs, defStyleAttr), PassengerDetailsContract.View {

    private val presenter: PassengerDetailsContract.Presenter = PassengerDetailsPresenter(this)
    var validationCallback: PassengerDetailsContract.Validator? = null

    init {
        View.inflate(context, R.layout.uisdk_view_booking_passenger_details, this)
        initialiseFieldListeners()
    }

    private fun validateAll() {
        validateFirstNameField(false)
        validateLastNameField(false)
        validateEmailField(false)
        validateMobileNumberField(false)
    }

    private fun validateFirstNameField(showError: Boolean) {
        presenter.validateField(firstNameLayout, showError, PersonNameValidator())
    }

    private fun validateLastNameField(showError: Boolean) {
        presenter.validateField(lastNameLayout, showError, PersonNameValidator())
    }

    private fun validateEmailField(showError: Boolean) {
        presenter.validateField(emailLayout, showError, EmailValidator())
    }

    private fun validateMobileNumberField(showError: Boolean) {
        presenter.validateField(mobileNumberLayout, showError, PhoneNumberValidator())
    }

    private fun initialiseFieldListeners() {
        firstNameInput.addTextChangedListener {
            validateFirstNameField(true)
            validationCallback?.onFieldsValidated(areFieldsValid())
        }
        lastNameInput.addTextChangedListener {
            validateLastNameField(true)
            validationCallback?.onFieldsValidated(areFieldsValid())
        }

        emailInput.addTextChangedListener {
            validateEmailField(true)
            validationCallback?.onFieldsValidated(areFieldsValid())
        }

        mobileNumberInput.addTextChangedListener {
            validateMobileNumberField(true)
            validationCallback?.onFieldsValidated(areFieldsValid())
        }

        countryFlagLayout.setOnClickListener { _ ->
            val builder = CountryPickerActivity.Builder().countryCode(presenter.getCountryCode())
            (context as Activity).startActivityForResult(builder.build(context),
                                                         CountryPickerActivity
                                                                 .COUNTRY_PICKER_ACTIVITY_CODE)
        }

        firstNameInput.onFocusChangeListener = createFocusChangeListener()
        lastNameInput.onFocusChangeListener = createFocusChangeListener()
        emailInput.onFocusChangeListener = createFocusChangeListener()
        mobileNumberInput.onFocusChangeListener = createFocusChangeListener()
    }

    override fun setErrorOnField(field: TextInputLayout, errorId: Int) {
        field.error = resources.getString(errorId)
    }

    override fun setCountryFlag(countryCode: String, dialingCode: String, validateField: Boolean,
                                focusPhoneNumber: Boolean) {
        val countryFlag = Utils.getMipmapResId(context, countryCode.toLowerCase() + "_flag")

        countryFlagImageView.setImageResource(countryFlag)
        countryPrefixCodeText.text = PLUS_SIGN + dialingCode

        presenter.setCountryCode(countryCode)
        presenter.setDialingCode(dialingCode)

        if (validateField) {
            presenter.validateField(mobileNumberLayout, true, PhoneNumberValidator())
            validationCallback?.onFieldsValidated(areFieldsValid())
        }

        if(focusPhoneNumber){
            mobileNumberInput.requestFocus()
        }
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
            }
        }

    }

    override fun areFieldsValid(): Boolean {
        return !firstNameLayout.isErrorEnabled
                && !lastNameLayout.isErrorEnabled
                && !emailLayout.isErrorEnabled
                && !mobileNumberLayout.isErrorEnabled
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
        mobileNumberInput.setText(presenter.removeCountryCodeFromPhoneNumber(passengerDetails.phoneNumber, resources))
        setCountryFlag(presenter.getCountryCode(context), presenter.getDialingCode(context), validateField = true)

        validateAll()
        if(!areFieldsValid())
            validationCallback?.onFieldsValidated(!areFieldsValid())
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

    override fun clickOnSaveButton() {
        presenter.updatePassengerDetails(
                firstName = firstNameInput.text.toString(),
                lastName = lastNameInput.text.toString(),
                email = emailInput.text.toString(),
                mobilePhoneNumber = parsePhoneNumber(mobileNumberInput.text.toString(), presenter
                        .getCountryCode()))

        getPassengerDetails()?.let {
            storePassenger(it)
        }
    }

    override fun storePassenger(passenger: PassengerDetails) {
        val pd = Gson().toJson(passenger)
        val countryCode = presenter.getCountryCode()
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPrefs.edit().putString(PASSENGER_DETAILS_SHARED_PREFS, pd).apply()
        sharedPrefs.edit().putString(COUNTRY_CODE_SHARED_PREFS, countryCode).apply()
    }

    override fun retrievePassenger(): PassengerDetails? {
        if (getPassengerDetails() != null) {
            return getPassengerDetails()
        }
        return try {
            val storedCountryCode = retrieveCountryCodeFromSharedPrefs()
            if (storedCountryCode != null) {
                presenter.setCountryCode(storedCountryCode)
            }

            retrievePassengerFromSharedPrefs()
        } catch (e: Exception) {
            null
        }
    }

    override fun retrievePassengerFromSharedPrefs(): PassengerDetails? {
        return try {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val json = sharedPrefs.getString(PASSENGER_DETAILS_SHARED_PREFS, null)
            return Gson().fromJson(json, PassengerDetails::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override fun retrieveCountryCodeFromSharedPrefs(): String? {
        return try {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPrefs.getString(COUNTRY_CODE_SHARED_PREFS, null)
        } catch (e: Exception) {
            null
        }
    }

    override fun revertPassengerDetails() {
        val passengerDetails = getPassengerDetails()
        passengerDetails?.let {
            bindPassengerDetails(it)
        } ?: run {
            firstNameInput.setText("")
            lastNameInput.setText("")
            emailInput.setText("")
            mobileNumberInput.setText("")

            validationCallback?.onFieldsValidated(true)
        }
    }


    companion object {
        private const val PASSENGER_DETAILS_SHARED_PREFS = "PASSENGER_DETAILS_SHARED_PREFS"
        private const val COUNTRY_CODE_SHARED_PREFS = "COUNTRY_CODE_SHARED_PREFS"
    }
}
