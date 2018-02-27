package com.tribe.app.presentation.view.popup.view;

/**
 * Created by tiago on 26/02/2018.
 */

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.tribe.app.presentation.view.popup.listener.PopupListener;
import java.lang.ref.WeakReference;
import rx.Observable;
import rx.subjects.PublishSubject;

public abstract class PopupView extends FrameLayout {

  protected PopupListener popupListener;

  protected PublishSubject<Void> onDone = PublishSubject.create();

  public PopupView(@NonNull Context context) {
    super(context);
  }

  public PopupView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public void setPopupListener(PopupListener popupListener) {
    this.popupListener = popupListener;
  }

  public Observable<Void> onDone() {
    return onDone;
  }
}

