package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.tribe.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.functions.Action;
import rx.subjects.PublishSubject;

public class LiveNotificationActionView extends LinearLayout {

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  // RESOURCES
  private int minHeight;

  // VARIABLES
  private Unbinder unbinder;
  private Action action;
  private boolean last;

  // OBSERVABLES
  private PublishSubject<Action> onClick = PublishSubject.create();

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
    private Action action;
    private boolean last;

    public Builder(Context context, Action action) {
      this.context = context;
      this.action = action;
    }

    public Builder isLast(boolean isLast) {
      last = isLast;
      return this;
    }

    public LiveNotificationActionView build() {
      LiveNotificationActionView view = new LiveNotificationActionView(context, last);
      view.setAction(action);
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
    onClick.onNext(action);
  }

  public void setAction(Action action) {
    this.action = action;
    this.txtTitle.setText(action.getTitle());
  }

  ////////////////
  // OBSERVABLE //
  ////////////////

  public Observable<Action> onClick() {
    return onClick;
  }

  ///////////////
  //  ACTION   //
  ///////////////

  public static class Action {
    private String id;
    private String title;
    private Intent intent;

    Action(String id, String title, Intent intent) {
      this.id = id;
      this.title = title;
      this.intent = intent;
    }

    public Intent getIntent() {
      return intent;
    }

    public String getId() {
      return id;
    }

    public String getTitle() {
      return title;
    }
  }
}
