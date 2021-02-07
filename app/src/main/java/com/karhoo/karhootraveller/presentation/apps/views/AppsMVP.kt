package com.karhoo.karhootraveller.presentation.apps.views

import com.karhoo.karhootraveller.models.Application

interface AppsMVP {
    interface View {
        fun onResume()
    }

    interface Actions {
        fun didSelectApplication(application: Application)
    }
}