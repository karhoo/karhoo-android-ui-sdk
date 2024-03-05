package com.karhoo.uisdk.screen.trip.bookingstatus.driverphoto

import android.app.SharedElementCallback
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.util.ViewsConstants.DRIVER_PHOTO_ACTIVITY_NAME_ANIM_START_OFFSET
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.lang.Exception

private const val DRIVER_PHOTO_URL = "DriverPhotoActivity.URL"
private const val DRIVER_NAME = "DriverPhotoActivity.DRIVER_NAME"

class DriverPhotoActivity : BaseActivity() {

    override val layout = R.layout.uisdk_activity_driver_photo

    private lateinit var driverPhotoImage: ImageView
    private lateinit var driverNameText: TextView
    private lateinit var layoutRoot: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setAndAnimateDriverNameWhenSharedElementTransitionsComplete()
        loadDriverPhotoBeforeTransition()
        layoutRoot.setOnClickListener { onBackPressed() }
    }

    private fun setAndAnimateDriverNameWhenSharedElementTransitionsComplete() {
        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementEnd(sharedElementNames: MutableList<String>?,
                                            sharedElements: MutableList<View>?,
                                            sharedElementSnapshots: MutableList<View>?) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                driverNameText.text = extras?.getString(DRIVER_NAME, "")
                val anim = AnimationUtils.loadAnimation(this@DriverPhotoActivity, R.anim.uisdk_scale_and_translate).apply {
                    startOffset = DRIVER_PHOTO_ACTIVITY_NAME_ANIM_START_OFFSET
                }
                driverNameText.startAnimation(anim)
            }
        })
    }

    private fun loadDriverPhotoBeforeTransition() {
        val url = extras?.getString(DRIVER_PHOTO_URL, "")

        supportPostponeEnterTransition()
        Picasso.get()
                .load(url)
                .into(driverPhotoImage, object : Callback {
                    override fun onSuccess() {
                        supportStartPostponedEnterTransition()
                    }

                    override fun onError(e: Exception?) {
                        supportStartPostponedEnterTransition()
                    }
                })
    }

    override fun handleExtras() {
        // Do nothing
    }

    override fun initialiseViews() {
        // Do nothing
    }

    override fun initialiseViewListeners() {
        // Do nothing
    }

    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        fun setDriverName(driverName: String): DriverPhotoActivity.Builder {
            extras.putString(DRIVER_NAME, driverName)
            return this
        }

        fun setDriverPhotoUrl(driverPhotoUrl: String): DriverPhotoActivity.Builder {
            extras.putString(DRIVER_PHOTO_URL, driverPhotoUrl)
            return this
        }

        fun build(context: Context): Intent {
            val intent = Intent(context, DriverPhotoActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: DriverPhotoActivity.Builder
                get() = Builder()
        }
    }

}
