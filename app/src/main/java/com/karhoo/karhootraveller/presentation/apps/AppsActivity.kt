package com.karhoo.karhootraveller.presentation.apps

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.karhoo.karhootraveller.R
import com.karhoo.karhootraveller.models.Application
import com.karhoo.karhootraveller.presentation.apps.views.AppsMVP
import kotlinx.android.synthetic.main.activity_apps.appsViewWidget

class AppsActivity : AppCompatActivity(), AppsMVP.Actions {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)
        appsViewWidget.actions = this
    }

    override fun didSelectApplication(application: Application) {
        val data = Intent()
        data.putExtra(DATA_APPLICATION, application)
        setResult(RESULT_OK, data)
        finish()
    }

    class Builder private constructor() {

        private val extras: Bundle = Bundle()

        fun build(context: Context): Intent {
            val intent = Intent(context, AppsActivity::class.java)
            intent.putExtras(extras)
            return intent
        }

        companion object {
            val builder: Builder
                get() = Builder()
        }
    }

    companion object {
        const val DATA_APPLICATION = "set::application"
        const val REQ_CODE_APPS = 302
    }
}