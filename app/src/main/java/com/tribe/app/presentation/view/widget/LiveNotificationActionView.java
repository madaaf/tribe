package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.tribe.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;

public class LiveNotificationActionView extends LinearLayout {

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  // RESOURCES
  private int minHeight;

  // VARIABLES
  private Unbinder unbinder;
  private String id;
  private boolean last;

  // OBSERVABLES
  private PublishSubject<String> onClick = PublishSubject.create();

  public LiveNotificationActionView(Context context, boolean isLast) {
    super(context);
    this.last = isLast;
    init(context, null);
  }

  public LiveNotificationActionView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void initResources() {
    minHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_item_height);
  }

  public static class Builder {

    private final Context context;
    private String title;
    private String id;
    private boolean last;

    public Builder(Context context, String id, String title) {
      this.context = context;
      this.id = id;
      this.title = title;
    }

    public Builder isLast(boolean isLast) {
      last = isLast;
      return this;
    }

    public LiveNotificationActionView build() {
      LiveNotificationActionView view = new LiveNotificationActionView(context, last);
      view.setTitle(title);
      view.setId(id);
      return view;
    }
  }

  private void init(Context context, AttributeSet attrs) {
    initResources();

    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(
        last ? R.layout.view_live_notification_action_last : R.layout.view_live_notification_action,
        this, true);

    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
    setClickable(false);
  }

  @OnClick(R.id.txtTitle) void click() {
    onClick.onNext(id);
  }

  public void setTitle(String title) {
    txtTitle.setText(title);
  }

  public void setId(String id) {
    this.id = id;
  }

  ////////////////
  // OBSERVABLE //
  ////////////////

  public Observable<String> onClick() {
    return onClick;
  }
}
