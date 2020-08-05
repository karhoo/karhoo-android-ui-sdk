package com.karhoo.uisdk.screen.booking.map

import com.karhoo.uisdk.analytics.Analytics
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BookingMapPresenterTest {

    internal lateinit var presenter: BookingMapPresenter

    internal var view: BookingMapMVP.View = mock()
    internal var pickupPresenter: BookingMapStategy.Presenter = mock()
    internal var pickupDropOffPresenter: BookingMapStategy.Presenter = mock()
    internal var analytics: Analytics? = mock()

    @Before
    fun setUp() {
        presenter = BookingMapPresenter(
                view = view,
                analytics = analytics,
                pickupOnlyPresenter = pickupPresenter,
                pickupDropoffPresenter = pickupDropOffPresenter
                                       )
    }

    /**
     * Given:   The user has given their permission for location
     * When:    A call is made to locationPermissionGranted
     * Then:    The map should reset and the user should be located
     */
    @Test
    @Throws(Exception::class)
    fun `Location permission granted`() {
        presenter.locationPermissionGranted()

        verify(view, atLeastOnce()).resetMap()
        verify(view, atLeastOnce()).locateUser()
    }

}