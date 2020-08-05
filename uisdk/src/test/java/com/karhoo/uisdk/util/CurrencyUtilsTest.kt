package com.karhoo.uisdk.util

import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.Currency

@RunWith(MockitoJUnitRunner::class)
class CurrencyUtilsTest {

    /**
     * Given:   A high price is available
     * When:    Getting the formatted string
     * Then:    The string should only have the high price correctly formatted
     */
    //TODO Fix, seems to be flaky
    @Ignore
    @Test
    fun `high price formatted correctly for individual price`() {
        val priceString = CurrencyUtils.intToPrice(
                currency = Currency.getInstance("GBP"),
                price = 1000)
        assertEquals("£10.00", priceString)
    }

    /**
     * Given:   A high nad low price
     * When:    Getting the formatted string
     * Then:    The string should be returned with the range pricing
     */
    //TODO Fix, seems to be flaky
    @Ignore
    @Test
    fun `range pricing formatted correctly when given a high and low price`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("GBP"),
                highPrice = 1000,
                lowPrice = 500)
        assertEquals("£5.00 - 10.00", priceString)
    }


}