package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_ERROR_DATA
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_TRIP_INFO_KEY
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutView
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.HashMap

internal class CheckoutFragment : Fragment() {
    private lateinit var checkoutActionButton: LoadingButtonView
    private lateinit var checkoutView: CheckoutView
    private lateinit var presenter: CheckoutPresenter
    private var expirationJob: Job? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {
        val view = inflater.inflate(R.layout.uisdk_booking_checkout_fragment, container, false)

        presenter = CheckoutPresenter()

        checkoutActionButton = view.findViewById(R.id.checkoutActionButton)
        checkoutActionButton.onLoadingComplete()

        checkoutView = view.findViewById(R.id.bookingCheckoutView)
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
                checkoutActionButton.setText(bookButtonState.resId)
            }
        }, object : CheckoutFragmentContract.WebViewListener {
            override fun showWebViewOnPress(url: String?) {
                if (activity is WebViewActions) {
                    (activity as WebViewActions).showWebView(url)
                }
            }
        }, object : CheckoutFragmentContract.PassengersListener {
            override fun onPassengerPageVisibilityChanged(visible: Boolean) {
                checkoutActionButton.setText(
                        presenter.getBookButtonState(
                                visible, checkoutView
                                .arePassengerDetailsValid(), checkoutView.isPaymentMethodValid(),
                                isTermsCheckBoxValid = checkoutView.isTermsCheckBoxValid()
                                                    ).resId
                                            )
            }

            override fun onPassengerSelected(passengerDetails: PassengerDetails?) {
                presenter.savePassenger(passengerDetails)
            }
        }, object : CheckoutFragmentContract.BookingListener {
            override fun onBookingFailed(error: KarhooError?) {
                val intent = Intent()
                intent.putExtra(BOOKING_CHECKOUT_ERROR_DATA, error)

                activity?.setResult(RESULT_CANCELED, intent)
                activity?.finish()
            }

            override fun onTripBooked(tripInfo: TripInfo?) {
                val intent = Intent()
                val data = Bundle()
                data.putParcelable(BOOKING_CHECKOUT_TRIP_INFO_KEY, tripInfo)

                intent.putExtras(data)

                activity?.setResult(RESULT_OK, intent)
                activity?.finish()
            }
        })

        val bundle = arguments as Bundle
        checkoutView.showBookingRequest(
                quote = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY)!!,
                journeyDetails = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_STATUS_KEY),
                outboundTripId = bundle.getString(CheckoutActivity.BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY),
                bookingMetadata = bundle.getSerializable(CheckoutActivity
                                                                 .BOOKING_CHECKOUT_METADATA_KEY) as HashMap<String, String>?,
                passengerDetails = bundle.getParcelable(CheckoutActivity
                                                                .BOOKING_CHECKOUT_PASSENGER_KEY),
                comments = bundle.getString(CheckoutActivity.BOOKING_CHECKOUT_COMMENTS_KEY))

        val validityTimestamp = bundle.getLong(CheckoutActivity.BOOKING_CHECKOUT_VALIDITY_KEY)

        if (validityTimestamp > 0) {
            val milisUntilInvalid = presenter.getValidMilisSPeriod(validityTimestamp)

            expirationJob = GlobalScope.launch {
                delay(milisUntilInvalid)

                activity?.runOnUiThread {
                    if (isAdded) {
                        val config = KarhooAlertDialogConfig(
                                titleResId = R.string.kh_uisdk_offer_expired,
                                messageResId = R.string.kh_uisdk_offer_expired_text,
                                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok) { _, _ ->
                                    this@CheckoutFragment.activity?.finish()
                                })

                        context?.let { KarhooAlertDialogHelper(it).showAlertDialog(config) }
                    }
                }
            }

        }

        checkoutActionButton.actions = object : LoadingButtonView.Actions {
            override fun onLoadingButtonClick() {
                if (checkoutView.isPassengerDetailsViewVisible()) {
                    if (checkoutView.arePassengerDetailsValid()) {
                        checkoutView.clickedPassengerSaveButton()
                        checkoutView.showPassengerDetailsLayout(false)
                        checkoutActionButton.onLoadingComplete()
                    }
                } else {
                    if (!checkoutView.arePassengerDetailsValid()) {
                        checkoutView.showPassengerDetailsLayout(true)
                        checkoutActionButton.onLoadingComplete()
                    } else {
                        if (!checkoutView.checkLoyaltyEligiblityAndStartPreAuth()) {
                            //Skip the loyalty flow, start the booking one directly
                            checkoutView.startBooking()
                        }
                    }
                }
            }
        }

        checkoutActionButton.setText(
                presenter.getBookButtonState(
                        arePassengerDetailsValid = checkoutView.arePassengerDetailsValid(),
                        isPaymentValid = checkoutView.isPaymentMethodValid(),
                        isTermsCheckBoxValid = checkoutView.isTermsCheckBoxValid()
                                            )
                        .resId
                                    )

        return view
    }

    fun onBackPressed() {
        if (!checkoutView.consumeBackPressed()) {
            activity?.finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        checkoutView.onStop()

        if (expirationJob?.isActive == true) {
            expirationJob?.cancel()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        checkoutView.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun newInstance(arguments: Bundle): CheckoutFragment {
            val fragment = CheckoutFragment()

            fragment.arguments = arguments
            return fragment
        }
    }
}
