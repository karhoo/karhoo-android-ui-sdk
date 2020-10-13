package com.karhoo.karhootraveller.profile.user

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.karhootraveller.common.preferences
import com.karhoo.karhootraveller.common.testrunner.TravellerTestConfig
import com.karhoo.karhootraveller.presentation.profile.ProfileActivity
import com.karhoo.uisdk.common.serverRobot
import com.karhoo.uisdk.util.TestData.Companion.GENERAL_ERROR
import com.karhoo.uisdk.util.TestData.Companion.MEDIUM
import com.karhoo.uisdk.util.TestData.Companion.USER
import com.karhoo.uisdk.util.TestData.Companion.USER_UPDATED_INFO
import com.schibsted.spain.barista.rule.flaky.AllowFlaky
import com.schibsted.spain.barista.rule.flaky.FlakyTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_OK

@RunWith(AndroidJUnit4::class)
class UserProfileTests : Launch {

    @get:Rule
    var wireMockRule = WireMockRule(TravellerTestConfig.PORT_NUMBER)

    @get:Rule
    val activityRule: ActivityTestRule<ProfileActivity> =
            ActivityTestRule(ProfileActivity::class.java, false, false)

    private var flakyRule = FlakyTestRule()

    @get:Rule
    var chain: RuleChain = RuleChain.outerRule(flakyRule)
            .around(activityRule)

    @Before
    fun clearUser() {
        preferences {
            clearUserPreference()
        }
    }

    @After
    fun tearDown() {
        preferences {
            clearUserPreference()
        }
    }

    override fun launch(intent: Intent?) {
        activityRule.launchActivity(Intent())
    }

    /**
     * Given:   I would like to see my profile details
     * When:    I have landed on the profile screen for the first time
     * Then:    I expect to see my current profile details
     */
    @Test
    @AllowFlaky(attempts = 10)
    fun profileDetailsShouldBeVisibleOnLaunch() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            sleep()
        } result {
            noProfileChangesFullCheck()
        }
    }

    /**
     * Given:   I want to edit my details.
     * When:    I click the menu button for the first time.
     * Then:    I expect to see edit and sign out in the menu.
     */
    @Test
    fun clickingMenuShouldShowEditAndSignOutButtons() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
        } result {
            checkEditButtonIsVisible()
            checkSignOutButtonIsVisible()
        }
    }

    /**
     * Given:   I want to edit my details.
     * When:    I click the edit button in the menu and open the menu again.
     * Then:    I expect to see save and discard in the menu.
     */
    @Test
    fun clickingEditShouldChangeTheMenuToSaveAndDiscardButtons() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            clickMenuToolbarButton()
        } result {
            checkSaveButtonIsVisible()
            checkDiscardButtonIsVisible()
        }
    }

    /**
     * Given:   I want to discard my details.
     * When:    I click discard in the menu and open the menu again.
     * Then:    I expect to see edit and sign out in the menu.
     */
    @Test
    fun clickingDiscardShouldChangeTheMenuBackToEditAndSignOut() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            clickMenuToolbarButton()
            clickDiscardButton()
            clickMenuToolbarButton()
        } result {
            checkEditButtonIsVisible()
            checkSignOutButtonIsVisible()
        }
    }

    /**
     * Given:   I want to verify that I cant edit a field whilst in non-edit mode.
     * When:    I click the first name field area.
     * Then:    I expect the field to not be focused and editable.
     */
    @Test
    fun clickingFirstNameFieldWhilstNotInEditModeShouldNotFocusTheView() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickFirstNameField()
        } result {
            checkFirstNameFieldIsNotFocused()
        }
    }

    /**
     * Given:   I want to edit my user details.
     * When:    I click the edit button in the menu.
     * Then:    I expect the first name field to be focused and editable.
     */
    @Test
    fun clickingEditShouldMakeTheFirsNameFieldFocusedAndEditable() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
        } result {
            checkFirstNameFieldIsFocused()
        }
    }

    /**
     * Given:   I want to edit my user details.
     * When:    I click the email field whilst in edit mode.
     * Then:    I expect the email field to not be focused because I shouldn't be able to edit the email field.
     */
    @Test
    fun clickingEmailFieldWhilstInEditModeShouldNotFocusTheView() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            clickEmailNameField()
        } result {
            checkEmailFieldIsNotFocused()
        }
    }

    /**
     * Given:   I was planning to update my first name but decided I want to discard my changes.
     * When:    I click discard after making all my changes.
     * Then:    I expect my profile details to be reset to what they were before.
     */
    @Test
    @AllowFlaky(attempts = 10)
    fun clickingDiscardShouldResetProfileDetails() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            fillFirstNameField()
            fillLastNameField()
            fillMobileNumberField()
            clickMenuToolbarButton()
            clickDiscardButton()
        } result {
            noProfileChangesFullCheck()
        }
    }

    /**
     * Given:   I am currently editing my details in edit mode and the first name field is empty.
     * When:    I check if I can save my changes by clicking the menu button.
     * Then:    The save button should be visible and I shouldn't be able to save my changes.
     */
    @Test
    fun whenTheFirstNameFieldIsEmptyTheSaveButtonShouldNotBeClickable() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            clearFirstNameField()
            clickMenuToolbarButton()
            clickSaveButton()
        } result {
            checkSaveButtonIsVisible()
            checkSaveButtonIsNotClickable()
        }
    }

    /**
     * Given:   I have a card linked to my account using Adyen payment as provider
     * When:    I am on the profile screen
     * Then:    I can see: First Name, Last Name, email, country code, mobile number, change card
     * button (add card) is not visible.
     **/
    @Test
    @AllowFlaky(attempts = 10)
    fun fullCheckProfilePageCardRegistered() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            waitFor(MEDIUM)
        } result {
            fullScreenCheckCardRegisteredBraintree()
        }
    }

    /**
     * Given:   I am currently editing my details in edit mode and the last name field is empty.
     * When:    I check if I can save my changes by clicking the menu button.
     * Then:    The save button should be visible and I shouldn't be able to save my changes.
     */
    @Test
    fun whenTheLastNameFieldIsEmptyTheSaveButtonShouldNotBeClickable() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            clearLastNameField()
            clickMenuToolbarButton()
            clickSaveButton()
        } result {
            checkSaveButtonIsVisible()
            checkSaveButtonIsNotClickable()
        }
    }

    /**
     * Given:   I am currently editing my details in edit mode and the last name field is empty.
     * When:    I check if I can save my changes by clicking the menu button.
     * Then:    The save button should be visible and I shouldn't be able to save my changes.
     */
    @Test
    fun whenTheMobileNumberFieldIsEmptyTheSaveButtonShouldNotBeClickable() {
        preferences {
            setUserPreference(USER)
        }
        userProfile(this) {
            clickMenuToolbarButton()
            clickEditButton()
            clearMobileNumberField()
            clickMenuToolbarButton()
            clickSaveButton()
        } result {
            checkSaveButtonIsVisible()
            checkSaveButtonIsNotClickable()
        }
    }

    /**
     * Given:   I want to update my profile details
     * When:    I click save after making all my changes.
     * Then:    I expect my profile details to be saved.
     */
    @Test
    @AllowFlaky(attempts = 5)
    fun savingProfileDetailsSuccessShouldShowSnackBarWithSuccess() {
        preferences {
            setUserPreference(USER)
        }
        serverRobot {
            successfulToken()
            userProfileUpdateResponse(HTTP_OK, USER_UPDATED_INFO)
        }
        userProfile(this) {
            updateUserProfileWithDefaultInfo()
            sleep()
        } result {
            updatedProfileChangesFullCheck()
            checkSnackbarWithText(R.string.profile_update_successful)
        }
    }

    /**
     * Given:   I want to update my profile details
     * When:    I click save after making all my changes.
     * Then:    I expect my profile details to be saved.
     */
    @Test
    fun savingProfileDetailsFailedShouldShowSnackBarWithError() {
        preferences {
            setUserPreference(USER)
        }
        serverRobot {
            successfulToken()
            userProfileUpdateResponse(HTTP_INTERNAL_ERROR, GENERAL_ERROR)
        }
        userProfile(this) {
            updateUserProfileWithDefaultInfo()
            sleep()
        } result {
            checkSnackbarWithText(R.string.K0001)
        }
    }

    /**
     * Given: I have no card linked to my account
     * When:  I am on the profile screen
     * Then:  I can see: First Name, Last Name, email, country code, mobile number, add card button
     **/
    @Test
    @AllowFlaky(attempts = 10)
    fun fullCheckProfilePageNoCardRegistered() {
        preferences {
            setUserPreferenceNoCard(USER)
        }
        userProfile(this) {
            waitFor(MEDIUM)
        } result {
            fullScreenCheckNoCardRegistered()
        }
    }
}