package com.tribe.tribelivesdk.rs.lut3d;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import com.tribe.tribelivesdk.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tiago on 17/05/2017.
 */

public class LUT3DFilterWrapper {

  private List<LUT3DFilter> filterList;
  private int current = 3;

  public LUT3DFilterWrapper(Context context, RenderScript renderScript) {
    filterList = new ArrayList<>();
    filterList.add(new LUT3DFilter(context, renderScript, LUT3DFilter.LUT3D_NONE, -1));
    filterList.add(
        new LUT3DFilter(context, renderScript, LUT3DFilter.LUT3D_DEFAULT, R.drawable.lut_food));
    filterList.add(
        new LUT3DFilter(context, renderScript, LUT3DFilter.LUT3D_TAN, R.drawable.lut_settled));
    filterList.add(
        new LUT3DFilter(context, renderScript, LUT3DFilter.LUT3D_BW, R.drawable.lut_litho));
    filterList.add(
        new LUT3DFilter(context, renderScript, LUT3DFilter.LUT3D_HIPSTER, R.drawable.lut_pola669));
  }

  public LUT3DFilter getFilter() {
    return filterList.get(current);
  }

  public void switchFilter() {
    current = ++current % filterList.size();
  }
}
