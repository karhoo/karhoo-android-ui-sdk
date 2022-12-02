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
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
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
    val quoteType: QuoteType?,
    val journeyDetails: JourneyDetails,
    val quote: Quote?,
    private val loyaltyMode: LoyaltyMode,
    private val loyaltyPoints: Int?
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


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_alert_prebook_confirmation, container, false)

        rideConfirmedLogo = view.findViewById(R.id.rideConfirmedLogo)
        fareText = view.findViewById(R.id.fareText)
        fareTypeText = view.findViewById(R.id.fareTypeText)
        bookingTimeText = view.findViewById(R.id.bookingTimeText)
        bookingDateText = view.findViewById(R.id.bookingDateText)
        closeButton = view.findViewById(R.id.masterBottomSheetCloseDialog)
        prebookAddressComponent = view.findViewById(R.id.prebookAddressComponent)
        loyaltyStaticDetails = view.findViewById(R.id.loyaltyStaticDetails)
        addToCalendar = view.findViewById(R.id.addToCalendar)

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
            bookingTimeText.text = DateUtil.getTimeFormat(requireContext(), it)
            bookingDateText.text = DateUtil.parseDateWithDay(it)
        }

        fareText.text =
            quote?.price?.highPrice?.let {
                Currency.getInstance(quote.price.currencyCode).formatted(it)
            }

        fareTypeText.text = quoteType?.toLocalisedString(requireContext()).orEmpty()

        if(loyaltyMode == LoyaltyMode.NONE || loyaltyMode == LoyaltyMode.BURN || loyaltyMode == LoyaltyMode.EARN) {
            loyaltyStaticDetails.setup(requireContext(), loyaltyMode, loyaltyPoints ?: 0)
            loyaltyStaticDetails.visibility = VISIBLE
        } else {
            loyaltyStaticDetails.visibility = GONE
        }

        addToCalendar.setOnClickListener {
            val cal = Calendar.getInstance()
            val intent = Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,  cal.timeInMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.TITLE, "Yoga")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, "rowan@example.com,trevor@example.com")

            startActivity(intent)
        }

        setupHeader(view = view, title = getString(R.string.kh_uisdk_booking_confirmation))
        setupButton(
            view = view,
            buttonId = R.id.prebookRideDetails,
            text = getString(R.string.kh_uisdk_ride_details)
        ) {
            actions?.openRideDetails()
        }

        closeButton.setOnClickListener {
            dismiss()
            actions?.dismissedPrebookDialog()
        }

        return view
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

    companion object {
        const val TAG = "PrebookConfirmationView"
    }

}
