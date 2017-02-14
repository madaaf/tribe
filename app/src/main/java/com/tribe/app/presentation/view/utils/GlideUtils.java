package com.tribe.app.presentation.view.utils;

import android.content.Context;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;

/**
 * Created by tiago on 08/02/2017.
 */

public class GlideUtils {

  public static void load(Context context, String url, int avatarSize, ImageView img) {
    Glide.with(context)
        .load(url)
        .thumbnail(0.25f)
        .error(R.drawable.picto_placeholder_avatar)
        .placeholder(R.drawable.picto_placeholder_avatar)
        .override(avatarSize, avatarSize)
        .bitmapTransform(new CropCircleTransformation(context))
        .crossFade()
        .into(img);
  }

  public static void load(Context context, int avatarSize, ImageView img) {
    Glide.with(context)
        .load(R.drawable.picto_placeholder_avatar)
        .thumbnail(0.25f)
        .override(avatarSize, avatarSize)
        .bitmapTransform(new CropCircleTransformation(context))
        .crossFade()
        .into(img);
  }
}
