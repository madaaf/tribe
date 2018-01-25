package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.model.TribeGuest;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 16/01/2018.
 */

public class BirdRush extends FrameLayout {

  private Context context;

  // VARIABLES
  private LayoutInflater inflater;
  private Unbinder unbinder;
  private int index;
  private boolean lost = false;
  private TribeGuest tribeGuest;

  @Inject ScreenUtils screenUtils;
  @Inject User currentUser;

  @BindView(R.id.bird) ImageView bird;
  @BindView(R.id.birdName) TextViewFont birdName;

  private String[] birdsColors = new String[] {
      "FBCF26", "FA7FD9", "BE9EFF", "42F4B2", "F85C02", "3DE9DA", "8B572A", "FFFFFF"
  };

  private Integer[] birdsImage = new Integer[] {
      R.drawable.game_bird1, R.drawable.game_bird2, R.drawable.game_bird3, R.drawable.game_bird4,
      R.drawable.game_bird5, R.drawable.game_bird6, R.drawable.game_bird7, R.drawable.game_bird8
  };

  private Integer[] birdsBackImage = new Integer[] {
      R.drawable.game_bird_bck, R.drawable.game_bird_bck_2, R.drawable.game_bird_bck_3,
      R.drawable.game_bird_bck_4, R.drawable.game_bird_bck_5, R.drawable.game_bird_bck_6,
      R.drawable.game_bird7, R.drawable.game_bird8,
  };

  public BirdRush(@NonNull Context context, int index, TribeGuest guest) {
    super(context);
    this.context = context;
    this.index = index;
    this.tribeGuest = guest;
    initView();
  }

  public boolean isLost() {
    return lost;
  }

  private void initView() {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_bird_item, this, true);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    bird.setImageResource(birdsImage[index]);
    birdName.setBackground(ContextCompat.getDrawable(context, birdsBackImage[index]));

    String name = (tribeGuest != null) ? tribeGuest.getDisplayName() : currentUser.getDisplayName();
    Timber.e("NEW BIRD " + tribeGuest.toString());
    birdName.setText(name);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }
}
