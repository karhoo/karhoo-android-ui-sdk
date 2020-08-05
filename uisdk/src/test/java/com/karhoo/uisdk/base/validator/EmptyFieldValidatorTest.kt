package com.karhoo.uisdk.base.validator

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EmptyFieldValidatorTest {

    internal lateinit var validator: EmptyFieldValidator

    @Before
    @Throws(Exception::class)
    fun setUp() {
        validator = EmptyFieldValidator()
    }

    /**
     * Given    A empty string is passed to the validator
     * When     Running a validation for no empty strings
     * Then     False should be returned
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun whenFieldIsEmptyValidatorReturnsFalse() {
        Assert.assertFalse(validator.validate(""))
    }

    /**
     * Given    A string with values are passed to the validator
     * When     Running a validation for no empty strings
     * Then     True should be returned
     *
     * @throws Exception
     */
    @Test
    @Throws(Exception::class)
    fun whenFieldHasDataValidationReturnsTrue() {
        Assert.assertTrue(validator.validate("John"))
    }
}
