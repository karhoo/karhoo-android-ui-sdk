package com.karhoo.uisdk.base.bottomSheet

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.view.LoadingButtonView


open class MasterBottomSheetFragment : BottomSheetDialogFragment() {

    private var fullScreen: Boolean = false

    fun setupHeader(view: View, title: String){
        val closeDialogButton = view.findViewById<ImageButton>(R.id.masterBottomSheetCloseDialog)
        closeDialogButton?.apply {
            setOnClickListener { dismiss() }
        }

        val titleView = view.findViewById<TextView>(R.id.masterBottomSheetTitle)
        titleView?.apply {
            text = title
        }
    }

    fun setupButton(view: View, buttonId: Int, text: String, callback: ((() -> Unit) -> Unit)? = null){
        val button = view.findViewById<LoadingButtonView>(buttonId)
        button.apply {
            setText(text)
            setOnClickListener {
                callback?.invoke {  }
                dismiss()
            }
        }
    }

    fun showFullScreen(manager: FragmentManager, tag: String?){
        fullScreen = true
        show(manager, tag)
    }

    override fun getTheme(): Int {
        return R.style.KhMasterBottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        if(fullScreen){
            val dialog = BottomSheetDialog(requireContext(), theme)
            dialog.setOnShowListener {

                val bottomSheetDialog = it as BottomSheetDialog
                val parentLayout =
                    bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                parentLayout?.let { layout ->
                    val behaviour = BottomSheetBehavior.from(layout)
                    setupFullHeight(layout)
                    behaviour.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
            return dialog
        }
        else
            return super.onCreateDialog(savedInstanceState)
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        bottomSheet.layoutParams = layoutParams
    }
}
