package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
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
  private List<FilterMask> filterList;
  private FilterMask current;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public FilterManager(Context context) {
    filterList = new ArrayList<>();
  }

  public void initFilters(List<FilterMask> filters) {
    filterList.clear();
    filterList.addAll(filters);

    current = filterList.get(0);
    current.setActivated(true);
  }

  public void setCurrentFilter(FilterMask filter) {
    if (filter == current) {
      this.current = null;
    } else {
      this.current = filter;
    }
  }

  public List<FilterMask> getFilterList() {
    return filterList;
  }

  public FilterMask getFilter() {
    return current;
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }
}
