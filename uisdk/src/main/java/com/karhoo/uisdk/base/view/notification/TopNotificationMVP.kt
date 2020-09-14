package com.karhoo.uisdk.base.view.notification

interface TopNotificationMVP {

    interface View {

        fun setNotificationText(notification: String)

        fun enableNotificationText(notification: String)

        fun animateNotification()

    }

    interface Presenter {

        fun setNotificationText(notification: String)

    }

}
