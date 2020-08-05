package com.karhoo.uisdk.base;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public abstract class BasePresenter<V> {

    private WeakReference<V> viewReference;

    protected BasePresenter() {

    }

    @Nullable
    public V getView() {
        return viewReference == null ? null : viewReference.get();
    }

    public void attachView(V view) {
        viewReference = new WeakReference<>(view);
    }

    public void detachView() {
        if (viewReference != null) {
            viewReference.clear();
            viewReference = null;
        }
    }

    protected boolean viewIsAttached() {
        return viewReference != null && viewReference.get() != null;
    }

}
