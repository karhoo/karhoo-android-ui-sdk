package com.karhoo.uisdk.screen.booking.map

import com.google.android.gms.maps.model.LatLng
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PickupDropoffPresenterTest {

    private lateinit var presenter: PickupDropoffPresenter

    private var owner: BookingMapStategy.Owner = mock()

    @Before
    fun setUp() {
        presenter = PickupDropoffPresenter()
        presenter.setOwner(owner)
    }

    /**
     * Given:   The map has been dragged
     * When:    The event is passed to the presenter
     * Then:    The presenter should do nothing
     */
    @Test
    fun mapDraggedDoesNothing() {
        presenter.mapDragged()
        verifyZeroInteractions(owner)
    }

    /**
     * Given:   The address has changed
     * When:    The event is passed to the presenter
     * Then:    The presenter should do nothing
     */
    @Test
    fun mapMovedDoesNothing() {
        presenter.mapMoved(LatLng(0.0, 0.0))
        verifyZeroInteractions(owner)
    }

    /**
     * Given:   The address has changed
     * When:    The event is passed to the presenter
     * Then:    The presenter should do nothing
     */
    @Test
    fun zoomToIncludeTheMarkersWhenUserPressesLocateMeh() {
        presenter.locateUserPressed()
        verify(owner, atLeastOnce()).zoomMapToMarkers()
    }

}