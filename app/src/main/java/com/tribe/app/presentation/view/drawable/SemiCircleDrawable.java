package com.tribe.app.presentation.view.drawable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * Created by horatiothomas on 8/22/16.
 */
public class SemiCircleDrawable extends Drawable {


    private Paint paint;
    private RectF rectF;
    private int color;
    private float width;
    private float height;
    private int fill;


    public SemiCircleDrawable(int color, float width, float height, int fill) {
        this.color = color;
        this.width = width;
        this.height = height;
        this.fill = fill;
        paint = new Paint();
        paint.setColor(color);
        paint.setStrokeWidth(20); // TODO: change to dp
        paint.setStyle(Paint.Style.STROKE);
        rectF = new RectF();
    }


    @Override
    public void draw(Canvas canvas) {
        canvas.save();

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

        rectF.set(20,
                20,
                height-20,
                width-20);

        canvas.drawArc(rectF, 270, fill, false, paint);

    }

    @Override
    public void setAlpha(int i) {

    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }

    public void setColor(int color) {
        this.color = color;
        paint.setColor(color);
    }

}
