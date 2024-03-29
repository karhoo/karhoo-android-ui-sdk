package com.karhoo.uisdk.screen.booking.checkout.comment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.textfield.TextInputEditText
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.bottomSheet.MasterBottomSheetFragment

class CheckoutCommentBottomSheet : MasterBottomSheetFragment() {

    var onCommentsChanged: ((comment: String?) -> Unit?)? = null
    var initialComments: String? = null

    private lateinit var checkoutCommentsEditText: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =
            inflater.inflate(R.layout.fragment_checkout_comment_bottom_sheet, container, false)

        checkoutCommentsEditText = view.findViewById(R.id.checkoutCommentsEditText)
        setupHeader(view = view, title = getString(R.string.kh_uisdk_checkout_comments_title))
        setupButton(
            view = view,
            buttonId = R.id.checkoutCommentsSave,
            text = getString(R.string.kh_uisdk_save)
        ) {
            checkoutCommentsEditText.text?.let { onCommentsChanged?.invoke(it.toString()) }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialComments?.let {
            checkoutCommentsEditText.setText(it)
        }
    }

    companion object {
        const val TAG = "CheckoutCommentBottomSheet"
    }
}
