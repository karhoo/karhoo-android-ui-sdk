package com.karhoo.uisdk.screen.booking.passengerdetails

import android.content.res.Resources
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.checkout.passengerdetails.PassengerDetailsContract
import com.karhoo.uisdk.screen.booking.checkout.passengerdetails.PassengerDetailsPresenter
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PassengerDetailsPresenterTest {

    private var res: Resources = mock()
    private var view: PassengerDetailsContract.View = mock()
    private var passengerDetails: PassengerDetails = PassengerDetails(firstName = "John",
                                                                      lastName = "Smith",
                                                                      email = "test.test@test.com",
                                                                      phoneNumber = "77777777")

    private lateinit var presenter: PassengerDetailsPresenter

    @Before
    fun setUp() {
        presenter = PassengerDetailsPresenter(view)
    }

    /**
     * Given:   A user views the passenger details
     * When:    The edit mode is updated
     * Then:    The view is correctly updated
     */
    @Test
    fun `view is correctly set when edit mode is updated`() {
        presenter.isEditingMode = false

        verify(view).bindEditMode(false)
    }

    /**
     * Given:   A user views the passenger details
     * When:    The details are prefilled
     * Then:    The view is correctly updated
     * And:     The correct details can be retrieved
     */
    @Test
    fun `view correctly updated with prefilled passenger details`() {
        presenter.prefillForPassengerDetails(passengerDetails)

        verify(view).bindPassengerDetails(passengerDetails)
        verify(view).bindEditMode(true)
        assertEquals(passengerDetails, presenter.passengerDetails)
    }

    /**
     * Given:   A user views the passenger details
     * When:    There are new details
     * Then:    The details are correctly updated
     */
    @Test
    fun `passenger details can be correctly updated`() {

        presenter.prefillForPassengerDetails(passengerDetails)
        presenter.updatePassengerDetails(firstName = "Jim",
                                         lastName = "Jones",
                                         email = "test1.test@test.com",
                                         mobilePhoneNumber = "77777771")

        assertEquals("Jim", presenter.passengerDetails?.firstName)
        assertEquals("Jones", presenter.passengerDetails?.lastName)
        assertEquals("test1.test@test.com", presenter.passengerDetails?.email)
        assertEquals("77777771", presenter.passengerDetails?.phoneNumber)
    }

    /**
     * Given:   A phone number
     * When:    It is validated
     * Then:    The number is correctly formatted
     */
    @Test
    fun `validated phone number is correctly formatted`() {
        val formattedNumber = presenter.validateMobileNumber(CODE, "07777")
        assertEquals("447777", formattedNumber)
    }

    /**
     * Given:   A phone number
     * When:    The country code is retrieved
     * Then:    It has the correct value
     */
    @Test
    fun `correct country code retrieved for phone number`() {
        val codes = arrayOf("22", "33", "44")
        whenever(res.getStringArray(any())).thenReturn(codes)

        val formattedNumber = presenter.getCountryCodeFromPhoneNumber("447777", res)
        assertEquals("44", formattedNumber)
    }

    /**
     * Given:   A phone number
     * When:    The phone number without country code is retrieved
     * Then:    It has the correct value
     */
    @Test
    fun `correct phone number without country code is retrieved`() {
        val codes = arrayOf("22", "33", "44")
        whenever(res.getStringArray(any())).thenReturn(codes)

        val formattedNumber = presenter.removeCountryCodeFromPhoneNumber("448888", res)
        assertEquals("8888", formattedNumber)
    }

    companion object {
        private const val CODE = "44"
    }
}
