package com.karhoo.uisdk.screen.booking.address.addressbar

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.address.AddressCodes
import com.karhoo.uisdk.screen.booking.address.DropOffEmptyView
import com.karhoo.uisdk.screen.booking.address.DropOffFullView
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerMVP
import com.karhoo.uisdk.screen.booking.address.timedatepicker.TimeDatePickerPresenter
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetailsStateViewModel
import com.karhoo.uisdk.screen.booking.domain.address.JourneyInfo
import com.karhoo.uisdk.util.DateUtil
import org.joda.time.DateTime

class AddressBarView
@JvmOverloads constructor(context: Context,
                          attrs: AttributeSet? = null)
    : LinearLayout(context, attrs), AddressBarMVP.View,
      TimeDatePickerMVP.View, AddressBarViewContract.Widget {

    private val addressPresenter: AddressBarMVP.Presenter = AddressBarPresenter(this, KarhooUISDK.analytics)

    private val timeDatePresenter: TimeDatePickerMVP.Presenter = TimeDatePickerPresenter(this, KarhooUISDK.analytics)

    private lateinit var clearDateTimeButtonIcon: ImageView
    private lateinit var clearDestinationButtonIcon: ImageView
    private lateinit var dateTimeLabelLayout: LinearLayout
    private lateinit var dateTimeLowerText: TextView
    private lateinit var dateTimeUpperText: TextView
    private lateinit var dropOffEmptyIcon: DropOffEmptyView
    private lateinit var dropOffFullIcon: DropOffFullView
    private lateinit var dropOffLabel: TextView
    private lateinit var flipButtonIcon: ImageView
    private lateinit var pickupLabel: TextView
    private lateinit var scheduledIcon: ImageView
    private lateinit var dateTimeDivider: View
    private lateinit var dateTimeLayout: ConstraintLayout

    init {
        inflate(context, R.layout.uisdk_view_address_picker, this)

        clearDateTimeButtonIcon = findViewById(R.id.clearDateTimeButtonIcon)
        clearDestinationButtonIcon = findViewById(R.id.clearDestinationButtonIcon)
        dateTimeLabelLayout = findViewById(R.id.dateTimeLabelLayout)
        dateTimeLowerText = findViewById(R.id.dateTimeLowerText)
        dateTimeUpperText = findViewById(R.id.dateTimeUpperText)
        dropOffEmptyIcon = findViewById(R.id.dropOffEmptyIcon)
        dropOffFullIcon = findViewById(R.id.dropOffFullIcon)
        dropOffLabel = findViewById(R.id.dropOffLabel)
        flipButtonIcon = findViewById(R.id.flipButtonIcon)
        pickupLabel = findViewById(R.id.pickupLabel)
        scheduledIcon = findViewById(R.id.scheduledIcon)
        dateTimeDivider = findViewById(R.id.dateTimeDivider)
        dateTimeLayout = findViewById(R.id.dateTimeLayout)

        pickupLabel.setOnClickListener { addressPresenter.pickUpAddressClicked() }
        dateTimeLabelLayout.setOnClickListener { timeDatePresenter.datePickerClicked() }
        scheduledIcon.setOnClickListener { timeDatePresenter.datePickerClicked() }
        clearDateTimeButtonIcon.setOnClickListener { timeDatePresenter.clearScheduledTimeClicked() }
        dropOffLabel.setOnClickListener { addressPresenter.dropOffAddressClicked() }
        clearDestinationButtonIcon.setOnClickListener { addressPresenter.clearDestinationClicked() }
        clearDestinationButtonIcon.contentDescription = context.getString(R.string.kh_uisdk_delete_destination_address)
        flipButtonIcon.setOnClickListener { addressPresenter.flipAddressesClicked(); }
    }

    override fun displayPrebookTime(time: DateTime) {
        dateTimeUpperText.text = DateUtil.getTimeFormat(context, time)
        dateTimeLowerText.text = DateUtil.getDateFormat(time)
        dateTimeLowerText.visibility = View.VISIBLE
        scheduledIcon.visibility = View.GONE
        dateTimeUpperText.visibility = View.VISIBLE
        clearDateTimeButtonIcon.visibility = View.VISIBLE
    }

    fun setPrebookTime(time: DateTime?){
        addressPresenter.dateSet(time)
    }

    override fun resetDateField() {
        timeDatePresenter.clearScheduledTimeClicked()
    }

    override fun hideDateViews() {
        dateTimeUpperText.visibility = View.GONE
        dateTimeLowerText.visibility = View.GONE
        clearDateTimeButtonIcon.visibility = View.GONE
        scheduledIcon.visibility = if (shouldHideScheduledIcon()) View.GONE else View.VISIBLE
        dateTimeDivider.visibility = if (shouldHideScheduledIcon()) View.GONE else View.VISIBLE
    }

    fun showPrebookIcon(visible: Boolean){
        dateTimeLayout.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setPickupAddress(displayAddress: String) {
        scheduledIcon.visibility = if (shouldHideScheduledIcon()) View.GONE else View.VISIBLE
        dateTimeDivider.visibility = if (shouldHideScheduledIcon()) View.GONE else View.VISIBLE
        pickupLabel.text = displayAddress
        pickupLabel.contentDescription = context.getString(R.string.kh_uisdk_accessibility_label_pickup_address) + " " + displayAddress

        if(displayAddress.isBlank()){
            pickupLabel.apply {
                setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_label))
            }
        } else {
            pickupLabel.apply {
                setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_headline))
            }
        }
    }

    override fun setDropoffAddress(displayAddress: String) {
        if (displayAddress.isBlank()) {
            dropOffLabel.apply {
                text = resources.getString(R.string.kh_uisdk_address_picker_dropoff_booking)
                setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_label))
                contentDescription = resources.getString(R.string.kh_uisdk_accessibility_label_drop_off_address) + " " + resources.getString(R.string.kh_uisdk_address_picker_dropoff_booking)
            }
            setDropoffAddressVisibility(true)
        } else {
            dropOffLabel.apply {
                text = displayAddress
                setTextColor(ContextCompat.getColor(context, R.color.kh_uisdk_headline))
                contentDescription = resources.getString(R.string.kh_uisdk_accessibility_label_drop_off_address) + " " + displayAddress
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
        pickupLabel.text = context.getString(R.string.kh_uisdk_address_picker_add_pickup)
        pickupLabel.contentDescription = context.getString(R.string.kh_uisdk_accessibility_label_pickup_address) + " " + context.getString(R.string.kh_uisdk_address_picker_add_pickup)
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

    override fun watchJourneyDetailsState(lifecycleOwner: LifecycleOwner, journeyDetailsStateViewModel: JourneyDetailsStateViewModel) {
        journeyDetailsStateViewModel.viewStates().apply {
            observe(lifecycleOwner, addressPresenter.subscribeToJourneyDetails(journeyDetailsStateViewModel))
            observe(lifecycleOwner, timeDatePresenter.subscribeToJourneyDetails(journeyDetailsStateViewModel))
        }
    }

    private fun shouldHideScheduledIcon(): Boolean {
        return pickupLabel.text == context.getString(R.string.kh_uisdk_address_picker_add_pickup) ||
                clearDateTimeButtonIcon.visibility == View.VISIBLE
    }
}
