package com.karhoo.uisdk.util

import android.content.Context
import android.content.res.Resources
import com.karhoo.sdk.api.model.ServiceCancellation
import com.karhoo.sdk.api.model.TripStatus
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.rides.upcoming.card.UpcomingRideCardPresenterTest
import com.karhoo.uisdk.util.extension.getCancellationText
import com.karhoo.uisdk.util.extension.hasValidCancellationDependingOnTripStatus
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner.Silent::class)
class ServiceCancellationExtTests {
    private var context: Context = mock()
    private var resources: Resources = mock()

    @Before
    fun setup() {
        whenever(context.resources).thenReturn(resources)
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_ONE, TEST_ONE)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTE, TEST_ONE))
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_TWO, TEST_TWO)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES, TEST_TWO))
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_TEN, TEST_TEN)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES, TEST_TEN))
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_minutes_plurals, TEST_THIRTY, TEST_THIRTY)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES, TEST_THIRTY))
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_hours_plural, TEST_ONE, TEST_ONE)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_HOUR, TEST_ONE))
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_hours_plural, TEST_TWO, TEST_TWO)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_HOURS, TEST_TWO))
        whenever(resources.getQuantityString(R.plurals.kh_uisdk_hours_plural, TEST_THREE, TEST_THREE)).thenReturn(String.format(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_HOURS, TEST_TWO))
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_driver_departure)).thenReturn(UpcomingRideCardPresenterTest.TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_start)).thenReturn(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_START)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_before_pickup_ending)).thenReturn(TEST_CANCELLATION_TEXT_BEFORE_PICKUP_END)
        whenever(context.getString(R.string.kh_uisdk_quote_cancellation_and_keyword)).thenReturn(TEST_CANCELLATION_TEXT_AND_KEYWORD)
    }


    @Test
    fun `When getting the cancellation text for a before pickup SLA of 1 minute, then the correct TEST_RESULT_1 is returned `() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_ONE)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_1)
    }

    @Test
    fun `When getting the cancellation text for a before pickup SLA of 10 minutes, then the correct TEST_RESULT_2 is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_TEN)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_2)
    }

    @Test
    fun `When getting the cancellation text for a before pickup SLA of 60 minutes, then the correct TEST_RESULT_3 is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_SIXTY)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_3)
    }

    @Test
    fun `When getting the cancellation text for a before time pickup of 120 minutes, then the correct TEST_RESULT_4 is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_ONE_HUNDRED_TWENTY)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_4)
    }

    @Test
    fun `When getting the cancellation text for a before pickup SLA of 150 minutes, then the correct TEST_RESULT_5 is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_ONE_HUNDRED_FIFTY)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_5)
    }

    @Test
    fun `When getting the cancellation text for a before pickup SLA of 121 minutes, then the correct TEST_RESULT_6 is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_ONE_HUNDRED_TWENTY_ONE)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_6)
    }

    @Test
    fun `When getting the cancellation text for a before pickup SLA with 0 minutes, then a null text is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_ZERO)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), null)
    }

    @Test
    fun `When getting the cancellation text for a before driver en route SLA, then the correct text is returned`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_BEFORE_DRIVER_EN_ROUTE)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), UpcomingRideCardPresenterTest.TEST_CANCELLATION_DRIVER_EN_ROUTE_TEXT)
    }

    @Test
    fun `When getting a cancellation SLA of type before pickup and a REQUESTED | CONFIRMED | DRIVER_EN_ROUTE | ARRIVED trip status, then the SLA is valid`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP)

        Assert.assertTrue(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.REQUESTED))
        Assert.assertTrue(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.CONFIRMED))
        Assert.assertTrue(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.DRIVER_EN_ROUTE))
        Assert.assertTrue(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.ARRIVED))
    }

    @Test
    fun `When getting a cancellation SLA of type before pickup and a COMPLETED trip status, then the SLA is not valid`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP)
        Assert.assertFalse(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.COMPLETED))
    }

    @Test
    fun `When getting a cancellation SLA of type before driver en route and an REQUESTED | CONFIRMED trip status, then the SLA is valid`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_BEFORE_DRIVER_EN_ROUTE)
        Assert.assertTrue(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.REQUESTED))
        Assert.assertTrue(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.CONFIRMED))
    }

    @Test
    fun `When getting a cancellation SLA of type before driver en route and an DRIVER_EN_ROUTE | ARRIVED trip status, then the SLA is not valid`() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_BEFORE_DRIVER_EN_ROUTE)
        Assert.assertFalse(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.DRIVER_EN_ROUTE))
        Assert.assertFalse(serviceCancellation.hasValidCancellationDependingOnTripStatus(TripStatus.ARRIVED))
    }

    @Test
    fun `When getting the cancellation text for a before pickup SLA of 2 minute, then the correct TEST_RESULT_7 is returned `() {
        val serviceCancellation = ServiceCancellation(CANCELLATION_TIME_BEFORE_PICKUP, TEST_TWO)

        Assert.assertEquals(serviceCancellation.getCancellationText(context), TEST_RESULT_7)
    }

    companion object {
        const val TEST_CANCELLATION_TEXT_BEFORE_PICKUP_START = "Free cancellation up to"
        const val TEST_CANCELLATION_TEXT_BEFORE_PICKUP_END = "before pickup"
        const val TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTE = "%d minute"
        const val TEST_CANCELLATION_TEXT_BEFORE_PICKUP_MINUTES = "%d minutes"
        const val TEST_CANCELLATION_TEXT_BEFORE_PICKUP_HOUR = "%d hour"
        const val TEST_CANCELLATION_TEXT_BEFORE_PICKUP_HOURS = "%d hours"
        const val TEST_CANCELLATION_TEXT_AND_KEYWORD = "and"
        const val TEST_RESULT_1 = "Free cancellation up to 1 minute before pickup"
        const val TEST_RESULT_2 = "Free cancellation up to 10 minutes before pickup"
        const val TEST_RESULT_3 = "Free cancellation up to 1 hour before pickup"
        const val TEST_RESULT_4 = "Free cancellation up to 2 hours before pickup"
        const val TEST_RESULT_5 = "Free cancellation up to 2 hours and 30 minutes before pickup"
        const val TEST_RESULT_6 = "Free cancellation up to 2 hours and 1 minute before pickup"
        const val TEST_RESULT_7 = "Free cancellation up to 2 minutes before pickup"
        const val TEST_ZERO = 0
        const val TEST_ONE = 1
        const val TEST_TWO = 2
        const val TEST_THREE = 2
        const val TEST_TEN = 10
        const val TEST_THIRTY = 30
        const val TEST_SIXTY = 60
        const val TEST_ONE_HUNDRED_TWENTY = 120
        const val TEST_ONE_HUNDRED_TWENTY_ONE = 121
        const val TEST_ONE_HUNDRED_FIFTY = 150
    }
}