package com.tribe.app.presentation.view.camera.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 *
 * Created by tiago on 2016/06/13.
 */
public class VisualizerView extends LinearLayout {

    private static int THRESHOLD = 150;
    private static int DURATION_ULTRA_SHORT = 100;
    private static int DURATION_SHORT = 200;
    private static int DURATION_LONG = 500;

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.viewFirstColumn)
    View viewFirstColumn;

    @BindView(R.id.viewSecondColumn)
    View viewSecondColumn;

    @BindView(R.id.viewThirdColumn)
    View viewThirdColumn;

    @BindView(R.id.viewFourthColumn)
    View viewFourthColumn;

    @BindView(R.id.viewFifthColumn)
    View viewFifthColumn;

    @BindView(R.id.viewSixthColumn)
    View viewSixthColumn;

    // Variables
    private int lastValue = Integer.MAX_VALUE;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_visualizer, this, true);
        ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        setWillNotDraw(false);
    }

    public void receive(final double[]... toTransform) {
        new Handler(Looper.getMainLooper()).post(() -> {
            int max = Integer.MIN_VALUE;

            for (int i = 0; i < toTransform[0].length; i++) {
                int value = (int) (toTransform[0][i] * 20);
                if (value > max) max = value;
            }

            if (Math.abs(lastValue - max) > THRESHOLD) {
                layoutBar(viewThirdColumn, null, max, true);
                layoutBar(viewFourthColumn, null, max, true);
                layoutBar(viewSecondColumn, viewThirdColumn, 0, true);
                layoutBar(viewFifthColumn, viewFourthColumn, 0, true);
                layoutBar(viewFirstColumn, viewSecondColumn, 0, true);
                layoutBar(viewSixthColumn, viewFifthColumn, 0, true);
                lastValue = max;
            }
        });
    }

    private void layoutBar(View bar, View barFrom, int height, boolean hasReturn) {
        int newHeight = 0;
        ValueAnimator va = null;

        if (barFrom == null) newHeight = height;
        else newHeight = barFrom.getHeight();

        if (hasReturn) va = ValueAnimator.ofInt(bar.getHeight(), newHeight, 0);
        else va =  ValueAnimator.ofInt(bar.getHeight(), newHeight);

        va.setDuration(DURATION_LONG);
        va.setInterpolator(new DecelerateInterpolator());
        va.addUpdateListener(animation -> {
            Integer value = (Integer) animation.getAnimatedValue();
            bar.getLayoutParams().height = value.intValue();
            bar.requestLayout();
        });
        bar.clearAnimation();
        va.start();
    }

    public void activate() {
        Random r = new Random();
        layoutBar(viewFirstColumn, null, r.nextInt((getHeight() >> 1) - 0) + 0, false);
        layoutBar(viewSecondColumn, null, r.nextInt((getHeight() >> 1) - 0) + 0, false);
        layoutBar(viewThirdColumn, null, r.nextInt((getHeight() >> 1) - 0) + 0, false);
        layoutBar(viewFourthColumn, null, r.nextInt((getHeight() >> 1) - 0) + 0, false);
        layoutBar(viewFifthColumn, null, r.nextInt((getHeight() >> 1) - 0) + 0, false);
        layoutBar(viewSixthColumn, null, r.nextInt((getHeight() >> 1) - 0) + 0, false);
        setAlpha(0);
        animate().alpha(0.2f).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION_SHORT).start();
    }

    public void startRecording() {
        animate().alpha(1f).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION_SHORT).start();
    }

    public void stopRecording() {
        activate();
    }

    public void deactivate() {
        layoutBar(viewFirstColumn, null, 0, false);
        layoutBar(viewSecondColumn, null, 0, false);
        layoutBar(viewThirdColumn, null, 0, false);
        layoutBar(viewFourthColumn, null, 0, false);
        layoutBar(viewFifthColumn, null, 0, false);
        layoutBar(viewSixthColumn, null, 0, false);
        animate().alpha(0.2f).setInterpolator(new DecelerateInterpolator()).setDuration(DURATION_ULTRA_SHORT).start();
    }
}
