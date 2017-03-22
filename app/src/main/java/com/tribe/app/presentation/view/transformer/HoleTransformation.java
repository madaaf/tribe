package com.tribe.app.presentation.view.transformer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapResource;

public class HoleTransformation implements Transformation<Bitmap> {

  public static final float RATIO = 0.16f;

  private BitmapPool bitmapPool;
  private Paint transparentPaint;
  private Context context;

  public HoleTransformation(Context context) {
    this(Glide.get(context).getBitmapPool());
    this.context = context;
    transparentPaint = new Paint();
    transparentPaint.setAntiAlias(true);
    transparentPaint.setDither(false);
    transparentPaint.setColor(ContextCompat.getColor(context, android.R.color.transparent));
    transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
  }

  public HoleTransformation(BitmapPool pool) {
    bitmapPool = pool;
  }

  @Override
  public Resource<Bitmap> transform(Resource<Bitmap> resource, int outWidth, int outHeight) {
    Bitmap source = resource.get();
    int size = Math.min(source.getWidth(), source.getHeight());

    Bitmap bitmap = bitmapPool.get(size, size, Bitmap.Config.ARGB_8888);
    if (bitmap == null) {
      bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    }

    Canvas canvas = new Canvas(bitmap);
    Paint paint = new Paint();
    canvas.drawBitmap(source, 0, 0, paint);
    float r = size * RATIO;
    canvas.drawCircle(size - r, size - r, r, transparentPaint);

    return BitmapResource.obtain(bitmap, bitmapPool);
  }

  @Override public String getId() {
    return "HoleTransform()+12";
  }
}
