package com.karhoo.uisdk.screen.booking.checkout.component.fragment

import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_CANCELLED
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_ERROR_DATA
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_EXPIRED
import com.karhoo.uisdk.screen.booking.checkout.CheckoutActivity.Companion.BOOKING_CHECKOUT_TRIP_INFO_KEY
import com.karhoo.uisdk.screen.booking.checkout.comment.CheckoutCommentBottomSheet
import com.karhoo.uisdk.screen.booking.checkout.component.views.CheckoutView
import com.karhoo.uisdk.screen.booking.checkout.payment.WebViewActions
import com.karhoo.uisdk.screen.booking.checkout.traveldetails.CheckoutTravelDetailsBottomSheet
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.screen.booking.domain.quotes.KarhooAvailability
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_DATE
import com.karhoo.uisdk.screen.booking.quotes.QuotesActivity.Companion.QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.HashMap

internal class CheckoutFragment : Fragment() {
    private lateinit var checkoutActionButton: LoadingButtonView
    private lateinit var passengerActionButton: LoadingButtonView
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
        passengerActionButton = view.findViewById(R.id.passengerActionButton)
        checkoutActionButton.onLoadingComplete()

        setupCheckoutView(view)

        val bundle = arguments as Bundle
        showBookingRequest(bundle)

        val validityTimestamp = bundle.getLong(CheckoutActivity.BOOKING_CHECKOUT_VALIDITY_KEY)

        if (validityTimestamp > 0) {
            val milisUntilInvalid = presenter.getValidMilisPeriod(validityTimestamp)

            expirationJob = GlobalScope.launch {
                delay(milisUntilInvalid)

                activity?.runOnUiThread {
                    if (isAdded) {
                        val config = KarhooAlertDialogConfig(
                            titleResId = R.string.kh_uisdk_offer_expired,
                            messageResId = R.string.kh_uisdk_offer_expired_text,
                            positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_ok) { _, _ ->
                                this@CheckoutFragment.activity?.setResult(BOOKING_CHECKOUT_EXPIRED)
                                this@CheckoutFragment.activity?.finish()
                            })

                        context?.let { KarhooAlertDialogHelper(it).showAlertDialog(config) }
                    }
                }
            }

        }

        passengerActionButton.setText(getString(R.string.kh_uisdk_save))
        passengerActionButton.actions = object : LoadingButtonView.Actions {
            override fun onLoadingButtonClick() {
                if (checkoutView.isPassengerDetailsViewVisible()) {
                    if (checkoutView.arePassengerDetailsValid()) {
                        checkoutView.clickedPassengerSaveButton()
                        checkoutView.showPassengerDetailsLayout(false)
                        checkoutActionButton.onLoadingComplete()
                    }
                }
            }
        }

        checkoutActionButton.actions = object : LoadingButtonView.Actions {
            override fun onLoadingButtonClick() {
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

        checkoutActionButton.setText(
            presenter.getBookButtonState(
                arePassengerDetailsValid = checkoutView.arePassengerDetailsValid(),
                isTermsCheckBoxValid = checkoutView.isTermsCheckBoxValid()
            )
                .resId
        )

        return view
    }

    private fun setupCheckoutView(view: View){
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

            override fun checkState() {
                checkoutActionButton.setText(
                    presenter.getBookButtonState(
                        false,
                        checkoutView.arePassengerDetailsValid(),
                        checkoutView.isTermsCheckBoxValid()
                    ).resId
                )
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
                        visible,
                        checkoutView.arePassengerDetailsValid(),
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
                finishCheckout(tripInfo)
            }

            override fun startBookingProcess() {
                checkoutActionButton.performClick()
            }
        })
        checkoutView.commentsListener = { dialog ->
            activity?.supportFragmentManager?.let {
                dialog.show(it, CheckoutCommentBottomSheet.TAG)
            }
        }
        checkoutView.travelDetailsListener = { dialog ->
            activity?.supportFragmentManager?.let {
                dialog.show(it, CheckoutTravelDetailsBottomSheet.TAG)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(
            PASSENGER_DETAILS,
            if (presenter.passengerDetails != null) presenter.passengerDetails else checkoutView.getPassengerDetails()
        )
        outState.putParcelable(SAVED_PAYMENT_INFO, KarhooApi.userStore.savedPaymentInfo)
        checkoutView.onPause()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        savedInstanceState?.let {
            if (it[PASSENGER_DETAILS] != null) {
                val passDetails = it[PASSENGER_DETAILS] as PassengerDetails
                checkoutView.bindPassenger(passDetails)
            }
//            if(it[SAVED_PAYMENT_INFO] != null){
//                val paymentInfo = it[SAVED_PAYMENT_INFO] as SavedPaymentInfo?
//                KarhooApi.userStore.savedPaymentInfo = paymentInfo
//                checkoutView.showUpdatedPaymentDetails(paymentInfo)
//            }//seems an issue from adyen that is not closing their screen and we must handle the tripId to save the paymentInfo, it's useless otherwise
        }
    }

    fun onBackPressed() {
        if (!checkoutView.consumeBackPressed()) {
            val intent = Intent()
            intent.putExtra(
                QUOTES_SELECTED_QUOTE_VALIDITY_TIMESTAMP,
                arguments?.getLong(CheckoutActivity.BOOKING_CHECKOUT_VALIDITY_KEY)
            )
            intent.putExtra(
                QUOTES_SELECTED_DATE,
                arguments?.getParcelable<JourneyDetails>(CheckoutActivity.BOOKING_CHECKOUT_JOURNEY_DETAILS_KEY)?.date
            )
            KarhooAvailability.pauseUpdates(fromBackButton = true)
            activity?.setResult(BOOKING_CHECKOUT_CANCELLED, intent)
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

    private fun finishCheckout(tripInfo: TripInfo?) {
        val intent = Intent()
        val data = Bundle()
        data.putParcelable(BOOKING_CHECKOUT_TRIP_INFO_KEY, tripInfo)

        intent.putExtras(data)

        activity?.setResult(RESULT_OK, intent)
        activity?.finish()

        KarhooAvailability.pauseUpdates()
    }

    private fun showBookingRequest(bundle: Bundle) {
        checkoutView.showBookingRequest(
            quote = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_QUOTE_KEY)!!,
            journeyDetails = bundle.getParcelable(CheckoutActivity.BOOKING_CHECKOUT_JOURNEY_DETAILS_KEY),
            outboundTripId = bundle.getString(CheckoutActivity.BOOKING_CHECKOUT_OUTBOUND_TRIP_ID_KEY),
            bookingMetadata = bundle.getSerializable(
                CheckoutActivity
                    .BOOKING_CHECKOUT_METADATA_KEY
            ) as HashMap<String, String>?,
            passengerDetails = bundle.getParcelable(
                CheckoutActivity
                    .BOOKING_CHECKOUT_PASSENGER_KEY
            ),
            comments = bundle.getString(CheckoutActivity.BOOKING_CHECKOUT_COMMENTS_KEY)
        )
    }

    companion object {
        private const val PASSENGER_DETAILS = "PASSENGER_DETAILS"
        private const val SAVED_PAYMENT_INFO = "SAVED_PAYMENT_INFO"

        fun newInstance(arguments: Bundle): CheckoutFragment {
            val fragment = CheckoutFragment()

            fragment.arguments = arguments
            return fragment
        }
    }
}
