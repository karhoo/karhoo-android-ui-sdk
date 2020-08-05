package com.karhoo.uisdk.base;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class BaseRecyclerView<V extends View> extends RecyclerView.ViewHolder {

    private final V view;

    public BaseRecyclerView(V itemView) {
        super(itemView);
        this.view = itemView;
    }

    public V getView() {
        return view;
    }
}
