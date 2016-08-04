package com.tribe.app.presentation.view.camera.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;

import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.camera.interfaces.AudioVisualizerCallback;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

/**
 *
 * Created by tiago on 2016/06/13.
 */
public class HistogramVisualizerView extends LinearLayout implements AudioVisualizerCallback {

    private static final int NB_COLUMNS = 6;

    private static final int THRESHOLD = 5;
    private static final int DURATION_ULTRA_SHORT = 100;
    private static final int DURATION_SHORT = 200;
    private static final int DURATION_LONG = 250;
    private static final int SAMPLING_DURATION = 100;

    @Inject
    ScreenUtils screenUtils;

    // Variables
    List<View> viewColumnList;
    private int lastValue = Integer.MAX_VALUE;
    private ValueAnimator[] vaArray;
    private int[] vaLastAnimationValueArray;
    private int[] vaLastValueArray;
    private Handler handler;
    private long lastSamplingTimestamp;

    public HistogramVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        viewColumnList = new ArrayList<>();

        for (int i = 0; i < NB_COLUMNS; i++) {
            View view = new View(context);
            view.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 0);
            lp.gravity = Gravity.BOTTOM;
            lp.weight = 1;
            addView(view, lp);
            viewColumnList.add(view);
        }

        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        vaArray = new ValueAnimator[viewColumnList.size()];
        vaLastAnimationValueArray = new int[viewColumnList.size()];
        vaLastValueArray = new int[viewColumnList.size()];

        for (int i = 0; i < vaLastAnimationValueArray.length; i++) {
            vaLastAnimationValueArray[i] = 0;
        }

        for (int i = 0; i < vaLastValueArray.length; i++) {
            vaLastValueArray[i] = 0;
        }

        handler = new Handler(Looper.getMainLooper());

        setWillNotDraw(false);
    }

    @Override
    public void receive(final double[]... toTransform) {
        if (System.currentTimeMillis() - lastSamplingTimestamp > SAMPLING_DURATION) {
            handler.post(() -> {
                lastSamplingTimestamp = System.currentTimeMillis();
                int max = Integer.MIN_VALUE;

                for (int i = 0; i < toTransform[0].length; i++) {
                    int value = (int) (toTransform[0][i] * 15);
                    if (value > max) max = value;
                }

                if (Math.abs(lastValue - max) > THRESHOLD) {
                    int i1 = viewColumnList.size() / 2;

                    for (int i = 0; i < i1; i++) {
                        if (i == i1 - 1) layoutBar(viewColumnList.get(i), null, max, true, i);
                        else layoutBar(viewColumnList.get(i), viewColumnList.get(i + 1), 0, true, i);
                    }

                    for (int i = viewColumnList.size() - 1; i >= i1; i--) {
                        if (i == i1) layoutBar(viewColumnList.get(i), null, max, true, i);
                        else layoutBar(viewColumnList.get(i), viewColumnList.get(i - 1), 0, true, i);
                    }

                    lastValue = max;
                }
            });
        }
    }

    private void layoutBar(View bar, View barFrom, int height, boolean hasReturn, int i) {
        ValueAnimator va = vaArray[viewColumnList.indexOf(bar)];
        if (va != null && va.isRunning()) va.cancel();

        int newHeight = 0;

        if (barFrom == null) {
            newHeight = height;
        } else {
            newHeight = vaLastValueArray[viewColumnList.indexOf(barFrom)];
        }

        vaLastValueArray[viewColumnList.indexOf(bar)] = newHeight;

        if (hasReturn) va = ValueAnimator.ofInt(vaLastAnimationValueArray[viewColumnList.indexOf(bar)], newHeight, 0);
        else va =  ValueAnimator.ofInt(vaLastAnimationValueArray[viewColumnList.indexOf(bar)], newHeight);

        va.setDuration(DURATION_LONG);
        va.setInterpolator(new LinearInterpolator());
        va.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            vaLastAnimationValueArray[viewColumnList.indexOf(bar)] = value.intValue();
            bar.getLayoutParams().height = value.intValue();
            bar.requestLayout();
        });

        va.start();
        vaArray[viewColumnList.indexOf(bar)] = va;
    }

    @Override
    public void activate() {
        Random r = new Random();
        for (int i = 0; i < viewColumnList.size(); i++) {
            layoutBar(viewColumnList.get(i), null, r.nextInt((getHeight() >> 1) - 0) + 0, false, i);
        }

        setAlpha(0);
        animate().alpha(0.2f).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION_SHORT).start();
    }

    @Override
    public void startRecording() {
        animate().alpha(1f).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION_SHORT).start();
    }

    @Override
    public void stopRecording() {
        activate();
    }

    @Override
    public void deactivate() {
        for (int i = 0; i < viewColumnList.size(); i++) {
            layoutBar(viewColumnList.get(i), null, 0, false, i);
        }
        animate().alpha(0.2f).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION_ULTRA_SHORT).start();
    }
}
