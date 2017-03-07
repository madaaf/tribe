package com.tribe.tribelivesdk.rs.base;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;

public class RSToolboxContext {

  public final RenderScript rs;
  public final Allocation ain;
  public final Element element;

  private RSToolboxContext(RenderScript rs, Allocation ain, Element bitmapElement) {
    this.rs = rs;
    this.ain = ain;
    this.element = bitmapElement;
  }

  public static RSToolboxContext createFromBitmap(RenderScript rs, Bitmap bitmap) {
    Allocation ain = Allocation.createFromBitmap(rs, bitmap);
    Element bitmapElement = ain.getElement();

    return new RSToolboxContext(rs, ain, bitmapElement);
  }

  public static RSToolboxContext createFromByteArray(RenderScript rs, byte[] array, int width,
      int height) {

    ScriptIntrinsicYuvToRGB intrinsicYuvToRGB =
        ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));

    Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.U8(rs)).setX(array.length);
    Type yuvType = yuvTypeBuilder.create();
    Allocation yuvAllocation = Allocation.createTyped(rs, yuvType, Allocation.USAGE_SCRIPT);
    yuvAllocation.copyFrom(array);

    Type.Builder rgbTypeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs));
    rgbTypeBuilder.setX(width);
    rgbTypeBuilder.setY(height);
    Allocation allocationIn = Allocation.createTyped(rs, rgbTypeBuilder.create());

    intrinsicYuvToRGB.setInput(yuvAllocation);
    intrinsicYuvToRGB.forEach(allocationIn);

    // allocation for the YUV input from the camera
    return new RSToolboxContext(rs, allocationIn, allocationIn.getElement());
  }
}
