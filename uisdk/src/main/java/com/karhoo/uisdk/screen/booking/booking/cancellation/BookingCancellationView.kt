package com.karhoo.uisdk.screen.booking.booking.cancellation

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.sdk.api.KarhooApi

class BookingCancellationView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), BookingCancellationMVP.View {

    private val presenter: BookingCancellationMVP.Presenter = BookingCancellationPresenter(this)

    override fun cancelTrip() {
        TODO("Not yet implemented")
    }

    override fun showCancellationFee(formattedPrice: String) {
        TODO("Not yet implemented")
    }

    override fun showCancellationError() {
        TODO("Not yet implemented")
    }

}
