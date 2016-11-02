package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;

import com.tribe.app.presentation.view.tutorial.Overlay;

/**
 * Created by horatiothomas on 10/17/16.
 */
public class CircleView extends View {

    // VARIABLES
    private Rect rect;
    private float radius;
    private Paint paint;

    // ERASER
    private Bitmap eraserBitmap;
    private Canvas eraserCanvas;
    private Paint transparentPaint;
    private Paint eraser;
    private Overlay overlay;
    private int[] pos;
    private View viewHole;

    public CircleView(Context context) {
        super(context);
    }

    public CircleView withMask(Overlay overlay, int[] pos, Point size, View viewHole) {
        this.overlay = overlay;
        this.pos = pos;
        this.viewHole = viewHole;

        eraserBitmap = Bitmap.createBitmap(size.x, size.y, Bitmap.Config.ARGB_8888);
        eraserCanvas = new Canvas(eraserBitmap);

        paint = new Paint();
        paint.setColor(0xcc000000);
        transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);

        return this;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (overlay != null) {
            eraserBitmap.eraseColor(Color.TRANSPARENT);
            eraserCanvas.drawColor(Color.WHITE);

            if (overlay.style == Overlay.RECTANGLE) {
                RectF rect = new RectF(pos[0] - overlay.holePadding + overlay.holeOffsetLeft,
                        pos[1] - overlay.holePadding + overlay.holeOffsetTop,
                        pos[0] + viewHole.getWidth() + overlay.holePadding + overlay.holeOffsetLeft,
                        pos[1] + viewHole.getHeight() + overlay.holePadding + overlay.holeOffsetTop);

                eraserCanvas.drawRoundRect(rect, overlay.holeCornerRadius, overlay.holeCornerRadius, eraser);

            } else {
                int holeRadius = overlay.holeRadius != Overlay.NOT_SET ? overlay.holeRadius : (int) radius;
                eraserCanvas.drawCircle(
                        pos[0] + viewHole.getWidth() / 2 + overlay.holeOffsetLeft,
                        pos[1] + viewHole.getHeight() / 2 + overlay.holeOffsetTop,
                        holeRadius, eraser);
            }

            canvas.drawBitmap(eraserBitmap, 0, 0, null);
        }

        //if (rect != null) {
        //    canvas.drawCircle(getWidth() / 2, getHeight() / 2, (int) (getWidth() / 2), paint);
        //}
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
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
