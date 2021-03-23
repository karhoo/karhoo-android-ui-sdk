package com.karhoo.karhootraveller.presentation.register

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.register.registration.RegistrationView
import com.karhoo.karhootraveller.util.SoftKeyboardUtils
import com.karhoo.sdk.api.model.UserInfo
import com.karhoo.uisdk.base.BaseActivity
import kotlinx.android.synthetic.main.activity_registration.registrationWidget
import kotlinx.android.synthetic.main.activity_registration.toolbar
import kotlinx.android.synthetic.main.activity_registration.toolbarProgressBar

class RegistrationActivity : BaseActivity(), RegistrationView.Actions {

    override val layout: Int = R.layout.activity_registration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(R.string.kh_uisdk_title_activity_register)
        }
    }

    override fun bindViews() {
        registrationWidget.actions = this
    }

    override fun handleExtras() {
        // Do nothing
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun userRegistrationFailed() {
        toolbarProgressBar.visibility = View.INVISIBLE
    }

    override fun userRegistered(userInfo: UserInfo) {
        setResult(Activity.RESULT_OK)
        SoftKeyboardUtils.hideSoftKeyboard(currentFocus)
        finish()
    }

    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        fun build(context: Context): Intent {
            val intent = Intent(context, RegistrationActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }

    companion object {
        const val REQ_CODE = 301
    }

}
