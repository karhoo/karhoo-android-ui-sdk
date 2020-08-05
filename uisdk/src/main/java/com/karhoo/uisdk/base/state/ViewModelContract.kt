package com.karhoo.uisdk.base.state

// A contract to have predefined events to update the state of the widget.
internal interface ViewModelContract<EVENT> {
    fun process(viewEvent: EVENT)
}
