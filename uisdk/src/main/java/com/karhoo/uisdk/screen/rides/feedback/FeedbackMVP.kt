package com.karhoo.uisdk.screen.rides.feedback

interface FeedbackMVP {

    interface View {
        fun finish()
    }

    interface Presenter {
        fun submit(answers: List<FeedbackAnswer>)
    }

}
