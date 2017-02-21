package com.tribe.app.presentation.view.component.live;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.GlideUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.model.TribeGuest;
import javax.inject.Inject;

/**
 * Created by tiago on 01/22/17.
 */
public class LiveAudioView extends FrameLayout {

  @Inject ScreenUtils screenUtils;

  @Inject PaletteGrid paletteGrid;

  @BindView(R.id.imgAvatar) ImageView imgAvatar;

  // VARIABLES
  private Unbinder unbinder;
  private TribeGuest guest;

  // RESOURCES
  private int avatarSize;

  // OBSERVABLES

  public LiveAudioView(Context context) {
    super(context);
    init();
  }

  public LiveAudioView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    initDependencyInjector();
    initResources();

    LayoutInflater.from(getContext()).inflate(R.layout.view_live_audio, this);
    unbinder = ButterKnife.bind(this);

    setBackgroundColor(PaletteGrid.getRandomColorExcluding(Color.BLACK));
  }

  private void initResources() {
    avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_big);
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

  private void loadAvatar() {
    if (guest != null) GlideUtils.load(getContext(), guest.getPicture(), avatarSize, imgAvatar);
  }

  ////////////
  // PUBLIC //
  ////////////

  public void setGuest(TribeGuest tribeGuest) {
    this.guest = tribeGuest;
    loadAvatar();
  }
}
