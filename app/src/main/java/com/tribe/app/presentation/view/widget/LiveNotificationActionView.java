package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import rx.Observable;
import rx.subjects.PublishSubject;

public class LiveNotificationActionView extends LinearLayout {

  @BindView(R.id.txtTitle) TextViewFont txtTitle;

  // RESOURCES
  private int smallHeight, bigHeight, paddingEnd;

  // VARIABLES
  private Unbinder unbinder;
  private Action action;
  private boolean last;
  private int count;

  // OBSERVABLES
  private PublishSubject<Action> onClick = PublishSubject.create();

  private LiveNotificationActionView(LiveNotificationActionView.Builder builder) {
    super(builder.context);
    this.last = builder.last;
    this.count = builder.count;

    init(builder.context, null);

    setAction(builder.action);
  }

  public LiveNotificationActionView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void initResources() {
    smallHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_item_height_small);
    bigHeight = getResources().getDimensionPixelSize(R.dimen.live_notification_item_height_big);
    paddingEnd =
        getContext().getResources().getDimensionPixelOffset(R.dimen.horizontal_margin_small);
  }

  public static class Builder {

    private final Context context;
    private Action action;
    private boolean last;
    private int count;

    public Builder(Context context, Action action) {
      this.context = context;
      this.action = action;
    }

    public Builder count(int count) {
      this.count = count;
      return this;
    }

    public Builder isLast(boolean isLast) {
      last = isLast;
      return this;
    }

    public Context getContext() {
      return context;
    }

    public LiveNotificationActionView build() {
      return new LiveNotificationActionView(this);
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

    if (!last && count > 1) {
      txtTitle.setPadding(0, 0, paddingEnd, 0);
      txtTitle.setGravity(Gravity.BOTTOM | Gravity.END);
      txtTitle.requestLayout();
    }

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
    private String sessionId;
    private String userId;

    Action(String id, String title) {
      this.id = id;
      this.title = title;
    }

    Action(String id, String title, Intent intent) {
      this.id = id;
      this.title = title;
      this.intent = intent;
    }

    Action(String id, String title, String sessionId) {
      this.id = id;
      this.title = title;
      this.sessionId = sessionId;
    }

    public String getSessionId() {
      return sessionId;
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

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }
}
