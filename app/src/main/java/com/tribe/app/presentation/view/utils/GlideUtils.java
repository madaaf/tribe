package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.transformer.HoleTransformation;
import java.io.File;
import java.util.Random;

/**
 * Created by tiago on 08/02/2017.
 */

public class GlideUtils {

  public static class Builder {

    private final Context context;
    private String url;
    private Bitmap bitmap;
    private int resourceId = 0;
    private File file;
    private int size = 0;
    private ImageView target;
    private boolean hasHole = false;
    private boolean hasPlaceholder = true;
    private boolean rounded = true;

    public Builder(Context context) {
      this.context = context;
    }

    public Builder url(String url) {
      this.url = url;
      return this;
    }

    public Builder bitmap(Bitmap bitmap) {
      this.bitmap = bitmap;
      return this;
    }

    public Builder resourceId(int resourceId) {
      this.resourceId = resourceId;
      return this;
    }

    public Builder file(File file) {
      this.file = file;
      return this;
    }

    public Builder size(int size) {
      this.size = size;
      return this;
    }

    public Builder target(ImageView target) {
      this.target = target;
      return this;
    }

    public Builder hasHole(boolean hasHole) {
      this.hasHole = hasHole;
      return this;
    }

    public Builder hasPlaceholder(boolean hasPlaceholder) {
      this.hasPlaceholder = hasPlaceholder;
      return this;
    }

    public Builder rounded(boolean rounded) {
      this.rounded = rounded;
      return this;
    }

    public void load() {
      DrawableRequestBuilder drawableRequestBuilder;

      Random random = new Random();
      int r = random.nextInt(6 - 1) + 1; // From 1 to 6
      int randomPlaceholder = context.getResources()
          .getIdentifier("picto_placeholder_" + r, "drawable", context.getPackageName());

      if (bitmap != null) {
        drawableRequestBuilder = Glide.with(context).load(bitmap);
      } else if (resourceId != 0) {
        drawableRequestBuilder = Glide.with(context).load(resourceId);
      } else if (file != null) {
        drawableRequestBuilder = Glide.with(context)
            .load(file)
            .signature(new StringSignature(String.valueOf(file.lastModified())));
      } else if (StringUtils.isEmpty(url)) {
        // drawableRequestBuilder = Glide.with(context).load(randomPlaceholder);
        drawableRequestBuilder = Glide.with(context).load("").placeholder(randomPlaceholder);
      } else {
        drawableRequestBuilder = Glide.with(context).load(url).error(randomPlaceholder);
      }

      if (hasPlaceholder) {
        drawableRequestBuilder = drawableRequestBuilder.thumbnail(0.25f)
            .error(randomPlaceholder)
            .placeholder(randomPlaceholder);
      }

      if (size != 0) {
        drawableRequestBuilder.override(size, size);
      }

      if (rounded) {
        if (hasHole) {
          drawableRequestBuilder.bitmapTransform(new CropCircleTransformation(context),
              new HoleTransformation(context));
        } else {
          drawableRequestBuilder.bitmapTransform(new CropCircleTransformation(context));
        }
      }

      drawableRequestBuilder.crossFade().diskCacheStrategy(DiskCacheStrategy.RESULT).into(target);
    }
  }
}
