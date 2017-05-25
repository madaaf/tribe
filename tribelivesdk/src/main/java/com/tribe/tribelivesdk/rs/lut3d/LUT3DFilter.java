package com.tribe.tribelivesdk.rs.lut3d;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;
import android.support.annotation.StringDef;
import android.support.v4.util.LruCache;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsic3DLUT;
import android.support.v8.renderscript.Type;
import com.tribe.tribelivesdk.rs.RSCompute;
import com.tribe.tribelivesdk.webrtc.Frame;

/**
 * Created by tiago on 17/05/2017.
 */

public class LUT3DFilter {

  private static final int RED_DIM = 64;
  private static final int GREEN_DIM = 64;
  private static final int BLUE_DIM = 64;

  @StringDef({ LUT3D_TAN, LUT3D_BW, LUT3D_HIPSTER, LUT3D_DEFAULT, LUT3D_NONE })
  public @interface LUT3DFilterType {
  }

  public static final String LUT3D_TAN = "LUT3D_TAN";
  public static final String LUT3D_BW = "LUT3D_BW";
  public static final String LUT3D_HIPSTER = "LUT3D_HIPSTER";
  public static final String LUT3D_DEFAULT = "LUT3D_DEFAULT";
  public static final String LUT3D_NONE = "LUT3D_NONE";

  private static final float MAX_MEMORY_PERCENTAGE = 0.15f;

  private static class Scripts {
    private ScriptIntrinsic3DLUT lutScript;

    private int getSize() {
      return lutScript == null ? 0 : 512 * 512 * 4;
    }
  }

  private static LruCache<LUT3DFilter, Scripts> scriptsCache;
  private static final int cacheSize;

  static {
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    cacheSize = (int) (maxMemory * MAX_MEMORY_PERCENTAGE);

    scriptsCache = new LruCache<LUT3DFilter, Scripts>(cacheSize) {
      @Override protected int sizeOf(LUT3DFilter key, @NonNull Scripts scripts) {
        return scripts.getSize();
      }
    };
  }

  private Context context;
  private RenderScript renderScript;
  private RSCompute rsCompute;
  private @LUT3DFilterType String id;
  private int resourceId;
  private Bitmap bitmapLUT;
  private Allocation lutAllocation;

  public LUT3DFilter(Context context, RenderScript renderScript, RSCompute rsCompute,
      @LUT3DFilterType String id, @DrawableRes @RawRes int resourceId) {
    this.context = context;
    this.renderScript = renderScript;
    this.rsCompute = rsCompute;
    this.id = id;
    this.resourceId = resourceId;
  }

  public @LUT3DFilterType String getId() {
    return id;
  }

  public Allocation getLut() {
    // LUT3D
    final BitmapFactory.Options opts = new BitmapFactory.Options();
    opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
    bitmapLUT = BitmapFactory.decodeResource(context.getResources(), resourceId, opts);

    final Type.Builder tb = new Type.Builder(renderScript, Element.U8_4(renderScript));
    tb.setX(RED_DIM);
    tb.setY(GREEN_DIM);
    tb.setZ(BLUE_DIM);

    lutAllocation = Allocation.createTyped(renderScript, tb.create());
    int w = bitmapLUT.getWidth();
    int h = bitmapLUT.getHeight();

    int[] pixels = new int[w * h];
    int[] lut = new int[w * h];
    bitmapLUT.getPixels(pixels, 0, w, 0, 0, w, h);
    int i = 0;

    for (int r = 0; r < RED_DIM; r++) {
      for (int g = 0; g < GREEN_DIM; g++) {
        for (int b = 0; b < BLUE_DIM; b++) {
          int blockX = b % 8;
          int blockY = b / 8;
          lut[i++] = pixels[(blockY * 64 + g) * 512 + (blockX * 64 + r)];
        }
      }
    }

    lutAllocation.copyFromUnchecked(lut);
    return lutAllocation;
  }

  private Scripts getScript() {
    Scripts scripts = scriptsCache.get(this);
    if (scripts == null) {
      scripts = new Scripts();
      scriptsCache.put(this, scripts);
    }

    return scripts;
  }

  public ScriptIntrinsic3DLUT getLutRenderScript() {
    Scripts scripts = getScript();
    ScriptIntrinsic3DLUT script = scripts.lutScript == null ? null : scripts.lutScript;
    if (script == null) {
      script = ScriptIntrinsic3DLUT.create(renderScript, Element.RGBA_8888(renderScript));
      script.setLUT(getLut());
      scripts.lutScript = script;
      scriptsCache.trimToSize(cacheSize);
    }

    return script;
  }

  public void onFrameSizeChange(Frame frame) {
    rsCompute.updateAllocations(frame.getWidth(), frame.getHeight());
  }

  public void apply(byte[] argb) {
    rsCompute.computeLUT3D(this, argb, argb);
  }
}
