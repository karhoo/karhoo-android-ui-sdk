package com.karhoo.uisdk.screen.booking.checkout.payment

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.network.request.PassengerDetails
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.cardLogoImage
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.cardNumberText
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.changeCardLabel
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.changeCardProgressBar
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.paymentLayout

class BookingPaymentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
                                                  ) : LinearLayout(context, attrs, defStyleAttr),
                                                      BookingPaymentContract.View,
                                                      BookingPaymentContract.Widget,
                                                      PaymentDropInContract.Actions {

    private var presenter: BookingPaymentContract.Presenter? = BookingPaymentPresenter(this)

    private var addCardIcon: Int = R.drawable.uisdk_ic_plus

    var paymentActions: BookingPaymentContract.PaymentActions? = null
    var cardActions: BookingPaymentContract.PaymentViewActions? = null
    private var dropInView: PaymentDropInContract.View? = null

    private var hasValidPayment = false

    init {
        inflate(context, R.layout.uisdk_view_booking_checkout_payment, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
        presenter?.getPaymentProvider()
        if (!isInEditMode) {
            this.setOnClickListener {
                changeCard()
            }
        }
    }

    override fun setPassengerDetails(passengerDetails: PassengerDetails?) {
        dropInView?.setPassenger(passengerDetails)
    }

    override fun bindDropInView() {
        presenter?.createPaymentView(this)
        bindPaymentDetails(KarhooApi.userStore.savedPaymentInfo)
    }

    override fun setPaymentView(view: PaymentDropInContract.View?) {
        dropInView = view
    }

    override fun setViewVisibility(visibility: Int) {
        cardActions?.handleViewVisibility(visibility)
    }

    override fun setPaymentViewVisibility() {
        presenter?.getPaymentViewVisibility()
    }

    private fun getCustomisationParameters(
        context: Context,
        attr: AttributeSet?,
        defStyleAttr: Int
                                          ) {
        val typedArray = context.obtainStyledAttributes(
            attr, R.styleable.BookingPaymentView,
            defStyleAttr, R.style.KhPaymentView
                                                       )
        addCardIcon = typedArray.getResourceId(
            R.styleable.BookingPaymentView_addCardIcon, R
                .drawable
                .uisdk_ic_plus
                                              )
    }

    private fun changeCard() {
        changeCardProgressBar.visibility = VISIBLE
        cardActions?.handleChangeCard()
    }

    override fun refresh() {
        editCardButtonVisibility(View.VISIBLE)
        changeCardProgressBar.visibility = GONE
    }

    override fun initialisePaymentFlow(quote: Quote?) {
        dropInView?.initialisePaymentFlow(quote)
    }

    override fun initialiseGuestPayment(quote: Quote?) {
        dropInView?.initialiseGuestPayment(quote)
    }

    private fun bindViews(cardType: CardType?, number: String) {
        cardNumberText.text = if (isGuest()) number else "•••• $number"
        setCardType(cardType)
    }

    private fun editCardButtonVisibility(visibility: Int) {
        KarhooApi.userStore.savedPaymentInfo?.let {
            changeCardLabel.visibility = visibility
        } ?: run {
            changeCardLabel.visibility = GONE
        }
    }

    private fun setCardType(cardType: CardType?) {
        when (cardType) {
            CardType.VISA -> cardLogoImage.background = ContextCompat.getDrawable(
                context, R.drawable
                    .uidsk_ic_card_visa
                                                                                 )
            CardType.MASTERCARD -> cardLogoImage.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_mastercard)
            CardType.AMEX -> cardLogoImage.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_amex)
            else -> cardLogoImage.background =
                ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_blank)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        dropInView?.onActivityResult(requestCode, resultCode, data)
    }

    override fun showError(error: Int, karhooError: KarhooError?) {
        paymentActions?.showPaymentFailureDialog(null, karhooError)
    }

    override fun showPaymentDialog(karhooError: KarhooError?) {
        paymentActions?.showPaymentFailureDialog(null, karhooError)
    }

    override fun hasValidPaymentType(): Boolean = hasValidPayment

    override fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        if (savedPaymentInfo != null && savedPaymentInfo.lastFour.isNotEmpty()) {
            hasValidPayment = true
            bindViews(savedPaymentInfo.cardType, savedPaymentInfo.lastFour)
            changeCardProgressBar.visibility = INVISIBLE
            editCardButtonVisibility(View.VISIBLE)
            changeCardLabel.visibility = VISIBLE
            changeCardLabel.text =
                resources.getString(R.string.kh_uisdk_booking_checkout_edit_passenger) //TODO fixme
            setCardType(savedPaymentInfo.cardType)
            paymentLayout.setBackgroundResource(
                R.drawable
                    .uisdk_border_background
                                               )
        } else {
            hasValidPayment = false
            cardNumberText.text =
                resources.getString(R.string.kh_uisdk_booking_checkout_add_payment_method_title)
            changeCardLabel.text =
                resources.getString(R.string.kh_uisdk_booking_checkout_add_payment_method)
            changeCardLabel.visibility = VISIBLE
            cardLogoImage.background = ContextCompat.getDrawable(context, addCardIcon)
            paymentLayout.setBackgroundResource(
                R.drawable
                    .uisdk_dotted_background
                                               )
        }
        paymentActions?.handlePaymentDetailsUpdate()
    }

    override fun showPaymentUI(sdkToken: String, paymentData: String?, quote: Quote?) {
        dropInView?.showPaymentDropInUI(
            context = context, sdkToken = sdkToken, paymentData =
            paymentData, quote = quote
                                       )
    }

    override fun showPaymentFailureDialog(error: KarhooError?) {
        refresh()
        paymentActions?.showPaymentFailureDialog(null, error)
    }

    override fun updatePaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        bindPaymentDetails(savedPaymentInfo)
    }

    override fun handlePaymentDetailsUpdate() {
        paymentActions?.handlePaymentDetailsUpdate()
    }

    override fun updatePaymentViewVisbility(visibility: Int) {
        paymentLayout.visibility = visibility
    }

    override fun initialiseChangeCard(quote: Quote?) {
        dropInView?.initialiseChangeCard(quote)
    }

    override fun threeDSecureNonce(threeDSNonce: String, tripId: String?) {
        paymentActions?.threeDSecureNonce(threeDSNonce, tripId)
    }

    override fun threeDSecureNonce(sdkToken: String, nonce: String, amount: String) {
        dropInView?.handleThreeDSecure(context, sdkToken, nonce, amount)
    }
}
