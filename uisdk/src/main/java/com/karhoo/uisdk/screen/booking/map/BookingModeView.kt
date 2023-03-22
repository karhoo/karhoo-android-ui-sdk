package com.karhoo.uisdk.screen.booking.map

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.AttributeSet
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.lifecycle.LifecycleOwner
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.booking.address.TimePickerTitleView
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerMVP
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerPresenter
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import kotlinx.android.synthetic.main.uisdk_view_booking_mode.view.*
import org.joda.time.DateTime
import java.util.*

class BookingModeView @JvmOverloads constructor(context: Context,
                                                attrs: AttributeSet? = null)
    : LinearLayout(context, attrs), BookingModeMVP.View, OnDateSetListener, TimeDatePickerMVP.View, TimePickerDialog.OnTimeSetListener {

    private val presenter: BookingModeMVP.Presenter = BookingModePresenter(this)
    private val timeDatePresenter: TimeDatePickerMVP.Presenter = TimeDatePickerPresenter(this, KarhooUISDK.analytics)

    var callbackToStartQuoteList: ((isPrebook: Boolean) -> Unit)? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_mode, this)

        nowActionButton.setOnClickListener {
            callbackToStartQuoteList?.invoke(false)
        }

        scheduleActionButton.setOnClickListener {
            if(presenter.isAllowedToBook){
                timeDatePresenter.datePickerClicked()
            }
        }

        nowActionButton.alpha = disabledOpacity
        scheduleActionButton.alpha = disabledOpacity
    }

    override fun enableNowButton(enable: Boolean) {
        nowActionButton.alpha = if (enable) 1F else disabledOpacity
        nowActionButton.isEnabled = enable

        loyaltyInfoLayout.visibility = if (enable) GONE else VISIBLE
    }

    override fun enableScheduleButton(enable: Boolean) {
        scheduleActionButton.alpha = if (enable) 1F else disabledOpacity
        scheduleActionButton.isEnabled = enable
    }

    override fun displayDatePicker(minDate: Long, maxDate: Long, timeZone: String) {
        val calendar = Calendar.getInstance()
        val previousSelectedDate = timeDatePresenter.getPreviousSelectedDateTime()
        val datePicker = DatePickerDialog(context,
            R.style.DialogTheme,
            this,
            previousSelectedDate?.year ?: calendar.get(Calendar.YEAR),
            previousSelectedDate?.monthOfYear?.minus(1) ?: calendar.get(Calendar.MONTH),
            previousSelectedDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH),
        ).apply {
            datePicker.minDate = minDate
            datePicker.maxDate = maxDate
        }
        datePicker.setCustomTitle(TimePickerTitleView(context).setTitle(R.string.kh_uisdk_prebook_timezone_title, timeZone))
        datePicker.show()
    }

    override fun displayTimePicker(hour: Int, minute: Int, timeZone: String) {
        val previousSelectedDate = timeDatePresenter.getPreviousSelectedDateTime()
        val dialog = TimePickerDialog(context,
            R.style.DialogTheme,
            this,
            previousSelectedDate?.hourOfDay ?: hour,
            previousSelectedDate?.minuteOfHour ?: minute,
            DateFormat.is24HourFormat(context))
        dialog.setCustomTitle(TimePickerTitleView(context).setTitle(R.string.kh_uisdk_prebook_timezone_title, timeZone))
        dialog.show()
    }

    override fun displayPrebookTime(time: DateTime) {
        callbackToStartQuoteList?.invoke(true)
    }

    override fun hideDateViews() {
        // no need to hide anything
    }

    fun watchJourneyDetailsState(lifecycleOwner: LifecycleOwner, journeyDetailsStateViewModel: JourneyDetailsStateViewModel) {
        journeyDetailsStateViewModel.viewStates().apply {
            observe(lifecycleOwner, timeDatePresenter.subscribeToJourneyDetails(journeyDetailsStateViewModel))
        }
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        timeDatePresenter.dateSelected(year, month, dayOfMonth)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        timeDatePresenter.timeSelected(hourOfDay, minute)
    }

    companion object {
        private const val disabledOpacity = 0.4F
    }
}
