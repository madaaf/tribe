package com.tribe.app.presentation.view.component.chat;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 09/25/17.
 */
public class ShortcutCompletionTokenView extends FrameLayout {

  @BindView(R.id.viewAvatar) AvatarView viewAvatar;

  @BindView(R.id.txtName) TextViewFont txtName;

  // VARIABLES
  private Unbinder unbinder;

  // RESOURCES
  private int avatarSize;
  private Shortcut shortcut;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ShortcutCompletionTokenView(Context context) {
    super(context);
    init();
  }

  public ShortcutCompletionTokenView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ShortcutCompletionTokenView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    subscriptions.clear();
    super.onDetachedFromWindow();
  }

  private void init() {
    initDependencyInjector();

    LayoutInflater.from(getContext()).inflate(R.layout.view_shortcut_completion_token, this);
    unbinder = ButterKnife.bind(this);

    setBackgroundResource(R.drawable.bg_shortcut_completion_badge);
    initResources();
    initUI();
  }

  private void initResources() {

  }

  private void initUI() {
    avatarSize =
        getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_shortcut_completion);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  /**
   * PUBLIC
   */

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;

    viewAvatar.load(shortcut);
    txtName.setText(shortcut.getDisplayName());
  }

  public Shortcut getShortcut() {
    return shortcut;
  }
}
