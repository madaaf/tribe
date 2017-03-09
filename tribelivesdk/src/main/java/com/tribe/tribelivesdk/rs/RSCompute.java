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

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicColorMatrix;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;
import com.tribe.tribelivesdk.scripts.ScriptC_main;
import timber.log.Timber;

/**
 * Created by tiago on 03/08/2017.
 */
public class RSCompute {

  RenderScript renderScript;

  // Script vars
  ScriptIntrinsicYuvToRGB scriptIntrinsicYuvToRGB;
  ScriptIntrinsicColorMatrix scriptColorMatrixRGBToYuv;
  ScriptC_main scriptCMain; // Our custom script

  // Allocations
  Allocation inputAllocation; // Camera preview YUV allocation
  Allocation mainAllocation; // Output allocation

  // Temporary intermediate allocation, used to store YUV to RGBA conversion output and
  // used as our custom script input
  Allocation allocationYUV;
  Allocation rgbToYuvAllocation;
  Allocation allocationOut;
  Allocation allocationIn;

  ///////////////
  //create bitmap for output.
  Bitmap outputBitmap;
  ScriptIntrinsicColorMatrix greyScaleMatrix;

  // Funcs
  public byte[] compute(byte[] dataIn, int width, int height) {
    byte[] dataOut = new byte[dataIn.length];

    long stepStart = System.nanoTime();

    allocationYUV.copyFrom(dataIn);
    // Run the scripts
    //conversion to RGB
    scriptIntrinsicYuvToRGB.setInput(allocationYUV);
    scriptIntrinsicYuvToRGB.forEach(allocationIn);

    long stepYuvToRGB = System.nanoTime();
    Timber.d("RS time: YuvToRGB : " + (stepYuvToRGB - stepStart) / 1000000.0f + " ms");

    //apply Blur
    greyScaleMatrix.forEach(allocationIn, allocationOut);

    long stepGreyScale = System.nanoTime();
    Timber.d("RS time grey matrix: " + (stepGreyScale - stepYuvToRGB) / 1000000.0f + " ms");

    allocationOut.copyTo(outputBitmap);

    Nv21Image nv21Output = Nv21Image.bitmapToNV21(renderScript, outputBitmap);
    System.arraycopy(nv21Output.nv21ByteArray, 0, dataOut, 0, nv21Output.nv21ByteArray.length);

    long stepBitmap = System.nanoTime();
    Timber.d("RS time bitmap : " + (stepBitmap - stepGreyScale) / 1000000.0f + " ms");

    renderScript.finish();

    long stepEnd = System.nanoTime();
    Timber.d("RS time total : " + (stepEnd - stepStart) / 1000000.0f + " ms");

    return dataOut;
  }

  public RSCompute(RenderScript renderScript, int width, int height) {
    this.renderScript = renderScript;

    outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

    Type.Builder typeYUV = new Type.Builder(renderScript,
        Element.createPixel(renderScript, Element.DataType.UNSIGNED_8, Element.DataKind.PIXEL_YUV));
    typeYUV.setYuvFormat(ImageFormat.NV21);
    // allocation for the YUV input from the camera
    allocationYUV = Allocation.createTyped(renderScript, typeYUV.setX(width).setY(height).create(),
        android.renderscript.Allocation.USAGE_SCRIPT);

    //create the instance of the YUV2RGB (built-in) RS intrinsic
    scriptIntrinsicYuvToRGB =
        ScriptIntrinsicYuvToRGB.create(renderScript, Element.U8_4(renderScript));

   /* scriptCMain.set_aIn(mainAllocation);
    scriptCMain.set_sizeIn(new Int2(width, height)); // Tells the script camera preview size

    // Tells the script the resize ratio from input to output
    scriptCMain.set_scaleInv(1.0f);

    // Sets brightness threshold (0-255), so that gray values brighter than it will be turned to red.
    scriptCMain.set_threshold(100);

    scriptColorMatrixRGBToYuv =
        ScriptIntrinsicColorMatrix.create(renderScript, mainAllocation.getElement());
    scriptColorMatrixRGBToYuv.setRGBtoYUV();
    scriptColorMatrixRGBToYuv.setAdd(0.0f, 0.5f, 0.5f, 0.0f);*/

    // Create an allocation (which is memory abstraction in the Renderscript) that corresponds to the outputBitmap
    allocationOut = Allocation.createFromBitmap(renderScript, outputBitmap);
    // allocationIn and allocationBlur matches the allocationOut
    allocationIn = Allocation.createTyped(renderScript, allocationOut.getType(),
        android.renderscript.Allocation.USAGE_SCRIPT);
    greyScaleMatrix = ScriptIntrinsicColorMatrix.create(renderScript, allocationOut.getElement());
    greyScaleMatrix.setGreyscale();
  }
}
