package com.karhoo.uisdk.screen.booking.booking.bookingrequest

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.TransitionDrawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.PoiType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.QuoteVehicle
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.KarhooUISDK
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.booking.BookingCodes
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogAction
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogConfig
import com.karhoo.uisdk.base.dialog.KarhooAlertDialogHelper
import com.karhoo.uisdk.base.listener.SimpleAnimationListener
import com.karhoo.uisdk.base.view.LoadingButtonView
import com.karhoo.uisdk.screen.booking.BookingActivity
import com.karhoo.uisdk.screen.booking.booking.passengerdetails.PassengerDetailsMVP
import com.karhoo.uisdk.screen.booking.booking.payment.BookingPaymentMVP
import com.karhoo.uisdk.screen.booking.booking.prebookconfirmation.PrebookConfirmationView
import com.karhoo.uisdk.screen.booking.domain.address.BookingStatusStateViewModel
import com.karhoo.uisdk.screen.booking.domain.bookingrequest.BookingRequestStateViewModel
import com.karhoo.uisdk.screen.rides.RidesActivity
import com.karhoo.uisdk.screen.rides.detail.RideDetailActivity
import com.karhoo.uisdk.service.preference.KarhooPreferenceStore
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.ViewsConstants.BOOKING_MAP_PREBOOK_CONF_DIALOG_WIDTH_HEIGHT_FACTOR
import com.karhoo.uisdk.util.extension.hideSoftKeyboard
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestButton
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestCommentsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestFlightDetailsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestLayout
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestPassengerDetailsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestPaymentDetailsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestPriceWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestQuotesWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.bookingRequestTermsWidget
import kotlinx.android.synthetic.main.uisdk_booking_request.view.cancelButton
import kotlinx.android.synthetic.main.uisdk_booking_request.view.passengerDetailsHeading
import kotlinx.android.synthetic.main.uisdk_view_booking_button.view.bookingButtonLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_button.view.bookingRequestLabel
import org.joda.time.DateTime
import java.util.Currency

@Suppress("TooManyFunctions")
class BookingRequestView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), BookingRequestMVP.Actions,
      BookingRequestMVP.View, BookingPaymentMVP.PaymentViewActions, BookingPaymentMVP.PaymentActions,
      BookingRequestViewContract.BookingRequestWidget, PassengerDetailsMVP.Actions, LoadingButtonView.Actions, LifecycleObserver {

    private val containerAnimateIn: Animation = AnimationUtils.loadAnimation(context, R.anim.uisdk_slide_in_bottom)
    private val containerAnimateOut: Animation = AnimationUtils.loadAnimation(context, R.anim.uisdk_slide_out_bottom)
    private var backgroundFade: TransitionDrawable? = null
    private var isGuest: Boolean = false

    private var holdOpenForPaymentFlow = false

    private var presenter: BookingRequestMVP.Presenter = BookingRequestPresenter(this,
                                                                                 KarhooUISDK.analytics,
                                                                                 KarhooPreferenceStore.getInstance(context.applicationContext),
                                                                                 KarhooApi.tripService,
                                                                                 KarhooApi.userStore)

    private val bookingComments: String
        get() = bookingRequestCommentsWidget.getBookingOptionalInfo()

    init {
        View.inflate(context, R.layout.uisdk_booking_request, this)
        isGuest = isGuest()
        attachListeners()
        bookingRequestFlightDetailsWidget.setHintText(context.getString(R.string.kh_uisdk_add_flight_details))
        presenter.setBookingFields(bookingRequestPassengerDetailsWidget.allFieldsValid())
        TextViewCompat.setTextAppearance(bookingRequestLabel, R.style.ButtonText)
    }

    override fun showGuestBookingFields(details: PassengerDetails) {
        val constraintSet = ConstraintSet()
        constraintSet.constrainHeight(R.id.bookingRequestScrollView, 0)
        constraintSet.clone(bookingRequestLayout)
        constraintSet.connect(R.id.bookingRequestScrollView, ConstraintSet.TOP, ConstraintSet.PARENT_ID,
                              ConstraintSet.TOP, 0)
        constraintSet.applyTo(bookingRequestLayout)

        bookingRequestPassengerDetailsWidget.visibility = VISIBLE
        bookingRequestCommentsWidget.visibility = VISIBLE
        passengerDetailsHeading.visibility = VISIBLE
        bookingRequestPassengerDetailsWidget.setPassengerDetails(details)
    }

    override fun updateBookingButtonForGuest() {
        bookingRequestLabel.text = resources.getString(R.string.kh_uisdk_checkout_as_guest)
    }

    override fun showAuthenticatedUserBookingFields() {
        bookingRequestPassengerDetailsWidget.visibility = GONE
        bookingRequestCommentsWidget.visibility = GONE
        passengerDetailsHeading.visibility = GONE

        val constraintSet = ConstraintSet()
        constraintSet.clone(bookingRequestLayout)
        constraintSet.constrainHeight(R.id.bookingRequestScrollView, ConstraintSet.WRAP_CONTENT)
        constraintSet.applyTo(bookingRequestLayout)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        if (holdOpenForPaymentFlow) {
            resetBookingButton()
            holdOpenForPaymentFlow = false
        } else {
            presenter.hideBookingRequest()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        isGuest = isGuest()
        presenter.setBookingEnablement(bookingRequestPassengerDetailsWidget.allFieldsValid())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        disableBooking()
        presenter.clearData()
    }

    private fun attachListeners() {
        bookingRequestQuotesWidget.setOnClickListener {}
        cancelButton.setOnClickListener {
            disableBooking()
            presenter.clearData()
            hideSoftKeyboard()
            hideWindow()
        }

        containerAnimateOut.setAnimationListener(object : SimpleAnimationListener() {
            override fun onAnimationEnd(animation: Animation) {
                visibility = View.GONE
            }
        })

        bookingRequestButton.actions = this
        bookingRequestPaymentDetailsWidget.cardActions = this
        bookingRequestPaymentDetailsWidget.paymentActions = this
        bookingRequestPassengerDetailsWidget.actions = this
        bookingRequestTermsWidget.actions = this
    }

    override fun disableBooking() {
        bookingRequestButton.isEnabled = false
        bookingButtonLayout.background = ContextCompat.getDrawable(context, R
                .drawable.uisdk_gradient_approved)
    }

    override fun enableBooking() {
        bookingRequestButton.isEnabled = true
        bookingButtonLayout.background = ContextCompat.getDrawable(context, R
                .drawable.uisdk_gradient_enabled)
    }

    override fun initialiseChangeCard(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseChangeCard(quote = quote)
    }

    override fun showBookingRequest(quote: Quote, outboundTripId: String?) {
        bookingRequestButton.onLoadingComplete()
        presenter.setBookingEnablement(bookingRequestPassengerDetailsWidget.allFieldsValid())
        visibility = View.VISIBLE
        presenter.showBookingRequest(quote = quote, outboundTripId = outboundTripId)
    }

    override fun animateIn() {
        backgroundFade?.startTransition(containerAnimateIn.duration.toInt())
        bookingRequestLayout.startAnimation(containerAnimateIn)
    }

    override fun animateOut() {
        bookingRequestLayout.startAnimation(containerAnimateOut)
        backgroundFade?.reverseTransition(containerAnimateOut.duration.toInt())
    }

    override fun bindViewToBookingStatus(lifecycleOwner: LifecycleOwner, bookingStatusStateViewModel:
    BookingStatusStateViewModel) {
        bookingStatusStateViewModel.viewStates().observe(lifecycleOwner, presenter.watchBookingStatus(bookingStatusStateViewModel))
    }

    override fun bindViewToBookingRequest(lifecycleOwner: LifecycleOwner,
                                          bookingRequestStateViewModel: BookingRequestStateViewModel) {
        bookingRequestStateViewModel.viewStates().observe(lifecycleOwner, presenter.watchBookingRequest(bookingRequestStateViewModel))
    }

    override fun bindEta(quote: Quote, card: String) {
        bindQuoteAndTerms(quote)
        bookingRequestPriceWidget.bindETAOnly(quote.vehicle.vehicleQta.highMinutes,
                                              context.getString(R.string.kh_uisdk_estimated_arrival_time),
                                              quote.quoteType)
    }

    override fun bindPrebook(quote: Quote, card: String, date: DateTime) {
        bindQuoteAndTerms(quote)
        val time = DateUtil.getTimeFormat(context, date)
        val currency = Currency.getInstance(quote.price.currencyCode)

        bookingRequestPriceWidget.bindPrebook(quote,
                                              time,
                                              DateUtil.getDateFormat(date),
                                              currency)
    }

    override fun bindPriceAndEta(quote: Quote, card: String) {
        bindQuoteAndTerms(quote)
        val currency = Currency.getInstance(quote.price.currencyCode)

        bookingRequestPriceWidget?.bindViews(quote, context.getString(R.string.kh_uisdk_estimated_arrival_time), currency)
    }

    private fun bindQuoteAndTerms(vehicle: Quote) {
        bookingRequestQuotesWidget.bindViews(
                vehicle.fleet.logoUrl,
                vehicle.fleet.name.orEmpty(),
                vehicle.vehicle.vehicleClass.orEmpty(),
                vehicle.serviceAgreements?.freeCancellation?.minutes
        )
        bookingRequestTermsWidget.bindViews(vehicle)
    }

    private fun hideWindow() {
        if (containerAnimateOut.hasEnded() || !containerAnimateOut.hasStarted()) {
            presenter.onPaymentFailureDialogCancelled()
        }
    }

    override fun onLoadingButtonClick() {
        hideSoftKeyboard()
        presenter.makeBooking()
        cancelButton.isEnabled = false
    }

    override fun onTripBookedSuccessfully(tripInfo: TripInfo) {
        bookingRequestButton.onLoadingComplete()
    }

    override fun onError() {
        bookingRequestButton.onLoadingComplete()
    }

    override fun resetBookingButton() {
        bookingRequestButton.onLoadingComplete()
        cancelButton.isEnabled = true
        bookingRequestLayout.setOnClickListener { hideWindow() }
    }

    override fun enableCancelButton() {
        cancelButton.isEnabled = true
    }

    override fun displayFlightDetailsField(poiType: PoiType?) {
        when (poiType) {
            PoiType.AIRPORT -> {
                bookingRequestFlightDetailsWidget.visibility = View.VISIBLE
            }
            else -> bookingRequestFlightDetailsWidget.visibility = View.GONE
        }
    }

    override fun populateFlightDetailsField(flightNumber: String?) {
        flightNumber?.let { bookingRequestFlightDetailsWidget.setBookingOptionalInfo(it) }
    }

    override fun setCapacity(vehicle: QuoteVehicle) {
        bookingRequestQuotesWidget.setCapacity(
                luggage = vehicle.luggageCapacity,
                people = vehicle.passengerCapacity)
    }

    override fun showPaymentFailureDialog(error: KarhooError?) {
        val config = KarhooAlertDialogConfig(
                titleResId = R.string.kh_uisdk_payment_issue,
                messageResId = R.string.kh_uisdk_payment_issue_message,
                karhooError = error,
                positiveButton = KarhooAlertDialogAction(R.string.kh_uisdk_add_card,
                                                         DialogInterface.OnClickListener { dialog, _ ->
                                                             onPaymentHandlePositive()
                                                             dialog.dismiss()
                                                         }),
                negativeButton = KarhooAlertDialogAction(R.string.kh_uisdk_cancel,
                                                         DialogInterface.OnClickListener { dialog, _ ->
                                                             onPaymentHandleCancelled()
                                                             dialog.dismiss()
                                                         }))
        KarhooAlertDialogHelper(context).showAlertDialog(config)

    }

    override fun showErrorDialog(stringId: Int, karhooError: KarhooError?) {
        resetBookingButton()
        presenter.handleError(stringId, karhooError)
    }

    override fun handleChangeCard() {
        presenter.handleChangeCard()
    }

    override fun handleViewVisibility(visibility: Int) {
        bookingRequestPaymentDetailsWidget.visibility = visibility
    }

    override fun showPaymentUI() {
        holdOpenForPaymentFlow = true
    }

    override fun showPrebookConfirmationDialog(quoteType: QuoteType?, tripInfo: TripInfo) {
        val activity = context as Activity
        val activityWasStartedForResult = activity.callingActivity != null

        if (activityWasStartedForResult) {
            val data = Intent().apply {
                putExtra(BookingCodes.BOOKED_TRIP, tripInfo)
            }
            activity.setResult(Activity.RESULT_OK, data)
            activity.finish()
        } else {
            bookingRequestButton.onLoadingComplete()
            animateOut()

            var prebookConfirmationView = PrebookConfirmationView(context).apply {
                bind(quoteType, tripInfo)
            }
            prebookConfirmationView.actions = this
        }
    }

    override fun finishedBooking() {
        presenter.resetBooking()
    }

    private fun onPaymentHandlePositive() {
        presenter.onPaymentFailureDialogPositive()
    }

    private fun onPaymentHandleCancelled() {
        presenter.onPaymentFailureDialogCancelled()
    }

    override fun hideLoading() {
        cancelButton.isEnabled = true
        bookingRequestButton.onLoadingComplete()
    }

    private fun showLoading() {
        cancelButton.isEnabled = false
        bookingRequestButton.showLoading()
    }

    override fun showUpdatedPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        bookingRequestPaymentDetailsWidget.bindPaymentDetails(savedPaymentInfo)
    }

    override fun threeDSecureNonce(threeDSNonce: String, tripId: String?) {
        showLoading()
        presenter.passBackPaymentIdentifiers(threeDSNonce, tripId,
                                             bookingRequestPassengerDetailsWidget.getPassengerDetails(), bookingComments)
    }

    override fun initialisePaymentProvider(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        bookingRequestPaymentDetailsWidget.initialiseGuestPayment(quote)
    }

    override fun handlePaymentDetailsUpdate() {
        presenter.setBookingEnablement(bookingRequestPassengerDetailsWidget.allFieldsValid())
    }

    override fun showPaymentDialog(error: KarhooError?) {
        showPaymentFailureDialog(error)
    }

    override fun setPassengerDetailsValidity(isValid: Boolean) {
        presenter.setBookingEnablement(isValid)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        bookingRequestPaymentDetailsWidget.onActivityResult(requestCode, resultCode, data)
    }

    override fun showWebView(url: String?) {
        presenter.onTermsAndConditionsRequested(url)
    }
}
