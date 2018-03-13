package com.tribe.tribelivesdk.filters.lut3d;

import android.content.Context;
import android.support.v8.renderscript.RenderScript;
import com.tribe.tribelivesdk.filters.Filter;
import com.tribe.tribelivesdk.filters.RSCompute;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 17/05/2017.
 */

public class FilterManager {

  private static FilterManager instance;

  public static FilterManager getInstance(Context context) {
    if (instance == null) {
      instance = new FilterManager(context);
    }

    return instance;
  }

  // VARIABLES
  private List<Filter> filterList;
  private Filter current;
  private Filter suspendedFilter;
  private RenderScript renderScript;
  private RSCompute rsCompute;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public FilterManager(Context context) {
    renderScript = RenderScript.create(context);
    rsCompute = new RSCompute(context, renderScript);
    filterList = new ArrayList<>();
  }

  public void initFilters(List<Filter> filters) {
    filterList.clear();
    filterList.addAll(filters);

    for (Filter filter : filterList) {
      if (filter instanceof LUT3DFilter) {
        LUT3DFilter lut3DFilter = (LUT3DFilter) filter;
        lut3DFilter.initWithFrameworks(renderScript, rsCompute);
      }
    }

    current = filterList.get(0);
    current.setActivated(true);
  }

  public void initFrameSizeChangeObs(Observable<Frame> obs) {
    subscriptions.add(obs.subscribe(frame -> {
      for (Filter filter : filterList) {
        if (filter instanceof LUT3DFilter) {
          LUT3DFilter lut3DFilter = (LUT3DFilter) filter;
          lut3DFilter.onFrameSizeChange(frame);
        }
      }
    }));
  }

  public void setCurrentFilter(Filter filter) {
    if (filter == current) {
      this.current = null;
    } else {
      this.current = filter;
    }
  }

  public void suspendFilter() {
    this.suspendedFilter = current;
    this.current = null;
  }

  public void resumeFilter() {
    this.current = suspendedFilter;
    this.suspendedFilter = null;
  }

  public List<Filter> getFilterList() {
    return filterList;
  }

  public Filter getFilter() {
    return current;
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }
}
