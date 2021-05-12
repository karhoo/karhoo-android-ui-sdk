package com.karhoo.uisdk.util

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import java.text.DecimalFormat
import java.util.Currency
import java.util.Locale

@RunWith(MockitoJUnitRunner::class)
class CurrencyExtensionsTest {

    val gbpCurrency = Currency.getInstance("GBP")
    val jodCurrency = Currency.getInstance("JOD")
    val jpyCurrency = Currency.getInstance("JPY")

    /**
     * Given:   A high price is available
     * When:    Getting the formatted string
     * Then:    The string should only have the high price correctly formatted
     */
    @Test
    fun `high price formatted correctly for individual price`() {
        val formattedString = gbpCurrency.formatted(
                price = 1000,
                locale = Locale.UK
        )
        assertCurrencyFormatting(
                currencyCode = gbpCurrency.currencyCode,
                expectedAmountString = "10.00",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Jordanian Dinnar prices, should display 3 fraction digits`() {
        val formattedString = jodCurrency.formatted(price = 10000, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = jodCurrency.currencyCode,
                expectedAmountString = "10.000",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Japanese Yen prices, should display 0 fraction digits`() {
        val formattedString = jpyCurrency.formatted(price = 180, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "180",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = jpyCurrency.formatted(price = 1800, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "1,800",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting UK Pounds prices with no symbol, should display 3 fraction digits`() {
        val priceString = gbpCurrency.intToPriceNoSymbol(
                price = 1000,
                locale = Locale.UK
        )
        assertEquals("10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with no symbol, should display 3 fraction digits`() {
        val formattedString = jodCurrency.intToPriceNoSymbol(price = 10000, locale = Locale.UK)
        assertEquals("10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with no symbol, should display 0 fraction digits`() {
        val formattedString = jpyCurrency.intToPriceNoSymbol(price = 180, locale = Locale.UK)
        assertEquals("180.", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with no symbol, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = jpyCurrency.intToPriceNoSymbol(price = 1800, locale = Locale.UK)
        assertEquals("1800.", formattedString)
    }

    /**
     * Given:   A high and low price
     * When:    Getting the formatted string
     * Then:    The string should be returned with the range pricing
     */
    @Test
    fun `range pricing formatted correctly when given a high and low price`() {
        val formattedString = gbpCurrency.intToRangedPrice(
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.UK
        )
        assertCurrencyFormatting(
                currencyCode = gbpCurrency.currencyCode,
                expectedAmountString = "5.00 - 10.00",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Jordanian Dinnar range prices, should display them with 3 fraction digits`() {
        val formattedString = jodCurrency.intToRangedPrice(
                highPrice = 10000,
                lowPrice = 5000,
                locale = Locale.UK
        )
        assertCurrencyFormatting(
                currencyCode = jodCurrency.currencyCode,
                expectedAmountString = "5.000 - 10.000",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Japanese Yen range prices, should display them with 0 fraction digits`() {
        val formattedString = jpyCurrency.intToRangedPrice(
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.UK
        )
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "500 - 1,000",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a valid currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `formatted currency string returned for currency string and price`() {
        val formattedString = gbpCurrency.formatted(price = 1000, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = gbpCurrency.currencyCode,
                expectedAmountString = "10.00",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with string currency code, should display 3 fraction digits`() {
        val formattedString = jodCurrency.formatted(price = 10000, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = jodCurrency.currencyCode,
                expectedAmountString = "10.000",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Japanese Yen prices with string currency code, should display 0 fraction digits`() {
        val formattedString = jpyCurrency.formatted(price = 180, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "180",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with string currency code, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = jpyCurrency.formatted(price = 1800, locale = Locale.UK)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "1,800",
                actualString = formattedString,
                locale = Locale.UK
        )
    }

    /**
     * Given:   A high price is available
     * When:    Getting the formatted string
     * Then:    The string should only have the high price correctly formatted
     */
    @Test
    fun `high price formatted correctly for individual price with US locale`() {
        val formattedString = gbpCurrency.formatted(
                price = 1000,
                locale = Locale.US
        )
        assertCurrencyFormatting(
                currencyCode = gbpCurrency.currencyCode,
                expectedAmountString = "10.00",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with US locale, should display 3 fraction digits`() {
        val formattedString = jodCurrency.formatted(price = 10000, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = jodCurrency.currencyCode,
                expectedAmountString = "10.000",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Japanese Yen prices with US locale, should display 0 fraction digits`() {
        val formattedString = jpyCurrency.formatted(price = 180, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "180",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with US locale, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = jpyCurrency.formatted(price = 1800, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "1,800",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting UK Pounds prices with no symbol with US locale, should display 3 fraction digits`() {
        val priceString = gbpCurrency.intToPriceNoSymbol(
                price = 1000,
                locale = Locale.US
        )
        assertEquals("10.00", priceString)
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with no symbol with US locale, should display 3 fraction digits`() {
        val formattedString = jodCurrency.intToPriceNoSymbol(price = 10000, locale = Locale.US)
        assertEquals("10.000", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen prices with no symbol with US locale, should display 0 fraction digits`() {
        val formattedString = jpyCurrency.intToPriceNoSymbol(price = 180, locale = Locale.US)
        assertEquals("180.", formattedString)
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with no symbol with US locale, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = jpyCurrency.intToPriceNoSymbol(price = 1800, locale = Locale.US)
        assertEquals("1800.", formattedString)
    }

    /**
     * Given:   A high and low price
     * When:    Getting the formatted string
     * Then:    The string should be returned with the range pricing
     */
    @Test
    fun `range pricing formatted correctly when given a high and low price with US locale`() {
        val formattedString = gbpCurrency.intToRangedPrice(
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.US
        )
        assertCurrencyFormatting(
                currencyCode = gbpCurrency.currencyCode,
                expectedAmountString = "5.00 - 10.00",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Jordanian Dinnar range prices with US locale, should display them with 3 fraction digits`() {
        val formattedString = jodCurrency.intToRangedPrice(
                highPrice = 10000,
                lowPrice = 5000,
                locale = Locale.US
        )
        assertCurrencyFormatting(
                currencyCode = jodCurrency.currencyCode,
                expectedAmountString = "5.000 - 10.000",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Japanese Yen range prices with US locale, should display them with 0 fraction digits`() {
        val formattedString = jpyCurrency.intToRangedPrice(
                highPrice = 1000,
                lowPrice = 500,
                locale = Locale.US
        )
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "500 - 1,000",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    /**
     * Given:   A formatted currency String is requested
     * When:    There is a valid currency code
     * Then:    An empty string is returned
     */
    @Test
    fun `formatted currency string returned for currency string and price with US locale`() {
        val formattedString = gbpCurrency.formatted(price = 1000, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = gbpCurrency.currencyCode,
                expectedAmountString = "10.00",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Jordanian Dinnar prices with string currency code, with US locale, should display 3 fraction digits`() {
        val formattedString = jodCurrency.formatted(price = 10000, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = jodCurrency.currencyCode,
                expectedAmountString = "10.000",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Japanese Yen prices with string currency code, with US locale, should display 0 fraction digits`() {
        val formattedString = jpyCurrency.formatted(price = 180, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "180",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    @Test
    fun `when formatting Japanese Yen over one thousand prices with string currency code, with US locale, should display 0 fraction digits and thousand comma separator`() {
        val formattedString = jpyCurrency.formatted(price = 1800, locale = Locale.US)
        assertCurrencyFormatting(
                currencyCode = jpyCurrency.currencyCode,
                expectedAmountString = "1,800",
                actualString = formattedString,
                locale = Locale.US
        )
    }

    /**
     * Will assert if a price is displayed as expected.
     * @param expectedAmountString The expected amount displayed in the price string
     * @param actualString The actual price string to evaluate
     * @param locale The locale which influences the formatting of the price string
     * @param currencyCode The currency code which will influence the currency symbol
     */
    private fun assertCurrencyFormatting(currencyCode: String, expectedAmountString: String, actualString: String, locale: Locale) {
        val expectedCurrencySymbol = expectedCurrencySymbol(locale, currencyCode)
        val expectedPriceString = "$expectedCurrencySymbol$expectedAmountString"
        assertEquals(expectedPriceString, actualString)
    }

    private fun expectedCurrencySymbol(locale: Locale, currencyCode: String): String {
        val currencyFormat: DecimalFormat = DecimalFormat.getCurrencyInstance(locale) as DecimalFormat
        val currency = Currency.getInstance(currencyCode)
        currencyFormat.currency = currency
        return currencyFormat.decimalFormatSymbols.currencySymbol
    }
}