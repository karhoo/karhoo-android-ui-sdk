package com.karhoo.uisdk.screen.booking.checkout.presenter

import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.BookButtonState
import com.karhoo.uisdk.screen.booking.checkout.component.fragment.CheckoutPresenter
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import java.util.*

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
        )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details visible then we return save state even if passenger details valid`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = true,
        )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details visible then we return save state even if payment is valid`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = false,
        )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details visible and valid, payment valid return save state`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = true,
            arePassengerDetailsValid = true,
        )

        Assert.assertEquals(BookButtonState.SAVE, buttonState)
    }

    @Test
    fun `If passenger details are not visible and invalid then button next`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = false,
            arePassengerDetailsValid = false,
        )

        Assert.assertEquals(BookButtonState.NEXT, buttonState)
    }

    @Test
    fun `If passenger details are valid then we get pay state`() {
        val buttonState = presenter.getBookButtonState(
            isPassengerDetailsVisible = false,
            arePassengerDetailsValid = true,
            isTermsCheckBoxValid = true
        )

        Assert.assertEquals(BookButtonState.PAY, buttonState)
    }

    @Test
    fun `Getting a valid period in millis works`() {
        Assert.assertEquals(
            presenter.getValidMilisPeriod(
                Date().time.plus(VALID_PERIOD_MILLIS)
            ).toFloat(),
            VALID_PERIOD_MILLIS.toFloat(),
            VALID_PERIOD_MILLIS_DELTA.toFloat()
        )
    }

    companion object {
        private const val VALID_PERIOD_MILLIS = 500000
        private const val VALID_PERIOD_MILLIS_DELTA = 100000
    }
}
