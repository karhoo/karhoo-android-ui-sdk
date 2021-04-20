package com.karhoo.uisdk.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

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
                price = 1000,
                locale = Locale.UK
        )
        assertEquals("£10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices, should display 3 fraction digits`() {
        val formattedString = CurrencyUtils.intToPrice(currency = Currency.getInstance("JOD"), price = 10000, locale = Locale.UK)
        assertEquals("JOD10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices, should display 0 fraction digits`() {
        val formattedString = CurrencyUtils.intToPrice(currency = Currency.getInstance("JPY"), price = 180, locale = Locale.UK)
        assertEquals("JPY180", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = CurrencyUtils.intToPrice(currency = Currency.getInstance("JPY"), price = 1800, locale = Locale.UK)
        assertEquals("JPY1,800", formattedString)
    }

    @Test
    fun `when formatting UK Pounds prices with no symbol, should display 3 fraction digits`() {
        val priceString = CurrencyUtils.intToPriceNoSymbol(
                currency = Currency.getInstance("GBP"),
                price = 1000,
                locale = Locale.UK
        )
        assertEquals("10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with no symbol, should display 3 fraction digits`() {
        val formattedString = CurrencyUtils.intToPriceNoSymbol(currency = Currency.getInstance("JOD"), price = 10000, locale = Locale.UK)
        assertEquals("10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with no symbol, should display 0 fraction digits`() {
        val formattedString = CurrencyUtils.intToPriceNoSymbol(currency = Currency.getInstance("JPY"), price = 180, locale = Locale.UK)
        assertEquals("180", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with no symbol, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = CurrencyUtils.intToPriceNoSymbol(currency = Currency.getInstance("JPY"), price = 1800, locale = Locale.UK)
        assertEquals("1,800", formattedString)
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
                lowPrice = 500,
                locale = Locale.UK
        )
        assertEquals("£5.00 - 10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar range prices, should display them with 3 fraction digits`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("JOD"),
                highPrice = 10000,
                lowPrice = 5000,
                locale = Locale.UK
        )
        assertEquals("JOD5.000 - 10.000", priceString)
    }

    @Test
    fun `when formatting Japanese Yen range prices, should display them with 0 fraction digits`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("JPY"),
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.UK
        )
        assertEquals("JPY500 - 1,000", priceString)
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a null currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `empty string returned for null currency string`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = null, price = 100, locale = Locale.UK)
        assertEquals("", formattedString)
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a valid currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `formatted currency string returned for currency string and price`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "GBP", price = 1000, locale = Locale.UK)
        assertEquals("£10.00", formattedString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with string currency code, should display 3 fraction digits`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "JOD", price = 10000, locale = Locale.UK)
        assertEquals("JOD10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with string currency code, should display 0 fraction digits`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "JPY", price = 180, locale = Locale.UK)
        assertEquals("JPY180", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with string currency code, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "JPY", price = 1800, locale = Locale.UK)
        assertEquals("JPY1,800", formattedString)
    }

    /**
     * Given:   A high price is available
     * When:    Getting the formatted string
     * Then:    The string should only have the high price correctly formatted
     */
    @Test
    fun `high price formatted correctly for individual price with US locale`() {
        val priceString = CurrencyUtils.intToPrice(
                currency = Currency.getInstance("GBP"),
                price = 1000,
                locale = Locale.US
        )
        assertEquals("GBP10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with US locale, should display 3 fraction digits`() {
        val formattedString = CurrencyUtils.intToPrice(currency = Currency.getInstance("JOD"), price = 10000, locale = Locale.US)
        assertEquals("JOD10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with US locale, should display 0 fraction digits`() {
        val formattedString = CurrencyUtils.intToPrice(currency = Currency.getInstance("JPY"), price = 180, locale = Locale.US)
        assertEquals("JPY180", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with US locale, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = CurrencyUtils.intToPrice(currency = Currency.getInstance("JPY"), price = 1800, locale = Locale.US)
        assertEquals("JPY1,800", formattedString)
    }

    @Test
    fun `when formatting UK Pounds prices with no symbol with US locale, should display 3 fraction digits`() {
        val priceString = CurrencyUtils.intToPriceNoSymbol(
                currency = Currency.getInstance("GBP"),
                price = 1000,
                locale = Locale.US
        )
        assertEquals("10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with no symbol with US locale, should display 3 fraction digits`() {
        val formattedString = CurrencyUtils.intToPriceNoSymbol(currency = Currency.getInstance("JOD"), price = 10000, locale = Locale.US)
        assertEquals("10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with no symbol with US locale, should display 0 fraction digits`() {
        val formattedString = CurrencyUtils.intToPriceNoSymbol(currency = Currency.getInstance("JPY"), price = 180, locale = Locale.US)
        assertEquals("180", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with no symbol with US locale, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = CurrencyUtils.intToPriceNoSymbol(currency = Currency.getInstance("JPY"), price = 1800, locale = Locale.US)
        assertEquals("1,800", formattedString)
    }

    /**
     * Given:   A high and low price
     * When:    Getting the formatted string
     * Then:    The string should be returned with the range pricing
     */
    @Test
    fun `range pricing formatted correctly when given a high and low price with US locale`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("GBP"),
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.US
        )
        assertEquals("GBP5.00 - 10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar range prices with US locale, should display them with 3 fraction digits`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("JOD"),
                highPrice = 10000,
                lowPrice = 5000,
                locale = Locale.US
        )
        assertEquals("JOD5.000 - 10.000", priceString)
    }

    @Test
    fun `when formatting Japanese Yen range prices with US locale, should display them with 0 fraction digits`() {
        val priceString = CurrencyUtils.intToRangedPrice(
                currency = Currency.getInstance("JPY"),
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.US
        )
        assertEquals("JPY500 - 1,000", priceString)
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a null currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `empty string returned for null currency string with US locale`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = null, price = 100, locale = Locale.US)
        assertEquals("", formattedString)
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a valid currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `formatted currency string returned for currency string and price with US locale`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "GBP", price = 1000, locale = Locale.US)
        assertEquals("GBP10.00", formattedString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with string currency code, with US locale, should display 3 fraction digits`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "JOD", price = 10000, locale = Locale.US)
        assertEquals("JOD10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with string currency code, with US locale, should display 0 fraction digits`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "JPY", price = 180, locale = Locale.US)
        assertEquals("JPY180", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with string currency code, with US locale, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = CurrencyUtils.getFormattedPrice(currency = "JPY", price = 1800, locale = Locale.US)
        assertEquals("JPY1,800", formattedString)
    }
}