package com.tribe.app.presentation.view.adapter.delegate.leaderboard;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.f2prateek.rx.preferences.Preference;
import com.tribe.app.R;
import com.tribe.app.domain.entity.PokeTiming;
import com.tribe.app.domain.entity.Score;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.preferences.PokeUserGame;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.notification.NotificationUtils;
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

import static com.tribe.app.presentation.view.adapter.viewholder.LeaderboardDetailsAdapter.LEADERBOARD_ADDRESS;

/**
 * Created by tiago on 12/08/17.
 */
public class LeaderboardDetailsAdapterDelegate extends RxAdapterDelegate<List<Score>> {

  public static final int maxWaitingTimeSeconde = 60 * 60;
  private static final int DURATION = 300;
  private static final int TRANSLATION = 200;

  // RX SUBSCRIPTIONS / SUBJECTS
  @Inject User user;

  @Inject @PokeUserGame Preference<String> pokeUserGame;

  // VARIABLES
  protected Context context;
  protected LayoutInflater layoutInflater;

  protected PublishSubject<Score> onClickPoke = PublishSubject.create();
  protected PublishSubject<Score> onClick = PublishSubject.create();

  private StateManager stateManager;
  private String emo = null;

  public LeaderboardDetailsAdapterDelegate(Context context, StateManager stateManager) {
    this.context = context;
    this.stateManager = stateManager;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    //initDependencyInjector();
  }

  @Override public boolean isForViewType(@NonNull List<Score> items, int position) {
    if (items.get(position) instanceof Score) {
      Score score = items.get(position);
      return score.getId() != null && !score.getId().equals(Score.ID_PROGRESS) && !score.getId()
          .startsWith(LEADERBOARD_ADDRESS);
    }

    return false;
  }

  @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
    final LeaderboardUserViewHolder vh = new LeaderboardUserViewHolder(
        layoutInflater.inflate(R.layout.item_leaderboard_details, parent, false));
    return vh;
  }

  public static Integer randInt(int low, int high) {
    Random r = new Random();
    return r.nextInt(high - low) + low;
  }

  @Override public void onBindViewHolder(@NonNull List<Score> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    LeaderboardUserViewHolder vh = (LeaderboardUserViewHolder) holder;
    Score score = items.get(position);
    int ranking = position + 4;
    score.setRanking(ranking);
    vh.viewAvatar.load(score.getUser().getProfilePicture());

    vh.txtRanking.setRanking(ranking);
    vh.txtRanking.setTextColor(Color.parseColor("#" + score.getGame().getPrimary_color()));

    vh.txtName.setText(score.getUser().getDisplayName());
    vh.txtScore.setScore(score.getValue());

    boolean isAbove = user.getScoreForGame(score.getGame().getId()) != null
        && score.getRanking() > user.getScoreForGame(score.getGame().getId()).getRanking();

    if (score.getUser().getId().equals(user.getId())) {
      emo = EmojiParser.demojizedText(context.getString(R.string.poke_emoji_own));
    } else if (isAbove) {
      String s = context.getString(R.string.poke_emoji_above);
      List<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
      emo = EmojiParser.demojizedText(myList.get(randInt(0, myList.size() - 1)));
    } else {
      String s = context.getString(R.string.poke_emoji_below);
      List<String> myList = new ArrayList<>(Arrays.asList(s.split(",")));
      emo = EmojiParser.demojizedText(myList.get(randInt(0, myList.size() - 1)));
    }

    // IS WAITING
    if (getWaitingTime(score) != null) {
      score.setWaiting(true);
      vh.pokeEmoji.setText(
          EmojiParser.demojizedText(context.getString(R.string.poke_emoji_disabled)));
    } else {
      score.setWaiting(false);
      vh.pokeEmoji.setText(emo);
    }

    score.setEmoticon(emo);

    score.setAbove(isAbove);
    vh.itemView.setOnClickListener(v -> onClick.onNext(score));

    vh.pokeEmoji.setOnClickListener(v -> {
      if (stateManager.shouldDisplay(StateManager.FIRST_POKE)) {
        NotificationUtils.displayPokeNotificationModel(context, score);
        stateManager.addTutorialKey(StateManager.FIRST_POKE);
        vh.pokeEmoji.setText(
            EmojiParser.demojizedText(context.getString(R.string.poke_emoji_disabled)));
      }
      score.setWaiting(true);
      score.setTextView(vh.pokeEmoji);
      onClickPoke.onNext(score);
    });

    if (position == 0) {
      vh.imgConnectTop.setVisibility(View.GONE);
    } else {
      vh.imgConnectTop.setVisibility(View.VISIBLE);
    }

    if (position == items.size() - 1) {
      vh.imgConnectBottom.setVisibility(View.GONE);
    } else {
      vh.imgConnectBottom.setVisibility(View.VISIBLE);
    }

    if (position % 2 == 0) {
      vh.itemView.setBackgroundResource(android.R.color.transparent);
    } else {
      vh.itemView.setBackgroundResource(R.color.black_opacity_5);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull List<Score> items, @NonNull RecyclerView.ViewHolder holder,
      int position, List<Object> payloads) {
    onBindViewHolder(items, position, holder);
  }

  static class LeaderboardUserViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.viewNewAvatar) NewAvatarView viewAvatar;
    @BindView(R.id.txtRanking) TextViewRanking txtRanking;
    @BindView(R.id.txtName) TextViewFont txtName;
    @BindView(R.id.txtScore) TextViewScore txtScore;
    @BindView(R.id.imgConnectTop) ImageView imgConnectTop;
    @BindView(R.id.imgConnectBottom) ImageView imgConnectBottom;
    @BindView(R.id.pokeEmoji) TextView pokeEmoji;

    public LeaderboardUserViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }
  }

  public Observable<Score> onClickPoke() {
    return onClickPoke;
  }

  public Observable<Score> onClick() {
    return onClick;
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) context).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) context));
  }

  private Long getWaitingTime(Score score) {
    Long timeSecond = null;
    String id = score.getGame().getId() + "_" + score.getUser().getId();
    List<PokeTiming> testList2 =
        PreferencesUtils.getPlayloadPokeTimingList(pokeUserGame, maxWaitingTimeSeconde);

    for (PokeTiming p : testList2) {
      if (p.getId().equals(id)) {
        long diff = System.currentTimeMillis() - p.getCreationDate();
        timeSecond = TimeUnit.MILLISECONDS.toSeconds(diff);
      }
    }
    return timeSecond;
  }
}
