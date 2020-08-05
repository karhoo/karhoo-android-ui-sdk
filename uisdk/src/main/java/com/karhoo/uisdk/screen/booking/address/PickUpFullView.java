package com.karhoo.uisdk.screen.booking.address;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.karhoo.uisdk.R;

public class PickUpFullView extends LinearLayout {

    public PickUpFullView(Context context) {
        this(context, null);
    }

    public PickUpFullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.uisdk_view_pickup_full, this);
    }
}
