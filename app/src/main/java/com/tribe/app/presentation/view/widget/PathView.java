package com.tribe.app.presentation.view.widget;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

public class PathView extends View {

    @Inject
    ScreenUtils screenUtils;

    // Variables
    private Path path;
    private Paint paint;
    private float length;
    private ObjectAnimator animator;

    // Dimens
    private int strokeWidth;
    private int timeToRecord;

    public PathView(Context context) {
        super(context);
        init(context, null);
    }

    public PathView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        strokeWidth = screenUtils.dpToPx(16);
        timeToRecord = context.getResources().getInteger(R.integer.time_record);

        setWillNotDraw(false);
    }

    public void start(int width, int height) {
        int x = 0, y = 0;
        paint = new Paint();
        paint.setColor(getResources().getColor(R.color.red));
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setAntiAlias(true);

        path = new Path();
        path.moveTo(width >> 1, y);
        path.lineTo(width, y);
        path.lineTo(width, height);
        path.lineTo(x, height);
        path.lineTo(x, y);
        path.lineTo(width >> 1, y);

        // Measure the path
        PathMeasure measure = new PathMeasure(path, false);
        length = measure.getLength();

        float[] intervals = new float[]{ length, length };

        animator = ObjectAnimator.ofFloat(PathView.this, "phase", 1.0f, 0.0f);
        animator.setDuration(timeToRecord);
        animator.setInterpolator(new LinearInterpolator());
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            animator.cancel();
        }
    }

    // Is called by animator object
    public void setPhase(float phase) {
        paint.setPathEffect(createPathEffect(length, phase, 0.0f));
        invalidate();
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        return new DashPathEffect(new float[] { pathLength, pathLength },
            Math.max(phase * pathLength, offset));
    }

    @Override
    public void onDraw(Canvas c) {
        if (path != null && paint != null) {
            c.drawPath(path, paint);
        }

        super.onDraw(c);
    }
}