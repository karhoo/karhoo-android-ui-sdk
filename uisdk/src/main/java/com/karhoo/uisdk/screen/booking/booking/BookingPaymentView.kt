package com.karhoo.uisdk.screen.booking.booking

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.karhoo.sdk.api.datastore.user.SavedPaymentInfo
import com.karhoo.sdk.api.model.CardType
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
    : LinearLayout(context, attrs, defStyleAttr), BookingPaymentMVP.View {

    private lateinit var presenter: BookingPaymentMVP.Presenter

    private var addCardIcon: Int = R.drawable.uisdk_ic_plus
    private var addPaymentBackground: Int = R.drawable.uisdk_background_light_grey_dashed_rounded
    private var changePaymentBackground: Int = R.drawable.uisdk_background_white_rounded
    private var lineTextStyle: Int = R.style.Text_Action
    private var linkTextStyle: Int = R.style.Text_Action_Primary

    var actions: BookingPaymentMVP.Actions? = null

    init {
        inflate(context, R.layout.uisdk_view_booking_payment, this)
        getCustomisationParameters(context, attrs, defStyleAttr)
        if (!isInEditMode) {
            presenter = BookingPaymentPresenter(view = this)
            this.setOnClickListener {
                changeCard()
            }
        }
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
        cardDetailsVisibility(View.GONE)
        cardNumberText.isEnabled = false
        changeCardLabel.visibility = View.GONE
        changeCardProgressBar.visibility = View.VISIBLE
        presenter.changeCard()
    }

    override fun refresh() {
        cardDetailsVisibility(View.VISIBLE)
        changeCardProgressBar.visibility = View.GONE
    }

    private fun bindViews(cardType: CardType?, number: String) {
        if (cardType != CardType.NOT_SET) {
            cardNumberText.text = if (isGuest()) number else "•••• $number"
        }
        setCardType(cardType)
    }

    private fun cardDetailsVisibility(visibility: Int) {
        cardLogoImage.visibility = visibility
        cardNumberText.visibility = visibility
    }

    private fun setCardType(cardType: CardType?) {
        when (cardType) {
            CardType.VISA -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable
                    .uidsk_ic_card_visa)
            CardType.MASTERCARD -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_mastercard)
            CardType.AMEX -> cardLogoImage.background = ContextCompat.getDrawable(context, R.drawable.uisdk_ic_card_amex)
        }
        visibility = View.VISIBLE
    }

    override fun showError(error: Int) {
        actions?.showErrorDialog(error)
    }

    override fun passBackBraintreeSDKNonce(braintreeSDKNonce: String) {
        presenter.passBackBraintreeSDKNonce(braintreeSDKNonce)
    }

    override fun bindCardDetails(savedPaymentInfo: SavedPaymentInfo?) {
        savedPaymentInfo?.let {
            apply {
                bindViews(it.cardType, it.lastFour)
                changeCardProgressBar.visibility = View.INVISIBLE
                cardDetailsVisibility(View.VISIBLE)
                changeCardLabel.visibility = View.VISIBLE
                paymentLayout.background = ContextCompat.getDrawable(context, changePaymentBackground)
                setCardType(it.cardType)
            }
        } ?: run {
            cardNumberText.text = resources.getString(R.string.add_payment)
            changeCardLabel.visibility = View.GONE
            cardLogoImage.background = ContextCompat.getDrawable(context, addCardIcon)
            paymentLayout.background = ContextCompat.getDrawable(context, addPaymentBackground)
        }
    }

    override fun showPaymentUI(braintreeSDKToken: String) {
        actions?.showPaymentUI(braintreeSDKToken)
    }

}
