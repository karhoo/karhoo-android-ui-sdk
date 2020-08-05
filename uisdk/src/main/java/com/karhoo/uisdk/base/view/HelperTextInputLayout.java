package com.karhoo.uisdk.base.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.EditText;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.google.android.material.textfield.TextInputLayout;
import com.karhoo.uisdk.R;

/**
 * Workaround for helper text which doesn't exist in TextInputLayout
 */

public class HelperTextInputLayout extends TextInputLayout {

    public HelperTextInputLayout(Context context) {
        super(context);
    }

    public HelperTextInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HelperTextInputLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    static final Interpolator FAST_OUT_SLOW_IN_INTERPOLATOR = new FastOutSlowInInterpolator();

    private CharSequence helperText;
    private ColorStateList helperTextColor;
    private boolean helperTextEnabled = false;
    private boolean errorEnabled = false;
    private TextView helperView;
    private int helperTextAppearance = R.style.HelperTextAppearance;

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if (child instanceof EditText) {
            if (!TextUtils.isEmpty(helperText)) {
                setHelperText(helperText);
            }
        }
    }

    public int getHelperTextAppearance() {
        return helperTextAppearance;
    }

    public void setHelperTextAppearance(int _helperTextAppearanceResId) {
        helperTextAppearance = _helperTextAppearanceResId;
    }

    public void setHelperTextColor(ColorStateList _helperTextColor) {
        helperTextColor = _helperTextColor;
        helperView.setTextColor(helperTextColor);
    }

    public boolean isHelperTextEnabled() {
        return helperTextEnabled;
    }

    public void setHelperTextEnabled(boolean _enabled) {
        if (helperTextEnabled == _enabled) return;
        if (_enabled && errorEnabled) {
            setErrorEnabled(false);
        }
        if (this.helperTextEnabled != _enabled) {
            if (_enabled) {
                this.helperView = new TextView(this.getContext());
                this.helperView.setTextAppearance(this.getContext(), this.helperTextAppearance);
                if (helperTextColor != null) {
                    this.helperView.setTextColor(helperTextColor);
                }
                this.helperView.setVisibility(VISIBLE);
                this.addView(this.helperView);
                if (this.helperView != null) {
                    ViewCompat.setPaddingRelative(
                            this.helperView,
                            ViewCompat.getPaddingStart(getEditText()),
                            0, ViewCompat.getPaddingEnd(getEditText()),
                            getEditText().getPaddingBottom());
                }
            } else {
                this.removeView(this.helperView);
                this.helperView = null;
            }

            this.helperTextEnabled = _enabled;
        }
    }

    public void setHelperText(CharSequence _helperText) {
        helperText = _helperText;
        if (!this.helperTextEnabled) {
            if (TextUtils.isEmpty(helperText)) {
                return;
            }
            this.setHelperTextEnabled(true);
        }

        if (!TextUtils.isEmpty(helperText)) {
            this.helperView.setText(helperText);
            this.helperView.setVisibility(VISIBLE);
            ViewCompat.setAlpha(this.helperView, 0.0F);
            ViewCompat.animate(this.helperView)
                    .alpha(1.0F).setDuration(200L)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(null).start();
        } else if (this.helperView.getVisibility() == VISIBLE) {
            ViewCompat.animate(this.helperView)
                    .alpha(0.0F).setDuration(200L)
                    .setInterpolator(FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        public void onAnimationEnd(View view) {
                            helperView.setText(null);
                            helperView.setVisibility(INVISIBLE);
                        }
                    }).start();
        }
        this.sendAccessibilityEvent(2048);
    }

    @Override
    public void setErrorEnabled(boolean _enabled) {
        if (errorEnabled == _enabled) return;
        errorEnabled = _enabled;
        if (_enabled && helperTextEnabled) {
            setHelperTextEnabled(false);
        }

        super.setErrorEnabled(_enabled);

        if (!(_enabled || TextUtils.isEmpty(helperText))) {
            setHelperText(helperText);
        }
    }

}
