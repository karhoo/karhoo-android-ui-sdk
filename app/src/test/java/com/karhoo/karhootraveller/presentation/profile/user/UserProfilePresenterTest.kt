package com.karhoo.karhootraveller.presentation.profile.user

import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.UserStore
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.user.UserService
import com.karhoo.sdk.call.Call
import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UserProfilePresenterTest {

    private var view: UserProfileMVP.View = mock()
    private var userStore: UserStore = mock()
    private val userService: UserService = mock()
    private val call: Call<UserInfo> = mock()
    private val analytics: Analytics = mock()
    private val oldUserInfo: UserInfo = mock()
    private val newUserInfo: UserInfo = mock()

    private val lambdaCaptor = argumentCaptor<(Resource<UserInfo>) -> Unit>()

    private lateinit var presenter: UserProfilePresenter

    @Before
    fun setUp() {
        whenever(userService.updateUserDetails(any())).thenReturn(call)
        whenever(userStore.currentUser).thenReturn(oldUserInfo)
        whenever(oldUserInfo.userId).thenReturn("a_user_id")
        whenever(oldUserInfo.locale).thenReturn("us_en")

        presenter = UserProfilePresenter(view, userStore, userService, analytics)

        doNothing().whenever(call).execute(lambdaCaptor.capture())
    }

    /**
     * Testing edit mode state
     */

    /**
     * Given:   A user has updated their profile details and wants to save their changes.
     * When:    The user clicks save.
     * Then:    The profile should no longer be in edit mode, the progress bar hidden and changes updated and saved.
     */
    @Test
    fun `profile should no longer be editable when the user clicks save`() {
        presenter.onProfileUpdateSuccess(newUserInfo)
        verify(view).showProfileUpdateSuccess(newUserInfo)
        verify(view).bindUserInfo(newUserInfo)
        verify(view).hideProgressView()
        verify(view).bindProfileEditMode(false)
        Assert.assertEquals(view.isEditingProfile(), false)
    }

    /**
     * Given:   A user wants to discard their changes.
     * When:    The user clicks cancel.
     * Then:    The profile should no longer be in edit mode and their details reset
     */
    @Test
    fun `profile should no longer be editable when the user clicks cancel`() {
        presenter.discardProfileEdit()
        verify(view).bindProfileEditMode(false)
        verify(view).bindUserInfo(presenter.currentUser)
        Assert.assertEquals(view.isEditingProfile(), false)
    }

    /**
     * Progress spinner show/hide
     */

    /**
     * Given:   A user has updated their profile details and wants to save their changes.
     * When:    The user clicks save.
     * Then:    The profile screen should show a progress spinner
     */
    @Test
    fun `profile should show a progress spinner when the user clicks save`() {
        presenter.saveProfileEdit("first", "last", "000000000")
        verify(view).showProgressView()
    }

    /**
     * Given:   A user has updated their profile details and wants to save their changes.
     * When:    The user clicks save and the update is successful.
     * Then:    The progress spinner should be hidden and the user details shown should reflect their changes.
     */
    @Test
    fun `when the user clicks save and the update is successful, profile should hide progress spinner and the ui updated`() {
        whenever(userService.updateUserDetails(any())).thenReturn(call)
        whenever(newUserInfo.firstName).thenReturn("firstName")
        whenever(newUserInfo.lastName).thenReturn("lastName")
        whenever(newUserInfo.phoneNumber).thenReturn("99999999")
        presenter.saveProfileEdit(newUserInfo.firstName, newUserInfo.lastName, newUserInfo.phoneNumber)
        lambdaCaptor.firstValue.invoke(Resource.Success(newUserInfo))
        verify(view).hideProgressView()
        verify(view).bindUserInfo(newUserInfo)
    }

    /**
     * Given:   A user has updated their profile details and wants to save their changes.
     * When:    The user clicks save, but an error occurs for reasons unknown
     * Then:    The progress spinner should be hidden and a snack bar should be visible with the correct error.
     */
    @Test
    fun `when the user clicks save and an error occurs, profile should hide progress spinner and display correct error`() {
        whenever(userService.updateUserDetails(any())).thenReturn(call)
        presenter.saveProfileEdit("first", "last", "000000000")
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))
        verify(view).hideProgressView()
    }

    /**
     * Validation
     */

    /**
     * Given:   A user wants to update their user details
     * When:    The user enters invalid user details
     * Then:    The profile update mode should be set
     */
    @Test
    fun `entering invalid user details should show validation errors immediately`() {
        whenever(view.allFieldsValid()).thenReturn(false)
        presenter.onProfileFieldsChanged(view.allFieldsValid())
        verify(view).bindProfileUpdateMode(false)
    }

    /**
     * Given:   A user goes to the profile screen
     * When:    Validate is called on the presenter
     * Then:    A call should be made to return the user details to the view
     */
    @Test
    fun `invoking validate user gets a valid user from the store and updates the ui`() {
        presenter.validateUser()
        verify(view).bindUserInfo(presenter.currentUser)
    }

    /**
     * Analytics
     */

    /**
     * Given:   The user wants to edit their details
     * When:    The user clicks the edit button
     * Then:    An event should be sent to analytics telling us that they have pressed the edit button
     */
    @Test
    fun `pressing the edit button should send an event to analytics`() {
        presenter.beginProfileEdit()
        verify(analytics).userProfileEditPressed()
    }

    /**
     * Given:   The users wants to edit their details
     * When:    The user clicks the save button
     * Then:    An event should be sent to analytics telling us that they have pressed the save button
     */
    @Test
    fun `pressing the save button should send an event to analytics`() {
        presenter.saveProfileEdit("first", "last", "99999")
        verify(analytics).userProfileSavePressed()
    }

    /**
     * Given:   The user wants to discard their changes
     * When:    The user clicks the discard button
     * Then:    An even tshould be sent to analytics telling us that they have pressed the discard button
     */
    @Test
    fun `pressing the discard button should send an event to analytics`() {
        presenter.discardProfileEdit()
        verify(analytics).userProfileDiscardPressed()
    }

    /**
     * Given:   The user wants to save their profile changes
     * When:    The user clicks the save button
     * Then:    An event should be sent to analytics telling us that they have saved their details
     */
    @Test
    fun `when user details are updated successfully, the analytics should be informed`() {
        whenever(newUserInfo.firstName).thenReturn("first")
        whenever(newUserInfo.lastName).thenReturn("last")
        whenever(newUserInfo.phoneNumber).thenReturn("99999")
        presenter.saveProfileEdit(newUserInfo.firstName, newUserInfo.lastName, newUserInfo.phoneNumber)
        lambdaCaptor.firstValue.invoke(Resource.Success(newUserInfo))
        verify(analytics).userProfileUpdateSuccess(newUserInfo)
    }

    /**
     * Given:   The user wants to save their profile changes
     * When:    The user clicks the save button and an error occurs for whatever reason
     * Then:    An event should be sent to analytics telling us an error occured
     */
    @Test
    fun `if an error occurs whilst attempting to save user details, analytics should be notified`() {
        whenever(newUserInfo.firstName).thenReturn("first")
        whenever(newUserInfo.lastName).thenReturn("last")
        whenever(newUserInfo.phoneNumber).thenReturn("99999")

        presenter.saveProfileEdit(newUserInfo.firstName, newUserInfo.lastName, newUserInfo.phoneNumber)
        lambdaCaptor.firstValue.invoke(Resource.Failure(KarhooError.InternalSDKError))

        verify(analytics).userProfileUpdateFailed()
    }

    /**
     * Given:   The user wants to update their profile
     * When:    The user clicks the edit button, which should change the editing profile state
     * Then:    Invoking is editing profile should return true
     */
    @Test
    fun `if the editing state is changed, get editing profile should return the correct state`() {
        presenter.beginProfileEdit()
        Assert.assertEquals(presenter.isEditingProfile(), true)
    }

    /**
     * Given:   The user is updating their profile
     * When:    The user backgrounds the app or changes their card on edit state
     * Then:    They re-open the app or successfully change their card and their edited fields restore
     */
    @Test
    fun `if user changes their card, edited fields will save instead of resetting`() {
        val userInfo2 = UserInfo()

        given(userStore.currentUser).willReturn(userInfo2)

        presenter = UserProfilePresenter(view, userStore, userService, analytics)

        presenter.saveOnStop(firstName = "steves", lastName = "robbinson", mobilePhoneNumber = "07111111111")
        Assert.assertEquals("steves", presenter.currentUser.firstName)
        Assert.assertEquals("robbinson", presenter.currentUser.lastName)
        Assert.assertEquals("07111111111", presenter.currentUser.phoneNumber)
    }

    /**
     * Given:   The user is updating their profile
     * When:    The user changes their card with a invalid phone number
     * Then:    The user should not be able to save the changes and should be notified that the phone number is incorrect
     */
    @Test
    fun `if user changes card with invalid number, saving should not be possible`() {
        val userInfo2 = UserInfo()

        given(userStore.currentUser).willReturn(userInfo2)

        presenter = UserProfilePresenter(view, userStore, userService, analytics)

        presenter.saveOnStop(firstName = "steves", lastName = "robbinson", mobilePhoneNumber = "7254")
        presenter.validateMobileNumber(userInfo2.phoneNumber, "7254")
    }
}
