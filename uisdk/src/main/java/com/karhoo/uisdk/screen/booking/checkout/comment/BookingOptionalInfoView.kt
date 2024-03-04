package com.karhoo.uisdk.screen.booking.checkout.comment

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.karhoo.uisdk.R

class BookingOptionalInfoView @JvmOverloads constructor(context: Context,
                                                        attrs: AttributeSet? = null,
                                                        defStyleAttr: Int = 0)
    : ConstraintLayout(context, attrs, defStyleAttr), BookingOptionalInfoMVP.View {

    private val presenter: BookingOptionalInfoMVP.Presenter = BookingOptionalInfoPresenter(this)
    private lateinit var bookingCommentsInput: TextInputEditText
    private lateinit var  bookingCommentsLayout: TextInputLayout
    private lateinit var  updateBookingCommentsMask: View

    init {
        View.inflate(context, R.layout.uisdk_view_booking_comments, this)

        bookingCommentsInput = findViewById(R.id.bookingCommentsInput)
        bookingCommentsLayout = findViewById(R.id.bookingCommentsLayout)
        updateBookingCommentsMask = findViewById(R.id.updateBookingCommentsMask)

        initialiseFieldListeners()
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
        bookingCommentsLayout.hint = hint
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
