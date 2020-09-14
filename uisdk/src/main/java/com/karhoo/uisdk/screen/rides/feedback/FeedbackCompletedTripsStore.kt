package com.karhoo.uisdk.screen.rides.feedback

import android.content.Context
import android.text.TextUtils

class FeedbackCompletedTripsStore(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun addTrip(tripId: String) {
        val list = getList()
        list.add(tripId)
        saveList(list)
    }

    fun contains(tripId: String): Boolean {
        return getList().contains(tripId)
    }

    private fun getList(): MutableList<String> {
        val listAsString = sharedPreferences.getString(LIST_KEY, "")
        val array = TextUtils.split(listAsString, SEPARATOR)
        return array.asList().toMutableList()
    }

    private fun saveList(list: MutableList<String>) {
        sharedPreferences.edit()
                .putString(LIST_KEY, list.toTypedArray().joinToString(separator = SEPARATOR))
                .apply()
    }

    fun clear() {
        saveList(emptyList<String>().toMutableList())
    }

    companion object {
        const val PREF_NAME = "FeedbackCompletedTripsStore"
        const val LIST_KEY = "listOfTripIds"
        const val SEPARATOR = "‚‗‚"
    }

}
