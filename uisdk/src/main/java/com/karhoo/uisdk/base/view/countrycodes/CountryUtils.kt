package com.karhoo.uisdk.base.view.countrycodes

import android.content.Context
import android.telephony.TelephonyManager
import android.text.TextUtils
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.heetch.countrypicker.Country
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.util.Locale
import java.util.Scanner

object CountryUtils {
    fun getCountriesJSON(context: Context?, resourceName: String): JSONObject? {
        var json: JSONObject? = null
        val resourceId = context?.resources?.getIdentifier(
                resourceName, "raw", context.applicationContext.packageName
                                                          )
        if (resourceId == 0) {
            return json
        }
        val stream = resourceId?.let { context.resources?.openRawResource(it) }

        try {
            json = JSONObject(
                    convertStreamToString(
                            stream
                                         )
                             )
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return json
    }

    private fun convertStreamToString(stream: InputStream?): String {
        val s = Scanner(stream).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }

    fun parseCountries(jsonCountries: JSONObject?): ArrayList<Country>? {
        val countries: ArrayList<Country> = ArrayList()
        val iterator = jsonCountries?.keys()

        if (iterator != null) {
            while (iterator.hasNext()) {
                val key = iterator.next()
                try {
                    val value = jsonCountries.get(key) as String
                    countries.add(Country(key, value))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
        return countries
    }

    fun getDefaultCountryCode(context: Context): String {
        var countryCode: String
        val tm =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        countryCode = tm.simCountryIso.toUpperCase(Locale.getDefault())

        if (TextUtils.isEmpty(countryCode)) {
            countryCode =
                    context.resources?.configuration?.locale?.country.toString()
        }

        return countryCode
    }

    fun getDefaultCountryDialingCode(countryCode: String): String {
        return PhoneNumberUtil.getInstance()
                .getCountryCodeForRegion(countryCode.toUpperCase(Locale.getDefault()))
                .toString()
    }
}
