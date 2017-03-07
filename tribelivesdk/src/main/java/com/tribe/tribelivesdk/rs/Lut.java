package com.tribe.tribelivesdk.rs;

import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicLUT;
import com.tribe.tribelivesdk.rs.base.ConvertingTool;
import com.tribe.tribelivesdk.rs.base.RSToolboxContext;
import com.tribe.tribelivesdk.rs.params.LutParams;

public class Lut {

  private static ConvertingTool.BaseToolScript lutToolScript =
      new ConvertingTool.BaseToolScript<LutParams>() {
        @Override public void runScript(RSToolboxContext rsToolboxContext, Allocation aout,
            LutParams scriptParams) {
          ScriptIntrinsicLUT lutScript =
              ScriptIntrinsicLUT.create(rsToolboxContext.rs, rsToolboxContext.ain.getElement());
          scriptParams.setLutParams(lutScript);
          lutScript.forEach(rsToolboxContext.ain, aout);
        }
      };

  /**
   * Apply a RGBA Look-Up table to a Bitmap image.
   *
   * @param rgbaLut the RGBA look-up table to apply.
   */
  public static Bitmap applyLut(RenderScript rs, Bitmap inputBitmap, LutParams.RGBALut rgbaLut) {
    ConvertingTool<LutParams> lutTool = new ConvertingTool<>(lutToolScript);
    return lutTool.doComputation(rs, inputBitmap, new LutParams(rgbaLut));
  }

  /**
   * Apply a RGBA Look-Up table to a NV21 image.
   *
   * @param nv21ByteArray the original NV21 byte array.
   * @param width the original NV21 image width.
   * @param height the original NV21 image height.
   * @param rgbaLut the RGBA look-up table to apply
   */
  public static byte[] applyLut(RenderScript rs, byte[] nv21ByteArray, int width, int height,
      LutParams.RGBALut rgbaLut) {
    ConvertingTool<LutParams> lutTool = new ConvertingTool<>(lutToolScript);
    return lutTool.doComputation(rs, nv21ByteArray, width, height, new LutParams(rgbaLut));
  }
}
