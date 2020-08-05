package com.karhoo.karhootraveller.menu

import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.common.BaseTestRobot
import com.karhoo.karhootraveller.common.Launch
import com.karhoo.uisdk.util.TestData.Companion.HELP_TEXT
import com.karhoo.uisdk.util.TestData.Companion.KARHOO_LIBRARIES
import com.karhoo.uisdk.util.TestData.Companion.KARHOO_PRIVACY
import com.karhoo.uisdk.util.TestData.Companion.KARHOO_TCS

fun menu(func: MenuRobot.() -> Unit) = MenuRobot().apply {
    func()
}

fun menu(launch: Launch, func: MenuRobot.() -> Unit) = MenuRobot().apply {
    launch.launch()
    func()
}

class MenuRobot : BaseTestRobot() {

    fun clickOnProfileButton() {
        clickButtonByString(R.string.profile)
    }

    fun clickOnHelpButton() {
        clickButtonByString(R.string.help)
    }

    fun clickOnAboutButton() {
        clickButtonByString(R.string.about)
    }

    fun clickOnTermsAndConditionsAbout() {
        clickButtonByString(R.string.terms_and_conditions)
    }

    fun clickOnPrivacyPolicyAbout() {
        clickButtonByString(R.string.privacy_policy)
    }

    fun clickOnLicenceAttributionAbout() {
        clickButtonByString(R.string.licence_attribution)
    }

    infix fun result(func: ResultRobot.() -> Unit): ResultRobot {
        return ResultRobot().apply { func() }
    }
}

class ResultRobot : BaseTestRobot() {

    fun helpPageIsShown() {
        stringIsVisibleIsDescendant(HELP_TEXT, R.id.webView)
    }

    fun versionElementIsVisible() {
        textIsVisible(R.string.version)
        viewIsVisible(R.id.versionCodeText)
    }

    fun termsAndConditionsButtonIsClickable() {
        buttonIsClickable(R.id.termsAndConditionsLabel)
    }

    fun privacyPolicyButtonIsClickable() {
        buttonIsClickable(R.id.privacyPolicyLayout)
    }

    fun licenceAttributionButtonIsClickable() {
        buttonIsClickable(R.id.licenceAttributionLayout)
    }

    fun aboutTitleIsVisible() {
        checkToolbarTitle(R.string.about)
    }

    fun fullCheckAbout() {
        versionElementIsVisible()
        termsAndConditionsButtonIsClickable()
        privacyPolicyButtonIsClickable()
        licenceAttributionButtonIsClickable()
        aboutTitleIsVisible()
    }

    fun termsAndConditionsPageIsShownAbout() {
        stringIsVisibleIsDescendant(KARHOO_TCS, R.id.webView)
    }

    fun privacyPolicyPageIsShownAbout() {
        stringIsVisibleIsDescendant(KARHOO_PRIVACY, R.id.webView)

    }

    fun licenceAttributionPageIsShownAbout() {
        stringIsVisibleIsDescendant(KARHOO_LIBRARIES, R.id.webView)
    }
}