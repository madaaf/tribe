package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import com.tribe.app.R;

public class GradientCircleView extends android.view.View {

  private Paint paint;
  private int width, height;

  public GradientCircleView(Context context) {
    super(context);
  }

  public GradientCircleView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public GradientCircleView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (paint == null) {
      paint = new Paint();
      paint.setColor(Color.BLACK);
      paint.setStrokeWidth(1);
      paint.setStyle(Paint.Style.FILL_AND_STROKE);
      paint.setShader(new RadialGradient(getWidth() / 2, getHeight() / 2, getHeight() / 2,
          getResources().getColor(R.color.black_opacity_70), Color.TRANSPARENT,
          Shader.TileMode.CLAMP));
      paint.setDither(true);
    }

    width = getWidth();
    height = getHeight();
    canvas.drawCircle(width / 2, height / 2, height / 3, paint);
  }
}