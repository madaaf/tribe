package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.tribe.app.R;

/**
 * Created by horatiothomas on 8/22/16.
 */
public class SemiCircleView extends View {
    public SemiCircleView(Context context) {
        super(context);
    }

    public SemiCircleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SemiCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public static final int NO_FRIENDS = 0;
    public static final int ONE_FRIEND = 120;
    public static final int TWO_FRIENDS = 240;
    public static final int THREE_FRIENDS = 360;

    private int currentFriends = NO_FRIENDS;

    public void setCurrentFriends(int currentFriends) {
        this.currentFriends = currentFriends;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = (float)getWidth();
        float height = (float)getHeight();
        float radius;

        if (width > height){
            radius = height/4;
        }else{
            radius = width/4;
        }

        Path path = new Path();
        path.addCircle(width/2,
                height/2, radius,
                Path.Direction.CW);

        Paint paint = new Paint();
        paint.setColor(ContextCompat.getColor(getContext(), R.color.blue_text));
        paint.setStrokeWidth(20);
        paint.setStyle(Paint.Style.FILL);

        float center_x, center_y;
        final RectF oval = new RectF();

        paint.setStyle(Paint.Style.STROKE);
        // TODO: fix location. Maybe use dps?
        oval.set(20,
                20,
                height-20,
                width-20);

        canvas.drawArc(oval, 270, currentFriends, false, paint);
    }
}
