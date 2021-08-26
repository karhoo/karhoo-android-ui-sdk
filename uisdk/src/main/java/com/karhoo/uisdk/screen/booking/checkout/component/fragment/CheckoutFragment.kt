package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karhoo.sdk.api.model.AuthenticationMethod
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutView
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import java.util.HashMap

internal class CheckoutFragment : Fragment() {
    private lateinit var checkoutActionButton: LoadingButtonView
    private lateinit var checkoutView: CheckoutView
    private lateinit var presenter: CheckoutPresenter
    private var isShowingPassengerDetails: Boolean = false

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.uisdk_booking_checkout_fragment, container, false)

        presenter = CheckoutPresenter()

        checkoutActionButton = view.findViewById(R.id.checkoutActionButton)
        checkoutActionButton.onLoadingComplete()

        checkoutView = view.findViewById(R.id.bookingCheckoutView)

        val bundle = arguments as Bundle
        checkoutView.setListeners(object : CheckoutFragmentContract.LoadingButtonListener {
            override fun onLoadingComplete() {
                checkoutActionButton.onLoadingComplete()
            }

            override fun showLoading() {
                checkoutActionButton.showLoading()
            }

            override fun enableButton(enable: Boolean) {
                checkoutActionButton.enableButton(enable)
            }

            override fun setState(bookButtonState: BookButtonState) {
                when (bookButtonState) {
                    is BookButtonState.Book ->
                }
                checkoutActionButton.enableButton(enable)
            }
        }, object : CheckoutFragmentContract.WebViewListener {
            override fun showWebViewOnPress(url: String?) {
                if (activity is WebViewActions) {
                    (activity as WebViewActions).showWebView(url)
                }
            }
        }, object : CheckoutFragmentContract.PassengersListener {
            override fun onPassengerPageVisibilityChanged(visible: Boolean) {
                if (visible) {
                    checkoutActionButton.setText(R.string.kh_uisdk_save)
                } else {
                    checkoutActionButton.setText(R.string.kh_uisdk_book_now)
                }

                isShowingPassengerDetails = visible
            }

            override fun onPassengerSelected(passengerDetails: PassengerDetails?) {
                presenter.savePassenger(passengerDetails)
            }
        })

        checkoutView.showBookingRequest(quote = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY)!!,
                                        bookingStatus = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_STATUS_KEY),
                                        outboundTripId = bundle.getString(CheckoutActivity.BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY),
                                        bookingMetadata = bundle.getSerializable(CheckoutActivity.BOOKING_CHECKOUT_METADATA_KEY) as HashMap<String, String>?)

        checkoutActionButton.actions = object : LoadingButtonView.Actions {
            override fun onLoadingButtonClick() {
                if (isShowingPassengerDetails) {
                    if (checkoutView.arePassengerDetailsValid()) {
                        checkoutView.clickedPassengerSaveButton()
                        checkoutView.showPassengerDetails(false)
                        checkoutActionButton.onLoadingComplete()
                    }
                } else {
                    if (KarhooUISDKConfigurationProvider.configuration.authenticationMethod() !is AuthenticationMethod.KarhooUser) {
                        checkoutActionButton.onLoadingComplete()
                    } else {
                        checkoutView.startBooking()
                    }
                }
            }
        }

        return view
    }

    fun onBackPressed() {
        if(isShowingPassengerDetails) {
            checkoutView.showPassengerDetails(false)
        } else {
            activity?.finish()
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
