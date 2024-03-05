package com.karhoo.uisdk.screen.booking.checkout.traveldetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment
import com.karhoo.uisdk.base.view.LoadingButtonView


class CheckoutTravelDetailsBottomSheet : MasterBottomSheetFragment() {

    var onValueChanged: ((value: String?) -> Unit?)? = null
    var initialValue: String? = null
    var isFlight: Boolean = false

    private lateinit var checkoutTravelDetailsEditText: TextInputEditText
    private lateinit var checkoutTravelDetailsSubtitle: TextView
    private lateinit var checkoutTravelDetailsTextField: TextInputLayout
    private lateinit var checkoutTravelDetailsSave: LoadingButtonView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_checkout_travel_details_bottom_sheet, container, false)

        checkoutTravelDetailsEditText = view.findViewById(R.id.checkoutTravelDetailsEditText)
        checkoutTravelDetailsSubtitle = view.findViewById(R.id.checkoutTravelDetailsSubtitle)
        checkoutTravelDetailsTextField = view.findViewById(R.id.checkoutTravelDetailsTextField)
        checkoutTravelDetailsSave = view.findViewById(R.id.checkoutTravelDetailsSave)

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
            checkoutTravelDetailsTextField.placeholderText = getString(R.string.kh_uisdk_placeholder_flight_number)
        }
        else{
            setupHeader(view = view, title = getString(R.string.kh_uisdk_checkout_train_title))
            checkoutTravelDetailsSubtitle.text = getString(R.string.kh_uisdk_checkout_train_subtitle)
            checkoutTravelDetailsTextField.hint = getString(R.string.kh_uisdk_checkout_train_title)
            checkoutTravelDetailsTextField.placeholderText = getString(R.string.kh_uisdk_placeholder_train_number)
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
