package com.karhoo.uisdk.screen.booking.address;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.karhoo.uisdk.R;

public class DropOffEmptyView extends LinearLayout {

    public DropOffEmptyView(Context context) {
        this(context, null);
    }

    public DropOffEmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.uisdk_view_dropoff_empty, this);
    }
}
