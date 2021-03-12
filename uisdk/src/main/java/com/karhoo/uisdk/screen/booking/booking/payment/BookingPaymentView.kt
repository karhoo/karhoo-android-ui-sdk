package com.karhoo.uisdk.screen.booking.booking.payment

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.karhoo.sdk.api.KarhooApi
import com.karhoo.sdk.api.KarhooError
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.CardType
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.extension.isGuest
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.cardLogoImage
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.cardNumberText
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.changeCardLabel
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.changeCardProgressBar
import kotlinx.android.synthetic.main.uisdk_view_booking_payment.view.paymentLayout

class BookingPaymentView @JvmOverloads constructor(context: Context,
                                                   attrs: AttributeSet? = null,
                                                   defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), BookingPaymentMVP.View,
      BookingPaymentMVP.Widget, PaymentDropInMVP.Actions {

    private var presenter: BookingPaymentMVP.Presenter? = BookingPaymentPresenter(this)

    private var addCardIcon: Int = R.drawable.uisdk_ic_plus
    private var addPaymentBackground: Int = R.drawable.uisdk_background_light_grey_dashed_rounded
    private var changePaymentBackground: Int = R.drawable.uisdk_background_white_rounded
    private var lineTextStyle: Int = R.style.Text_Action
    private var linkTextStyle: Int = R.style.Text_Action_Primary

    var paymentActions: BookingPaymentMVP.PaymentActions? = null
    var cardActions: BookingPaymentMVP.PaymentViewActions? = null
    private var dropInView: PaymentDropInMVP.View? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_payment, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
        presenter?.getPaymentProvider()
        if (!isInEditMode) {
            this.setOnClickListener {
                changeCard()
            }
        }
    }

    override fun bindDropInView() {
        presenter?.createPaymentView(this)
        bindPaymentDetails(KarhooApi.userStore.savedPaymentInfo)
    }

    override fun setPaymentView(view: PaymentDropInMVP.View?) {
        dropInView = view
    }

    override fun setViewVisibility(visibility: Int) {
        cardActions?.handleViewVisibility(visibility)
    }

    override fun setPaymentViewVisibility() {
        presenter?.getPaymentViewVisibility()
    }

    private fun getCustomisationParameters(context: Context, attr: AttributeSet?, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attr, R.styleable.BookingPaymentView,
                                                        defStyleAttr, R.style.KhPaymentView)
        addCardIcon = typedArray.getResourceId(R.styleable.BookingPaymentView_addCardIcon, R
                .drawable
                .uisdk_ic_plus)
        addPaymentBackground = typedArray.getResourceId(R.styleable.BookingPaymentView_addPaymentBackground, R
                .drawable
                .uisdk_background_light_grey_dashed_rounded)
        changePaymentBackground = typedArray.getResourceId(R.styleable.BookingPaymentView_changePaymentBackground, R
                .drawable
                .uisdk_background_white_rounded)
        lineTextStyle = typedArray.getResourceId(R.styleable.BookingPaymentView_lineText, R
                .style
                .Text_Action)
        linkTextStyle = typedArray.getResourceId(R.styleable.BookingPaymentView_actionText, R
                .style
                .Text_Action_Primary)
        TextViewCompat.setTextAppearance(cardNumberText, lineTextStyle)
        TextViewCompat.setTextAppearance(changeCardLabel, linkTextStyle)
    }

    private fun changeCard() {
        cardDetailsVisibility(GONE)
        editCardButtonVisibility(GONE)
        cardNumberText.isEnabled = false
        changeCardProgressBar.visibility = VISIBLE
        cardActions?.handleChangeCard()
    }

    override fun refresh() {
        cardDetailsVisibility(VISIBLE)
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

    private fun cardDetailsVisibility(visibility: Int) {
        cardLogoImage.visibility = visibility
        cardNumberText.visibility = visibility
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
            CardType.VISA -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable
                    .uidsk_ic_card_visa)
            CardType.MASTERCARD -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_mastercard)
            CardType.AMEX -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_amex)
            else -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_blank)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        dropInView?.onActivityResult(requestCode, resultCode, data)
    }

    override fun showError(error: Int, karhooError: KarhooError?) {
        cardActions?.showErrorDialog(error, karhooError)
    }

    override fun showPaymentDialog(karhooError: KarhooError?) {
        paymentActions?.showPaymentDialog(karhooError)
    }

    override fun bindPaymentDetails(savedPaymentInfo: SavedPaymentInfo?) {
        savedPaymentInfo?.let {
            apply {
                bindViews(it.cardType, it.lastFour)
                changeCardProgressBar.visibility = INVISIBLE
                cardDetailsVisibility(VISIBLE)
                editCardButtonVisibility(View.VISIBLE)
                changeCardLabel.visibility = VISIBLE
                paymentLayout.background = ContextCompat.getDrawable(context, changePaymentBackground)
                setCardType(it.cardType)
            }
        } ?: run {
            cardNumberText.text = resources.getString(R.string.add_payment)
            changeCardLabel.visibility = GONE
            cardLogoImage.background = ContextCompat.getDrawable(context, addCardIcon)
            paymentLayout.background = ContextCompat.getDrawable(context, addPaymentBackground)
        }
        paymentActions?.handlePaymentDetailsUpdate()
    }

    override fun showPaymentUI(sdkToken: String, paymentData: String?, quote: Quote?) {
        paymentActions?.showPaymentUI()
        dropInView?.showPaymentDropInUI(context = context, sdkToken = sdkToken, paymentData =
        paymentData, quote = quote)
    }

    override fun showPaymentFailureDialog(error: KarhooError?) {
        refresh()
        paymentActions?.showPaymentFailureDialog(error)
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
