package com.karhoo.uisdk.base.state

import android.app.Application
import android.util.Log
import androidx.annotation.CallSuper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

open class BaseStateViewModel<STATE, ACTION, EVENT>(application: Application) : AndroidViewModel(application), ViewModelContract<EVENT>  {

    val _viewStates: MutableLiveData<STATE> = MutableLiveData()
    fun viewStates(): LiveData<STATE> = _viewStates
    val currentState: STATE
        get() = _viewState
                ?: throw UninitializedPropertyAccessException("\"viewState\" was queried before being initialized")

    private var _viewState: STATE? = null
    protected var viewState: STATE
        get() = _viewState
                ?: throw UninitializedPropertyAccessException("\"viewState\" was queried before being initialized")
        set(value) {
            Log.d("TAG", "setting viewState : $value")
            _viewState = value
            _viewStates.value = value
        }

    private val _viewActions: SingleLiveEvent<ACTION> = SingleLiveEvent()
    fun viewActions(): SingleLiveEvent<ACTION> = _viewActions

    private var _viewAction: ACTION? = null
    protected var viewAction: ACTION
        get() = _viewAction
                ?: throw UninitializedPropertyAccessException("\"viewAction\" was queried before being initialized")
        set(value) {
            Log.d("TAG", "setting viewEffect : $value")
            _viewAction = value
            _viewActions.value = value
        }

    // update the state by using a set of predefined contracts. Some of the event can trigger an
    // action to be performed (e.g. output of the widget)
    @CallSuper
    override fun process(viewEvent: EVENT) {
        if (!viewStates().hasObservers()) {
            throw NoObserverAttachedException("No observer attached. In case of custom View \"startObserving()\" function needs to be called manually.")
        }
        Log.d("TAG", "processing viewEvent: $viewEvent")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("TAG", "onCleared")
    }
}