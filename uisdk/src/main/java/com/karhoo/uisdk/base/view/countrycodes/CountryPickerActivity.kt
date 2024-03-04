package com.karhoo.uisdk.base.view.countrycodes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.heetch.countrypicker.Country
import com.karhoo.uisdk.KarhooUISDK
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

    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var searchViewLayout: androidx.appcompat.widget.SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.uisdk_activity_country_picker)

        toolbar = findViewById(R.id.toolbar)
        searchViewLayout = findViewById(R.id.search_view_layout)

        searchViewLayout.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                .setHintTextColor(resources.getColor(R.color.kh_uisdk_text_button))

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

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

        searchView?.queryHint = getString(R.string.kh_uisdk_country_search)
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

    class Builder {
        private val extrasBundle: Bundle = Bundle()

        /**
         * If a country code is set, it will
         */
        fun countryCode(countryCode: String): Builder {
            extrasBundle.putString(COUNTRY_CODE_KEY, countryCode)
            return this
        }

        /**
         * Returns a launchable Intent to the configured booking activity with the given
         * builder parameters in the extras bundle
         */
        fun build(context: Context): Intent = Intent(context, KarhooUISDK.Routing.countryPicker).apply {
            putExtras(extrasBundle)
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
