package com.karhoo.karhootraveller.presentation.profile.user

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.base.listener.SimpleTextWatcher
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import kotlinx.android.synthetic.main.view_user_profile.view.countryCodeSpinner
import kotlinx.android.synthetic.main.view_user_profile.view.emailInput
import kotlinx.android.synthetic.main.view_user_profile.view.emailLayout
import kotlinx.android.synthetic.main.view_user_profile.view.firstNameInput
import kotlinx.android.synthetic.main.view_user_profile.view.firstNameLayout
import kotlinx.android.synthetic.main.view_user_profile.view.lastNameInput
import kotlinx.android.synthetic.main.view_user_profile.view.lastNameLayout
import kotlinx.android.synthetic.main.view_user_profile.view.mobileNumberInput
import kotlinx.android.synthetic.main.view_user_profile.view.mobileNumberLayout
import kotlinx.android.synthetic.main.view_user_profile.view.updateProfileMask
import kotlinx.android.synthetic.main.view_user_profile.view.updateUserDetailsProgressBar

class UserProfileView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null,
                                                defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), UserProfileMVP.View, LifecycleObserver {

    private var presenter: UserProfileMVP.Presenter = UserProfilePresenter(view = this)

    var actions: UserProfileMVP.Actions? = null

    private var allFieldsValidTextWatcher: SimpleTextWatcher = object : SimpleTextWatcher() {
        override fun afterTextChanged(s: Editable) {
            super.afterTextChanged(s)
            presenter.onProfileFieldsChanged(allFieldsValid())
        }
    }

    private val phoneNumber: String
        get() = presenter.validateMobileNumber(
                code = countryCodeSpinner.selectedItem.toString(),
                number = mobileNumberInput.text.toString()
                                              )

    init {
        View.inflate(context, R.layout.view_user_profile, this)

        bindProfileEditMode(presenter.isEditingProfile())

        initialiseFieldListeners()
        initialiseFieldValidators()
        initialiseFieldErrors()

        if (!isInEditMode) {
            presenter.validateUser()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        presenter.saveOnStop(
                firstName = firstNameInput.text.toString(),
                lastName = lastNameInput.text.toString(),
                mobilePhoneNumber = phoneNumber)
    }

    private fun saveUserProfile() {
        presenter.saveProfileEdit(
                firstName = firstNameInput.text.toString(),
                lastName = lastNameInput.text.toString(),
                mobilePhoneNumber = phoneNumber)
    }

    private fun initialiseFieldListeners() {
        firstNameInput.addTextChangedListener(allFieldsValidTextWatcher)
        firstNameInput.setOnFocusChangeListener { _, hasFocus ->
            onFocusChange(firstNameLayout, hasFocus)
        }

        lastNameInput.addTextChangedListener(allFieldsValidTextWatcher)
        lastNameInput.setOnFocusChangeListener { _, hasFocus ->
            onFocusChange(lastNameLayout, hasFocus)
        }

        mobileNumberInput.addTextChangedListener(allFieldsValidTextWatcher)
        mobileNumberInput.setOnFocusChangeListener { _, hasFocus ->
            onFocusChange(mobileNumberLayout, hasFocus)
        }
    }

    private fun initialiseFieldValidators() {
        mobileNumberLayout.setValidator(EmptyFieldValidator())
        firstNameLayout.setValidator(EmptyFieldValidator())
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

        mobileNumberLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_empty_field)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
    }

    private fun onFocusChange(layout: SelfValidatingTextLayout, hasFocus: Boolean) {
        layout.setFocus(hasFocus)
        presenter.onProfileFieldsChanged(allFieldsValid())
    }

    override fun bindUserInfo(userInfo: UserInfo) {
        firstNameInput.setText(userInfo.firstName)
        lastNameInput.setText(userInfo.lastName)
        emailInput.setText(userInfo.email)
        countryCodeSpinner.setCountryCode(presenter.getCountryCodeFromPhoneNumber(userInfo.phoneNumber, resources))
        mobileNumberInput.setText(presenter.removeCountryCodeFromPhoneNumber(userInfo.phoneNumber, resources))
    }

    override fun allFieldsValid(): Boolean {
        return mobileNumberLayout.isValid
                && firstNameLayout.isValid
                && lastNameLayout.isValid
    }

    override fun validateUser() {
        presenter.validateUser()
    }

    override fun onProfileEditButtonPressed() {
        presenter.beginProfileEdit()
    }

    override fun onProfileSaveButtonPressed() {
        saveUserProfile()
    }

    override fun onProfileEditDiscardButtonPressed() {
        presenter.discardProfileEdit()
    }

    override fun isEditingProfile(): Boolean {
        return presenter.isEditingProfile()
    }

    override fun bindProfileUpdateMode(canUpdateProfile: Boolean) {
        actions?.onProfileUpdateModeChanged(canUpdateProfile)
    }

    override fun bindProfileEditMode(isEditing: Boolean) {
        emailInput?.isEnabled = false
        emailLayout.isHintAnimationEnabled = false
        emailLayout.clearFocus()

        updateProfileMask.visibility =
                if (presenter.isEditingProfile())
                    View.GONE else View.VISIBLE

        firstNameLayout.isHintAnimationEnabled = isEditing
        lastNameLayout.isHintAnimationEnabled = isEditing
        mobileNumberLayout.isHintAnimationEnabled = isEditing

        firstNameLayout.clearFocus()
        lastNameLayout.clearFocus()
        mobileNumberLayout.clearFocus()

        if (isEditing) {
            firstNameLayout.requestFocus()
        }

        actions?.onProfileEditModeChanged(isEditing)
    }

    override fun showProfileUpdateSuccess(userInfo: UserInfo) {
        actions?.showSnackbar(SnackbarConfig(text = null, messageResId = R.string.kh_uisdk_profile_update_successful))
    }

    override fun showProfileUpdateFailure(error: KarhooError) {
        actions?.showSnackbar(SnackbarConfig(text = error.userFriendlyMessage, karhooError = error))
    }

    override fun showProgressView() {
        updateUserDetailsProgressBar.visibility = View.VISIBLE
    }

    override fun hideProgressView() {
        updateUserDetailsProgressBar.visibility = View.GONE
    }
}
