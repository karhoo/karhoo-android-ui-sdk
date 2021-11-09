package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.network.request.QuoteQTA
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QtaSortTest {

    private val qtaSort = QtaSort()

    /**
     * Given:   An unsorted list of vehicles
     * When:    sorted using QtaSort
     * Then:    The Vehicles should be sorted by QTA
     */
    @Test
    fun `sorted by QTA when QTAs are different`() {
        val v1 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 10)))
        val v2 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 16)))
        val v3 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 4)))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortOrder = listOf(v3, v1, v2)

        val result = vehiclesToSort.sortedWith(qtaSort)

        assertEquals(expectedSortOrder, result)
    }

    /**
     * Given:   An unsorted list of vehicles with no QTA
     * When:    sorted using QtaSort
     * Then:    The Vehicles should be sorted by highPrice
     */
    @Test
    fun `sorted by price when no QTA`() {
        val v1 = Quote(price = QuotePrice(highPrice = 52))
        val v2 = Quote(price = QuotePrice(highPrice = 5))
        val v3 = Quote(price = QuotePrice(highPrice = 15))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortedOrder = listOf(v2, v3, v1)

        val result = vehiclesToSort.sortedWith(qtaSort)

        assertEquals(expectedSortedOrder, result)
    }

    /**
     * Given:   An unsorted list of vehicles with equal QTAs
     * When:    sorted using QtaSort
     * Then:    The matching Vehicles should be sorted by highPrice
     */
    @Test
    fun `sorted by price when QTAs are equal`() {
        val v1 = Quote(
                vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 10)),
                price = QuotePrice(highPrice = 52))
        val v2 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 10)),
                         price = QuotePrice(highPrice = 5))
        val v3 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 10)),
                         price = QuotePrice(highPrice = 15))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortedOrder = listOf(v2, v3, v1)

        val result = vehiclesToSort.sortedWith(qtaSort)

        assertEquals(expectedSortedOrder, result)
    }

}
