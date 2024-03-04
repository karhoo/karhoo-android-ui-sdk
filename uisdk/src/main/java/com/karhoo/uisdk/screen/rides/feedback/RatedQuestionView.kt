package com.karhoo.uisdk.screen.rides.feedback

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.RatingBar
import android.widget.TextView
import androidx.annotation.AttrRes
import com.google.android.material.textfield.TextInputEditText
import com.karhoo.uisdk.R
import com.karhoo.uisdk.base.listener.SimpleTextWatcher
import com.karhoo.uisdk.base.validator.EmptyFieldValidator
import com.karhoo.uisdk.base.view.SelfValidatingTextLayout
import com.karhoo.uisdk.util.extension.showSoftKeyboard

/**
 * Widget for providing a question with a rating bar and comment
 * Is configured via layout XML. See example if FeedbackView's layout.
 */
class RatedQuestionView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0)
    : FrameLayout(context, attrs, defStyleAttr) {

    private lateinit var commentEditText: TextInputEditText
    private lateinit var commentEditTextLayout: SelfValidatingTextLayout
    private lateinit var questionText: TextView
    private lateinit var ratingWidget: RatingBar

    var fieldId: String = ""
        private set

    val rating: Int by lazy { ratingWidget.rating.toInt() }

    val additionalComments by lazy { commentEditText.text.toString() }

    var actions: RatedQuestionsMVP.Actions? = null

    var isCommentAdded = false

    private var validated = false

    init {
        View.inflate(context, R.layout.uisdk_view_rated_question, this)

        commentEditText = findViewById(R.id.commentEditText)
        commentEditTextLayout = findViewById(R.id.commentEditTextLayout)
        questionText = findViewById(R.id.questionText)
        ratingWidget = findViewById(R.id.ratingWidget)

        with(context.obtainStyledAttributes(attrs, R.styleable.RatedQuestionView)) {
            questionText.text = getString(R.styleable.RatedQuestionView_question)
            fieldId = getString(R.styleable.RatedQuestionView_field_id).orEmpty()
            recycle()
        }

        setListeners()
        setErrors()
        setValidators()
    }

    private fun setListeners() {
        ratingWidget.setOnRatingBarChangeListener { _, _, _ ->
            checkRatedQuestionComplete()
            commentEditText.requestFocus()
            commentEditText.showSoftKeyboard()
        }

        commentEditText.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                if (commentEditText.editableText.isEmpty()) {
                    isCommentAdded = false
                    checkRatedQuestionInvalidated()
                } else if (!isCommentAdded) {
                    isCommentAdded = true
                    checkRatedQuestionComplete()
                }
            }
        })
        commentEditText.setOnFocusChangeListener { _, hasFocus -> commentEditTextLayout.setFocus(hasFocus) }
    }

    private fun setErrors() {
        commentEditTextLayout.apply {
            setErrorMsg(R.string.kh_uisdk_invalid_empty_field)
            setErrorTextAppearance(R.style.Text_Red_Small)
        }
    }

    private fun setValidators() {
        commentEditTextLayout.setValidator(EmptyFieldValidator())
    }

    private fun checkRatedQuestionComplete() {
        commentEditTextLayout.setFocus(false)
        if (ratingWidget.rating > 0 && commentEditText.editableText.isNotEmpty()) {
            validated = true
            actions?.onRatedQuestionViewComplete()
            ratingWidget.setOnRatingBarChangeListener { _, _, _ -> }
        }
    }

    private fun checkRatedQuestionInvalidated() {
        if (ratingWidget.rating > 0 && commentEditText.editableText.isEmpty() && validated) {
            validated = false
            actions?.onRatedQuestionInvalidated()
        }
    }

}
