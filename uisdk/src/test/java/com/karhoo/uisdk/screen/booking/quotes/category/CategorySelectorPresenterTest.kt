package com.karhoo.uisdk.screen.booking.quotes.category

import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider
import com.nhaarman.mockitokotlin2.atLeastOnce
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CategorySelectorPresenterTest {

    private var view: CategorySelectorMVP.View = mock()
    private var availabilityProvider: AvailabilityProvider = mock()

    @Captor
    private lateinit var categoryListCaptor: ArgumentCaptor<List<Category>>

    @InjectMocks
    private lateinit var presenter: CategorySelectorPresenter

    /**
     * Given    A request has been made for quotes
     * When     A successful response is returned
     * And      The list object is null
     * Then     The categories should be deactivated
     */
    @Test
    fun `null list not returned when requesting quotes`() {
        val observer = presenter.subscribeToAvailableCategories()

        presenter.availabilityProvider = availabilityProvider

        doAnswer { observer.onChanged(createMockCategoryList()) }
                .whenever(availabilityProvider).filterVehicleListByCategory("")

        presenter.setVehicleCategory("")
        verify(view, never()).showCategories()
    }

    /**
     * Given    A request has been made for quotes
     * When     A successful response is returned
     * And      There are no quotes available
     * Then     The list should be empty
     */
    @Test
    fun `empty quotes list returned when requesting quotes`() {
        val observer = presenter.subscribeToAvailableCategories()
        val observerBooking = presenter.subscribeToBookingStatus()

        presenter.availabilityProvider = availabilityProvider

        doAnswer { observer.onChanged(createMockCategoryList()) }
                .whenever(availabilityProvider).filterVehicleListByCategory("human")

        observerBooking.onChanged(BookingStatus(mock(), mock(), null))

        presenter.setVehicleCategory("human")

        verify(view, atLeastOnce()).setCategories(capture(categoryListCaptor))

        categoryListCaptor.value.forEach({ category -> Assert.assertFalse(category.isAvailable) })
    }

    /**
     * Given    A request has been made for quotes
     * When     A successful response is returned
     * And      There are quotes available
     * Then     The populated category list should contain the added vehicle as available
     */
    @Test
    fun `available categories are returned when requesting categories`() {
        val observer = presenter.subscribeToAvailableCategories()
        val observerBooking = presenter.subscribeToBookingStatus()

        presenter.availabilityProvider = availabilityProvider

        doAnswer { observer.onChanged(createQuoteAvailableMockList()) }
                .whenever(availabilityProvider).filterVehicleListByCategory("MPV")

        observerBooking.onChanged(BookingStatus(mock(), mock(), null))
        presenter.setVehicleCategory("MPV")

        verify(view, atLeastOnce()).setCategories(capture(categoryListCaptor))
        categoryListCaptor.value.forEach { category ->
            if (category.categoryName == "MPV") {
                Assert.assertTrue(category.isAvailable)
            }
        }
    }

    private fun createMockCategoryList() = listOf(
            Category("Human", false),
            Category("Animal", false),
            Category("Pet", false),
            Category("MPV", false),
            Category("Any", false)
                                                 )

    private fun createQuoteAvailableMockList() = listOf(
            Category("Human", true),
            Category("Animal", true),
            Category("Pet", false),
            Category("MPV", true),
            Category("Any", false)
                                                       )

}
