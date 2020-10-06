package com.karhoo.uisdk.common

import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.CoordinatesProvider
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.swipeLeft
import androidx.test.espresso.action.ViewActions.swipeRight
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.Visibility
import androidx.test.espresso.matcher.ViewMatchers.hasFocus
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility
import androidx.test.espresso.matcher.ViewMatchers.withHint
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.karhoo.uisdk.R
import com.karhoo.uisdk.common.matcher.RecyclerMatcher
import com.karhoo.uisdk.common.matcher.withDrawable
import com.schibsted.spain.barista.assertion.BaristaVisibilityAssertions
import com.schibsted.spain.barista.interaction.BaristaClickInteractions
import com.schibsted.spain.barista.interaction.BaristaClickInteractions.clickOn
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.anything
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Assert
import java.util.Date

open abstract class BaseTestRobot {

    @Throws(UiObjectNotFoundException::class)
    fun tapTurnOnGpsBtn() {
        val allowGpsBtn: UiObject = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
                .findObject(UiSelector()
                                    .className("android.widget.Button").packageName("com.google.android.gms")
                                    .resourceId("android:id/button1")
                                    .clickable(true).checkable(false))
        if (allowGpsBtn.exists() && allowGpsBtn.isEnabled) {
            do {
                allowGpsBtn.click()
            } while (allowGpsBtn.exists())
        }
    }

    fun fillEditText(resId: Int, text: String): ViewInteraction =
            onView(withId(resId)).perform(replaceText(text), closeSoftKeyboard())

    fun fillText(resId: Int, text: String): ViewInteraction =
            onView(withId(resId)).perform(typeText(text), closeSoftKeyboard())

    fun fillTextIsDescendant(id: Int, text: String, resId: Int): ViewInteraction =
            onView(allOf(withId(id), isDescendantOfA(withId(resId)))).perform(typeText(text),
                                                                              closeSoftKeyboard())

    fun clickButton(resId: Int) {
        clickOn(resId)
    }

    fun textIsNotVisible(text: Int) {
        BaristaVisibilityAssertions.assertNotExist(text)
    }

    fun longClickButton(resId: Int) {
        BaristaClickInteractions.longClickOn(resId)
    }

    fun clickButtonIsDescendant(id: Int, resId: Int): ViewInteraction =
            onView(allOf(withId(id), isDescendantOfA(withId(resId)))).perform(click())

    fun clickButtonNotOnView(resId: Int): ViewInteraction = onData((withId(resId))).perform(click())

    fun buttonIsEnabled(resId: Int): ViewInteraction = onView((withId(resId))).check(matches(isEnabled()))

    fun buttonIsDisabled(resId: Int): ViewInteraction = onView((withId(resId))).check(matches(not(isEnabled())))

    fun buttonIsClickable(resId: Int): ViewInteraction = onView((withId(resId))).check(matches(isClickable()))

    fun buttonIsNotClickable(resId: Int): ViewInteraction = onView((withId(resId))).check(matches(not(isClickable())))

    fun buttonWithTextIsClickable(text: Int): ViewInteraction = onView((withText(text))).check(matches(isClickable()))

    fun buttonWithTextIsNotClickable(text: Int): ViewInteraction = onView((withText(text))).check(matches(not(isClickable())))

    fun dialogButtonByTextIsEnabled(text: Int): ViewInteraction =
            onView((withText(text))).inRoot(isDialog()).check(matches(isEnabled()))

    fun dialogButtonByTextIsDisabled(text: Int): ViewInteraction =
            onView((withText(text))).inRoot(isDialog()).check(matches(not(isEnabled())))

    fun dialogClickButtonByText(text: Int): ViewInteraction =
            onView((withText(text))).inRoot(isDialog()).perform(click())

    fun dialogClickButtonByString(text: String): ViewInteraction =
            onView((withText(text))).inRoot(isDialog()).perform(click())

    fun clickButtonByContentDescription(desc: Int): ViewInteraction =
            onView(withContentDescription(desc)).perform(click())

    fun buttonByContentDescriptionIsEnabled(desc: Int): ViewInteraction =
            onView(withContentDescription(desc)).check(matches(isEnabled()))

    fun buttonIsEnabledIsDescendant(id: Int, resId: Int): ViewInteraction =
            onView(allOf(withId(id), isDescendantOfA(withId(resId)))).check(matches(isEnabled()))

    fun clickButtonByText(text: String): ViewInteraction = onView(withText(text)).perform((click()))

    fun clickButtonByString(resId: Int): ViewInteraction = onView(withText(resId)).perform((click()))

    fun viewButtonByText(text: String): ViewInteraction = onView(withText(text)).check(matches(isDisplayed()))

    fun textView(resId: Int): ViewInteraction = onView(withId(resId))

    fun textIsVisible(text: Int) = onView(withText(text)).check(matches(isDisplayed()))

    fun stringIsVisible(text: String) = onView(withText(text)).check(matches(isDisplayed()))

    fun stringIsNotVisible(text: String) = onView(withText(text)).check(doesNotExist())

    fun stringIsNotDisplayed(text: String) = onView(withText(text)).check(matches(not(isDisplayed())))

    fun dialogTextIsVisible(text: Int) =
            onView(withText(text)).inRoot(isDialog()).check(matches(isDisplayed()))

    fun dialogTextIsNotVisible(text: Int) =
            onView(withText(text)).inRoot(isDialog()).check(matches(not(isDisplayed())))

    fun viewIsVisible(resId: Int): ViewInteraction =
            onView(withId(resId)).check(matches(isDisplayed()))

    fun viewIsNotVisible(resId: Int): ViewInteraction =
            onView(withId(resId)).check(matches(not(isDisplayed())))

    fun viewDoesNotExist(resId: Int) = onView(withId(resId)).check(doesNotExist())

    fun viewIsNotDisplayed(resId: Int) = onView(withId(resId)).check(matches(not(isDisplayed())))

    fun viewIsVisibleWithIndex(text: Int, page: Int): ViewInteraction =
            onView(withIndex(withText(text), page)).check(matches(isDisplayed()))

    fun textIsVisibleIsDescendant(text: Int, resId: Int): ViewInteraction =
            onView(allOf(withText(text), isDescendantOfA(withId(resId))))

    fun textStringIsVisibleIsDescendant(text: String, resId: Int): ViewInteraction =
            onView(allOf(withText(text), isDescendantOfA(withId(resId))))

    fun stringIsVisibleIsDescendant(text: String, resId: Int) {
        Assert.assertEquals(text, getStringFromTextView(resId))
    }

    fun stringIsVisibleIsDescendantWeb(text: String, resId: Int)
            : ViewInteraction =
            onView(allOf(withText(text), isDescendantOfA(withId(resId))))

    fun dateIsVisibleDescendant(date: Date, resId: Int) {
        Assert.assertEquals(date, getStringFromTextView(resId))
    }

    fun stringIsVisibleIsDescendantIgnoreCase(text: String, resId: Int) {
        Assert.assertEquals(text.toLowerCase(), getStringFromTextView(resId).toLowerCase())
    }

    fun viewIsVisibleIsDescendant(id: Int, resId: Int): ViewInteraction =
            onView(allOf(withId(id), isDescendantOfA(withId(resId))))

    fun viewIsFocused(resId: Int): ViewInteraction =
            onView(withId(resId)).check(matches(hasFocus()))

    fun viewIsNotFocused(resId: Int): ViewInteraction =
            onView(withId(resId)).check(matches(not(hasFocus())))

    fun matchText(viewInteraction: ViewInteraction, text: Int): ViewInteraction = viewInteraction
            .check(matches(withText(text)))

    fun matchText(resId: Int, text: Int): ViewInteraction = matchText(textView(resId), text)

    fun clickListItem(listRes: Int, position: Int) {
        onData(anything())
                .inAdapterView(allOf(withId(listRes)))
                .atPosition(position).perform(click())
    }

    fun pressItemInList(listRes: Int, position: Int): ViewInteraction =
            onView(withId(listRes))
                    .perform(
                            RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(position, click())
                            )

    fun checkListSize(listRes: Int, listSize: Int) {
        onView(withId(listRes)).check(RecyclerMatcher(listSize))
    }

    fun checkTextViewIfMultiMatches(resId: Int, text: Int) {
        onView(allOf(withId(resId),
                     withEffectiveVisibility(Visibility.VISIBLE)))
                .check(matches(isCompletelyDisplayed()))
                .check(matches(withText(text)))
    }

    fun checkListSizeFromParent(parentRes: Int, listRes: Int, listSize: Int) {
        onView(allOf(withId(listRes), isDescendantOfA(withId(parentRes)))).check(RecyclerMatcher(listSize))
    }

    fun sleep(millis: Long = 350) = apply {
        Thread.sleep(millis)
    }

    fun pressDeviceBackButton() {
        Espresso.pressBack()
    }

    fun checkTripStatusBarText(resId: Int, text: String): ViewInteraction =
            onView(withId(resId)).check(matches(withText(text)))

    fun checkSnackbarWithText(expectedText: Int) {
        onView(allOf(withId(com.google.android.material.R.id.snackbar_text), withText(expectedText))).check(matches(isDisplayed()))
    }

    fun checkSnackbarButtonIsEnabled(button: Int) {
        onView(allOf(withId(R.id.snackbar_action), withText(button))).check(matches(isEnabled()))
    }

    fun checkToolbarTitle(expectedText: Int) {
        onView(allOf(isAssignableFrom(TextView::class.java),
                     withParent(isAssignableFrom(Toolbar::class.java))))
                .check(matches(withText(expectedText)))
    }

    fun checkToolbarTitleString(expectedText: String) {
        onView(allOf(isAssignableFrom(TextView::class.java),
                     withParent(isAssignableFrom(Toolbar::class.java))))
                .check(matches(withText(expectedText)))
    }

    fun clickBackToolbarButton() {
        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click())
    }

    fun swipeLeftTabView() {
        onView(withId(R.id.viewPager)).perform(swipeLeft())
    }

    fun swipeRightTabView() {
        onView(withId(R.id.viewPager)).perform(swipeRight())
    }

    enum class AlertDialogButton(@IdRes val resId: Int) {
        POSITIVE(android.R.id.button1),
        NEGATIVE(android.R.id.button2),
        NEUTRAL(android.R.id.button3)
    }

    fun clickOnButtonInAlertDialog(button: AlertDialogButton) {
        onView(withId(button.resId)).perform(click())
    }

    fun getStringFromTextView(resId: Int): String {
        val stringHolder = arrayListOf<String>()
        onView(withId(resId)).perform(object : ViewAction {
            override fun getDescription(): String {
                return "getting text from a TextView"
            }

            override fun getConstraints(): org.hamcrest.Matcher<View> {
                return isAssignableFrom(TextView::class.java)
            }

            override fun perform(uiController: UiController?, view: View?) {
                val tv = view as TextView //Save, because of check in getConstraints()
                stringHolder.add(tv.text.toString())
            }
        })
        return stringHolder[0]
    }

    fun hintIsVisible(resId: Int, text: Int): ViewInteraction =
            onView(withId(resId)).check(matches(withHint(text)))

    fun clickXY(x: Int, y: Int): ViewAction {
        return GeneralClickAction(
                Tap.SINGLE,
                CoordinatesProvider { view ->
                    val screenPos = IntArray(2)
                    view.getLocationOnScreen(screenPos)

                    val screenX = (screenPos[0] + x).toFloat()
                    val screenY = (screenPos[1] + y).toFloat()

                    floatArrayOf(screenX, screenY)
                },
                Press.FINGER,
                InputDevice.SOURCE_MOUSE,
                MotionEvent.BUTTON_PRIMARY)
    }

    fun withIndex(matcher: Matcher<View>, index: Int): Matcher<View> {
        return object : TypeSafeMatcher<View>() {
            internal var currentIndex = 0

            override fun describeTo(description: Description) {
                description.appendText("with index: ")
                description.appendValue(index)
                matcher.describeTo(description)
            }

            override fun matchesSafely(view: View): Boolean {
                return matcher.matches(view) && currentIndex++ == index
            }
        }
    }

    fun drawableIsVisible(resId: Int, drawable: Int): ViewInteraction =
            onView(withId(resId)).check(matches(withDrawable(drawable)))

    fun scrollUp(resId: Int): ViewInteraction =
            onView(withId(resId)).perform(ViewActions.swipeUp())

}