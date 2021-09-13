package com.karhoo.uisdk.base.view.countrycodes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.heetch.countrypicker.Country
import com.heetch.countrypicker.Utils
import com.karhoo.uisdk.R
import java.util.Locale

internal class CountryListAdapter(
        private val mContext: Context,
        countries: ArrayList<Country>,
        selectedCountryCode: String,
        onCountryPicked: (country: Country) -> Unit
                                 ) : RecyclerView.Adapter<CountryListAdapter.ViewHolder>() {
    private val inflater: LayoutInflater
    private val countryList: List<Country>
    private var selectedCountryCode: String
    private var onCountryPicked: (country: Country) -> Unit

    init {
        this.countryList = countries
        this.onCountryPicked = onCountryPicked
        this.selectedCountryCode = selectedCountryCode
        this.inflater =
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int): ViewHolder {
        return ViewHolder(inflater.inflate(R.layout.uisdk_country_picker_item, parent, false))
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val country = countryList.get(position)

        vh.selectableIcon?.setBackgroundResource(getCheckboxDrawable(country))

        vh.countryItemLayout?.setOnClickListener {
            selectedCountryCode = country.isoCode.toString()

            vh.selectableIcon?.setBackgroundResource(getCheckboxDrawable(country))

            onCountryPicked.invoke(country)

            notifyDataSetChanged()
        }

        val text = Locale(mContext.resources.configuration.locale.language,
                country.isoCode).displayCountry + " (+" + country.dialingCode.toString() + ")"

        vh.countryItemTitle?.text = text

        val drawableName: String =
                country.isoCode?.toLowerCase(Locale.ENGLISH).toString() + "_flag"

        vh.countryItemFlag?.setBackgroundResource(Utils.getMipmapResId(mContext, drawableName))
    }

    override fun getItemCount(): Int {
        return countryList.size
    }

    private fun getCheckboxDrawable(country: Country): Int {
        return if (selectedCountryCode.equals(country.isoCode, ignoreCase = true))
            R.drawable.kh_ic_check_selected else R.drawable.kh_ic_check_unselected
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var selectableIcon: ImageView? = null
        var countryItemLayout: CardView? = null
        var countryItemTitle: TextView? = null
        var countryItemFlag: ImageView? = null

        init {
            selectableIcon = v.findViewById(R.id.selectableIcon)
            countryItemLayout = v.findViewById(R.id.countryItemLayout)
            countryItemTitle = v.findViewById(R.id.countryItemTitle)
            countryItemFlag = v.findViewById(R.id.countryItemFlag)
        }
    }
}
