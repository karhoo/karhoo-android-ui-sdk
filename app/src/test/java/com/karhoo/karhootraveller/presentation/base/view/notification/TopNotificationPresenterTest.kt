package com.karhoo.karhootraveller.presentation.base.view.notification

import com.karhoo.uisdk.base.view.notification.TopNotificationMVP
import com.karhoo.uisdk.base.view.notification.TopNotificationPresenter
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TopNotificationPresenterTest {

    var view: TopNotificationMVP.View = mock()

    @InjectMocks
    private lateinit var presenter: TopNotificationPresenter

    /**
     * Given:   A valid text has been set
     * When:    Going to show the notification
     * Then:    The view should show the text sent
     */
    @Test
    fun `valid text shows top notification with that text`() {
        presenter.setNotificationText("Hello der")
        verify(view).enableNotificationText("Hello der")
        verify(view).animateNotification()
    }

    /**
     * Given:   A valid text has been set
     * When:    Going to show the notification
     * Then:    The view should show the text sent
     */
    @Test
    fun `invalid text doesnt show top notification with that text`() {
        presenter.setNotificationText("")
        verify(view, never()).enableNotificationText(anyString())
        verify(view, never()).animateNotification()
    }
}