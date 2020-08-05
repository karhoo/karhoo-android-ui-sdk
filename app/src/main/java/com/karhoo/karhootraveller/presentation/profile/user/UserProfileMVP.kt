package com.karhoo.karhootraveller.presentation.profile.user

import android.content.res.Resources
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.base.listener.ErrorView

interface UserProfileMVP {

    interface View {

        fun bindUserInfo(userInfo: UserInfo)

        fun bindProfileEditMode(isEditing: Boolean)

        fun bindProfileUpdateMode(canUpdateProfile: Boolean)

        fun validateUser()

        fun allFieldsValid(): Boolean

        fun isEditingProfile(): Boolean

        fun onProfileEditButtonPressed()

        fun onProfileSaveButtonPressed()

        fun onProfileEditDiscardButtonPressed()

        fun showProfileUpdateSuccess(userInfo: UserInfo)

        fun showProfileUpdateFailure(error: KarhooError)

        fun showProgressView()

        fun hideProgressView()
    }

    interface Presenter {

        fun isEditingProfile(): Boolean

        fun validateUser()

        fun onProfileFieldsChanged(canUpdateProfile: Boolean)

        fun onProfileUpdateSuccess(updatedUserInfo: UserInfo)

        fun onProfileUpdateFailure(error: KarhooError)

        fun saveProfileEdit(firstName: String, lastName: String, mobilePhoneNumber: String)

        fun beginProfileEdit()

        fun discardProfileEdit()

        fun validateMobileNumber(code: String, number: String): String

        fun getCountryCodeFromPhoneNumber(number: String, res: Resources): String

        fun removeCountryCodeFromPhoneNumber(number: String, res: Resources): String

        fun saveOnStop(firstName: String, lastName: String, mobilePhoneNumber: String)
    }

    interface Actions : ErrorView {

        fun onProfileUpdateModeChanged(canUpdateProfile: Boolean)

        fun onProfileEditModeChanged(canEditProfile: Boolean)
    }

}
