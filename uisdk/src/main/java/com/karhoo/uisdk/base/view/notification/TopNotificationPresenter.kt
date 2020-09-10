package com.karhoo.uisdk.base.view.notification

import com.karhoo.uisdk.base.BasePresenter

class TopNotificationPresenter(view: TopNotificationMVP.View) : BasePresenter<TopNotificationMVP.View>(),
                                                                TopNotificationMVP.Presenter {

    init {
        attachView(view)
    }

    override fun setNotificationText(notification: String) {
        if (!notification.isEmpty()) {
            view?.enableNotificationText(notification)
            view?.animateNotification()
        }
    }
}
