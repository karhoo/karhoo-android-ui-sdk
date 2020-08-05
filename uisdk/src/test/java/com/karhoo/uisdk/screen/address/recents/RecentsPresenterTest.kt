package com.karhoo.uisdk.screen.address.recents

import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Position
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RecentsPresenterTest {

    private val location1 = makeLocationInfo(1)
    private val location2 = makeLocationInfo(2)
    private val location3 = makeLocationInfo(3)
    private val location4 = makeLocationInfo(4)
    private val location5 = makeLocationInfo(5)
    private val location6 = makeLocationInfo(6)

    @Mock
    lateinit var view: RecentsMVP.View

    /**
     * Given:   there are no items stored
     */
    @Test
    fun `when load locations then show empty state`() {
        val locationStore = LocationStoreTestDouble(emptyList())
        val presenter = RecentsPresenter(view, locationStore)

        presenter.loadLocations()
        verify(view).showEmptyState()
    }

    /**
     * Given:   There are five locations in the store
     */
    @Test
    fun `when load locations then show five locations`() {
        val fiveLocations = listOf(location1, location2, location3, location4, location5)
        val locationStore = LocationStoreTestDouble(fiveLocations)
        val presenter = RecentsPresenter(view, locationStore)

        presenter.loadLocations()
        verify(view).showLocations(fiveLocations)
    }

    /**
     * Given:   There are already less than five locations in the store
     */
    @Test
    fun `when a new location is saved then the location is saved to position 0`() {
        val fourLocations = listOf(location1, location2, location3, location4)
        val locationStore = spy(LocationStoreTestDouble(fourLocations))
        val presenter = RecentsPresenter(view, locationStore)

        presenter.save(location5)

        val expected = listOf(location5, location1, location2, location3, location4)
        verify(locationStore).save(expected)
    }

    /**
     * Given:   There are already five items in the store
     */
    @Test
    fun `when a new location is saved then the existing fifth item on the list is removed`() {
        val fiveLocations = listOf(location1, location2, location3, location4, location5)
        val locationStore = spy(LocationStoreTestDouble(fiveLocations))
        val presenter = RecentsPresenter(view, locationStore)

        presenter.save(location6)

        val expected = listOf(location6, location1, location2, location3, location4)
        verify(locationStore).save(expected)
    }

    /**
     * Given:   there are items in the store
     */
    @Test
    fun `when an item already in the list is saved then its position is moved to the top of the list`() {
        val threeLocations = listOf(location1, location2, location3)
        val locationStore = spy(LocationStoreTestDouble(threeLocations))
        val presenter = RecentsPresenter(view, locationStore)

        presenter.save(location2)

        val expected = listOf(location2, location1, location3)
        verify(locationStore).save(expected)
    }

    /**
     * Given:   there are five items in the store
     */
    @Test
    fun `when an item already in the list of five items is saved then its position is moved to the top of the list and none are deleted`() {
        val fiveLocations = listOf(location1, location2, location3, location4, location5)
        val locationStore = spy(LocationStoreTestDouble(fiveLocations))
        val presenter = RecentsPresenter(view, locationStore)

        presenter.save(location3)

        val expected = listOf(location3, location1, location2, location4, location5)
        verify(locationStore).save(expected)
    }

    private open class LocationStoreTestDouble(private var locations: List<LocationInfo>) : LocationStore {
        override fun save(locations: List<LocationInfo>): Boolean {
            this.locations = locations
            return true
        }

        override fun retrieve() = locations

        override fun clear() {
            locations = emptyList()
        }

    }

    private fun makeLocationInfo(idNumber: Int): LocationInfo {
        val address = Address(displayAddress = "show this $idNumber")
        return LocationInfo(position = Position(idNumber.toDouble(), 1.0),
                            placeId = "id$idNumber",
                            address = address)
    }
}
