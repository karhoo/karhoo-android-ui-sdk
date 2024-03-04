package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.karhoo.uisdk.R

class ActionCellView @kotlin.jvm.JvmOverloads constructor(context: Context,
                                                          attrs: AttributeSet? = null,
                                                          defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), ActionCellContract.View {
    private val view: View = inflate(context, R.layout.uisdk_action_cell_view, this)
    private val actionViewIcon: ImageView = view.findViewById(R.id.actionViewIcon)
    private val actionViewSubtitle: TextView = view.findViewById(R.id.actionViewSubtitle)
    private val actionViewTitle: TextView = view.findViewById(R.id.actionViewTitle)


    override fun setActionIcon(iconId: Int) {
        actionViewIcon.setBackgroundResource(iconId)
    }

    override fun setSubtitle(subtitle: String) {
        actionViewSubtitle.text = subtitle
    }

    override fun setTitle(title: String) {
        actionViewTitle.text = title
    }

    override fun setTitleContentDescription(text: String) {
        actionViewTitle.contentDescription = text
    }
}
