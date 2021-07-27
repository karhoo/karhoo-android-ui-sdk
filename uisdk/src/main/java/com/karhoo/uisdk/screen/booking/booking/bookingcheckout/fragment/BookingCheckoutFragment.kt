package com.karhoo.uisdk.screen.booking.booking.bookingcheckout.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.booking.bookingcheckout.activity.BookingCheckoutActivity
import com.karhoo.uisdk.screen.booking.booking.bookingcheckout.views.BookingCheckoutView
import java.util.HashMap

internal class BookingCheckoutFragment : Fragment(), LoadingButtonView.Actions {
    private lateinit var bookingRequestButton: LoadingButtonView
    private lateinit var bookingCheckoutView: BookingCheckoutView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.uisdk_booking_checkout_fragment, container, false)

        bookingRequestButton = view.findViewById(R.id.bookingRequestButton)
        bookingRequestButton.actions = this
        bookingRequestButton.onLoadingComplete()

        bookingCheckoutView = view.findViewById(R.id.bookingCheckoutView)

        val bundle = arguments as Bundle
        bookingCheckoutView.setLoadingButtonCallback(object  :BookingCheckoutFragmentContract.LoadingButtonListener {
            override fun onLoadingComplete() {
                bookingRequestButton.onLoadingComplete()
            }

            override fun showLoading() {
                bookingRequestButton.showLoading()
            }
        })

        bookingCheckoutView.showBookingRequest(
                quote = bundle.getParcelable(BookingCheckoutActivity.BOOKING_REQUEST_QUOTE_KEY)!!,
                bookingStatus = bundle.getParcelable(BookingCheckoutActivity.BOOKING_REQUEST_STATUS_KEY),
                outboundTripId = bundle.getString(BookingCheckoutActivity.BOOKING_REQUEST_OUTBOUND_TRIP_ID_KEY),
                bookingMetadata = bundle.getSerializable(BookingCheckoutActivity.BOOKING_REQUEST_METADATA_KEY) as HashMap<String, String>?
        )

        return view;
    }

    override fun onLoadingButtonClick() {
        if (KarhooUISDKConfigurationProvider.configuration.authenticationMethod() !is AuthenticationMethod.KarhooUser) {
            bookingRequestButton.onLoadingComplete()
        } else {
            bookingCheckoutView.startBooking()
        }
    }

    companion object {
        fun newInstance(arguments: Bundle): BookingCheckoutFragment {
            val fragment = BookingCheckoutFragment()

            fragment.arguments = arguments
            return fragment
        }
    }
}
