package com.karhoo.uisdk.screen.booking.checkout.basefare

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import com.karhoo.uisdk.R

class BaseFareView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

            private lateinit var tollTipIcon: ImageView

    init {
        inflate(context, R.layout.uisdk_alert_faretype_base, this)
        tollTipIcon = findViewById(R.id.tollTipIcon)
        tollTipIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.uisdk_toll_tip_anim))
    }
}
