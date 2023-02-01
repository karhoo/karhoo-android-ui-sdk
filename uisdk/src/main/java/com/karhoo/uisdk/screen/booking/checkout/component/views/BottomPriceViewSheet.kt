package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.karhoo.sdk.api.model.QuoteType
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment
import com.karhoo.uisdk.util.extension.toLocalisedInfoString

class BottomPriceViewSheet(private val quoteType: QuoteType) : MasterBottomSheetFragment() {
    lateinit var bottomSheetPriceViewSubtitle: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.uisdk_view_price_bottom_sheet, container, false)

        bottomSheetPriceViewSubtitle = view.findViewById(R.id.bottomSheetPriceViewSubtitle)
        bottomSheetPriceViewSubtitle.text = context?.let { quoteType.toLocalisedInfoString(it) }
        setupHeader(view = view, getString(R.string.kh_uisdk_price_bottom_sheet_title))

        return view
    }

    companion object {
        const val TAG = "QuotesSortView"
    }

}
