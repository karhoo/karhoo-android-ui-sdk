package com.karhoo.uisdk.util

import android.content.Context
import androidx.annotation.StringRes
import com.karhoo.sdk.api.KarhooError
import com.karhoo.uisdk.R

@Suppress("ComplexMethod")
@StringRes
fun returnErrorStringOrLogoutIfRequired(error: KarhooError): Int {

    return when (error) {
        KarhooError.CouldNotReadAuthorisationToken,
        KarhooError.CouldNotParseAuthorisationToken,
        KarhooError.CouldNotAuthenticate -> {
            R.string.temporary_message_error_codes_unknown
        }

        KarhooError.GeneralRequestError -> R.string.K0001
        KarhooError.InvalidRequestPayload -> R.string.K0002
        KarhooError.CouldNotReadAuthorisationToken -> R.string.K0003
        KarhooError.CouldNotParseAuthorisationToken -> R.string.K0004
        KarhooError.AuthenticationRequired -> R.string.K0005
        KarhooError.RateLimitExceeded -> R.string.K0006
        KarhooError.CircuitBreakerTrigger -> R.string.K0007

        KarhooError.Register -> R.string.K1001
        KarhooError.RegisterInvalidRequest -> R.string.K1003
        KarhooError.RegisterInvalidPhoneNumber -> R.string.K1004
        KarhooError.CouldNotGetUserInvalidToken -> R.string.K1005
        KarhooError.UserDoesNotExist -> R.string.K1006
        KarhooError.RequiredRolesNotAvailable -> R.string.K1999

        KarhooError.CouldNotGetAddress -> R.string.K2001

        KarhooError.CouldNotGetEstimates -> R.string.K3001
        KarhooError.CouldNotGetEstimatesNoAvailability -> R.string.K3002
        KarhooError.CouldNotGetEstimatesCouldNotFindSpecifiedQuote -> R.string.K3003

        KarhooError.CouldNotBook -> R.string.K4001
        KarhooError.CouldNotBookRequirePassengerDetails -> R.string.K4002
        KarhooError.CouldNotBookCouldNotFindSpecifiedQuote -> R.string.K4003
        KarhooError.CouldNotBookExpiredQuote -> R.string.K4004
        KarhooError.CouldNotBookPermissionDenied -> R.string.K4005
        KarhooError.CouldNotBookPaymentPreAuthFailed -> R.string.K4006
        KarhooError.CouldNotCancel -> R.string.K4007
        KarhooError.CouldNotCancelCouldNotFindSpecifiedTrip -> R.string.K4008
        KarhooError.CouldNotCancelPermissionDenied -> R.string.K4009
        KarhooError.CouldNotCancelAlreadyCancelled -> R.string.K4010
        KarhooError.CouldNotGetTrip -> R.string.K4011
        KarhooError.CouldNotGetTripCouldNotFindSpecifiedTrip -> R.string.K4012
        KarhooError.CouldNotGetTripPermissionDenied -> R.string.K4013
        KarhooError.CouldNotBookTripAsAgent -> R.string.K4014
        KarhooError.CouldNotBookTripAsTraveller -> R.string.K4015
        KarhooError.CouldNotBookTripQuoteNoLongerAvailable -> R.string.K4018

        KarhooError.CouldNotGetEstimatesInternalError -> R.string.K5001
        KarhooError.CouldNotGetAvailabilityNoneFound -> R.string.K5002
        KarhooError.CouldNotGetAvailabilityNoCategories -> R.string.K5003

        KarhooError.CouldNotAuthenticate -> R.string.K6001

        KarhooError.OriginAndDestinationIdentical -> R.string.Q0001
        KarhooError.FailedToGetUserId -> R.string.P0001

        KarhooError.CouldNotFindCustomer -> R.string.KP001
        KarhooError.CouldNotInitailizeClient -> R.string.KP002
        KarhooError.CouldNotFindDefaultPayment -> R.string.KP003
        KarhooError.CouldNotFindDefaultCard -> R.string.KP004
        KarhooError.FailedToGenerateNonce -> R.string.KP005
        KarhooError.FailedToCallMoneyService -> R.string.P0002

        else -> R.string.temporary_message_error_codes_unknown
    }

}
