package com.karhoo.uisdk.util

import android.content.res.Resources
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.karhoo.uisdk.R

data class VehicleTags(val tag: String) {
    fun getTagIcon(resources: Resources): Drawable? {
        return when (tag.toLowerCase()) {
            "executive" -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.kh_uisdk_ic_tag_executive,
                    null
                                                      )
            "wheelchair" -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.kh_uisdk_ic_tag_wheelchair,
                    null
                                                       )
            "electric" -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.kh_uisdk_ic_tag_electric,
                    null
                                                     )
            "childseat" -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.kh_uisdk_ic_tag_child_seat,
                    null
                                                      )
            "taxi" -> ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.kh_uisdk_ic_tag_cab,
                    null
                                                 )
            "hybrid" ->
                ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.kh_uisdk_ic_tag_hybrid,
                        null
                                           )
            else ->
                ResourcesCompat.getDrawable(
                        resources,
                        R.drawable.kh_uisdk_ic_tag_other_vehicle,
                        null
                                           )
        }
    }
}
