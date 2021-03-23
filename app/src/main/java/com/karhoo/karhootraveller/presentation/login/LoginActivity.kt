package com.karhoo.karhootraveller.presentation.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.presentation.login.password.ForgotPasswordMVP
import com.karhoo.karhootraveller.presentation.login.signin.LoginView
import com.karhoo.uisdk.base.BaseActivity
import com.karhoo.uisdk.screen.booking.BookingActivity
import kotlinx.android.synthetic.main.activity_login.forgotPasswordWidget
import kotlinx.android.synthetic.main.activity_login.loginWidget
import kotlinx.android.synthetic.main.activity_login.toolbar
import kotlinx.android.synthetic.main.activity_login.toolbarProgressBar

class LoginActivity : BaseActivity(), LoginView.Actions, ForgotPasswordMVP.Actions {

    override val layout: Int = R.layout.activity_login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(R.string.kh_uisdk_title_activity_login)
        }
        forgotPasswordWidget.actions = this
    }

    override fun onResume() {
        super.onResume()
        loginWidget.actions = this
    }

    override fun handleExtras() {
        // Do nothing
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun goToBooking() {
        toolbarProgressBar.visibility = View.INVISIBLE
        startActivity(BookingActivity.Builder
                              .builder
                              .build(this))
        finish()
    }

    override fun showProgress() {
        toolbarProgressBar.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        toolbarProgressBar.visibility = View.INVISIBLE
    }

    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        fun build(context: Context): Intent {
            val intent = Intent(context, LoginActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }
}
