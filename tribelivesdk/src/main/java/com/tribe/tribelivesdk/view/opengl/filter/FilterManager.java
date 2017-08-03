package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import com.tribe.tribelivesdk.util.FileUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;
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
  private Context context;
  private List<FilterMask> filterList;
  private FilterMask current;
  private FilterMask previous;
  private ImageFilter baseFilter;
  private String basePath, maskAndGlassesPath;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<FilterMask> onFilterChange = PublishSubject.create();

  public FilterManager(Context context) {
    this.context = context;
    filterList = new ArrayList<>();
    baseFilter = new ImageFilter(context, ImageFilter.IMAGE_FILTER_NONE, "None", -1);
    basePath = context.getFilesDir().toString() + File.separator;
    maskAndGlassesPath = basePath + "maskAndGlasses" + File.separator;

    new Thread(() -> checkFiles()).start();
  }

  ///////////////
  //  PRIVATE  //
  ///////////////

  private void checkFiles() {
    File clipartDir = new File(maskAndGlassesPath);
    if (!clipartDir.exists()) {
      clipartDir.mkdirs();
    }

    FileUtils.copyFolderFromAssets(context, "ulsdata", maskAndGlassesPath);
  }

  ///////////////
  //  PUBLIC  //
  ///////////////

  public void initFilters(List<FilterMask> filters) {
    filterList.clear();
    filterList.addAll(filters);

    setToDefault();
  }

  public void setCurrentFilter(FilterMask filter) {
    previous = current;

    deactivate(previous);

    if (filter == current) {
      deactivate(current);
      current = null;
    } else {
      current = filter;
      activate(current);
    }

    onFilterChange.onNext(current);
  }

  private void deactivate(FilterMask mask) {
    if (mask != null) mask.setActivated(false);
  }

  private void activate(FilterMask mask) {
    if (mask != null) mask.setActivated(true);
  }

  public List<FilterMask> getFilterList() {
    return filterList;
  }

  public FilterMask getFilter() {
    return current;
  }

  public String getMaskAndGlassesPath() {
    return maskAndGlassesPath;
  }

  public void setToDefault() {
    setCurrentFilter(filterList.get(0));
  }

  public void setToPrevious() {
    setCurrentFilter(previous);
  }

  public ImageFilter getBaseFilter() {
    return baseFilter;
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<FilterMask> onFilterChange() {
    return onFilterChange;
  }
}
