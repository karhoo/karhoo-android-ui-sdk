import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.karhoo.sdk.api.model.Address
import com.karhoo.sdk.api.model.LocationInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.address.static.AddressStaticComponent
import com.karhoo.uisdk.screen.booking.domain.address.JourneyDetails
import com.karhoo.uisdk.util.DateUtil.getTimeFormat
import com.karhoo.uisdk.util.extension.removeLastOccurrenceOf
import com.karhoo.uisdk.util.extension.removeSubstringWithRegexUsing
import org.joda.time.DateTime
import java.util.Locale

private val addressCountryRegexPattern = ",{1} *[A-Za-z]*$"

@Composable
fun AddressStaticComponentComposable(
    journeyDetails: JourneyDetails,
) {
    val smallDimension = 8.dp
    val iconSize = 24.dp
    val pickup: LocationInfo = journeyDetails.pickup!!
    val destination: LocationInfo = journeyDetails.destination!!
    val time: DateTime? = journeyDetails.date
    val type: AddressStaticComponent.AddressComponentType = AddressStaticComponent.AddressComponentType.NORMAL

    Column(modifier = Modifier.padding(smallDimension).fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.kh_ic_prebook_pickup_icon),
                contentDescription = "Pickup Icon",
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(smallDimension))
            Column {
                Text(text = getFirstAddressText(pickup.address), style = MaterialTheme.typography.h6)
                Text(text = getSecondAddressText(pickup.address, false), style = MaterialTheme.typography.body2)
            }
            Spacer(modifier = Modifier.weight(1f))
            time?.let {
                Text(text = getTimeFormat(LocalContext.current, time), style = MaterialTheme.typography.body2)
            }
        }
        Spacer(modifier = Modifier.height(smallDimension))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = R.drawable.kh_prebook_destination_icon),
                contentDescription = "Dropoff Icon",
                modifier = Modifier.size(iconSize)
            )
            Spacer(modifier = Modifier.width(smallDimension))
            Column {
                Text(text = getFirstAddressText(destination.address), style = MaterialTheme.typography.h6)
                Text(text = getSecondAddressText(destination.address, false), style = MaterialTheme.typography.body2)
            }
        }
    }
}

private fun getFirstAddressText(address: Address): String {
    val result = address.displayAddress
        .removeLastOccurrenceOf(address.city)
        .removeLastOccurrenceOf(address.postalCode)

    return if (result.endsWith(address.countryCode)) {
        result.removeLastOccurrenceOf(", ".plus(address.countryCode))
    } else {
        result.removeSubstringWithRegexUsing(addressCountryRegexPattern)
    }
}

private fun getSecondAddressText(address: Address, showCountry: Boolean): String {
    var secondLine = ""
    if (address.city.isNotEmpty()) {
        secondLine = secondLine.plus(address.city)
    }
    if (address.postalCode.isNotEmpty()) {
        if(secondLine.isNotEmpty()) {
            secondLine = secondLine.plus(" ")
        }
        secondLine = secondLine.plus(address.postalCode)
    }
    if (address.countryCode.isNotEmpty() && showCountry) {
        val country = Locale("", address.countryCode).displayCountry

        if (secondLine.isNotEmpty()) {
            secondLine = secondLine.plus(", ")
        }

        secondLine = secondLine.plus(country)
    }

    return secondLine ?: ""
}