package com.karhoo.karhootraveller.presentation.profile.user

import android.content.res.Resources
import com.karhoo.karhootraveller.presentation.base.BasePresenter
import com.karhoo.karhootraveller.service.analytics.KarhooAnalytics
import com.karhoo.uisdk.util.formatMobileNumber
import com.karhoo.uisdk.util.getCodeFromMobileNumber
import com.karhoo.uisdk.util.getMobileNumberWithoutCode
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.request.UserDetailsUpdateRequest
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.uisdk.analytics.Analytics

internal class UserProfilePresenter(view: UserProfileMVP.View,
                                    private val userStore: UserStore = KarhooApi.userStore,
                                    private val userService: UserService = KarhooApi.userService,
                                    private val analytics: Analytics = KarhooAnalytics.INSTANCE)
    : BasePresenter<UserProfileMVP.View>(), UserProfileMVP.Presenter {

    private var isEditingProfile = false
        set(value) {
            field = value
            view?.bindProfileEditMode(field)
        }

    internal var currentUser: UserInfo

    init {
        attachView(view)
        currentUser = userStore.currentUser.copy()
    }

    override fun isEditingProfile(): Boolean {
        return isEditingProfile
    }

    override fun validateUser() {
        view?.bindUserInfo(currentUser)
    }

    override fun onProfileFieldsChanged(canUpdateProfile: Boolean) {
        view?.bindProfileUpdateMode(canUpdateProfile)
    }

    override fun saveProfileEdit(firstName: String, lastName: String, mobilePhoneNumber: String) {
        analytics.userProfileSavePressed()
        val userId = userStore.currentUser.userId
        val locale = userStore.currentUser.locale

        view?.showProgressView()
        userService.updateUserDetails(UserDetailsUpdateRequest(
                userId = userId,
                firstName = firstName,
                lastName = lastName,
                phoneNumber = mobilePhoneNumber,
                locale = locale))
                .execute { result ->
                    when (result) {
                        is Resource.Success -> onProfileUpdateSuccess(result.data)
                        is Resource.Failure -> onProfileUpdateFailure(result.error)
                    }
                }
    }

    override fun beginProfileEdit() {
        analytics.userProfileEditPressed()
        isEditingProfile = true
    }

    override fun discardProfileEdit() {
        analytics.userProfileDiscardPressed()
        isEditingProfile = false
        validateUser()
    }

    override fun onProfileUpdateSuccess(updatedUserInfo: UserInfo) {
        isEditingProfile = false
        view?.showProfileUpdateSuccess(updatedUserInfo)
        view?.hideProgressView()
        view?.bindUserInfo(updatedUserInfo)
        analytics.userProfileUpdateSuccess(updatedUserInfo)
    }

    override fun onProfileUpdateFailure(error: KarhooError) {
        view?.showProfileUpdateFailure(error)
        view?.hideProgressView()
        analytics.userProfileUpdateFailed()
    }

    override fun validateMobileNumber(code: String, number: String): String {
        return formatMobileNumber(code, number)
    }

    override fun getCountryCodeFromPhoneNumber(number: String, res: Resources): String {
        return getCodeFromMobileNumber(number, res)
    }

    override fun removeCountryCodeFromPhoneNumber(number: String, res: Resources): String {
        return getMobileNumberWithoutCode(number, res)
    }

    override fun saveOnStop(firstName: String, lastName: String, mobilePhoneNumber: String) {
        currentUser = currentUser.copy(firstName = firstName, lastName = lastName, phoneNumber = mobilePhoneNumber)
    }
}
