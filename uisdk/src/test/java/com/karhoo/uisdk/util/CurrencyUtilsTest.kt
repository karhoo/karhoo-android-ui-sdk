package com.karhoo.uisdk.util

import org.junit.Assert.assertEquals
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
    @Test
    fun `high price formatted correctly for individual price`() {
        val priceString = CurrencyUtils.intToPrice(
                currency = Currency.getInstance("GBP"),
                price = 1000)
        assertEquals("£10.00", priceString)
    }

    /**
     * Given:   A high and low price
     * When:    Getting the formatted string
     * Then:    The string should be returned with the range pricing
     */
    @Test
    fun `range pricing formatted correctly when given a high and low price`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("GBP"),
                highPrice = 1000,
                lowPrice = 500)
        assertEquals("£5.00 - 10.00", priceString)
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a null currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `empty string returned for null currency string`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = null, price = 100)
        assertEquals("", formattedString)
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a valid currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `formatted currency string returned for currency string and price`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "GBP", price = 1000)
        assertEquals("£10.00", formattedString)
    }
}