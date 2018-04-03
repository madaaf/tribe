package com.tribe.app.presentation.view.component.games;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.PokeTiming;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.preferences.PokeUserGame;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.notification.NotificationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TextViewRanking;
import com.tribe.app.presentation.view.widget.TextViewScore;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/09/2017.
 */
public class LeaderboardUserView extends ConstraintLayout {

  public static final int MAX_WAITING_TIME_SECONDS = 60 * 60;

  @Inject ScreenUtils screenUtils;

  @Inject User user;

  @Inject @PokeUserGame Preference<String> pokeUserGame;

  @Inject StateManager stateManager;

  @BindView(R.id.viewNewAvatar) NewAvatarView viewAvatar;
  @BindView(R.id.txtRanking) TextViewRanking txtRanking;
  @BindView(R.id.txtName) TextViewFont txtName;
  @BindView(R.id.txtScore) TextViewScore txtScore;
  @BindView(R.id.imgConnectTop) ImageView imgConnectTop;
  @BindView(R.id.imgConnectBottom) ImageView imgConnectBottom;
  @BindView(R.id.pokeEmoji) TextView pokeEmoji;

  // VARIABLES
  private Score score;
  private String emo;

  // DIMENS

  // BINDERS / SUBSCRIPTIONS
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Score> onClickPoke = PublishSubject.create();
  private PublishSubject<Score> onClick = PublishSubject.create();

  public LeaderboardUserView(Context context) {
    super(context);
  }

  public LeaderboardUserView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();

    LayoutInflater.from(getContext()).inflate(R.layout.view_game_leaderboard_user, this);
    unbinder = ButterKnife.bind(this);

    ApplicationComponent applicationComponent =
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
    applicationComponent.inject(this);
    screenUtils = applicationComponent.screenUtils();

    initResources();
    initUI();
    initSubscriptions();
  }

  public void dispose() {
    subscriptions.clear();
  }

  private void initUI() {

  }

  private void initResources() {

  }

  private void initSubscriptions() {

  }

  private static Integer randInt(int low, int high) {
    Random r = new Random();
    return r.nextInt(high - low) + low;
  }

  private Long getWaitingTime(Score score) {
    Long timeSecond = null;
    String id = score.getGame().getId() + "_" + score.getUser().getId();
    List<PokeTiming> testList2 =
        PreferencesUtils.getPlayloadPokeTimingList(pokeUserGame, MAX_WAITING_TIME_SECONDS);

    for (PokeTiming p : testList2) {
      if (p.getId().equals(id)) {
        long diff = System.currentTimeMillis() - p.getCreationDate();
        timeSecond = TimeUnit.MILLISECONDS.toSeconds(diff);
      }
    }
    return timeSecond;
  }

  ///////////////////////
  //      PUBLIC       //
  ///////////////////////

  public void initCell(Score score, int position) {
    if (score == null) return;
    this.score = score;
    int ranking = position + 4;
    score.setRanking(ranking);
    viewAvatar.load(score.getUser().getProfilePicture());

    txtRanking.setRanking(ranking);
    txtRanking.setTextColor(Color.parseColor("#" + score.getGame().getPrimary_color()));

    txtName.setText(score.getUser().getId().equals(user.getId()) ? getContext().getString(
        R.string.leaderboards_you) : score.getUser().getDisplayName());
    txtScore.setScore(score.getValue());

    setOnClickListener(v -> onClick.onNext(score));
  }

  public void initPoke(Score score) {
    this.score = score;

    pokeEmoji.setVisibility(View.VISIBLE);

    boolean isAbove = user.getScoreForGame(score.getGame().getId()) != null &&
        score.getRanking() > user.getScoreForGame(score.getGame().getId()).getRanking();

    if (score.getUser().getId().equals(user.getId())) {
      emo = EmojiParser.demojizedText(getContext().getString(R.string.poke_emoji_own));
    } else if (isAbove) {
      String s = getContext().getString(R.string.poke_emoji_above);
      List<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
      emo = EmojiParser.demojizedText(myList.get(randInt(0, myList.size() - 1)));
    } else {
      String s = getContext().getString(R.string.poke_emoji_below);
      List<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
      emo = EmojiParser.demojizedText(myList.get(randInt(0, myList.size() - 1)));
    }

    // IS WAITING
    if (getWaitingTime(score) != null) {
      score.setWaiting(true);
      pokeEmoji.setText(
          EmojiParser.demojizedText(getContext().getString(R.string.poke_emoji_disabled)));
    } else {
      score.setWaiting(false);
      pokeEmoji.setText(emo);
    }

    score.setEmoticon(emo);
    score.setAbove(isAbove);

    pokeEmoji.setOnClickListener(v -> {
      if (stateManager.shouldDisplay(StateManager.FIRST_POKE)) {
        NotificationUtils.displayPokeNotificationModel(getContext(), score);
        stateManager.addTutorialKey(StateManager.FIRST_POKE);
        pokeEmoji.setText(
            EmojiParser.demojizedText(getContext().getString(R.string.poke_emoji_disabled)));
      }
      score.setWaiting(true);
      score.setTextView(pokeEmoji);
      onClickPoke.onNext(score);
    });
  }

  public void manageListItems(int position, int itemsCount) {
    if (position == 0) {
      imgConnectTop.setVisibility(View.GONE);
    } else {
      imgConnectTop.setVisibility(View.VISIBLE);
    }

    if (position == itemsCount) {
      imgConnectBottom.setVisibility(View.GONE);
    } else {
      imgConnectBottom.setVisibility(View.VISIBLE);
    }

    if (position % 2 == 0) {
      setBackgroundResource(android.R.color.transparent);
    } else {
      setBackgroundResource(R.color.black_opacity_5);
    }
  }

  ///////////////////////
  //    ANIMATIONS     //
  ///////////////////////

  ///////////////////////
  //    OBSERVABLES    //
  ///////////////////////

  public Observable<Score> onClickPoke() {
    return onClickPoke;
  }

  public Observable<Score> onClick() {
    return onClick;
  }
}