package com.karhoo.uisdk.screen.booking.domain.quotes

import com.karhoo.sdk.api.model.FleetInfo
import com.karhoo.sdk.api.model.QuotePrice
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.network.request.QuoteQTA
import junit.framework.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PriceSortTest {

    private val priceSort = PriceSort()

    /**
     * Given:   An unsorted list of vehicles
     * When:    Price sort is added
     * Then:    The Vehicles should be sorted by Price
     */
    @Test
    fun `sorted by price when prices different`() {
        val v1 = Quote(price = QuotePrice(highPrice = 52))
        val v2 = Quote(price = QuotePrice(highPrice = 5))
        val v3 = Quote(price = QuotePrice(highPrice = 15))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortedOrder = listOf(v2, v3, v1)

        val result = vehiclesToSort.sortedWith(priceSort)

        assertEquals(expectedSortedOrder, result)
    }

    /**
     * Given:   An unsorted list of vehicles with no Price
     * When:    sorted using PriceSort
     * Then:    The Vehicles should be sorted by QTA
     */
    @Test
    fun `sorted by QTA when no prices`() {
        val v1 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 8)))
        val v2 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 10)))
        val v3 = Quote(vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 3)))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortedOrder = listOf(v3, v1, v2)

        val result = vehiclesToSort.sortedWith(priceSort)

        assertEquals(expectedSortedOrder, result)
    }

    /**
     * Given:   An unsorted list of vehicles with equal prices
     * When:    sorted using PriceSort
     * Then:    The matching Vehicles should be sorted by QTA
     */
    @Test
    fun `sorted by QTA when prices are equal`() {
        val v1 = Quote(price = QuotePrice(highPrice = 5),
                         vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 8)))
        val v2 = Quote(price = QuotePrice(highPrice = 5),
                         vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 10)))
        val v3 = Quote(price = QuotePrice(highPrice = 5),
                         vehicle = QuoteVehicle(vehicleQta = QuoteQTA(highMinutes = 3)))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortedOrder = listOf(v3, v1, v2)
        val result = vehiclesToSort.sortedWith(priceSort)

        assertEquals(expectedSortedOrder, result)
    }

    /**
     * Given:   a list of vehicles with equal prices and no QTAs
     * When:    sorted using PriceSort
     * Then:    vehicles should be sorted by supplier name
     */
    @Test
    fun `sorted by supplier name when equal prices and no QTAs`() {
        val v1 = Quote(price = QuotePrice(highPrice = 5), fleet = FleetInfo(name = "B Cars"))
        val v2 = Quote(price = QuotePrice(highPrice = 5), fleet = FleetInfo(name = "Z Cars"))
        val v3 = Quote(price = QuotePrice(highPrice = 5), fleet = FleetInfo(name = "A Cars"))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortedOrder = listOf(v3, v1, v2)

        val result = vehiclesToSort.sortedWith(priceSort)

        assertEquals(expectedSortedOrder, result)
    }

    /**
     * Given:   a list of vehicles with same supplier, equal prices and no QTAs
     * When:    sorted using PriceSort
     * Then:    vehicles should be sorted alphabetically by category name
     */
    @Test
    fun `sorted alphabetically by category name when supplier is same, equal prices and no QTAs`() {
        val v1 = Quote(price = QuotePrice(highPrice = 5), fleet = FleetInfo(name = "A Cars"),
                         vehicle = QuoteVehicle(vehicleClass = "ABC"))
        val v2 = Quote(price = QuotePrice(highPrice = 5), fleet = FleetInfo(name = "A Cars"),
                         vehicle = QuoteVehicle(vehicleClass = "XYZ"))
        val v3 = Quote(price = QuotePrice(highPrice = 5), fleet = FleetInfo(name = "A Cars"),
                         vehicle = QuoteVehicle(vehicleClass = "DEF"))
        val vehiclesToSort = listOf(v1, v2, v3)
        val expectedSortOrder = listOf(v1, v3, v2)

        val result = vehiclesToSort.sortedWith(priceSort)

        assertEquals(expectedSortOrder, result)
    }

}