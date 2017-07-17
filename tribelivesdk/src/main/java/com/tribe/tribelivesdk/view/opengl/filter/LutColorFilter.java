package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.view.opengl.utils.BitmapFactoryUtils;
import com.tribe.tribelivesdk.view.opengl.utils.ImgSdk;

import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameterf;
import static android.opengl.GLES20.glTexParameteri;

public class LutColorFilter extends ImageFilter {

  private final int[] textures = new int[1];

  @Nullable private Bitmap lutBitmap;
  private final int lutResourceId;

  @NonNull private final Paint intensityPaint;

  private final static String FILTER_UNIFORM_SAMPLER = "lutTexture";

  private boolean lutBitmapInOpenGlUse = false;

  private final static String FRAGMENT_SHADER_DUMMY = "precision highp float;\n" +
      " varying highp vec2 interp_tc;\n" +
      " uniform " +
      TARGET_PLACEHOLDER +
      " sTexture;\n" +
      " uniform sampler2D lutTexture; // lookup texture\n" +
      " \n" +
      " void main()\n" +
      " {\n" +
      "     highp vec4 textureColor = texture2D(sTexture, interp_tc);\n" +
      "     textureColor = clamp(textureColor, 0.0, 1.0);\n" +

      "     highp float blueColor = textureColor.b * 63.0;\n" +

      "     highp vec2 quad1;\n" +
      "     quad1.y = floor(floor(blueColor) / 8.0);\n" +
      "     quad1.x = floor(blueColor) - (quad1.y * 8.0);\n" +

      "     highp vec2 quad2;\n" +
      "     quad2.y = floor(ceil(blueColor) / 8.0);\n" +
      "     quad2.x = ceil(blueColor) - (quad2.y * 8.0);\n" +

      "     highp vec2 texPos1;\n" +
      "     texPos1.x = clamp((quad1.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n" +
      "     texPos1.y = clamp((quad1.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n" +

      "     highp vec2 texPos2;\n" +
      "     texPos2.x = clamp((quad2.x * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.r), 0.0, 1.0);\n" +
      "     texPos2.y = clamp((quad2.y * 0.125) + 0.5/512.0 + ((0.125 - 1.0/512.0) * textureColor.g), 0.0, 1.0);\n" +

      "     highp vec4 newColor1 = texture2D(lutTexture, texPos1);\n" +
      "     highp vec4 newColor2 = texture2D(lutTexture, texPos2);\n" +

      "     gl_FragColor = mix(newColor1, newColor2, fract(blueColor));\n" +
      " }";

  public LutColorFilter(Context context, @ImageFilterType String id, String name,
      @DrawableRes int thumbnailRes, @DrawableRes @RawRes int lutResource) {
    super(context, id, name, thumbnailRes, DEFAULT_VERTEX_SHADER, FRAGMENT_SHADER_DUMMY);

    intensityPaint = new Paint();
    intensityPaint.setAntiAlias(false);
    intensityPaint.setFilterBitmap(false);

    lutResourceId = lutResource;
  }

  @Override protected void onDraw(int x, int y, int width, int height) {
    GLES20.glViewport(x, y, width, height);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
    GLES20.glUniform1i(getHandle(FILTER_UNIFORM_SAMPLER), 3);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
  }

  @Override public void release() {
    super.release();

    if (!lutBitmapInOpenGlUse) {
      glBindTexture(GL_TEXTURE_2D, 0);
      glDeleteTextures(textures.length, textures, 0);
    }

    if (lutBitmap != null) {
      lutBitmapInOpenGlUse = false;
      lutBitmap.recycle();
      lutBitmap = null;
    }
  }

  /**
   * Return the Lut as Bitmap. Look at the imgly_lut_identity.png drawable to get a basic non
   * changing LUT.
   *
   * @return a lut to change Image Colors.
   */
  @Nullable public synchronized Bitmap getLutBitmap() {
    return BitmapFactoryUtils.decodeResource(context.getResources(), lutResourceId);
  }

  @Override protected synchronized void setup(final int texTarget) {
    super.setup(texTarget);

    glGenTextures(1, textures, 0);

    glBindTexture(GL_TEXTURE_2D, textures[0]);

    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    lutBitmapInOpenGlUse = true;
    GLUtils.texImage2D(GL_TEXTURE_2D, 0, getLutBitmap(), 0);
  }
}
