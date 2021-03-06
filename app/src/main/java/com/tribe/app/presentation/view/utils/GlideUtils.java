package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.StringSignature;
import com.tribe.app.R;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
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
      int randomPlaceholder = R.drawable.picto_avatar_placeholder;

      if (bitmap != null) {
        drawableRequestBuilder = Glide.with(context).load(bitmap);
      } else if (resourceId != 0) {
        drawableRequestBuilder = Glide.with(context).load(resourceId);
      } else if (file != null) {
        drawableRequestBuilder = Glide.with(context)
            .load(file)
            .signature(new StringSignature(String.valueOf(file.lastModified())));
      } else if (StringUtils.isEmpty(url)) {
        drawableRequestBuilder = Glide.with(context).load(randomPlaceholder);
        //drawableRequestBuilder = Glide.with(context).load("").placeholder(randomPlaceholder);
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
        drawableRequestBuilder.bitmapTransform(new CropCircleTransformation(context));
      }

      drawableRequestBuilder.crossFade().diskCacheStrategy(DiskCacheStrategy.RESULT).into(target);
    }
  }

  public static class GameImageBuilder {

    private final Context context;
    private final ScreenUtils screenUtils;
    private String url;
    private ImageView target;
    private boolean hasPlaceholder = true;
    private boolean rounded = true;
    private boolean hasBorder = false;

    public GameImageBuilder(Context context, ScreenUtils screenUtils) {
      this.context = context;
      this.screenUtils = screenUtils;
    }

    public GameImageBuilder url(String url) {
      this.url = url;
      return this;
    }

    public GameImageBuilder target(ImageView target) {
      this.target = target;
      return this;
    }

    public GameImageBuilder hasPlaceholder(boolean hasPlaceholder) {
      this.hasPlaceholder = hasPlaceholder;
      return this;
    }

    public GameImageBuilder rounded(boolean rounded) {
      this.rounded = rounded;
      return this;
    }

    public GameImageBuilder hasBorder(boolean hasBorder) {
      this.hasBorder = hasBorder;
      return this;
    }

    public void load() {
      DrawableRequestBuilder drawableRequestBuilder;

      int randomPlaceholder = R.drawable.picto_avatar_placeholder;

      if (StringUtils.isEmpty(url)) {
        drawableRequestBuilder = Glide.with(context).load(randomPlaceholder);
      } else {
        drawableRequestBuilder = Glide.with(context).load(url).error(randomPlaceholder);
      }

      if (hasPlaceholder) {
        drawableRequestBuilder = drawableRequestBuilder.thumbnail(0.25f)
            .error(randomPlaceholder)
            .placeholder(randomPlaceholder);
      }

      if (rounded && !hasBorder) {
        drawableRequestBuilder.bitmapTransform(new CropCircleTransformation(context));
      } else if (rounded && hasBorder) {
        drawableRequestBuilder.bitmapTransform(new CropCircleTransformation(context),
            new RoundedCornersTransformation(context, screenUtils.dpToPx(400),
                screenUtils.dpToPx(3), "#FFFFFF", screenUtils.dpToPx(2)));
      } else if (!rounded && hasBorder) {
        drawableRequestBuilder.bitmapTransform(
            new RoundedCornersTransformation(context, screenUtils.dpToPx(400),
                screenUtils.dpToPx(3), "#FFFFFF", screenUtils.dpToPx(2)));
      }

      drawableRequestBuilder.override(screenUtils.dpToPx(100), screenUtils.dpToPx(100))
          .crossFade()
          .diskCacheStrategy(DiskCacheStrategy.RESULT)
          .into(target);
    }
  }

  public static class TrophyImageBuilder {

    private final Context context;
    private final ScreenUtils screenUtils;
    private int drawableRes;
    private ImageView target;
    private CardView cardView;
    private boolean hasBorder = false;

    public TrophyImageBuilder(Context context, ScreenUtils screenUtils) {
      this.context = context;
      this.screenUtils = screenUtils;
    }

    public TrophyImageBuilder target(ImageView target) {
      this.target = target;
      return this;
    }

    public TrophyImageBuilder cardView(CardView cardView) {
      this.cardView = cardView;
      return this;
    }

    public TrophyImageBuilder drawableRes(int drawableRes) {
      this.drawableRes = drawableRes;
      return this;
    }

    public TrophyImageBuilder hasBorder(boolean hasBorder) {
      this.hasBorder = hasBorder;
      return this;
    }

    public void load() {
      if (cardView != null) {
        cardView.getViewTreeObserver()
            .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
              @Override public void onGlobalLayout() {
                cardView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                cardView.setRadius(getRadius(cardView, screenUtils));
              }
            });
      }

      target.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
              target.getViewTreeObserver().removeOnGlobalLayoutListener(this);

              DrawableRequestBuilder drawableRequestBuilder = Glide.with(context).load(drawableRes);

              if (hasBorder) {
                drawableRequestBuilder.bitmapTransform(
                    new RoundedCornersTransformation(context, getRadius(target, screenUtils),
                        screenUtils.dpToPx(3), "#FFFFFF", screenUtils.dpToPx(4)));
              }

              drawableRequestBuilder.diskCacheStrategy(DiskCacheStrategy.RESULT).into(target);
            }
          });
    }
  }

  private static int getRadius(View view, ScreenUtils screenUtils) {
    return (int) (((float) view.getMeasuredWidth() / (float) 4) / (float) 1.61803398874989484820);
  }
}
