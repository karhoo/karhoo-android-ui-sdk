package com.karhoo.uisdk.util

import android.content.Context
import android.widget.ImageView
import com.karhoo.uisdk.util.extension.convertDpToPixels
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

object PicassoLoader {
    fun loadImage(context: Context,
                  target: ImageView,
                  url: String?,
                  placeHolderId: Int,
                  logoSizeDimenId: Int,
                  logoRadiusIntId: Int,
                  callback: Callback? = null) {
        val logoSize = context.resources.getDimension(logoSizeDimenId).convertDpToPixels()

        val picasso = Picasso.with(context)
        val creator: RequestCreator

        creator = if (!url.isNullOrBlank()) {
            picasso.load(url)
        } else {
            picasso.load(placeHolderId)
        }

        creator.placeholder(placeHolderId)
                .resize(logoSize, logoSize)
                .transform(LogoTransformation(context.resources.getInteger(logoRadiusIntId)))
                .into(target, callback)
    }
}
