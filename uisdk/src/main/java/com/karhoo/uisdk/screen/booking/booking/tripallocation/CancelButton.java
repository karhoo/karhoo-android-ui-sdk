package com.karhoo.uisdk.screen.booking.booking.tripallocation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karhoo.uisdk.R;
import com.karhoo.uisdk.base.listener.SimpleAnimationListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CancelButton extends FrameLayout implements View.OnTouchListener {

    private static final int MAX_PROGRESS = 100;

    public interface CancelButtonInteractions {
        void onProgressComplete();
    }

    private FloatingActionButton cancelButton;
    private ProgressBar progressBar;
    private TextView cancellingText;
    private Animation scaleToFullSize;
    private Vibrator vibrator;

    private CancelButtonInteractions listener;
    private int counter = 0;
    private ScheduledExecutorService updater;

    public CancelButton(Context context) {
        super(context);
        init(context);
    }

    public CancelButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CancelButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setListener(CancelButtonInteractions listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        View.inflate(context, R.layout.uisdk_view_cancel_button, this);

        cancelButton = findViewById(R.id.cancellationButton);
        progressBar = findViewById(R.id.progressBar);
        cancellingText = findViewById(R.id.cancellingLabel);

        scaleToFullSize = AnimationUtils.loadAnimation(context, R.anim.uisdk_scale_to_full_size);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        progressBar.setMax(MAX_PROGRESS);
        progressBar.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        cancelButton.setOnTouchListener(this);
        setEnabled(true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                progressBar.setVisibility(View.INVISIBLE);
                cancellingText.setVisibility(View.INVISIBLE);
                scaleButtonBackToNormal();
                stopUpdating();
                break;

            case MotionEvent.ACTION_DOWN:
                progressBar.setVisibility(View.VISIBLE);
                cancellingText.setVisibility(View.VISIBLE);
                scaleToFullSize.setDuration(200);
                progressBar.startAnimation(scaleToFullSize);
                cancellingText.startAnimation(scaleToFullSize);
                scaleButtonToOversize();
                startUpdating();
                break;
            default:
                break;
        }

        return !isEnabled();
    }

    private void startUpdating() {
        if (updater != null) {
            Log.e(getClass().getSimpleName(), "Another executor is still active");
            return;
        }

        updater = Executors.newSingleThreadScheduledExecutor();
        updater.scheduleAtFixedRate(new UpdateCounter(), 0, 20, TimeUnit.MILLISECONDS);
    }

    private void stopUpdating() {
        counter = 0;
        progressBar.setProgress(0);

        progressBar.clearAnimation();

        if (updater != null) {
            updater.shutdownNow();
            updater = null;
        }
    }

    private class UpdateCounter implements Runnable {

        public UpdateCounter() {
        }

        public void run() {
            progressHandler.sendEmptyMessage(0);
        }
    }

    @SuppressLint("MissingPermission")
    private void increment() {
        counter++;
        int progress = (int) (MAX_PROGRESS * (counter / 100f));
        if (progress < MAX_PROGRESS) {
            progressBar.setProgress(progress);
        } else if (progress == MAX_PROGRESS) {
            progressBar.setProgress(MAX_PROGRESS);
            if (listener != null) {
                vibrator.vibrate(200);
                listener.onProgressComplete();
            }
        }
    }

    private void scaleButtonToOversize() {
        Animation animation = new ScaleAnimation(1.0f, 1.5f, 1.0f, 1.5f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(200);
        animation.setFillAfter(true);
        cancelButton.startAnimation(animation);
    }

    private void scaleButtonBackToNormal() {
        cancelButton.clearAnimation();
    }

    private final Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                increment();
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!cancelButton.isEnabled() && enabled) {
            scaleToFullSize.setDuration(500);
            scaleToFullSize.setAnimationListener(new SimpleAnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    cancelButton.setVisibility(View.VISIBLE);
                }
            });
            cancelButton.startAnimation(scaleToFullSize);

            progressBar.setVisibility(View.INVISIBLE);
            cancellingText.setVisibility(View.INVISIBLE);
            stopUpdating();
        }
        cancelButton.setEnabled(enabled);
        cancelButton.setClickable(enabled);
        cancelButton.setFocusable(enabled);
    }
}
