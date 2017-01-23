package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * Created by horatiothomas on 10/17/16.
 */
public class CircleView extends View {

    private Rect rect;
    private float radius;
    private Paint paint;

    public CircleView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (rect != null) {
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
        }
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    public Rect getRect() {
        return rect;
    }

    public void setRect(Rect rect) {
        this.rect = rect;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }
}
