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

import android.graphics.ImageFormat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;
import com.tribe.tribelivesdk.scripts.ScriptC_rgb2yuv;
import timber.log.Timber;

/**
 * Created by tiago on 03/08/2017.
 */
public class RSCompute {

  RenderScript renderScript;

  // Script vars
  ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB;
  ScriptC_rgb2yuv scriptRGB2Yuv;

  // Allocations
  Allocation inAllocation; // Allocation for the frame in
  Allocation midAllocation; // Allocation for the YUV to RGBA conv
  Allocation outAllocation; // Allocation for the frame out

  // Funcs
  public byte[] compute(byte[] dataIn, int width, int height) {
    long stepStart = System.nanoTime();

    inAllocation.copyFrom(dataIn);
    // Run the scripts
    //conversion to RGB
    scriptIntrinsicYuvToRGB.forEach(midAllocation);

    long stepYuvToRGB = System.nanoTime();
    Timber.d("RS time: YuvToRGB : " + (stepYuvToRGB - stepStart) / 1000000.0f + " ms");

    byte[] dataOut = new byte[dataIn.length];

    Timber.d("Created dataOut");

    //scriptRGB2Yuv.forEach_convert(midAllocation);

    //Timber.d("Script rgb2yuv done");

    //outAllocation.copyTo(dataOut);

    //Timber.d("copy to out allocation done");

    renderScript.finish();

    Timber.d("renderscript finish");

    long stepEnd = System.nanoTime();
    Timber.d("RS time total : " + (stepEnd - stepStart) / 1000000.0f + " ms");

    return dataOut;
  }

  public RSCompute(RenderScript renderScript, int width, int height) {
    this.renderScript = renderScript;

    Type.Builder yuvTypeBuilder = new Type.Builder(renderScript,
        Element.createPixel(renderScript, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
    yuvTypeBuilder.setYuvFormat(ImageFormat.NV21);

    // allocation for the YUV input from the camera
    inAllocation =
        Allocation.createTyped(renderScript, yuvTypeBuilder.setX(width).setY(height).create(),
            Allocation.USAGE_SCRIPT);

    // create the instance of the YUV2RGB (built-in) RS intrinsic
    scriptIntrinsicYuvToRGB =
        ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));

    scriptIntrinsicYuvToRGB.setInput(inAllocation);

    Type.Builder rgbaType =
        new Type.Builder(renderScript, Element.RGBA_8888(renderScript)).setX(width).setY(height);

    midAllocation =
        Allocation.createTyped(renderScript, rgbaType.create(), Allocation.USAGE_SCRIPT);

    outAllocation =
        Allocation.createTyped(renderScript, yuvTypeBuilder.setX(width).setY(height).create(),
            Allocation.USAGE_SCRIPT);

    scriptRGB2Yuv = new ScriptC_rgb2yuv(renderScript);
    scriptRGB2Yuv.set_gOut(outAllocation);
    scriptRGB2Yuv.set_width(width);
    scriptRGB2Yuv.set_height(height);
    scriptRGB2Yuv.set_frameSize(width * height);
  }
}
