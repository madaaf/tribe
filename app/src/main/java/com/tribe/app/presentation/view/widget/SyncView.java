package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.f2prateek.rx.preferences.Preference;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 12/06/2016.
 */
public class SyncView extends FrameLayout {

  @IntDef({ FB, ADDRESSBOOK }) public @interface SyncViewType {
  }

  public static final int FB = 0;
  public static final int ADDRESSBOOK = 1;

  @Inject ScreenUtils screenUtils;

  @Inject @AddressBook Preference<Boolean> addressBook;

  @BindView(R.id.viewInd) View viewInd;

  @BindView(R.id.imgIcon) ImageView imgIcon;

  @BindView(R.id.viewSynced) ImageView viewSynced;

  @BindView(R.id.progressView) CircularProgressView progressView;

  // VARIABLES
  private int iconId;
  private int type;
  private int backgroundId;
  private boolean active;

  // RESOURCES

  // OBSERVABLES
  private final PublishSubject<SyncView> onClick = PublishSubject.create();

  public SyncView(Context context) {
    this(context, null);
    init(context, null);
  }

  public SyncView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_sync, this, true);
    ButterKnife.bind(this);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SyncView);

    setType(a.getInt(R.styleable.SyncView_syncType, FB));

    if (a.hasValue(R.styleable.SyncView_syncIcon)) {
      setIcon(a.getResourceId(R.styleable.SyncView_syncIcon, 0));
    }

    if (a.hasValue(R.styleable.SyncView_syncBackground)) {
      setBackgroundId(a.getResourceId(R.styleable.SyncView_syncBackground, 0));
    }

    setOnClickListener((v) -> {
      showLoading();
      onClick.onNext(this);
    });

    updateSync();

    a.recycle();
  }

  public void setType(int type) {
    this.type = type;
  }

  public void setIcon(@DrawableRes int iconId) {
    this.iconId = iconId;
    imgIcon.setImageResource(iconId);
  }

  public void setBackgroundId(@DrawableRes int backgroundId) {
    this.backgroundId = backgroundId;
    imgIcon.setBackgroundResource(backgroundId);
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active, boolean animated) {
    this.active = active;

    if (progressView.getVisibility() == View.VISIBLE) hideLoading();

    refactorSync(animated);
  }

  private void refactorSync(boolean animated) {
    if (active) {
      TransitionDrawable bgIcon = (TransitionDrawable) imgIcon.getBackground();
      bgIcon.startTransition(animated ? 200 : 0);

      TransitionDrawable bgSynced = (TransitionDrawable) viewInd.getBackground();
      bgSynced.startTransition(animated ? 200 : 0);

      viewSynced.setImageResource(R.drawable.picto_synced_small);
    } else {
      TransitionDrawable bgIcon = (TransitionDrawable) imgIcon.getBackground();
      bgIcon.reverseTransition(animated ? 200 : 0);

      TransitionDrawable bgSynced = (TransitionDrawable) viewInd.getBackground();
      bgSynced.reverseTransition(animated ? 200 : 0);

      viewSynced.setImageResource(R.drawable.picto_exclamation);
    }
  }

  public void updateSync() {
    if (type == FB && FacebookUtils.isLoggedIn()) {
      setActive(true, false);
    } else if (type == ADDRESSBOOK && addressBook.get()) {
      setActive(true, false);
    }
  }

  private void showLoading() {
    viewSynced.setVisibility(View.GONE);
    progressView.setVisibility(View.VISIBLE);
  }

  private void hideLoading() {
    progressView.setVisibility(View.GONE);
    viewSynced.setVisibility(View.VISIBLE);
  }

  // OBSERVABLES
  public Observable<SyncView> onClick() {
    return onClick;
  }
}
