package com.karhoo.uisdk.util

import android.content.res.Resources
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class FormattedMobileNumberTest {

    private var res: Resources = mock()

    @Before
    fun setUp() {
        val codes = arrayOf("22", "33", "44")
        whenever(res.getStringArray(any())).thenReturn(codes)
    }

    /**
     * Given:   A phone number is formatted
     * When:    It starts with 0
     * Then:    The number is correctly formatted
     */
    @Test
    fun `phone number starting with zero is correctly formatted`() {
        val formattedNumber = formatMobileNumber(CODE, "08888")
        assertEquals("448888", formattedNumber)
    }

    /**
     * Given:   A phone number is formatted
     * When:    It does not start with 0
     * Then:    The number is correctly formatted
     */
    @Test
    fun `phone number not starting with zero is correctly formatted`() {
        val formattedNumber = formatMobileNumber(CODE, "77777")
        assertEquals("4477777", formattedNumber)
    }

    /**
     * Given:   A phone number
     * When:    When getting the country code from the number
     * And:     The number does not contain the country code
     * Then:    The correct value is returned
     */
    @Test
    fun `correct country code value returned for number with no country code`() {
        val countryCode = getCodeFromMobileNumber("77777", res)
        assertEquals("", countryCode)
    }

    /**
     * Given:   A phone number
     * When:    When getting the country code from the number
     * And:     The number contains the country code
     * Then:    The correct value is returned
     */
    @Test
    fun `correct country code value returned for number with country code`() {
        val countryCode = getCodeFromMobileNumber("4477777", res)
        assertEquals("44", countryCode)
    }

    /**
     * Given:   A phone number
     * When:    When getting the phone number
     * And:     The number does not contain the country code
     * Then:    The correct value is returned
     */
    @Test
    fun `correct phone number returned for number with no country code`() {
        val countryCode = getMobileNumberWithoutCode("77777", res)
        assertEquals("77777", countryCode)
    }

    /**
     * Given:   A phone number
     * When:    When getting the phone number
     * And:     The number contains the country code
     * Then:    The correct value is returned
     */
    @Test
    fun `correct phone number returned for number with country code`() {
        val countryCode = getMobileNumberWithoutCode("4488888", res)
        assertEquals("88888", countryCode)
    }

    /**
     * Given:   A country code and phone number
     * When:    When the number is invalid
     * Then:    The correct validity is returned
     */
    @Test
    fun `correct validity returned for invalid phone number`() {
        assertFalse(isValidNumber("+447777111"))
    }

    /**
     * Given:   A country code and phone number
     * When:    When the number is valid
     * Then:    The correct validity is returned
     */
    @Test
    fun `correct validity returned for valid phone number`() {
        assertTrue(isValidNumber("+447777111111"))
    }

    companion object {
        private const val CODE = "44"
    }
}