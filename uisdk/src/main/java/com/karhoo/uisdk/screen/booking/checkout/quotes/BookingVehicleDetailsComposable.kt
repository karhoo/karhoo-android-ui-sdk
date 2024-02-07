package com.karhoo.uisdk.screen.booking.checkout.quotes

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.karhoo.sdk.api.model.Quote
import com.karhoo.uisdk.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

@Composable
fun BookingVehicleDetailsComposable(quote: Quote) {
    val isExpandedSectionShown = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var image by remember { mutableStateOf<ImageBitmap?>(null) }
    var drawable by remember { mutableStateOf<Drawable?>(null) }

    DisposableEffect(quote.fleet.logoUrl) {
        val picasso = Picasso.get()

        val target = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                drawable = placeHolderDrawable
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                drawable = errorDrawable
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                image = bitmap?.asImageBitmap()//?.asImageAsset()
            }
        }

        picasso
            .load(quote.fleet.logoUrl)
            .placeholder(R.drawable.uisdk_ic_quotes_logo_empty,)
            .into(target)

        onDispose {
            image = null
            drawable = null
            picasso.cancelRequest(target)
        }
    }
    Column {
        Text(text = quote.quoteType.name)
        Text(text = quote.fleet.name!!)
        if (image != null) {

            Image(image!!, contentDescription = "Vehicle Fleet Logo")
        }
        Text(text = quote.serviceAgreements.toString())
        Button(onClick = { isExpandedSectionShown.value = !isExpandedSectionShown.value }) {
            Text(text = if (isExpandedSectionShown.value) "Hide Details" else "Show Details")
        }
        if (isExpandedSectionShown.value) {
            Text(text = "Expanded Section")
        }
    }
}