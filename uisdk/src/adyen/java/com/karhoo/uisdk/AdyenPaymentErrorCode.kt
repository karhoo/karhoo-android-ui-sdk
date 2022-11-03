package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import com.karhoo.uisdk.R

enum class AdyenPaymentErrorCode(val reason: Int = -1) {
    EMPTY0,
    EMPTY1,
    Refused(R.string.kh_uisdk_adyen_payment_error_02),
    Referral(R.string.kh_uisdk_adyen_payment_error_03),
    AcquirerError(R.string.kh_uisdk_adyen_payment_error_04),
    BlockedCard(R.string.kh_uisdk_adyen_payment_error_05),
    ExpiredCard(R.string.kh_uisdk_adyen_payment_error_06),
    InvalidAmount(R.string.kh_uisdk_adyen_payment_error_07),
    InvalidCardNumber(R.string.kh_uisdk_adyen_payment_error_08),
    IssuerUnavailable(R.string.kh_uisdk_adyen_payment_error_09),
    NotSupported(R.string.kh_uisdk_adyen_payment_error_10),
    NotAuthenticated3D(R.string.kh_uisdk_adyen_payment_error_11),
    NotEnoughBalance(R.string.kh_uisdk_adyen_payment_error_12),
    EMPTY13,
    AcquirerFraud(R.string.kh_uisdk_adyen_payment_error_14),
    Cancelled(R.string.kh_uisdk_adyen_payment_error_15),
    ShopperCancelled(R.string.kh_uisdk_adyen_payment_error_16),
    InvalidPin(R.string.kh_uisdk_adyen_payment_error_17),
    PinTriesExceeded(R.string.kh_uisdk_adyen_payment_error_18),
    PinValidationNotPossible(R.string.kh_uisdk_adyen_payment_error_19),
    FRAUD(R.string.kh_uisdk_adyen_payment_error_20),
    NotSubmitted(R.string.kh_uisdk_adyen_payment_error_21),
    FRAUDCANCELLED(R.string.kh_uisdk_adyen_payment_error_22),
    TransactionNotPermitted(R.string.kh_uisdk_adyen_payment_error_23),
    CVCDeclined(R.string.kh_uisdk_adyen_payment_error_24),
    RestrictedCard(R.string.kh_uisdk_adyen_payment_error_25),
    RevocationOfAuth(R.string.kh_uisdk_adyen_payment_error_26),
    DeclinedNonGeneric(R.string.kh_uisdk_adyen_payment_error_27),
    WithdrawalAmountExceeded(R.string.kh_uisdk_adyen_payment_error_28),
    WithdrawalCountExceeded(R.string.kh_uisdk_adyen_payment_error_29),
    EMPTY30,
    IssuerSuspectedFraud(R.string.kh_uisdk_adyen_payment_error_31),
    AVSDeclined(R.string.kh_uisdk_adyen_payment_error_32),
    CardRequiresOnlinePin(R.string.kh_uisdk_adyen_payment_error_33),
    NoCheckingAccountAvailableOnCard(R.string.kh_uisdk_adyen_payment_error_34),
    NoSavingsAccountAvailableOnCard(R.string.kh_uisdk_adyen_payment_error_35),
    MobilePinRequired(R.string.kh_uisdk_adyen_payment_error_36),
    ContactlessFallback(R.string.kh_uisdk_adyen_payment_error_37),
    AuthenticationRequired(R.string.kh_uisdk_adyen_payment_error_38),
    RReqNotReceivedFromDS(R.string.kh_uisdk_adyen_payment_error_39),
    CurrentAIDIsInPenaltyBox(R.string.kh_uisdk_adyen_payment_error_40),
    CVMRequiredRestartPayment(R.string.kh_uisdk_adyen_payment_error_41),
    AuthenticationError3DS(R.string.kh_uisdk_adyen_payment_error_42);

    companion object {
        fun getByRefusalCode(code: String): Int {
            return if(values().size >= code.toInt())
                values()[code.toInt()].reason
            else -1
        }
    }
}
