package com.tribe.app.presentation.view.component.live.game.AliensAttack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Random;
import java.util.UUID;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/01/2017.
 */

public class GameAliensAttackAlienView extends FrameLayout {

  private static final String ID_KEY = "id";
  private static final String TYPE_KEY = "type";
  private static final String ROTATION_KEY = "rotation";
  private static final String SCALE_KEY = "scale";
  private static final String START_RATIO_KEY = "start";
  private static final String SPEED_KEY = "speed";

  private static final int DURATION = 300;

  @IntDef({ YELLOW, BLUE }) public @interface AlienType {
  }

  public static final int YELLOW = 0;
  public static final int BLUE = 1;

  @Inject ScreenUtils screenUtils;

  /**
   * VARIABLES
   */

  private String id;
  private ImageView imgAlien, imgAlienBG, imgAlienLost;
  private int alienType;
  private float startX, speed, scale, rotation;
  private boolean lost = false;

  /**
   * RESOURCES
   */

  /**
   * OBSERVABLES
   */

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameAliensAttackAlienView(@NonNull Context context, GameAliensAttackEngine.Level level) {
    super(context);
    this.alienType = level.getType();
    this.startX = level.startX();
    this.scale = level.scale();
    this.rotation = level.rotation();
    this.speed = level.speed();
    this.id = (UUID.randomUUID().toString());
    init();
  }

  public GameAliensAttackAlienView(@NonNull Context context, @NonNull JSONObject json) {
    super(context);

    try {
      this.alienType = json.getInt(TYPE_KEY);
      this.startX = Float.parseFloat(json.getString(START_RATIO_KEY));
      this.scale = Float.parseFloat(json.getString(SCALE_KEY));
      this.rotation = Float.parseFloat(json.getString(ROTATION_KEY));
      this.speed = Float.parseFloat(json.getString(SPEED_KEY));
      this.id = (UUID.randomUUID().toString());
    } catch (JSONException ex) {
      ex.printStackTrace();
    }

    init();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    dispose();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  private void init() {
    initDependencyInjector();
    initResources();
    initView();
  }

  private void initResources() {

  }

  private void initView() {
    int drawableAlienId, drawableAlienBGId;

    if (alienType == BLUE) {
      drawableAlienId = R.drawable.game_aliens_attack_alien_1;
      drawableAlienBGId = R.drawable.game_aliens_attack_alien_gradient_blue;
    } else {
      int alienIndex = new Random().nextInt(3 - 2) + 2;
      drawableAlienId =
          getResources().getIdentifier("game_aliens_attack_alien_" + alienIndex, "drawable",
              getContext().getPackageName());
      drawableAlienBGId = R.drawable.game_aliens_attack_alien_gradient_yellow;
    }

    imgAlienBG = new ImageView(getContext());
    FrameLayout.LayoutParams paramsAlienBG =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    paramsAlienBG.gravity = Gravity.CENTER;
    imgAlienBG.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    imgAlienBG.setImageResource(drawableAlienBGId);
    addView(imgAlienBG, paramsAlienBG);

    imgAlien = new ImageView(getContext());
    FrameLayout.LayoutParams paramsAlien =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    paramsAlien.gravity = Gravity.BOTTOM;
    paramsAlien.topMargin = screenUtils.dpToPx(50);
    imgAlien.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
    imgAlien.setImageResource(drawableAlienId);
    addView(imgAlien, paramsAlien);

    imgAlienLost = new ImageView(getContext());
    FrameLayout.LayoutParams paramsAlienLost =
        new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT);
    paramsAlienLost.gravity = Gravity.BOTTOM;
    imgAlienLost.setVisibility(View.GONE);
    imgAlienLost.setImageResource(R.drawable.game_aliens_attack_alien_black);
    addView(imgAlienLost, paramsAlien);

    imgAlien.setRotation(rotation);
    imgAlienLost.setRotation(rotation);
  }

  /**
   * PUBLIC
   */

  public float getStartX() {
    return startX;
  }

  public float getStartScale() {
    return scale;
  }

  public float getSpeed() {
    return speed;
  }

  public ImageView getAlienImageView() {
    return imgAlien;
  }

  public void animateKill() {
    AnimationUtils.fadeOut(imgAlienBG, DURATION);

    imgAlien.animate()
        .scaleY(0)
        .scaleX(0)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            animation.removeAllListeners();
            ViewGroup parent = ((ViewGroup) getParent());
            if (parent != null) parent.removeView(GameAliensAttackAlienView.this);
          }
        })
        .start();
  }

  public void lost() {
    imgAlien.setVisibility(View.GONE);
    imgAlienLost.setVisibility(View.VISIBLE);
    lost = true;
  }

  public boolean isLost() {
    return lost;
  }

  public JSONObject asJSON() {
    JSONObject alien = new JSONObject();
    JsonUtils.jsonPut(alien, ID_KEY, id);
    JsonUtils.jsonPut(alien, TYPE_KEY, alienType);
    JsonUtils.jsonPut(alien, ROTATION_KEY, rotation);
    JsonUtils.jsonPut(alien, SCALE_KEY, scale);
    JsonUtils.jsonPut(alien, START_RATIO_KEY, startX);
    JsonUtils.jsonPut(alien, SPEED_KEY, speed);
    return alien;
  }

  public void dispose() {
    animate().setDuration(0).setStartDelay(0).setListener(null).start();
    clearAnimation();
    subscriptions.clear();
  }

  /**
   * OBSERVABLES
   */

}
