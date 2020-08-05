package com.karhoo.uisdk.screen.booking.address.addressbar

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.address.TimePickerTitleView
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerMVP
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerPresenter
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.util.DateUtil
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.clearDateTimeButtonIcon
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.clearDestinationButtonIcon
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.dateTimeLabelLayout
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.dateTimeLowerText
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.dateTimeUpperText
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.dropOffEmptyIcon
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.dropOffFullIcon
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.dropOffLabel
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.flipButtonIcon
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.pickupLabel
import kotlinx.android.synthetic.main.uisdk_view_address_picker.view.scheduledIcon
import org.joda.time.DateTime
import java.util.Calendar

class AddressBarView
@JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null)
    : LinearLayout(context, attrs), AddressBarMVP.View,
      DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
      TimeDatePickerMVP.View, AddressBarViewContract.Widget {

    private val addressPresenter: AddressBarMVP.Presenter = AddressBarPresenter(this, KarhooUISDK.analytics)

    private val timeDatePresenter: TimeDatePickerMVP.Presenter = TimeDatePickerPresenter(this, KarhooUISDK.analytics)

    init {
        inflate(context, R.layout.uisdk_view_address_picker, this)

        pickupLabel.setOnClickListener { addressPresenter.pickUpAddressClicked() }
        dateTimeLabelLayout.setOnClickListener { timeDatePresenter.datePickerClicked() }
        scheduledIcon.setOnClickListener { timeDatePresenter.datePickerClicked() }
        clearDateTimeButtonIcon.setOnClickListener { timeDatePresenter.clearScheduledTimeClicked() }
        dropOffLabel.setOnClickListener { addressPresenter.dropOffAddressClicked() }
        clearDestinationButtonIcon.setOnClickListener { addressPresenter.clearDestinationClicked() }
        flipButtonIcon.setOnClickListener { addressPresenter.flipAddressesClicked(); }
    }

    override fun displayDatePicker(minDate: Long, maxDate: Long, timeZone: String) {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(context,
                                          R.style.DialogTheme,
                                          this,
                                          calendar.get(Calendar.YEAR),
                                          calendar.get(Calendar.MONTH),
                                          calendar.get(Calendar.DAY_OF_MONTH)).apply {
            datePicker.minDate = minDate
            datePicker.maxDate = maxDate
        }
        datePicker.setCustomTitle(TimePickerTitleView(context).setTitle(R.string.prebook_timezone_title, timeZone))
        datePicker.show()
    }

    override fun displayTimePicker(hour: Int, minute: Int, timeZone: String) {
        val dialog = TimePickerDialog(context, R.style.DialogTheme, this,
                                      hour, minute, DateFormat.is24HourFormat(context))
        dialog.setCustomTitle(TimePickerTitleView(context).setTitle(R.string.prebook_timezone_title, timeZone))
        dialog.show()
    }

    override fun displayPrebookTime(time: DateTime) {
        dateTimeUpperText.text = DateUtil.getTimeFormat(context, time)
        dateTimeLowerText.text = DateUtil.getDateFormat(time)
        dateTimeLowerText.visibility = View.VISIBLE
        scheduledIcon.visibility = View.GONE
        dateTimeUpperText.visibility = View.VISIBLE
        clearDateTimeButtonIcon.visibility = View.VISIBLE
    }

    override fun resetDateField() {
        timeDatePresenter.clearScheduledTimeClicked()
    }

    override fun hideDateViews() {
        dateTimeUpperText.visibility = View.GONE
        dateTimeLowerText.visibility = View.GONE
        clearDateTimeButtonIcon.visibility = View.GONE
        scheduledIcon.visibility = if (shouldHideScheduledIcon()) View.INVISIBLE else View.VISIBLE
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        timeDatePresenter.dateSelected(year, month, dayOfMonth)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        timeDatePresenter.timeSelected(hourOfDay, minute)
    }

    override fun setPickupAddress(displayAddress: String) {
        scheduledIcon.visibility = if (shouldHideScheduledIcon()) View.INVISIBLE else View.VISIBLE
        pickupLabel.text = displayAddress
    }

    override fun setDropoffAddress(displayAddress: String) {
        if (displayAddress.isBlank()) {
            dropOffLabel.apply {
                text = resources.getString(R.string.address_picker_dropoff_booking)
                setTextColor(ContextCompat.getColor(context, R.color.uisdk_text_action))
            }
            setDropoffAddressVisibility(true)
        } else {
            dropOffLabel.apply {
                text = displayAddress
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }
            setDropoffAddressVisibility(false)
        }
    }

    private fun setDropoffAddressVisibility(visible: Boolean) {
        clearDestinationButtonIcon.visibility = if (visible) View.GONE else View.VISIBLE
        dropOffFullIcon.visibility = if (visible) View.GONE else View.VISIBLE
        dropOffEmptyIcon.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun bindTripToView(tripDetails: TripInfo?) {
        tripDetails?.let {
            addressPresenter.setBothPickupDropoff(tripDetails)
        }
    }

    override fun setDefaultPickupText() {
        pickupLabel.text = context.getString(R.string.address_picker_add_pickup)
    }

    override fun showFlipButton() {
        flipButtonIcon.visibility = View.VISIBLE
    }

    override fun hideFlipButton() {
        flipButtonIcon.visibility = View.GONE
    }

    override fun setPickup(address: LocationInfo, addressPositionInList: Int) {
        addressPresenter.pickupSet(address, addressPositionInList)
    }

    override fun setDestination(address: LocationInfo, addressPositionInList: Int) {
        addressPresenter.destinationSet(address, addressPositionInList)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK && data != null) {
            when (requestCode) {
                AddressCodes.PICKUP -> extractActivityResultAndSetPickup(data)
                AddressCodes.DESTINATION -> extractActivityResultAndSetDestination(data)
            }
        }
    }

    private fun extractActivityResultAndSetPickup(data: Intent) {
        val pickup = data.getParcelableExtra<LocationInfo>(AddressCodes.DATA_ADDRESS)
        val addressPositionInList = data.getIntExtra(AddressCodes.DATA_POSITION_IN_LIST, -1)
        pickup?.let {
            setPickup(it, addressPositionInList)
        }
    }

    private fun extractActivityResultAndSetDestination(data: Intent) {
        val destination = data.getParcelableExtra<LocationInfo>(AddressCodes.DATA_ADDRESS)
        val addressPositionInList = data.getIntExtra(AddressCodes.DATA_POSITION_IN_LIST, -1)
        destination?.let {
            setDestination(it, addressPositionInList)
        }
    }

    override fun setJourneyInfo(journeyInfo: JourneyInfo?) {
        journeyInfo?.let {
            addressPresenter.prefillForJourney(it)
        }
    }

    override fun watchBookingStatusState(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel: BookingStatusStateViewModel) {
        bookingStatusStateViewModel.viewStates().apply {
            observe(lifecycleOwner, addressPresenter.subscribeToBookingStatus(bookingStatusStateViewModel))
            observe(lifecycleOwner, timeDatePresenter.subscribeToBookingStatus(bookingStatusStateViewModel))
        }
    }

    private fun shouldHideScheduledIcon(): Boolean {
        return pickupLabel.text == context.getString(R.string.address_picker_add_pickup) ||
                clearDateTimeButtonIcon.visibility == View.VISIBLE
    }
}
