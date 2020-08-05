package com.karhoo.karhootraveller.presentation.splash.domain

interface AppVersionValidator {

    fun isCurrentVersionValid(listener: Listener)

    fun saveLoginTime(loginTimeMillis: Long)

    interface Listener {
        fun isAppValid(isValid: Boolean)

        fun isTokenValid(isValid: Boolean)
    }

}
