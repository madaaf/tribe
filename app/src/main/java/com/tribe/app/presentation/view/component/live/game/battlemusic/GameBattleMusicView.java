package com.tribe.app.presentation.view.component.live.game.battlemusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionListenerAdapter;
import android.support.transition.TransitionManager;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.animation.OvershootInterpolator;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.battlemusic.BattleMusicTrack;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswerView;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswersView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2018
 */

public class GameBattleMusicView extends GameViewWithRanking {

  private static final String ANSWER_KEY = "answer";
  private static final String TRACK_KEY = "track";
  private static final String NAME_KEY = "name";
  private static final String WINNER_KEY = "winner";
  private static final String TITLE_KEY = "title";
  private static final String SHOW_WINNER_KEY = "showWinner";
  private static final String WINNERS_NAMES_KEY = "winnersNamesKey";

  private static final String ACTION_PICK_PLAYLIST = "pickPlaylist";
  private static final String ACTION_PRELOAD_TRACK = "preloadTrack";
  private static final String ACTION_PLAY_TRACK = "playTrack";
  private static final String ACTION_END_TRACK = "endTrack";
  private static final String ACTION_HIDE_GAME = "hideGame";
  private static final String ACTION_PAUSE = "pause";
  private static final String ACTION_RESUME = "resume";

  private static final String ANSWER_TRACK_PRELOADED = "trackPreloaded";
  private static final String ANSWER_GUESS = "guess";

  private static final int NB_QUESTIONS = 12;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;
  @BindView(R.id.viewCategories) GameBattleMusicCategoriesView viewCategories;
  @BindView(R.id.txtTitle) TextViewFont txtTitle;
  @BindView(R.id.groupInit) Group groupInit;
  @BindView(R.id.viewAnswers) GameAnswersView viewAnswers;
  @BindView(R.id.viewPlay) GameBattleMusicPlayView viewPlay;

  // VARIABLES
  private BattleMusicPlaylist playlist;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private Map<String, BattleMusicPlaylist> mapPlaylists;
  private List<BattleMusicTrack> tracks;
  private String playlistTitle;
  private boolean weHaveAWinner = false, isBuffered = false;
  private String rightAnswer;
  private int nbAnswers = 0, nbPlayingPeers = 0, nbPreloads = 0;

  // SUBSCRIPTIONS
  protected Subscription rightAnswerSubscription, wrongAnswerSubscription;
  protected CompositeSubscription subscriptionsTrack = new CompositeSubscription();

  public GameBattleMusicView(@NonNull Context context) {
    super(context);
  }

  public GameBattleMusicView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_battlemusic_title_only, this, true);
    unbinder = ButterKnife.bind(this);

    mapPlaylists = new HashMap<>();

    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onBattleMusicData(Map<String, BattleMusicPlaylist> map) {
        mapPlaylists.clear();
        mapPlaylists.putAll(map);

        if (StringUtils.isEmpty(playlistTitle)) {
          String text = getResources().getString(R.string.game_song_pop_status_pick_playlist);
          showInstructions(Arrays.asList(new String[] { text }), false, true, finished -> {
            if (finished) {
              showPlaylists();
            }
          });
        } else {
          tracks = playlist.getRandomTracks(NB_QUESTIONS);
          endTrack(false, null);
        }
      }
    };

    subscriptions.add(viewCategories.onPlaylistSelected().subscribe(playlist -> {
      tracks = playlist.getRandomTracks(NB_QUESTIONS);
      sendAction(ACTION_PICK_PLAYLIST, getPlaylistPayload(playlist));
      nextTrack();
    }));

    subscriptions.add(viewPlay.onDonePlaying().subscribe(aBoolean -> {
      if (!weHaveAWinner) {
        weHaveAWinner = true;
        endTrack(false, null);
      }
    }));

    subscriptions.add(viewPlay.onStarted().subscribe(aVoid -> {
      viewAnswers.enableClicks(true);
      viewPlay.play();
    }));

    subscriptions.add(
        viewPlay.onPause().subscribe(aVoid -> sendAction(ACTION_PAUSE, new JSONObject())));

    subscriptions.add(
        viewPlay.onResume().subscribe(aVoid -> sendAction(ACTION_RESUME, new JSONObject())));
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          JSONObject jsonObject = pair.second;
          TribeSession tribeSession = pair.first;

          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
              receiveMessage(tribeSession, message);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  @Override protected void takeOverGame() {
    gamePresenter.getBattleMusicData();
  }

  private void showPlaylists() {
    viewCategories.computeCategories(new ArrayList<>(mapPlaylists.values()));

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_battlemusic);
    constraintSet.setAlpha(R.id.groupInit, 1);
    animateLayoutWithConstraintSet(constraintSet, true, null);
  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet, boolean animated,
      LabelListener labelListener) {
    Transition transition = new ChangeBounds();
    transition.setDuration(animated ? 500 : 0);
    transition.setInterpolator(new OvershootInterpolator(0.75f));
    transition.addListener(new TransitionListenerAdapter() {
      @Override public void onTransitionEnd(@NonNull Transition transition) {
        if (labelListener != null) labelListener.call();
      }
    });
    TransitionManager.beginDelayedTransition(layoutConstraint, transition);
    constraintSet.applyTo(layoutConstraint);
  }

  private void nextTrack() {
    Collection<Integer> rankings = mapRanking.values();
    List<String> leadersDisplayName = new ArrayList<>();
    int maxRanking = rankings != null && rankings.size() > 0 ? Collections.max(rankings) : 0;
    for (TribeGuest tribeGuest : mapRanking.keySet()) {
      if (mapRanking.get(tribeGuest) == maxRanking) {
        leadersDisplayName.add(tribeGuest.getDisplayName());
      }
    }

    if (tracks != null && tracks.size() > 0) {
      BattleMusicTrack track = tracks.remove(0);
      rightAnswer = track.getName();
      nbAnswers = 0;

      int nbPlayersThatCanPlay = 0;
      for (TribeGuest guest : peerMap.values()) {
        if (guest.canPlayGames(game.getId())) nbPlayersThatCanPlay++;
      }

      nbPlayingPeers = nbPlayersThatCanPlay;
      nbPreloads = 0;
      sendAction(ACTION_PRELOAD_TRACK, getPreloadTrack(track));
    } else if (leadersDisplayName != null && leadersDisplayName.size() > 0) {
      sendAction(ACTION_HIDE_GAME, getHideGamePayload(leadersDisplayName));
    } else {
      sendAction(ACTION_HIDE_GAME, getHideGamePayload(null));
    }
  }

  @Override protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    if (jsonObject.has(game.getId())) {
      try {

        JSONObject message = jsonObject.getJSONObject(game.getId());

        if (message.has(ACTION_KEY)) {
          String actionKey = message.getString(ACTION_KEY);
          if (actionKey.equals(ACTION_PICK_PLAYLIST)) {
            playlistTitle = message.getString(TITLE_KEY);
          } else if (actionKey.equals(ACTION_PRELOAD_TRACK)) {
            currentMasterId = tribeSession == null ? currentUser.getId() : tribeSession.getUserId();
            game.setCurrentMaster(peerMap.get(currentMasterId));
            preloadTrack(message, tribeSession);
          } else if (actionKey.equals(ACTION_PLAY_TRACK)) {
            viewPlay.start();
          } else if (actionKey.equals(ACTION_END_TRACK)) {
            viewPlay.hide();

            if (message.has(WINNER_KEY)) {
              TribeGuest guest = peerMap.get(message.getString(WINNER_KEY));
              if (guest.getId().equals(currentUser.getId())) {
                onAddScore.onNext(Pair.create(game.getId(), getScore(guest.getId())));
              }

              String text = getResources().getString(R.string.game_song_pop_status_winner,
                  guest.getDisplayName());
              showInstructions(Arrays.asList(new String[] { text }), true, true, finished -> {
                if (finished) {
                  txtTitle.setText(text);
                  showTracks(true, () -> viewAnswers.animateAnswerResult());
                }
              });
            } else {
              String text = getResources().getString(R.string.game_song_pop_status_no_winner);
              showInstructions(Arrays.asList(new String[] { text }), true, true, finished -> {
                if (finished) {
                  txtTitle.setText(text);
                  showTracks(true, () -> viewAnswers.animateAnswerResult());
                }
              });
            }
          } else if (actionKey.equals(ACTION_HIDE_GAME)) {
            if (message.has(SHOW_WINNER_KEY)) {
              if (message.has(WINNERS_NAMES_KEY)) {
                JSONArray array = message.getJSONArray(WINNERS_NAMES_KEY);
                showResults(array);
              } else {
                showResults(null);
              }

              return;
            }

            subscriptionsRoom.clear();
            subscriptionsRoom.add(Observable.timer(10, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> stop()));
          } else if (actionKey.equals(ACTION_PAUSE)) {
            viewPlay.stop();
          } else if (actionKey.equals(ACTION_RESUME)) {
            viewPlay.play();
          }
        } else if (message.has(ANSWER_KEY)) {
          String answer = message.getString(ANSWER_KEY);
          if (answer.equals(ANSWER_GUESS)) {
            nbAnswers++;
            if (message.getString(NAME_KEY).equals(rightAnswer)) {
              if (!weHaveAWinner) {
                weHaveAWinner = true;
                endTrack(true, tribeSession);
              }
            } else if (nbAnswers == nbPlayingPeers) {
              weHaveAWinner = true;
              endTrack(false, tribeSession);
            }
          } else if (answer.equals(ANSWER_TRACK_PRELOADED)) {
            nbPreloads++;
            if (nbPreloads == nbPlayingPeers) {
              weHaveAWinner = false;
              sendAction(ACTION_PLAY_TRACK, new JSONObject());
            }
          }
        }
      } catch (JSONException ex) {
        ex.printStackTrace();
      }
    }

    super.receiveMessage(tribeSession, jsonObject);
  }

  private void showInstructions(List<String> steps, boolean disappearAnimated,
      boolean automaticallyReappear, InstructionsListener completionListener) {
    TextViewCompat.setTextAppearance(txtTitle, R.style.Title24_2_White);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    updateSteps(steps, false, () -> {
      if (completionListener != null) completionListener.finished(false);
      if (automaticallyReappear) {
        completionListener.finished(true);
      }
    });

    showInstructions(true, null);
  }

  private void updateSteps(List<String> steps, boolean animated, LabelListener completionListener) {
    if (steps.size() > 0) {
      //if (animated) {
      txtTitle.setText(steps.get(0));
      //}

      subscriptions.add(Observable.timer(2, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              aLong -> updateSteps(steps.subList(1, steps.size()), true, completionListener)));
    } else {
      completionListener.call();
    }
  }

  private void showTracks(boolean animated, LabelListener completionListener) {
    TextViewCompat.setTextAppearance(txtTitle, R.style.Headline_White_2);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_battlemusic_track);

    animateLayoutWithConstraintSet(constraintSet, animated, () -> {
      if (completionListener != null) completionListener.call();
    });
  }

  private void showInstructions(boolean animated, LabelListener completionListener) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_battlemusic_title_only);
    animateLayoutWithConstraintSet(constraintSet, animated, null);
    if (completionListener != null) completionListener.call();
  }

  private void preloadTrack(JSONObject message, TribeSession tribeSession) {
    subscriptionsTrack.clear();
    viewAnswers.hide();

    if (message.has(TRACK_KEY)) {
      try {
        BattleMusicTrack track = new BattleMusicTrack(message.getJSONObject(TRACK_KEY));

        isBuffered = false;
        List<String> steps = new ArrayList<>();
        steps.add(getResources().getString(R.string.game_song_pop_status_guess));

        showInstructions(steps, true, true, finished -> {
          if (!finished) setupAnswers(track, tribeSession);
          viewPlay.initTrack(track);
          viewAnswers.enableClicks(false);

          subscriptionsTrack.add(viewPlay.onBuffered().subscribe(aBoolean -> {
            if (aBoolean && !isBuffered) {
              isBuffered = true;
              sendAnswer(tribeSession, ANSWER_TRACK_PRELOADED, new JSONObject());
            }
          }));
        });
      } catch (JSONException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setupAnswers(BattleMusicTrack track, TribeSession tribeSession) {
    viewAnswers.initQuestion(track.getName(), track.getAlternativeNames(),
        GameAnswerView.TYPE_BATTLE_MUSIC);

    if (rightAnswerSubscription != null) rightAnswerSubscription.unsubscribe();
    if (wrongAnswerSubscription != null) wrongAnswerSubscription.unsubscribe();

    rightAnswerSubscription = viewAnswers.onAnsweredRight().subscribe(clickedAnswer -> {
      viewAnswers.enableClicks(false);
      viewPlay.stop();
      sendAnswer(tribeSession, ANSWER_GUESS, getAnswerPayload(clickedAnswer.getAnswer()));
      viewAnswers.computeAnswers(clickedAnswer, true);
    });

    wrongAnswerSubscription = viewAnswers.onAnsweredWrong().subscribe(clickedAnswer -> {
      viewAnswers.enableClicks(false);
      viewPlay.stop();
      txtTitle.setText(R.string.game_song_pop_status_wrong_answer);
      sendAnswer(tribeSession, ANSWER_GUESS, getAnswerPayload(clickedAnswer.getAnswer()));
      viewAnswers.computeAnswers(clickedAnswer, false);
    });

    showTracks(true, () -> {
      viewPlay.showDone();
      viewAnswers.showDone();
    });
  }

  private void endTrack(boolean isWinner, TribeSession tribeSession) {
    viewPlay.stop();

    if (isWinner) {
      String winnerId = tribeSession != null ? tribeSession.getUserId() : currentUser.getId();
      sendAction(ACTION_END_TRACK, getWinnerPayload(winnerId));
      addPoints(1, winnerId, true);
    } else {
      sendAction(ACTION_END_TRACK, new JSONObject());
    }

    subscriptions.add(Observable.timer(6, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> nextTrack()));
  }

  private void showResults(JSONArray winnerNames) {
    boolean isWinner = winnerNames != null && winnerNames.length() > 0;
    String instruction = "";

    if (isWinner) {
      try {
        if (winnerNames.length() == 1) {
          instruction = getResources().getString(R.string.game_song_pop_status_winner_step,
              winnerNames.getString(0));
        } else {
          String winners = "";

          for (int i = 0; i < winnerNames.length(); i++) {
            winners += winnerNames.get(i);

            if (i < winnerNames.length() - 1) winners += ", ";
          }

          instruction =
              getResources().getString(R.string.game_song_pop_status_winners_step, winners);
        }
      } catch (JSONException ex) {
        ex.printStackTrace();
      }
    } else {
      instruction = getResources().getString(R.string.game_song_pop_status_no_winner_step);
    }

    showInstructions(Arrays.asList(new String[] { instruction }), true, false, finished -> {
      if (!finished) {
        subscriptions.add(Observable.timer(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> subscriptions.add(DialogFactory.dialogMultipleChoices(getContext(),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.game_song_pop_new_popup_title)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.game_song_pop_new_popup_description)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.game_song_pop_new_popup_again)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.game_song_pop_new_popup_other)),
                EmojiParser.demojizedText(
                    getContext().getString(R.string.game_song_pop_new_popup_stop)))
                .subscribe(integer -> {
                  switch (integer) {
                    case 0:
                      onRestart.onNext(game);
                      game.getContextMap().put(SCORES_KEY, new HashMap<String, Integer>());
                      resetLiveScores();
                      updateRanking(null);
                      showPlaylists();
                      break;
                    case 1:
                      onPlayOtherGame.onNext(null);
                      break;
                    case 2:
                      onStop.onNext(game);
                      break;
                  }
                }))));
      }
    });
  }

  protected interface InstructionsListener {
    void finished(boolean finished);
  }

  /**
   * JSON PAYLOAD
   */

  private void sendAction(String action, JSONObject obj) {
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(obj, ACTION_KEY, action);
    JsonUtils.jsonPut(game, this.game.getId(), obj);
    webRTCRoom.sendToPeers(game, true);
    receiveMessage(null, game);
  }

  private void sendAnswer(TribeSession tribeSession, String answer, JSONObject obj) {
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(obj, ANSWER_KEY, answer);
    JsonUtils.jsonPut(game, this.game.getId(), obj);
    if (tribeSession != null) {
      webRTCRoom.sendToUser(tribeSession.getUserId(), game, true);
    } else {
      receiveMessage(null, game);
    }
  }

  private JSONObject getPlaylistPayload(BattleMusicPlaylist playlist) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, TITLE_KEY, playlist.getTitle());
    return obj;
  }

  private JSONObject getPreloadTrack(BattleMusicTrack track) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, TRACK_KEY, track.asJSON());
    return obj;
  }

  private JSONObject getAnswerPayload(String value) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, NAME_KEY, value);
    return obj;
  }

  private JSONObject getWinnerPayload(String winnerId) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, WINNER_KEY, winnerId);
    return obj;
  }

  private JSONObject getHideGamePayload(List<String> displayNames) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, SHOW_WINNER_KEY, true);

    if (displayNames != null) {
      JSONArray displayNamesObj = new JSONArray();
      for (String name : displayNames) {
        displayNamesObj.put(name);
      }

      JsonUtils.jsonPut(obj, WINNERS_NAMES_KEY, displayNamesObj);
    }

    return obj;
  }

  /**
   * PUBLIC
   */

  public void initSubscriptions() {

  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, mapObservable, liveViewsObservable, userId);

    gamePresenter.getBattleMusicData();
  }

  @Override public void stop() {
    viewPlay.stop();
    soundManager.cancelMediaPlayer();
    super.stop();
  }

  @Override public void dispose() {
    viewPlay.dispose();
    subscriptionsTrack.clear();
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
