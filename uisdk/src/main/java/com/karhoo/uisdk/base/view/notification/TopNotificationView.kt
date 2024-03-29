package com.karhoo.uisdk.base.view.notification

import android.content.Context
import android.util.AttributeSet
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.uisdk.R

class TopNotificationView @JvmOverloads constructor(context: Context,
                                                    attrs: AttributeSet? = null,
                                                    @AttrRes defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), TopNotificationMVP.View {

    private val fadeInOutAnimationSet = AnimationUtils.loadAnimation(context, R.anim.uisdk_anim_fade_in_fade_out)
    private val presenter = TopNotificationPresenter(this)

    private lateinit var notificationLabel: TextView
    init {
        inflate(context, R.layout.uisdk_view_top_notification, this)
        notificationLabel = findViewById(R.id.notificationLabel)
    }

    override fun setNotificationText(notification: String) {
        presenter.setNotificationText(notification)
    }

    override fun enableNotificationText(notification: String) {
        this.notificationLabel.text = notification
    }

    override fun animateNotification() {
        notificationLabel.startAnimation(fadeInOutAnimationSet)
    }
}
