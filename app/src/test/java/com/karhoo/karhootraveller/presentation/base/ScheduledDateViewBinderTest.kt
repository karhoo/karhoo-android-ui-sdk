package com.karhoo.karhootraveller.presentation.base

import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.uisdk.base.ScheduledDateView
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.nhaarman.mockitokotlin2.eq
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date

@RunWith(MockitoJUnitRunner::class)
class ScheduledDateViewBinderTest {


    @Mock
    private lateinit var view: ScheduledDateView

    private val binder = ScheduledDateViewBinder()

    /**
     * Given:   date scheduled unix time is equal to 0
     * When:    binding date
     * Then:    display no date available
     */
    @Test
    fun `display pending when date scheduled unix time is 0`() {
        val trip = TripInfo(dateScheduled = Date(0))

        binder.bind(view, trip)

        Mockito.verify(view).displayNoDateAvailable()
    }

    /**
     * Given:   origin timezone is null
     * When:    binding date
     * Then:    display no date available
     */
    @Test
    fun `display pending when origin timezone is null`() {
        val trip = TripInfo(
                dateScheduled = Date(0),
                origin = TripLocationInfo())

        binder.bind(view, trip)

        Mockito.verify(view).displayNoDateAvailable()
    }

    /**
     * Given:   a scheduled date and an origin timezone
     * When:    binding date
     * Then:    display date with correct offset
     */
    @Test
    fun `display date with correct offset`() {
        val localDate = DateTime(86400000L)
        val timeZone = DateTimeZone.getDefault().id

        val trip = TripInfo(
                dateScheduled = localDate.toDate(),
                origin = TripLocationInfo(timezone = timeZone))

        binder.bind(view, trip)

        Mockito.verify(view).displayDate(eq(localDate))
    }

}