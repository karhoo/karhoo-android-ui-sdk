package com.karhoo.uisdk.screen.booking.checkout.prebookconfirmation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateView
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutViewContract
import com.karhoo.uisdk.screen.booking.domain.quotes.VehicleMappingsProvider
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.PicassoLoader
import com.karhoo.uisdk.util.extension.getCorrespondingLogoMapping
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.karhoo.uisdk.util.formatted
import org.joda.time.DateTime
import java.util.*

class PrebookConfirmationView(val quoteType: QuoteType?, val trip: TripInfo, val quote: Quote?) :
    MasterBottomSheetFragment(), ScheduledDateView {
    private val scheduledDateViewBinder = ScheduledDateViewBinder()
    var actions: CheckoutViewContract.PrebookViewActions? = null
    lateinit var pickUpAddressText: TextView
    lateinit var dropOffAddressText: TextView
    lateinit var rideConfirmedLogo: ImageView
    lateinit var fareText: TextView
    lateinit var fareTypeText: TextView
    lateinit var bookingTimeText: TextView
    lateinit var bookingDateText: TextView
    lateinit var closeButton: ImageButton


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_alert_prebook_confirmation, container, false)

        pickUpAddressText = view.findViewById(R.id.pickupAddressText)
        dropOffAddressText = view.findViewById(R.id.dropoffAddressText)
        rideConfirmedLogo = view.findViewById(R.id.rideConfirmedLogo)
        fareText = view.findViewById(R.id.fareText)
        fareTypeText = view.findViewById(R.id.fareTypeText)
        bookingTimeText =  view.findViewById(R.id.bookingTimeText)
        bookingDateText =  view.findViewById(R.id.bookingDateText)
        closeButton =  view.findViewById(R.id.masterBottomSheetCloseDialog)

        pickUpAddressText.text = trip.origin?.displayAddress
        dropOffAddressText.text = trip.destination?.displayAddress

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

        scheduledDateViewBinder.bind(this, trip)

        fareText.text =
            Currency.getInstance(trip.quote?.currency).formatted(trip.quote?.total.orZero())

        fareTypeText.text = quoteType?.toLocalisedString(requireContext()).orEmpty()

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
    }

    override fun displayNoDateAvailable() {
        // Do nothing
    }


    companion object {
        const val TAG = "PrebookConfirmationView"
    }

}
