package com.karhoo.uisdk.base;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;

import com.karhoo.uisdk.R;

public class CollapsiblePanelView extends RelativeLayout {

    public enum PanelState {
        COLLAPSED, EXPANDED
    }

    private PanelState panelState = PanelState.COLLAPSED;

    private float collapsedHeight;
    private float expandedHeight;

    public CollapsiblePanelView(Context context) {
        super(context, null, 0);
    }

    public CollapsiblePanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CollapsiblePanelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final Resources resources = context.getResources();
        collapsedHeight = resources.getDimension(R.dimen.kh_uisdk_collapsible_panel_collapsed_height);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        expandedHeight = displayMetrics.heightPixels;
    }

    public PanelState getPanelState() {
        return panelState;
    }

    public void setHeights(float collapsedHeight, float expandedHeight) {
        this.collapsedHeight = collapsedHeight;
        this.expandedHeight = expandedHeight;
    }

    private void setPanelState(PanelState panelState) {
        if (isEnabled()) {
            this.panelState = panelState;
            if (panelState == PanelState.COLLAPSED) {
                animateToNewHeight(collapsedHeight);
            } else {
                animateToNewHeight(expandedHeight);
            }
        }
    }

    public void togglePanelState() {
        if (isEnabled()) {
            if (panelState == PanelState.EXPANDED) {
                this.panelState = PanelState.COLLAPSED;
                animateToNewHeight(collapsedHeight);
            } else {
                this.panelState = PanelState.EXPANDED;
                animateToNewHeight(expandedHeight);
            }
        }
    }

    public void resetView() {
        if (panelState == PanelState.COLLAPSED) {
            animateToNewHeight(collapsedHeight);
        } else if (panelState == PanelState.EXPANDED) {
            animateToNewHeight(expandedHeight);
        }
    }

    private void animateToNewHeight(float height) {
        final ValueAnimator anim = ValueAnimator.ofInt(getHeight(), (int) height);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                LayoutParams params = (LayoutParams) getLayoutParams();
                if (params.height != animatedValue) {
                    params.height = animatedValue;
                    setLayoutParams(params);
                    requestLayout();
                }
            }
        });
        anim.setDuration(getResources().getInteger(R.integer.kh_uisdk_animation_duration_slide_out_or_in));
        anim.start();
    }

    public void enable() {
        if (!isEnabled()) {
            setEnabled(true);
        }
    }

    public void collapseAndDisable() {
        setPanelState(PanelState.COLLAPSED);
        if (isEnabled()) {
            setEnabled(false);
        }
    }

}