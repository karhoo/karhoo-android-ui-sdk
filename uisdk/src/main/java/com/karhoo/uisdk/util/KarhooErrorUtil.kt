package com.karhoo.uisdk.util

import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.R

@Suppress("ComplexMethod", "LongMethod")
@StringRes
fun returnErrorStringOrLogoutIfRequired(error: KarhooError): Int {

    return when (error) {
        KarhooError.CouldNotReadAuthorisationToken,
        KarhooError.CouldNotParseAuthorisationToken,
        KarhooError.CouldNotAuthenticate -> {
            R.string.kh_uisdk_temporary_message_error_codes_unknown
        }

        KarhooError.GeneralRequestError -> R.string.kh_uisdk_K0001
        KarhooError.InvalidRequestPayload -> R.string.kh_uisdk_K0002
        KarhooError.CouldNotReadAuthorisationToken -> R.string.kh_uisdk_K0003
        KarhooError.CouldNotParseAuthorisationToken -> R.string.kh_uisdk_K0004
        KarhooError.AuthenticationRequired -> R.string.kh_uisdk_K0005
        KarhooError.RateLimitExceeded -> R.string.kh_uisdk_K0006
        KarhooError.CircuitBreakerTrigger -> R.string.kh_uisdk_K0007

        KarhooError.Register -> R.string.kh_uisdk_K1001
        KarhooError.RegisterInvalidRequest -> R.string.kh_uisdk_K1003
        KarhooError.RegisterInvalidPhoneNumber -> R.string.kh_uisdk_K1004
        KarhooError.CouldNotGetUserInvalidToken -> R.string.kh_uisdk_K1005
        KarhooError.UserDoesNotExist -> R.string.kh_uisdk_K1006
        KarhooError.RequiredRolesNotAvailable -> R.string.kh_uisdk_K1999

        KarhooError.CouldNotGetAddress -> R.string.kh_uisdk_K2001

        KarhooError.CouldNotGetEstimates -> R.string.kh_uisdk_K3001
        KarhooError.CouldNotGetEstimatesNoAvailability -> R.string.kh_uisdk_K3002
        KarhooError.CouldNotGetEstimatesCouldNotFindSpecifiedQuote -> R.string.kh_uisdk_K3003

        KarhooError.CouldNotBook -> R.string.kh_uisdk_K4001
        KarhooError.CouldNotBookRequirePassengerDetails -> R.string.kh_uisdk_K4002
        KarhooError.CouldNotBookCouldNotFindSpecifiedQuote -> R.string.kh_uisdk_K4003
        KarhooError.CouldNotBookExpiredQuote -> R.string.kh_uisdk_K4004
        KarhooError.CouldNotBookPermissionDenied -> R.string.kh_uisdk_K4005
        KarhooError.CouldNotBookPaymentPreAuthFailed -> R.string.kh_uisdk_K4006
        KarhooError.CouldNotCancel -> R.string.kh_uisdk_K4007
        KarhooError.CouldNotCancelCouldNotFindSpecifiedTrip -> R.string.kh_uisdk_K4008
        KarhooError.CouldNotCancelPermissionDenied -> R.string.kh_uisdk_K4009
        KarhooError.CouldNotCancelAlreadyCancelled -> R.string.kh_uisdk_K4010
        KarhooError.CouldNotGetTrip -> R.string.kh_uisdk_K4011
        KarhooError.CouldNotGetTripCouldNotFindSpecifiedTrip -> R.string.kh_uisdk_K4012
        KarhooError.CouldNotGetTripPermissionDenied -> R.string.kh_uisdk_K4013
        KarhooError.CouldNotBookTripAsAgent -> R.string.kh_uisdk_K4014
        KarhooError.CouldNotBookTripAsTraveller -> R.string.kh_uisdk_K4015
        KarhooError.CouldNotBookTripQuoteNoLongerAvailable -> R.string.kh_uisdk_K4018
        KarhooError.CouldNotBookTripWithSelectedDMS -> R.string.kh_uisdk_K4020
        KarhooError.CouldNotBookTripQuotePriceIncreased -> R.string.kh_uisdk_K4025
        KarhooError.CouldNotGetEstimatesInternalError -> R.string.kh_uisdk_K5001
        KarhooError.CouldNotGetAvailabilityNoneFound -> R.string.kh_uisdk_K5002
        KarhooError.CouldNotGetAvailabilityNoCategories -> R.string.kh_uisdk_K5003
        KarhooError.CouldNotAuthenticate -> R.string.kh_uisdk_K6001
        KarhooError.OriginAndDestinationIdentical -> R.string.kh_uisdk_Q0001
        KarhooError.FailedToGetUserId -> R.string.kh_uisdk_P0001
        KarhooError.CouldNotFindCustomer -> R.string.kh_uisdk_KP001
        KarhooError.CouldNotInitailizeClient -> R.string.kh_uisdk_KP002
        KarhooError.CouldNotFindDefaultPayment -> R.string.kh_uisdk_KP003
        KarhooError.CouldNotFindDefaultCard -> R.string.kh_uisdk_KP004
        KarhooError.FailedToGenerateNonce -> R.string.kh_uisdk_KP005
        KarhooError.FailedToCallMoneyService -> R.string.kh_uisdk_P0002
        KarhooError.LoyaltyNotAllowedToBurnPoints -> R.string.kh_uisdk_loyalty_pre_auth_not_allowed_to_burn
        KarhooError.LoyaltyIncomingPointsExceedBalance -> R.string.kh_uisdk_loyalty_pre_auth_not_enough_points
        KarhooError.LoyaltyEmptyCurrency -> R.string.kh_uisdk_loyalty_unsupported_currency
        KarhooError.LoyaltyUnknownCurrency -> R.string.kh_uisdk_loyalty_unsupported_currency
        KarhooError.LoyaltyInternalError -> R.string.kh_uisdk_temporary_message_error_codes_unknown

        else -> R.string.kh_uisdk_temporary_message_error_codes_unknown
    }

}
