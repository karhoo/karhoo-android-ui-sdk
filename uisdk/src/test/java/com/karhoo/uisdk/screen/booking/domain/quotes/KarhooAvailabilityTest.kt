package com.karhoo.uisdk.screen.booking.domain.quotes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.karhoo.sdk.api.model.*
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.quotes.QuotesService
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.base.snackbar.SnackbarConfig
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.quotes.category.CategoriesViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityHandler
import com.karhoo.uisdk.screen.booking.domain.quotes.AvailabilityProvider

@RunWith(MockitoJUnitRunner::class)
class KarhooAvailabilityTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private var quotesService: QuotesService = mock()
    private var analytics: Analytics = mock()
    private var lifecycleOwner: LifecycleOwner = mock()
    private var quotesCall: PollCall<QuoteList> = mock()
    private var observable: Observable<QuoteList> = mock()
    private var locationInfo = LocationInfo()
    private lateinit var liveFleetsViewModel: LiveFleetsViewModel
    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var availability: AvailabilityProvider
    private var lifecycle = LifecycleRegistry(lifecycleOwner)
    private lateinit var availabilityHandler: AvailabilityHandler
    private lateinit var journeyDetailsStateViewModel: JourneyDetailsStateViewModel

    private val lambdaCaptor = argumentCaptor<com.karhoo.sdk.api.network.observable
    .Observer<Resource<QuoteList>>>()

    @Before
    fun setUp() {
        whenever(quotesCall.observable()).thenReturn(observable)
        doNothing().whenever(observable).subscribe(lambdaCaptor.capture(), any())

        whenever(lifecycleOwner.lifecycle).thenReturn(lifecycle)

        liveFleetsViewModel = LiveFleetsViewModel()
        categoriesViewModel = CategoriesViewModel()
        journeyDetailsStateViewModel = JourneyDetailsStateViewModel(mock())

        availability = KarhooAvailability
        availability.setup(
            quotesService = quotesService,
            journeyDetailsStateViewModel = journeyDetailsStateViewModel,
            liveFleetsViewModel = liveFleetsViewModel,
            lifecycleOwner = lifecycleOwner,
            categoriesViewModel = categoriesViewModel
        )
        (availability as KarhooAvailability).setAnalytics(analytics)

        availabilityHandler = object : AvailabilityHandler {
            override var hasAvailability: Boolean = false
            override var hasNoResults: Boolean = false
            override fun handleAvailabilityError(snackbarConfig: SnackbarConfig) {
                /** do nothing **/
            }

            override fun handleSameAddressesError() {
                /** do nothing **/
            }

            override fun handleNoResultsForFiltersError() {
                /** do nothing **/
            }
        }

        availabilityHandler.hasNoResults = false
        availabilityHandler.hasAvailability = false
    }

    @Test
    fun `When getting some quotes categories with empty quotes and an incomplete status, the availability handler will have hasAvailability set to true`() {
        whenever(quotesService.quotes(any(), any())).thenReturn(quotesCall)

        setCategories(CATEGORIES_WITH_EMPTY_QUOTES, QuoteStatus.PROGRESSING)

        Assert.assertTrue(availabilityHandler.hasAvailability)
    }

    @Test
    fun `When getting some quotes categories with empty quotes and a complete status, the availability handler will be set to no result`() {
        whenever(quotesService.quotes(any(), any())).thenReturn(quotesCall)

        setCategories(CATEGORIES_WITH_EMPTY_QUOTES, QuoteStatus.COMPLETED)

        Assert.assertTrue(availabilityHandler.hasNoResults)
    }

    private fun setCategories(categories: Map<String, MutableList<Quote>>, status: QuoteStatus) {
        val observer = availability.journeyDetailsObserver()
        observer.onChanged(JourneyDetails(locationInfo, locationInfo, null))

        availability.setAvailabilityHandler(availabilityHandler)
        availability.setAllCategory(ALL)
        lambdaCaptor.firstValue.onValueChanged(
            Resource.Success(
                QuoteList(
                    categories = categories,
                    id = QuoteId(),
                    status = status,
                    validity = 30
                )
            )
        )
    }

    companion object {

        const val ALL = "ALL"
        const val MPV = "MPV"
        const val SALOON = "SALOON"

        val CATEGORIES = mapOf(
            SALOON to mutableListOf<Quote>().apply {
                add(Quote(vehicle = QuoteVehicle(vehicleClass = SALOON)))
                add(Quote(vehicle = QuoteVehicle(vehicleClass = SALOON)))
                add(Quote(vehicle = QuoteVehicle(vehicleClass = SALOON)))
                add(Quote(vehicle = QuoteVehicle(vehicleClass = SALOON)))
            },
            MPV to mutableListOf<Quote>().apply
            {
                add(Quote(vehicle = QuoteVehicle(vehicleClass = MPV)))
                add(Quote(vehicle = QuoteVehicle(vehicleClass = MPV)))
                add(Quote(vehicle = QuoteVehicle(vehicleClass = MPV)))
            }
        )
        val CATEGORIES_WITH_EMPTY_QUOTES = mapOf(
            SALOON to mutableListOf<Quote>(),
            MPV to mutableListOf<Quote>()
        )
    }

}
