package com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.activity.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.checkoutActivity.views.CheckoutView
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import java.util.*

internal class CheckoutFragment : Fragment(), LoadingButtonView.Actions {
    private lateinit var bookingRequestButton: LoadingButtonView
    private lateinit var checkoutView: CheckoutView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.uisdk_booking_checkout_fragment, container, false)

        bookingRequestButton = view.findViewById(R.id.bookingRequestButton)
        bookingRequestButton.actions = this
        bookingRequestButton.onLoadingComplete()

        checkoutView = view.findViewById(R.id.bookingCheckoutView)

        val bundle = arguments as Bundle
        checkoutView.setListeners(object  : CheckoutFragmentContract.LoadingButtonListener {
            override fun onLoadingComplete() {
                bookingRequestButton.onLoadingComplete()
            }

            override fun showLoading() {
                bookingRequestButton.showLoading()
            }
        }, object : CheckoutFragmentContract.TermsListener {
            override fun showWebViewOnPress(url: String?) {
                if(activity is WebViewActions) {
                    (activity as WebViewActions).showWebView(url)
                }
            }
        })

        checkoutView.showBookingRequest(
                quote = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY)!!,
                bookingStatus = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_STATUS_KEY),
                outboundTripId = bundle.getString(CheckoutActivity.BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY),
                bookingMetadata = bundle.getSerializable(CheckoutActivity.BOOKING_CHECKOUT_METADATA_KEY) as HashMap<String, String>?
        )

        return view;
    }

    override fun onLoadingButtonClick() {
        if (KarhooUISDKConfigurationProvider.configuration.authenticationMethod() !is AuthenticationMethod.KarhooUser) {
            bookingRequestButton.onLoadingComplete()
        } else {
            checkoutView.startBooking()
        }
    }

    companion object {
        fun newInstance(arguments: Bundle): CheckoutFragment {
            val fragment = CheckoutFragment()

            fragment.arguments = arguments
            return fragment
        }
    }
}
