package com.karhoo.uisdk.screen.booking.checkout.component.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_action_cell_view.view.actionViewContainer
import kotlinx.android.synthetic.main.uisdk_action_cell_view.view.actionViewIcon
import kotlinx.android.synthetic.main.uisdk_action_cell_view.view.actionViewSubtitle
import kotlinx.android.synthetic.main.uisdk_action_cell_view.view.actionViewTitle

class ActionCellView @kotlin.jvm.JvmOverloads constructor(context: Context,
                                                          attrs: AttributeSet? = null,
                                                          defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), ActionCellContract.View {
    private val view: View = inflate(context, R.layout.uisdk_action_cell_view, this)

    override fun setActionIcon(iconId: Int) {
        view.actionViewIcon.setBackgroundResource(iconId)
    }

    override fun setDottedBackground(show: Boolean) {
        view.actionViewContainer.setBackgroundResource(if (show) R.drawable
            .uisdk_dotted_background else R.drawable
            .uisdk_border_background)
    }

    override fun setSubtitle(subtitle: String) {
        view.actionViewSubtitle.text = subtitle
    }

    override fun setTitle(title: String) {
        view.actionViewTitle.text = title
    }
}
