package com.karhoo.uisdk.screen.booking.booking.prebookconfirmation

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.karhoo.sdk.api.model.PickupType
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.sdk.api.model.TripInfo
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.ScheduledDateView
import com.karhoo.uisdk.base.ScheduledDateViewBinder
import com.karhoo.uisdk.util.CurrencyUtils
import com.karhoo.uisdk.util.DateUtil
import com.karhoo.uisdk.util.extension.orZero
import com.karhoo.uisdk.util.extension.toLocalisedString
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.bookingDateText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.bookingTimeText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.dropoffAddressText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.fareText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.fareTypeText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.meetingPointText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.pickupAddressText
import kotlinx.android.synthetic.main.uisdk_alert_prebook_confirmation.view.pickupTypeText
import org.joda.time.DateTime
import java.util.Currency

class PrebookConfirmationView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr), ScheduledDateView {
    private val scheduledDateViewBinder = ScheduledDateViewBinder()

    init {
        View.inflate(context, R.layout.uisdk_alert_prebook_confirmation, this)
    }

    fun bind(quoteType: QuoteType?, trip: TripInfo) {
        pickupAddressText.text = trip.origin?.displayAddress
        dropoffAddressText.text = trip.destination?.displayAddress

        when (trip.meetingPoint?.pickupType) {
            PickupType.DEFAULT,
            PickupType.NOT_SET -> {
                meetingPointText.visibility = View.INVISIBLE
                pickupTypeText.visibility = View.INVISIBLE
            }
            else -> {
                pickupTypeText.text = trip.meetingPoint?.pickupType?.toLocalisedString(context.applicationContext)
                meetingPointText.text = trip.meetingPoint?.instructions.orEmpty()
            }
        }

        scheduledDateViewBinder.bind(this, trip)

        val currency = Currency.getInstance(trip.quote?.currency)
        fareText.text = CurrencyUtils.intToPrice(currency, trip.quote?.total.orZero())

        fareTypeText.text = quoteType?.toLocalisedString(context.applicationContext).orEmpty()
    }

    override fun displayDate(date: DateTime) {
        bookingTimeText.text = DateUtil.getTimeFormat(context, date)
        bookingDateText.text = DateUtil.getDateFormat(date)
    }

    override fun displayNoDateAvailable() {
        // Do nothing
    }

}
