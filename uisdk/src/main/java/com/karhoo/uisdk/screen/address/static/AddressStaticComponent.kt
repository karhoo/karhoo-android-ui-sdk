package com.karhoo.uisdk.screen.address.static

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.DateUtil.getTimeFormat
import kotlinx.android.synthetic.main.uisdk_address_static_component.view.*
import org.joda.time.DateTime
import java.util.*

class AddressStaticComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), AddressStaticComponentContract.View {


    init {
        inflate(context, R.layout.uisdk_address_static_component, this)
    }

    override fun setup(
        pickup: LocationInfo,
        destination: LocationInfo,
        time: DateTime?,
        type: AddressComponentType
    ) {
        setPickupAddress(pickup.address)
        setDestinationAddress(destination.address)
        setType(type = type, time = time)
    }

    private fun setPickupAddress(pickup: Address) {
        setAddressLines(pickup, pickupAddressTextPrimary, pickupAddressTextSecondary)

        pickupAddressTextPrimary.contentDescription = context.resources.getString(R.string.kh_uisdk_acc_pickup_address) +
        " " + pickupAddressTextPrimary.text + " " + pickupAddressTextSecondary.text
    }

    private fun setDestinationAddress(destination: Address) {
        setAddressLines(destination, destinationAddressTextPrimary, destinationAddressTextSecondary)


        destinationAddressTextPrimary.contentDescription = context.resources.getString(R.string.kh_uisdk_acc_destination_address) +
                " " + destinationAddressTextPrimary.text + " " + destinationAddressTextSecondary.text
    }

    override fun setType(type: AddressComponentType, text: String?, time: DateTime?) {
        when (type) {
            AddressComponentType.NORMAL -> {
                staticAddressComponentTime.visibility = View.INVISIBLE
            }
            AddressComponentType.LIGHT -> {
                staticAddressComponentTime.visibility = View.INVISIBLE
                staticAddressComponentLayout.background = AppCompatResources.getDrawable(
                    context,
                    R.drawable.uisdk_background_light_grey_rounded
                )
            }
            AddressComponentType.WITH_TIME -> {
                staticAddressComponentTime.visibility = View.VISIBLE
                time?.let {
                    staticAddressComponentTime.text = getTimeFormat(context, time)
                }
            }
            AddressComponentType.WITH_TEXT -> {
                staticAddressComponentTime.visibility = View.VISIBLE
                staticAddressComponentTime.text = text
            }
        }
    }

    private fun setAddressLines(address: Address,
                                firstAddressLine: TextView,
                                secondAddressLine: TextView) {
        var secondLine = ""

        firstAddressLine.text = address.displayAddress

        if (address.city.isNotEmpty()) {
            secondLine = secondLine.plus(address.city)
        }
        if (address.postalCode.isNotEmpty()) {
            if(secondLine.isNotEmpty()) {
                secondLine = secondLine.plus(" ")
            }
            secondLine = secondLine.plus(address.postalCode)
        }
        if (address.countryCode.isNotEmpty()) {
            val country = Locale("", address.countryCode).displayCountry

            if (secondLine.isNotEmpty()) {
                secondLine = secondLine.plus(", ")
            }

            secondLine = secondLine.plus(country)
        }

        if(secondLine.isNotEmpty())
            secondAddressLine.text = secondLine
        else{
            secondAddressLine.visibility = View.GONE
        }
    }

    enum class AddressComponentType {
        NORMAL,
        LIGHT,
        WITH_TIME,
        WITH_TEXT
    }
}
