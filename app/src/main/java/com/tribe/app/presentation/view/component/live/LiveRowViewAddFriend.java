package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/16/18.
 */
public class LiveRowViewAddFriend extends FrameLayout {

  @BindView(R.id.viewBG) View viewBG;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES
  private int roundedCorners;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onClick = PublishSubject.create();

  public LiveRowViewAddFriend(Context context) {
    super(context);
    init();
  }

  public LiveRowViewAddFriend(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveRowViewAddFriend(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_row_live_add_friends, this);
    unbinder = ButterKnife.bind(this);

    GradientDrawable drawable = new GradientDrawable();
    drawable.setCornerRadius(roundedCorners);
    drawable.setColor(ContextCompat.getColor(getContext(), R.color.white_opacity_50));
    viewBG.setBackground(drawable);

    setClickable(true);
    setForeground(ContextCompat.getDrawable(getContext(),
        R.drawable.selectable_button_all_rounded_5_black_5));

    setOnClickListener(v -> onClick.onNext(null));
  }

  private void initResources() {
    roundedCorners =
        getContext().getResources().getDimensionPixelSize(R.dimen.avatar_live_rounded_corners);
  }

  ////////////
  // PUBLIC //
  ////////////

  public void dispose() {
    if (subscriptions != null) subscriptions.clear();
    if (unbinder != null) unbinder.unbind();
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onClick() {
    return onClick;
  }
}
