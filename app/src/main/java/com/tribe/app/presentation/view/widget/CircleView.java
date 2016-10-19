package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import java.util.Random;

/**
 * Created by horatiothomas on 10/17/16.
 */
public class CircleView extends View {

    private Rect mRect;
    private float mRadius;
    private Paint mPaint;

    public CircleView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mRect != null) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, (int) (getWidth() / 2), mPaint);
        }
    }

    public float getRadius() {
        return mRadius;
    }

    public void setRadius(float mRadius) {
        this.mRadius = mRadius;
    }

    public Rect getRect() {
        return mRect;
    }

    public void setRect(Rect mRect) {
        this.mRect = mRect;
    }

    public void setPaint(Paint paint) {
        this.mPaint = paint;
    }
}
