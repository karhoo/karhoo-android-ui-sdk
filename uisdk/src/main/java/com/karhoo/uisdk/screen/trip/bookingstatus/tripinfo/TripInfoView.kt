package com.karhoo.uisdk.screen.trip.bookingstatus.tripinfo

import android.animation.LayoutTransition
import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.karhoo.uisdk.KarhooUISDKConfigurationProvider
import com.karhoo.uisdk.R
import com.karhoo.uisdk.screen.trip.bookingstatus.BookingStatusMVP
import com.karhoo.uisdk.screen.trip.bookingstatus.driverphoto.DriverPhotoActivity
import com.karhoo.uisdk.util.LogoTransformation
import com.karhoo.uisdk.util.extension.convertDpToPixels
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.carTypeText
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.contactOptionsWidget
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.detailsArrowIcon
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.driverDetailsLayout
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.driverNameText
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.driverPhotoImage
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.licenceNumberText
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.locateMeButton
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.registrationPlateText
import kotlinx.android.synthetic.main.uisdk_view_trip_info.view.rideOptionsLabel

class TripInfoView @JvmOverloads constructor(context: Context,
                                             attrs: AttributeSet? = null,
                                             defStyleAttr: Int = 0)
    : LinearLayout(context, attrs, defStyleAttr), TripInfoMVP.View, LayoutTransition.TransitionListener {

    private var presenter: TripInfoPresenter = TripInfoPresenter(this)
    internal var actions: TripInfoActions? = null

    init {
        inflate(context, R.layout.uisdk_view_trip_info, this)
        setLocateMeButtonVisibility(context)
        driverDetailsLayout.setOnClickListener { toggleContactOptionsVisibility() }
        contactOptionsWidget.layoutTransition?.addTransitionListener(this@TripInfoView)
        showTripInfo()
    }

    private fun setLocateMeButtonVisibility(context: Context) {
        if (KarhooUISDKConfigurationProvider.isGuest()) {
            locateMeButton.visibility = View.GONE
        }
    }

    override fun startTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {
        driverDetailsLayout.setOnClickListener { }
    }

    override fun endTransition(transition: LayoutTransition?, container: ViewGroup?, view: View?, transitionType: Int) {
        driverDetailsLayout.setOnClickListener { toggleContactOptionsVisibility() }
    }

    private fun toggleContactOptionsVisibility() {
        if (contactOptionsWidget.visibility == VISIBLE) {
            contactOptionsWidget.visibility = GONE
            detailsArrowIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.uisdk_anim_rotate_quater_anticlockwise))
        } else {
            contactOptionsWidget.visibility = VISIBLE
            detailsArrowIcon.startAnimation(AnimationUtils.loadAnimation(context, R.anim.uisdk_anim_rotate_quater_clockwise))
        }
    }

    override fun bindViews(driverName: String, carType: String,
                           numberPlate: String, taxiNumber: String,
                           driverPhotoUrl: String) {
        this.driverNameText.text = driverName
        this.carTypeText.text = carType
        registrationPlateText.text = numberPlate
        licenceNumberText.text = taxiNumber
        loadDriverPhoto(driverPhotoUrl, driverName)
    }

    override fun showDriverDetails() {
        driverDetailsLayout.visibility = View.VISIBLE
    }

    override fun hideDetailsOptions() {
        driverDetailsLayout.setOnClickListener { }
        detailsArrowIcon.visibility = View.INVISIBLE
        rideOptionsLabel.visibility = View.INVISIBLE
    }

    override fun showDetailsOptions() {
        driverDetailsLayout.setOnClickListener { toggleContactOptionsVisibility() }
        detailsArrowIcon.visibility = View.VISIBLE
        rideOptionsLabel.visibility = View.VISIBLE
    }

    private fun loadDriverPhoto(url: String, driverName: String) {
        if (url.isBlank()) {
            driverPhotoImage.setOnClickListener { }
            driverPhotoImage.setImageResource(R.drawable.uisdk_ic_driver_photo)
        } else {
            val logoSize = resources.getDimension(R.dimen.logo_size).convertDpToPixels()

            Picasso.get()
                    .load(url)
                    .placeholder(R.drawable.uisdk_ic_driver_photo)
                    .error(R.drawable.uisdk_ic_driver_photo)
                    .resize(logoSize, logoSize)
                    .transform(LogoTransformation(logoSize))
                    .noFade()
                    .into(driverPhotoImage, object : Callback.EmptyCallback() {
                        override fun onSuccess() {
                            driverPhotoImage.setOnClickListener {
                                val intent = DriverPhotoActivity.Builder.builder
                                        .setDriverName(driverName)
                                        .setDriverPhotoUrl(url)
                                        .build(context)
                                val options = ActivityOptions
                                        .makeSceneTransitionAnimation(context as Activity, driverPhotoImage, "driverPhoto")
                                context.startActivity(intent, options.toBundle())
                            }
                        }
                    })
        }
    }

    override fun showTripInfo() {
        actions?.tripInfoVisibility(true)
    }

    override fun observeTripStatus(bookingStatusPresenter: BookingStatusMVP.Presenter) {
        presenter.observeTripStatus(bookingStatusPresenter)
    }

}
