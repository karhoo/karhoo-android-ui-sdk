package com.karhoo.uisdk.screen.address.static

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.content.res.AppCompatResources
import com.karhoo.sdk.api.model.TripLocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.DateUtil.getTimeFormat
import com.karhoo.uisdk.util.extension.toSimpleLocationInfo
import kotlinx.android.synthetic.main.uisdk_address_static_component.view.*
import org.joda.time.DateTime

class AddressStaticComponent @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), AddressStaticComponentContract.View {


    init {
        inflate(context, R.layout.uisdk_address_static_component, this)
    }

    override fun setup(
        pickup: TripLocationInfo,
        destination: TripLocationInfo,
        time: DateTime?,
        type: AddressComponentType
    ) {
        setPickupAddress(pickup)
        setDestinationAddress(destination)
        setType(type =  type, time = time)
    }

    private fun setPickupAddress(pickup: TripLocationInfo) {
        pickupAddressTextPrimary.text = pickup.toSimpleLocationInfo().displayAddress
        pickupAddressTextSecondary.text = pickup.toSimpleLocationInfo().displayAddress
    }

    private fun setDestinationAddress(destination: TripLocationInfo) {
        destinationAddressTextPrimary.text = destination.toSimpleLocationInfo().displayAddress
        destinationAddressTextSecondary.text = destination.toSimpleLocationInfo().displayAddress
    }

    override fun setType(type: AddressComponentType, text: String?, time: DateTime?) {
        when (type) {
            AddressComponentType.LIGHT -> {
                staticAddressComponentTime.visibility = View.INVISIBLE
                staticAddressComponentLayout.background = AppCompatResources.getDrawable(context, R.drawable.uisdk_background_grey_rounded)
            }
            AddressComponentType.WITH_TIME -> {
                staticAddressComponentTime.visibility = View.VISIBLE
                time?.let {
                    staticAddressComponentTime.text = getTimeFormat(context, time)
                    fillerView.text = getTimeFormat(context, time)
                }
            }
            AddressComponentType.WITH_TEXT -> {
                staticAddressComponentTime.visibility = View.VISIBLE
                staticAddressComponentTime.text =
                    context.getText(R.string.kh_uisdk_static_address_component_now)
                fillerView.text =
                    context.getText(R.string.kh_uisdk_static_address_component_now)
            }
        }
    }


    enum class AddressComponentType {
        LIGHT,
        WITH_TIME,
        WITH_TEXT
    }
}
