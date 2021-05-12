package com.karhoo.uisdk.screen.booking.booking.comment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.karhoo.uisdk.R
import kotlinx.android.synthetic.main.uisdk_view_booking_comments.view.bookingCommentsInput
import kotlinx.android.synthetic.main.uisdk_view_booking_comments.view.bookingCommentsLayout
import kotlinx.android.synthetic.main.uisdk_view_booking_comments.view.updateBookingCommentsMask

class BookingOptionalInfoView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null,
                                                        defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), BookingOptionalInfoMVP.View {

    private val presenter: BookingOptionalInfoMVP.Presenter = BookingOptionalInfoPresenter(this)

    init {
        View.inflate(context, R.layout.uisdk_view_booking_comments, this)
        initialiseFieldListeners()
        setHintText(context.getString(R.string.kh_uisdk_additional_comments))
    }

    private fun initialiseFieldListeners() {
        bookingCommentsLayout.setOnFocusChangeListener { _, hasFocus ->
            onFocusChange(hasFocus)
        }
    }

    private fun onFocusChange(hasFocus: Boolean) {
        if (!hasFocus) {
            presenter.updateOptionalInfo(bookingCommentsInput.text.toString())
        }
    }

    fun setHintText(hint: String) {
        bookingCommentsInput.hint = hint
    }

    override fun setBookingOptionalInfo(comments: String) {
        presenter.prefillForBookingOptionalInfo(comments)
    }

    override fun getBookingOptionalInfo(): String {
        presenter.updateOptionalInfo(bookingCommentsInput.text.toString())
        return presenter.bookingOptionalInfoValue()
    }

    override fun bindBookingOptionalInfo(comments: String) {
        bookingCommentsInput.setText(comments)
    }

    override fun bindEditMode(isEditing: Boolean) {
        updateBookingCommentsMask.visibility = if (isEditing) View.GONE else View.VISIBLE
    }

    override fun allFieldsValid(): Boolean {
        return true
    }
}
