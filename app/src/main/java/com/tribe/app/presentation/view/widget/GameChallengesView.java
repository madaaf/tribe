package com.tribe.app.presentation.view.widget;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengesView extends FrameLayout {

  @Inject User user;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private GameChallengeViewPagerAdapter adapter;
  private GameChallenge gameChallenge;
  private GameManager gameManager;

  @BindView(R.id.pager) ViewPager viewpager;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<GameChallenge> onNextChallenge = PublishSubject.create();

  public GameChallengesView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public GameChallengesView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  private void initView(Context context) {
    this.context = context;
    initDependencyInjector();
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_game_challenges, this, true);
    unbinder = ButterKnife.bind(this);
    gameManager = GameManager.getInstance(getContext());

    Timber.e("init GameChallengeViewPagerAdapter");
    adapter = new GameChallengeViewPagerAdapter(context, user);
    viewpager.setAdapter(adapter);

    viewpager.setOnTouchListener((v, event) -> true);
  }

  private static int pos = 0;
  private List<String> items = new ArrayList<>();
  private List<TribeGuest> guestList = new ArrayList<>();

  public void setGameChallenge(GameChallenge gameChallenge) {
    Timber.e("soef setGameChallenge");
    setVisibility(VISIBLE);
    this.gameChallenge = gameChallenge;
    items = gameChallenge.getNameList();
    guestList = gameChallenge.getGuestList();
    TribeGuest me =
        new TribeGuest(user.getId(), user.getDisplayName(), user.getProfilePicture(), false, false,
            null, false, user.getUsername());
    guestList.add(me);
    if (gameChallenge.isUserAction()) ok(null, null, gameChallenge.isUserAction());
  }

  public void setNextChallenge(String challenge, TribeGuest guestChallenged) {
    setVisibility(VISIBLE); // MAYBE call setGameChallenge
    new Handler().post(() -> {
      pos++;
      viewpager.setCurrentItem(pos, true);
      Timber.e("soef set next challenge " + pos);
    });
    ok(challenge, guestChallenged, true);
  }

  private void ok(String challenge, TribeGuest guestChallenged, boolean nextChallenge) {
    if (challenge != null && guestChallenged != null) {
      Timber.w(" CHALLANGE RECEIVED : " + challenge);
      Timber.w(
          "user challenged " + guestChallenged.getId() + " " + guestChallenged.getDisplayName());
      Timber.w(" me=" + user.getId());
      gameManager.setCurrentChallengerId(guestChallenged.getId());
      adapter.setChallenge(challenge, guestChallenged);
    } else {
      TribeGuest guest = null;
      if (items != null && !items.isEmpty()) {
        challenge = getRandom(items);
        if (!guestList.isEmpty()) {
          guest = getRandomGuest(guestList);
        }
      }
      gameChallenge.setCurrentChallengerId(guest.getId());
      gameChallenge.setCurrentChallenge(challenge);
      if (nextChallenge) onNextChallenge.onNext(gameChallenge);
      Timber.w("FIND RANDOM CHALLANGE : " + challenge);
      Timber.w(" user challenged " + guest.getId() + " " + guest.getDisplayName());
      Timber.w(" me=" + user.getId());
      gameManager.setCurrentChallengerId(guest.getId());
      adapter.setChallenge(challenge, guest);
    }
    adapter.notifyDataSetChanged();
  }

  private static String getRandom(List<String> array) {
    int rnd = new Random().nextInt(array.size());
    return array.get(rnd);
  }

  private static TribeGuest getRandomGuest(List<TribeGuest> array) {
    int rnd = new Random().nextInt(array.size());
    return array.get(rnd);
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

  public Observable<GameChallenge> onNextChallenge() {
    return onNextChallenge;
  }
}
