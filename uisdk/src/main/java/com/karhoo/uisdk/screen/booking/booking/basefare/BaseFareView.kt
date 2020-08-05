package com.karhoo.uisdk.screen.booking.booking.basefare

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_alert_faretype_base.view.tollTipIcon

class BaseFareView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        inflate(context, R.layout.uisdk_alert_faretype_base, this)
        tollTipIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.uisdk_toll_tip_anim))
    }
}
