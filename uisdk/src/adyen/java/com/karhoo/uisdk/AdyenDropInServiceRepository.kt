package com.karhoo.uisdk.screen.booking.checkout.payment.adyen

import android.content.Context
import androidx.core.app.JobIntentService

class AdyenDropInServiceRepository(val context: Context) : AdyenDropInServiceMVP.Repository {

    override var tripId: String?
        get() = retrieveTripId()
        set(value) = storeTripId(value)

    override var supplyPartnerId: String?
        get() = retrieveSupplyPartnerId()
        set(value) = storeSupplyPartnerId(value)

    private fun retrieveTripId(): String? {
        return context.getSharedPreferences(TRIP_ID, JobIntentService.MODE_PRIVATE)
                .getString(TRIP_ID, "")
    }

    private fun retrieveSupplyPartnerId(): String? {
        return context.getSharedPreferences(SUPPLY_PARTNER_ID, JobIntentService.MODE_PRIVATE)
                .getString(SUPPLY_PARTNER_ID, "")
    }

    private fun storeTripId(value: String?) {
        value?.let {
            val sharedPref = context.getSharedPreferences(TRIP_ID, JobIntentService.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(TRIP_ID, value)
                commit()
            }
        } ?: run {
            clearTripId()
        }
    }

    private fun storeSupplyPartnerId(value: String?) {
        value?.let {
            val sharedPref = context.getSharedPreferences(SUPPLY_PARTNER_ID, JobIntentService.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(SUPPLY_PARTNER_ID, value)
                commit()
            }
        } ?: run {
            clearSupplyPartnerId()
        }

    }

    override fun clearTripId() {
        context.getSharedPreferences(TRIP_ID, JobIntentService.MODE_PRIVATE)
                .edit()
                .clear().apply()
    }

    private fun clearSupplyPartnerId() {
        context.getSharedPreferences(SUPPLY_PARTNER_ID, JobIntentService.MODE_PRIVATE)
                .edit()
                .clear().apply()
    }

    companion object {
        const val TRIP_ID = "trip_id"
        const val SUPPLY_PARTNER_ID = "supply_partner_id"
    }
}
