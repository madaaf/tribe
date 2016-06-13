package com.tribe.app.presentation.view.camera.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

/**
 *
 * Created by tiago on 2016/06/13.
 */
public class VisualizerView extends FrameLayout {

    @Inject
    ScreenUtils screenUtils;

    private int numColumns = 6;
    private int color;

    private float columnWidth;

    private Canvas canvas;
    private Bitmap canvasBitmap;
    private Rect rect = new Rect();
    private Paint paint = new Paint();

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    private void init(Context context, AttributeSet attrs) {
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
        paint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        rect.set(0, 0, getWidth(), getHeight());

        if (canvasBitmap == null) {
            canvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }

        if (canvas == null) {
            canvas = new Canvas(canvasBitmap);
        }

        columnWidth = (float) getWidth() / (float) numColumns;

        canvas.drawBitmap(canvasBitmap, new Matrix(), null);
    }

    public void receive(final int volume) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (canvas == null) {
                return;
            }

            if (volume == 0) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            }

            for (int i = 0; i < numColumns; i++) {
                float height = computeHeight(volume);
                float left = i * columnWidth;
                float right = (i + 1) * columnWidth;

                RectF rect = createRect(left, right, height);
                canvas.drawRect(rect, paint);
            }

            invalidate();
        });
    }

    private float computeHeight(int volume) {
        double randomVolume = Math.random() * volume + 1;
        return (getHeight() / 60f) * (float) randomVolume;
    }

    private RectF createRect(float left, float right, float height) {
        return new RectF(left, height, right, 0);
    }
}
