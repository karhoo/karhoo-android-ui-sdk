package com.karhoo.uisdk.screen.booking.checkout.presenter

import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.BookButtonState
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.CheckoutPresenter
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CheckoutPresenterTests {
    private lateinit var presenter: CheckoutPresenter

    @Before
    fun setUp() {
        presenter = CheckoutPresenter()
    }

    @Test
    fun `Saving passenger details is successful`() {
        val mockPassengerDetails: PassengerDetails = mock()

        presenter.savePassenger(mockPassengerDetails)

        Assert.assertEquals(mockPassengerDetails, presenter.passengerDetails)
    }

    @Test
    fun `If passenger details visible then we return save state even if other params are false`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = false,
            isPaymentValid = false
                                                      )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details visible then we return save state even if passenger details valid`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = true,
            isPaymentValid = false
                                                      )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details visible then we return save state even if payment is valid`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = false,
            isPaymentValid = true
                                                      )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details visible and valid, payment valid return save state`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = true,
            isPaymentValid = true
                                                      )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If payment is not valid and passenger details are not visible and invalid then button next`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = false,
            arePassengerDetailsValid = false,
            isPaymentValid = false
                                                      )

        Assert.assertEquals(BookButtonState.NEXT, buttonState)
    }

    @Test
    fun `If passenger details are not visible yet valid and payment is valid then we get book state`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = false,
            arePassengerDetailsValid = true,
            isPaymentValid = true
                                                      )

        Assert.assertEquals(BookButtonState.BOOK, buttonState)
    }
}
