package com.tribe.tribelivesdk.rs.lut3d;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import com.tribe.tribelivesdk.R;
import com.tribe.tribelivesdk.rs.RSCompute;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 17/05/2017.
 */

public class LUT3DManager {

  // VARIABLES
  private List<LUT3DFilter> filterList;
  private int current = 1;
  private RenderScript renderScript;
  private RSCompute rsCompute;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public LUT3DManager(Context context) {
    renderScript = RenderScript.create(context);
    rsCompute = new RSCompute(context, renderScript);
    filterList = new ArrayList<>();
    filterList.add(new LUT3DFilter(context, renderScript, rsCompute, LUT3DFilter.LUT3D_NONE, -1));
    filterList.add(new LUT3DFilter(context, renderScript, rsCompute, LUT3DFilter.LUT3D_DEFAULT,
        R.drawable.lut_food));
    filterList.add(new LUT3DFilter(context, renderScript, rsCompute, LUT3DFilter.LUT3D_TAN,
        R.drawable.lut_settled));
    filterList.add(new LUT3DFilter(context, renderScript, rsCompute, LUT3DFilter.LUT3D_BW,
        R.drawable.lut_litho));
    filterList.add(new LUT3DFilter(context, renderScript, rsCompute, LUT3DFilter.LUT3D_HIPSTER,
        R.drawable.lut_pola669));
  }

  public void initFrameSizeChangeObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> {
      for (LUT3DFilter filter : filterList) {
        filter.onFrameSizeChange(frame);
      }
    }));
  }

  public LUT3DFilter getFilter() {
    return filterList.get(current);
  }

  public void switchFilter() {
    current = ++current % filterList.size();
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }
}
