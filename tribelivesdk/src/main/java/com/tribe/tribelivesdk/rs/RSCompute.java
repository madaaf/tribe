/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 - Alberto Marchetti <alberto.marchetti@hydex11.net>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tribe.tribelivesdk.rs;

import android.content.Context;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicColorMatrix;
import android.support.v8.renderscript.ScriptIntrinsicLUT;
import android.support.v8.renderscript.Type;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilter;

/**
 * Created by tiago on 03/08/2017.
 */
public class RSCompute {

  private Context context;
  private RenderScript renderScript;

  private Type.Builder rgbaType;

  // Script vars
  private ScriptIntrinsicColorMatrix greyScaleMatrix;
  private ScriptIntrinsicLUT scriptIntrinsicLUT;

  // Allocations
  private Allocation inAllocation; // Allocation for the frame in
  private Allocation outAllocation; // Allocation for the frame out

  private int previousWidth, previousHeight;

  // Funcs
  public byte[] compute(byte[] dataIn, int width, int height, byte[] dataOut) {
    if (width != previousWidth) {
      previousWidth = width;
      previousHeight = height;
      updateAllocations();
    }

    inAllocation.copyFrom(dataIn);
    greyScaleMatrix.forEach(inAllocation, outAllocation);
    outAllocation.copyTo(dataOut);

    renderScript.finish();

    return dataOut;
  }

  public byte[] computeLUT(byte[] dataIn, int width, int height, byte[] dataOut) {
    if (width != previousWidth) {
      previousWidth = width;
      previousHeight = height;
      updateAllocations();
    }

    inAllocation.copyFrom(dataIn);

    scriptIntrinsicLUT.forEach(inAllocation, outAllocation);

    outAllocation.copyTo(dataOut);

    renderScript.finish();
    return dataOut;
  }

  public byte[] computeLUT3D(LUT3DFilter lut3DFilter, byte[] dataIn, int width, int height,
      byte[] dataOut) {
    if (width != previousWidth) {
      previousWidth = width;
      previousHeight = height;
      updateAllocations();
    }

    inAllocation.copyFrom(dataIn);

    lut3DFilter.getLutRenderScript().forEach(inAllocation, outAllocation);

    outAllocation.copyTo(dataOut);

    renderScript.finish();

    return dataOut;
  }

  public RSCompute(Context context, RenderScript renderScript, int width, int height) {
    this.context = context;
    this.renderScript = renderScript;

    previousWidth = width;
    previousHeight = height;

    rgbaType = new Type.Builder(renderScript, Element.U8_4(renderScript)).setX(previousWidth)
        .setY(previousHeight);

    // LUT
    scriptIntrinsicLUT = ScriptIntrinsicLUT.create(renderScript, Element.U8_4(renderScript));

    for (int ct = 0; ct < 256; ct++) {
      float f = ((float) ct) / 255.f;

      float r = f;
      if (r < 0.5f) {
        r = 4.0f * r * r * r;
      } else {
        r = 1.0f - r;
        r = 1.0f - (4.0f * r * r * r);
      }
      scriptIntrinsicLUT.setRed(ct, (int) (r * 255.f + 0.5f));

      float g = f;
      if (g < 0.5f) {
        g = 2.0f * g * g;
      } else {
        g = 1.0f - g;
        g = 1.0f - (2.0f * g * g);
      }
      scriptIntrinsicLUT.setGreen(ct, (int) (g * 255.f + 0.5f));

      float b = f * 0.5f + 0.25f;
      scriptIntrinsicLUT.setBlue(ct, (int) (b * 255.f + 0.5f));
    }

    updateAllocations();
  }

  private void updateAllocations() {
    rgbaType.setX(previousWidth).setY(previousHeight);

    inAllocation = Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SHARED);

    outAllocation =
        Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SHARED);

    greyScaleMatrix = ScriptIntrinsicColorMatrix.create(renderScript, outAllocation.getElement());
    greyScaleMatrix.setGreyscale();
  }
}
