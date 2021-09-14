package com.karhoo.uisdk.base.view.countrycodes

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heetch.countrypicker.Country
import com.karhoo.uisdk.R
import java.text.Collator
import java.util.Locale

internal class CountryPickerActivity : AppCompatActivity() {
    private var countries: ArrayList<Country>? = ArrayList()
    private var allCountries: ArrayList<Country>? = ArrayList()
    var callback: ((country: Country, resId: Int) -> Unit)? = null

    private var adapter: CountryListAdapter? = null
    private var listView: RecyclerView? = null
    private var selectedCountryCode: String? = ""
    private var searchView: SearchView? = null
    private var countriesResourceName: String = "countries_dialing_code"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uisdk_activity_country_picker)

        selectedCountryCode = intent.getStringExtra(COUNTRY_CODE_KEY) ?: ""

        listView = findViewById(R.id.country_picker_list)
        searchView = findViewById(R.id.search_view_layout)
        allCountries = CountryUtils.parseCountries(
                CountryUtils.getCountriesJSON(this, countriesResourceName))

        sortCountriesAlphabetically(allCountries)

        allCountries?.iterator()?.asSequence()?.let { countries?.addAll(it) }

        adapter = CountryListAdapter(this, countries!!, selectedCountryCode!!) {
            searchView?.setQuery("", false)

            val intent = Intent()
            intent.putExtra(COUNTRY_CODE_KEY, it.isoCode)
            intent.putExtra(COUNTRY_DIALING_CODE_KEY, it.dialingCode)

            setResult(COUNTRY_PICKER_ACTIVITY_RESULT_CODE, intent)
            finish()
        }

        listView?.layoutManager = LinearLayoutManager(this)
        listView?.adapter = adapter

        searchView?.queryHint = "Search"
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                if (searchView?.query.toString().isEmpty()) {
                    countries?.clear()
                    countries?.addAll(allCountries!!)
                } else {
                    countries?.clear()

                    for (country in allCountries!!) {
                        val countryName = Locale(
                                resources.configuration.locale.language,
                                country.isoCode).displayCountry

                        if (countryName.lowercase(Locale.getDefault()).contains(
                                        searchView?.query.toString().lowercase(Locale.getDefault()))
                        ) {
                            countries?.add(country)
                        }
                    }
                }

                adapter?.notifyDataSetChanged()

                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        })
    }

    private fun sortCountriesAlphabetically(countryList: ArrayList<Country>?) {
        countryList?.sortWith { country1: Country, country2: Country ->
            val locale: Locale? = resources.configuration.locale
            val collator = Collator.getInstance(locale)
            collator.strength = Collator.PRIMARY
            collator.compare(
                    Locale(locale?.language, country1.isoCode).displayCountry,
                    Locale(locale?.language, country2.isoCode).displayCountry
                            )
        }
    }

    companion object {
        const val COUNTRY_PICKER_ACTIVITY_CODE = 15
        const val COUNTRY_PICKER_ACTIVITY_RESULT_CODE = 16
        const val COUNTRY_CODE_KEY = "COUNTRY_CODE_KEY"
        const val COUNTRY_DIALING_CODE_KEY = "COUNTRY_DIALING_CODE_KEY"
        const val COUNTRIES_JSON_MAPPINGS = "countries_dialing_code"
    }
}