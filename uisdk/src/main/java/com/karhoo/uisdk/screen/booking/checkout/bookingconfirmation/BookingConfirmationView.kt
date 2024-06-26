package com.karhoo.uisdk.screen.booking.checkout.bookingconfirmation

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateView
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment
import com.karhoo.uisdk.screen.address.static.AddressStaticComponent
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyMode
import com.karhoo.uisdk.screen.booking.checkout.loyalty.LoyaltyStaticDetails
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.quotes.VehicleMappingsProvider
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.PicassoLoader
import com.karhoo.uisdk.util.extension.getCorrespondingLogoMapping
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.karhoo.uisdk.util.formatted
import org.joda.time.DateTime
import java.util.*

class BookingConfirmationView(
    val journeyDetails: JourneyDetails,
    val quote: Quote?,
    private val flightNumber: String?,
    private val trainNumber: String?,
    private val tripId: String?,
    private val showAddRideToCalendar: Boolean = true
) :
    MasterBottomSheetFragment(), ScheduledDateView {
    var actions: CheckoutViewContract.BookingConfirmationActions? = null
    lateinit var prebookAddressComponent: AddressStaticComponent
    lateinit var loyaltyStaticDetails: LoyaltyStaticDetails
    lateinit var rideConfirmedLogo: ImageView
    lateinit var fareText: TextView
    lateinit var fareTypeText: TextView
    lateinit var bookingTimeText: TextView
    lateinit var bookingDateText: TextView
    lateinit var addToCalendar: TextView
    lateinit var closeButton: ImageButton
    private lateinit var loyaltyMode: LoyaltyMode
    private var loyaltyPoints: Int? = 0
    private var loyaltyVisible: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_booking_confirmation, container, false)
        rideConfirmationScreenOpened()
        setupViews(view)

        prebookAddressComponent.setup(
            journeyDetails.pickup!!,
            journeyDetails.destination!!,
            journeyDetails.date,
            AddressStaticComponent.AddressComponentType.NORMAL
        )

        val logoImageUrl = VehicleMappingsProvider.getVehicleMappings()?.let {
            quote?.vehicle?.getCorrespondingLogoMapping(it)?.vehicleImagePNG
        } ?: quote?.fleet?.logoUrl

        PicassoLoader.loadImage(
            requireContext(),
            rideConfirmedLogo,
            logoImageUrl,
            R.drawable.uisdk_ic_quotes_logo_empty,
            R.dimen.kh_uisdk_driver_photo_size,
            R.integer.kh_uisdk_logo_radius
        )

        journeyDetails.date?.let {
            bookingDateText.text = DateUtil.parseDateWithDay(it)
            bookingTimeText.text = DateUtil.getTimeFormat(requireContext(), it)
            bookingTimeText.contentDescription =
                context?.getString(R.string.kh_uisdk_acc_pickup_time) + " " + bookingTimeText.text + " " + bookingDateText.text
        }

        fareTypeText.text = quote?.quoteType?.toLocalisedString(requireContext()).orEmpty()
        fareText.text =
            quote?.price?.highPrice?.let {
                Currency.getInstance(quote.price.currencyCode).formatted(it)
            }
        fareText.contentDescription = fareTypeText.text as String + " " + fareText.text

        setupLoyaltyComponent(loyaltyVisible, loyaltyMode, loyaltyPoints)

        addToCalendar.setOnClickListener {
            rideConfirmationAddToCalendarSelected()
            addCalendarEvent()
        }

        addToCalendar.visibility = if(showAddRideToCalendar) VISIBLE else GONE

        setupHeader(view = view, title = getString(R.string.kh_uisdk_booking_confirmation))
        setupButton(
            view = view,
            buttonId = R.id.prebookRideDetails,
            text = getString(R.string.kh_uisdk_ride_details)
        ) {
            rideConfirmationDetailsSelected()
            actions?.openRideDetails()
        }
        setupButtonContentDescription(R.id.prebookRideDetails, context?.getString(R.string.kh_uisdk_acc_ride_details) ?: "")

        closeButton.setOnClickListener {
            dismiss()
            actions?.dismissedPrebookDialog()
        }

        return view
    }

    private fun setupViews(view: View){
        rideConfirmedLogo = view.findViewById(R.id.rideConfirmedLogo)
        fareText = view.findViewById(R.id.fareText)
        fareTypeText = view.findViewById(R.id.fareTypeText)
        bookingTimeText = view.findViewById(R.id.bookingTimeText)
        bookingDateText = view.findViewById(R.id.bookingDateText)
        closeButton = view.findViewById(R.id.masterBottomSheetCloseDialog)
        prebookAddressComponent = view.findViewById(R.id.prebookAddressComponent)
        loyaltyStaticDetails = view.findViewById(R.id.loyaltyStaticDetails)
        addToCalendar = view.findViewById(R.id.addToCalendar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun rideConfirmationScreenOpened(){
        KarhooUISDK.analytics?.rideConfirmationScreenOpened(
            date = journeyDetails.date!!.toDate(),
            tripId = tripId,
            quoteId = quote?.id
        )
    }

    private fun rideConfirmationAddToCalendarSelected(){
        KarhooUISDK.analytics?.rideConfirmationAddToCalendarSelected(
            date = journeyDetails.date!!.toDate(),
            tripId = tripId,
            quoteId = quote?.id
        )
    }

    private fun rideConfirmationDetailsSelected(){
        KarhooUISDK.analytics?.rideConfirmationDetailsSelected(
            date = journeyDetails.date!!.toDate(),
            tripId = tripId,
            quoteId = quote?.id
        )
    }

    override fun displayDate(date: DateTime) {
        bookingTimeText.text = DateUtil.getTimeFormat(requireContext(), date)
        bookingDateText.text = DateUtil.parseDateWithDay(date)

        prebookAddressComponent.setup(
            pickup = journeyDetails.pickup!!,
            destination = journeyDetails.destination!!,
            date,
            AddressStaticComponent.AddressComponentType.WITH_TIME
        )
    }

    override fun displayNoDateAvailable() {
        prebookAddressComponent.setup(
            pickup = journeyDetails.pickup!!,
            destination = journeyDetails.destination!!,
            type = AddressStaticComponent.AddressComponentType.WITH_TIME
        )
    }


    override fun onCancel(dialog: DialogInterface) {
        actions?.dismissedPrebookDialog()
        super.onCancel(dialog)
    }

    private fun addCalendarEvent() {
        var flightDescription: String = ""
        flightNumber?.let {
            flightDescription =
                requireContext().getString(R.string.kh_uisdk_trip_summary_flight_number) + ": " + flightNumber
        }

        var trainDescription: String = ""
        trainNumber?.let {
            trainDescription =
                requireContext().getString(R.string.kh_uisdk_trip_summary_train_number) + ": " + trainNumber
        }

        val pickup: String = journeyDetails.pickup!!.address.displayAddress
        val dropOff: String = journeyDetails.destination!!.address.displayAddress

        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, journeyDetails.date?.millis)
            .putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                journeyDetails.date?.millis?.plus(CALENDAR_HOUR)
            )
            .putExtra(
                CalendarContract.Events.TITLE, String.format(
                    requireContext().getString(R.string.kh_uisdk_trip_summary_calendar_event_title),
                    journeyDetails.destination!!.address.displayAddress
                )
            )
            .putExtra(
                CalendarContract.Events.DESCRIPTION,
                "$flightDescription \n $trainDescription \n $pickup \n $dropOff"
            )
            .putExtra(
                CalendarContract.Events.EVENT_LOCATION,
                pickup
            )
            .putExtra(
                CalendarContract.Events.AVAILABILITY,
                CalendarContract.Events.AVAILABILITY_BUSY
            )
            .putExtra(
                CalendarContract.Events.HAS_ALARM,
                1
            )

        startActivity(intent)
    }

    private fun setupLoyaltyComponent(
        show: Boolean,
        loyaltyMode: LoyaltyMode,
        loyaltyPoints: Int?
    ) {
        val eligibleLoyalty =
            loyaltyMode == LoyaltyMode.NONE || loyaltyMode == LoyaltyMode.BURN || loyaltyMode == LoyaltyMode.EARN
        if (show && eligibleLoyalty) {
            loyaltyStaticDetails.setup(requireContext(), loyaltyMode, loyaltyPoints ?: 0)
            loyaltyStaticDetails.visibility = VISIBLE
        } else {
            loyaltyStaticDetails.visibility = GONE
        }
    }

    fun setLoyaltyProperties(visibile: Boolean, loyaltyMode: LoyaltyMode, loyaltyPoints: Int?) {
        this.loyaltyMode = loyaltyMode
        this.loyaltyPoints = loyaltyPoints
        this.loyaltyVisible = visibile
    }

    companion object {
        const val TAG = "PrebookConfirmationView"
        const val CALENDAR_HOUR = 60 * 60 * 1000
    }

}
