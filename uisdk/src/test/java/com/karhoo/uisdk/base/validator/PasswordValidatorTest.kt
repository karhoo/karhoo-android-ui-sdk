package com.karhoo.uisdk.base.validator

import junit.framework.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PasswordValidatorTest {

    private val validator: PasswordValidator = PasswordValidator()

    @Test
    fun `valid password returns true`() {
        val validPassword = "abcDEF12"
        assertTrue(validator.validate(validPassword))
    }

    @Test
    fun `validator checks for eight characters minimum`() {
        val passwordLessThanEightCharacters = "abcDEF1"
        assertFalse(validator.validate(passwordLessThanEightCharacters))
    }

    @Test
    fun `validator checks for lowercase characters`() {
        val passwordWithNoLowercaseCharacters = "ABCDEF12"
        assertFalse(validator.validate(passwordWithNoLowercaseCharacters))
    }

    @Test
    fun `validator checks for uppercase characters`() {
        val passwordWithNoUppercaseCharacters = "abcdef12"
        assertFalse(validator.validate(passwordWithNoUppercaseCharacters))
    }

    @Test
    fun `validator checks for numeric digits`() {
        val passwordWithNoNumericDigits = "abcDEFGH"
        assertFalse(validator.validate(passwordWithNoNumericDigits))
    }

}
