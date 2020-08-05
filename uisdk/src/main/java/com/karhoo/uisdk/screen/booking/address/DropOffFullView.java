package com.karhoo.uisdk.screen.booking.address;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.karhoo.uisdk.R;

public class DropOffFullView extends LinearLayout {

    public DropOffFullView(Context context) {
        this(context, null);
    }

    public DropOffFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.uisdk_view_dropoff_full, this);
    }
}
