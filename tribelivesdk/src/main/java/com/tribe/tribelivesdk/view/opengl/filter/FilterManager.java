package com.tribe.tribelivesdk.view.opengl.filter;

import android.content.Context;
import android.os.Environment;
import com.tribe.tribelivesdk.util.FileUtils;
import java.io.File;
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
  private Context context;
  private List<FilterMask> filterList;
  private FilterMask current;
  private ImageFilter baseFilter;
  private String basePath, maskAndGlassesPath;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public FilterManager(Context context) {
    this.context = context;
    filterList = new ArrayList<>();
    baseFilter = new ImageFilter(context, ImageFilter.IMAGE_FILTER_NONE, "None", -1);
    basePath = Environment.getExternalStorageDirectory().toString() +
        File.separator +
        "ULSee" +
        File.separator;
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

  public String getMaskAndGlassesPath() {
    return maskAndGlassesPath;
  }

  public void setToDefault() {
    if (current != null) {
      current.setActivated(false);
      current.release();
    }

    current = filterList.get(3);
    current.setActivated(true);
  }

  public ImageFilter getBaseFilter() {
    return baseFilter;
  }

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
  }
}
