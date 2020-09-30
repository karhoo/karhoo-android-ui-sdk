package com.karhoo.uisdk.screen.booking.domain.supplier

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.Observer
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteId
import com.karhoo.sdk.api.model.QuoteList
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.network.observable.Observable
import com.karhoo.sdk.api.network.response.Resource
import com.karhoo.sdk.api.service.quotes.QuotesService
import com.karhoo.sdk.call.PollCall
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.supplier.category.CategoriesViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

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
    private lateinit var bookingStatusStateViewModel: BookingStatusStateViewModel

    private val lambdaCaptor = argumentCaptor<com.karhoo.sdk.api.network.observable
    .Observer<Resource<QuoteList>>>()

    @Before
    fun setUp() {
        whenever(quotesCall.observable()).thenReturn(observable)
        doNothing().whenever(observable).subscribe(lambdaCaptor.capture(), any())

        whenever(lifecycleOwner.lifecycle).thenReturn(lifecycle)

        liveFleetsViewModel = LiveFleetsViewModel()
        categoriesViewModel = CategoriesViewModel()
        bookingStatusStateViewModel = BookingStatusStateViewModel(mock())

        availability = KarhooAvailability(
                quotesService = quotesService,
                bookingStatusStateViewModel = bookingStatusStateViewModel,
                analytics = analytics,
                liveFleetsViewModel = liveFleetsViewModel,
                lifecycleOwner = lifecycleOwner,
                categoriesViewModel = categoriesViewModel
                                         )

    }

    /**
     * Given:   A list of quotes come in
     * When:    Parsing the quotes
     * Then:    The all category should be populated with all the quotes
     **/
    @Test
    fun `all category gets populated with a full list of quotes`() {
        whenever(quotesService.quotes(any())).thenReturn(quotesCall)

        val observer = availability.bookingStatusObserver()
        observer.onChanged(BookingStatus(locationInfo, locationInfo, null))

        availability.setAllCategory(ALL)
        availability.filterVehicleListByCategory(ALL)
        lambdaCaptor.firstValue.onValueChanged(Resource.Success(QuoteList(categories =
                                                                            CATEGORIES, id = QuoteId())))
        liveFleetsViewModel.liveFleets.observe(lifecycleOwner, Observer {
            assertEquals(7, it?.size)
        })
    }

    /**
     * Given:   A list of quotes come in
     * When:    Parsing the quotes for MPV
     * Then:    Only the MPV vehicles should be returned
     **/
    @Test
    fun `selecting a category filter only returns vehicles of that category`() {
        whenever(quotesService.quotes(any())).thenReturn(quotesCall)

        val observer = availability.bookingStatusObserver()
        observer.onChanged(BookingStatus(locationInfo, locationInfo, null))

        availability.setAllCategory(ALL)
        availability.filterVehicleListByCategory(MPV)
        lambdaCaptor.firstValue.onValueChanged(Resource.Success(QuoteList(categories =
                                                                            CATEGORIES, id = QuoteId())))

        liveFleetsViewModel.liveFleets.observe(lifecycleOwner, Observer {
            assertEquals(3, it?.size)
            it?.forEach {
                assertEquals(MPV, it.vehicle.vehicleClass)
            }
        })
    }

    @Test
    fun `selecting a vehicle category does not trigger an event if there is an empty filtered list`() {
        availability.filterVehicleListByCategory(MPV)

        verify(analytics, never()).vehicleSelected(any(), any())
    }

    @Test
    fun `selecting a vehicle category triggers an event if there is a filtered list`() {
        whenever(quotesService.quotes(any())).thenReturn(quotesCall)

        val observer = availability.bookingStatusObserver()
        observer.onChanged(BookingStatus(locationInfo, locationInfo, null))

        availability.setAllCategory(ALL)
        lambdaCaptor.firstValue.onValueChanged(Resource.Success(QuoteList(categories =
                                                                            CATEGORIES, id = QuoteId())))

        availability.filterVehicleListByCategory(MPV)
        verify(analytics).vehicleSelected(MPV, null)
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
    }

}