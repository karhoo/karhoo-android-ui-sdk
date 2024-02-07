package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.karhoo.sdk.api.model.Quote
import com.karhoo.sdk.api.model.QuoteSource
import com.karhoo.uisdk.R
import com.karhoo.uisdk.util.extension.toLocalisedString
import com.karhoo.uisdk.util.formatted
import com.karhoo.uisdk.util.intToRangedPrice
import java.util.Currency
import java.util.Locale

@Composable
fun BookingPriceComposable(
    quote: Quote,
) {
    val smallDimension = 8.dp
    val etaType = quote.vehicle.vehicleQta.highMinutes.toString()
    val eta = String.format("%s %s", quote.vehicle.vehicleQta.highMinutes, LocalContext.current.getString(
        R.string.kh_uisdk_min))
    val pickUpType = quote.pickupType.toString()
    val priceType = formatQuoteType(quote, LocalContext.current)
    val price = formatPriceText(quote, Currency.getInstance(Locale.getDefault()))
    val pricingType = quote.quoteType.toLocalisedString(LocalContext.current)

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(smallDimension)
        ) {
            EtaLayout(etaType, eta, pickUpType)
            PriceLayout(priceType, price, pricingType)
        }
//        if (priceInfoVisibility) {
//            PriceInfoLayout(quote.price)
//        }
    }
}

@Composable
fun EtaLayout(etaType: String, eta: String, pickUpType: String) {
    Column(
        modifier = Modifier
//            .weight(1f, true)
//            .align(Alignment.Start)
    ) {
        Text(text = etaType, style = MaterialTheme.typography.body1)
        Text(text = eta, style = MaterialTheme.typography.h6)
        Text(text = pickUpType, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun PriceLayout(priceType: String, price: String, pricingType: String) {
    Column(
        modifier = Modifier
//            .weight(1f, true)
//            .align(Alignment.End)
    ) {
        Text(text = priceType, style = MaterialTheme.typography.body1)
        Text(text = price, style = MaterialTheme.typography.h6)
        Row {
            Image(painter = painterResource(id = R.drawable.kh_ic_error_outline), contentDescription = null)
            Text(text = pricingType, style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
fun PriceInfoLayout(priceInfo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Image(painter = painterResource(id = R.drawable.kh_ic_error_outline), contentDescription = null)
        Text(
            text = priceInfo,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

fun formatPriceText(quote: Quote, currency: Currency): String {
    val quotePrice = when (quote.quoteSource) {
        QuoteSource.FLEET -> {
            currency.formatted(quote.price.highPrice, locale = Locale.getDefault())
        }
        QuoteSource.MARKET -> {
            currency.intToRangedPrice(quote.price.lowPrice, quote.price.highPrice, locale = Locale.getDefault())
        }
    }
    return quotePrice
}

fun formatQuoteType(quote: Quote, context: Context): String {
    // Implement your logic to format the quote type
    return quote.quoteType.toLocalisedString(context)
}

fun formatPickUpType(quote: Quote): String {
    // Implement your logic to format the pick up type
    return ""
}