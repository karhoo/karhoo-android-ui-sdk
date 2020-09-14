package com.karhoo.uisdk.screen.booking.address.timedatepicker

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.analytics.Analytics
import com.karhoo.uisdk.screen.booking.address.addressbar.AddressBarViewContract
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatus
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.firstValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.secondValue
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TimeDatePickerPresenterTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private var bookingStatusStateViewModel: BookingStatusStateViewModel = mock()
    private var bookingStatus: BookingStatus = mock()

    private val view: TimeDatePickerMVP.View = mock()
    private val analytics: Analytics = mock()

    @Captor
    private lateinit var intArgumentCaptor: ArgumentCaptor<Int>

    @Captor
    private lateinit var longArgumentCaptor: ArgumentCaptor<Long>

    @Captor
    private lateinit var dateTimeArgumentCaptor: ArgumentCaptor<DateTime>

    private lateinit var timePickerPresenter: TimeDatePickerPresenter

    @Before
    fun setUp() {
        timePickerPresenter = TimeDatePickerPresenter(view, analytics)
        timePickerPresenter.subscribeToBookingStatus(bookingStatusStateViewModel)
    }

    /**
     * Given:   The presenter is asked to subscribe to booking status
     * When:    The booking status is called
     * Then:    The observer should be updated
     **/
    @Test
    fun `subscribing to booking status returns observer`() {
        val observer = timePickerPresenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        assertNotNull(observer)
    }

    /**
     * Given:   The presenter has subscribed to the booking status
     * When:    The date field is nulled out
     * Then:    A call to hide the time/date field is made
     **/
    @Test
    fun `when the date is null a call is made to hide the time date field`() {
        val observer = timePickerPresenter.subscribeToBookingStatus(bookingStatusStateViewModel)
        observer.onChanged(BookingStatus(null, null, null))
        verify(view).hideDateViews()
    }

    /**
     * Given:   The presenter has been told the date picker is clicked
     * When:    Telling the view to show the dialog
     * Then:    The timezone name should be a parameter
     **/
    @Test
    fun `when the date picker is clicked the view is passes the timezone name`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_INFO)
        timePickerPresenter.datePickerClicked()
        verify(view).displayDatePicker(any(), any(), any())
    }

    /**
     * Given:   The presenter has been told the date picker is clicked
     * When:    There is no pickup set
     * Then:    No call should be made to show the display date picker
     **/
    @Test
    fun `no call made to display date picker when pickup isnt set`() {
        timePickerPresenter.datePickerClicked()
        verify(view, never()).displayDatePicker(any(), any(), any())
    }

    /**
     * Given:   The presenter is told the date picker is clicked
     * When:    Launching the date picker
     * Then:    The min and max time should be today and one years time
     **/
    @Test
    fun `launching date picker should have min date of today and max of seven days time`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatusStateViewModel.currentState.pickup).thenReturn(LOCATION_AMSTERDAM)

        val now = DateTime.now()
        val oneHourAhead = now.plusMinutes(59)
        val twoHoursAhead = now.plusHours(2)
        val oneYearAhead = now.plusYears(1)
        val oneYearOneDayAhead = now.plusYears(1).plusDays(1)

        timePickerPresenter.datePickerClicked()

        verify(view).displayDatePicker(capture(longArgumentCaptor), capture(longArgumentCaptor), any())

        assertThat(longArgumentCaptor.firstValue).isGreaterThanOrEqualTo(oneHourAhead.millis)
        assertThat(longArgumentCaptor.firstValue).isLessThanOrEqualTo(twoHoursAhead.millis)
        assertThat(longArgumentCaptor.secondValue).isGreaterThanOrEqualTo(oneYearAhead.millis)
        assertThat(longArgumentCaptor.secondValue).isLessThanOrEqualTo(oneYearOneDayAhead.millis)
    }

    /**
     * Given:   The presenter is told the date picker is clicked
     * When:    The date picker has opened
     * Then:    An event is sent to analytics that the prebook has opened
     **/
    @Test
    fun `analytical event prebook opened sends when date picker is clicked`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_INFO)
        timePickerPresenter.datePickerClicked()
        verify(analytics).prebookOpened()
    }

    /**
     * Given:   A date has been selected
     * When:    Updating the date feilds
     * Then:    A call should be made to display the time picker
     **/
    @Test
    fun `time picker is displayed after the date has been selected`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_INFO)
        timePickerPresenter.dateSelected(1, 1, 1)
        verify(view).displayTimePicker(any(), any(), any())
    }

    /**
     * Given:   A date has been selected
     * When:    Presetting the time
     * Then:    A localised string of the timezone should be sent
     **/
    @Test
    fun `localised timezone string is sent to the time picker`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_INFO)
        timePickerPresenter.dateSelected(1, 1, 1)
        verify(view).displayTimePicker(any(), any(), eq("GMT"))
    }

    /**
     * Given:   A date has been selected
     * When:    Presetting the time
     * Then:    The time should be at least 1 hour ahead of the current time
     **/
    @Test
    fun `when selecting a time the inital set time is one hour ahead`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_INFO)

        timePickerPresenter.dateSelected(1, 1, 1)

        verify(view).displayTimePicker(
                capture(intArgumentCaptor),
                any(),
                any())

        assertThat(intArgumentCaptor.firstValue).isGreaterThanOrEqualTo(oneHourAheadLondon.hourOfDay)
    }

    /**
     * Given:   A date has been selected in a foriegn country
     * When:    Selecting a time for booking in the country
     * Then:    The offet should match the pickup offset
     **/
    @Test
    fun `prebook picker shows offset of the country of booking`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_AMSTERDAM)

        timePickerPresenter.dateSelected(1, 1, 1)

        verify(view).displayTimePicker(
                capture(intArgumentCaptor),
                any(),
                any())

        assertThat(intArgumentCaptor.firstValue).isGreaterThanOrEqualTo(oneHourAheadAmsterdam.hourOfDay)
    }

    /**
     * Given:   A time has been selected
     * When:    Parsing the time
     * Then:    The time should be checked if its an hour ahead before showing
     **/
    @Test
    fun `time that is more than an hour ahead will be returned correctly`() {
        val localOneHourAheadAmsterdam: DateTime
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_AMSTERDAM)

        val now = DateTime.now(DateTimeZone.forID(LOCATION_AMSTERDAM.timezone))
        localOneHourAheadAmsterdam = now.plusMinutes(59)
        timePickerPresenter.dateSelected(now.year, now.monthOfYear - 1, now.dayOfMonth)
        timePickerPresenter.timeSelected(twoHoursAheadAmsterdam.hourOfDay, 0)

        verify(view).displayPrebookTime(capture(dateTimeArgumentCaptor))

        assertThat(dateTimeArgumentCaptor.firstValue.millis).isGreaterThanOrEqualTo(localOneHourAheadAmsterdam.millis)
    }

    /**
     * Given:   A time has been selected
     * When:    The time is less than an hour from the current time
     * Then:    The time is round up
     **/
    @Test
    fun `time gets round up if the time is less than the current time plus one hour`() {
        val localOneHourAheadAmsterdam: DateTime
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_AMSTERDAM)

        val now = DateTime.now(DateTimeZone.forID(LOCATION_AMSTERDAM.timezone))
        localOneHourAheadAmsterdam = now.plusMinutes(59)
        timePickerPresenter.dateSelected(now.year, now.monthOfYear - 1, now.dayOfMonth)
        timePickerPresenter.timeSelected(now.hourOfDay, 0)

        verify(view).displayPrebookTime(capture(dateTimeArgumentCaptor))

        assertThat(dateTimeArgumentCaptor.firstValue.millis).isGreaterThanOrEqualTo(localOneHourAheadAmsterdam.millis)
    }

    /**
     * Given:   A time has been selected
     * When:    The time is set
     * Then:    An analytical event is sent
     **/
    @Test
    fun `analytical event is sent when the time is set`() {
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatus.pickup).thenReturn(LOCATION_AMSTERDAM)

        val now = DateTime.now(DateTimeZone.forID(LOCATION_AMSTERDAM.timezone))
        timePickerPresenter.dateSelected(now.year, now.monthOfYear - 1, now.dayOfMonth)
        timePickerPresenter.timeSelected(twoHoursAheadAmsterdam.hourOfDay, 0)

        verify(analytics).prebookSet(any(), any())
    }

    /**
     * Given:   The clear button has been clicked
     * Then:    The view should be told to hide the date
     **/
    @Test
    fun `view told to hide the date when clear button has been clicked`() {
        timePickerPresenter.clearScheduledTimeClicked()
        verify(view).hideDateViews()
    }

    /**
     * Given:   The clear button has been clicked
     * When:    The booking status is checked
     * Then:    The date should be null
     **/
    @Test
    fun `date should be null when clear prebook time is clicked`() {
        timePickerPresenter.clearScheduledTimeClicked()
        verify(bookingStatusStateViewModel).process(AddressBarViewContract.AddressBarEvent.BookingDateEvent(null))
    }

    /**
     * Given:   The date has been set
     * When:    Checking the live booking status
     * Then:    The date should exist
     **/
    @Test
    fun `once date has been selected it is set in live booking status`() {
        whenever(bookingStatus.pickup).thenReturn(LOCATION_AMSTERDAM)
        whenever(bookingStatusStateViewModel.currentState).thenReturn(bookingStatus)
        whenever(bookingStatusStateViewModel.currentState.pickup).thenReturn(LOCATION_AMSTERDAM)

        val now = DateTime.now()
        timePickerPresenter.dateSelected(now.year, now.monthOfYear - 1, now.dayOfMonth)
        timePickerPresenter.timeSelected(twoHoursAheadAmsterdam.hourOfDay, 0)

        verify(bookingStatusStateViewModel).process(any())
        verify(analytics).prebookSet(any(), any())
    }

    companion object {

        val LOCATION_INFO = LocationInfo(
                timezone = "Europe/London"
                                        )

        val LOCATION_AMSTERDAM = LocationInfo(
                timezone = "Europe/Amsterdam"
                                             )

        val timezoneAmsterdam: DateTimeZone = DateTimeZone.forID(LOCATION_AMSTERDAM.timezone)
        val timezoneLondon: DateTimeZone = DateTimeZone.forID(LOCATION_INFO.timezone)

        val oneHourAheadAmsterdam: DateTime = DateTime.now(timezoneAmsterdam).plusHours(1)
        val twoHoursAheadAmsterdam: DateTime = DateTime.now(timezoneAmsterdam).plusHours(2)

        val oneHourAheadLondon: DateTime = DateTime.now(timezoneLondon).plusHours(1)
    }

}
