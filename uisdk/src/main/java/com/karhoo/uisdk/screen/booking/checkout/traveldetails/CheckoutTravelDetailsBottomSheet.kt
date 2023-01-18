package com.karhoo.uisdk.screen.booking.checkout.traveldetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment
import kotlinx.android.synthetic.main.fragment_checkout_travel_details_bottom_sheet.*

class CheckoutTravelDetailsBottomSheet : MasterBottomSheetFragment() {

    var onValueChanged: ((value: String?) -> Unit?)? = null
    var initialValue: String? = null
    var isFlight: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_checkout_travel_details_bottom_sheet, container, false)

        setupButton(view = view, buttonId = R.id.checkoutTravelDetailsSave, text = getString(R.string.kh_uisdk_save)) {
            checkoutTravelDetailsEditText.text?.let { onValueChanged?.invoke(it.toString()) }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(isFlight){
            setupHeader(view = view, title = getString(R.string.kh_uisdk_checkout_airport_title))
            checkoutTravelDetailsSubtitle.text = getString(R.string.kh_uisdk_checkout_airport_subtitle)
            checkoutTravelDetailsTextField.placeholderText = getString(R.string.kh_uisdk_checkout_airport_example)
        }
        else{
            setupHeader(view = view, title = getString(R.string.kh_uisdk_checkout_train_title))
            checkoutTravelDetailsSubtitle.text = getString(R.string.kh_uisdk_checkout_train_subtitle)
            checkoutTravelDetailsTextField.hint = getString(R.string.kh_uisdk_checkout_train_title)
            checkoutTravelDetailsTextField.placeholderText = getString(R.string.kh_uisdk_checkout_train_example)
        }
        checkoutTravelDetailsSave.enableButton(false)

        initialValue?.let {
            checkoutTravelDetailsEditText.setText(it)
            checkoutTravelDetailsSave.enableButton(true)
        }

        checkoutTravelDetailsEditText.addTextChangedListener {
            if(!checkString(it.toString())){
                checkoutTravelDetailsSave.enableButton(false)
                checkoutTravelDetailsTextField.error = getString(R.string.kh_uisdk_checkout_error_only_letters_and_digits_allowed)
            }
            else{
                if(it.toString().isNotEmpty())
                    checkoutTravelDetailsSave.enableButton(true)
                checkoutTravelDetailsTextField.error = null
            }
        }
    }

    private fun checkString(input: String): Boolean {
        return Regex("[a-zA-Z0-9]*").matches(input)
    }

    companion object {
        const val TAG = "CheckoutTravelDetailsBottomSheet"
    }
}
