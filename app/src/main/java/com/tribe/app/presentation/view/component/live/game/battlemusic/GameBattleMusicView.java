package com.tribe.app.presentation.view.component.live.game.battlemusic;

import android.content.Context;
import android.graphics.Color;
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
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.battlemusic.BattleMusicPlaylist;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswersView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
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

/**
 * Created by tiago on 12/21/2017.
 */

public class GameBattleMusicView extends GameViewWithRanking {

  private static final String ACTION_POP_ALIEN = "popAlien";
  private static final String ALIEN_KEY = "alien";

  private static final String ANSWER_KEY = "answer";
  private static final String TRACK_KEY = "track";
  private static final String NAME_KEY = "name";
  private static final String WINNER_KEY = "winner";
  private static final String TITLE_KEY = "title";
  private static final String ALL_KEY = "all";
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
  @BindView(R.id.txtTriviaTitle) TextViewFont txtTitle;
  @BindView(R.id.txtQuestion) TextViewFont txtQuestion;
  @BindView(R.id.txtQuestionCount) TextViewFont txtQuestionCount;
  @BindView(R.id.groupInit) Group groupInit;
  @BindView(R.id.progressBar) CircularProgressBar progressBar;
  @BindView(R.id.viewAnswers) GameAnswersView viewAnswers;
  @BindView(R.id.imgBackground) ImageView imgBackground;

  // VARIABLES
  private BattleMusicPlaylist playlist;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private Map<String, BattleMusicPlaylist> mapPlaylists;
  private List<BattleMusicPlaylist> tracks;
  private String categoryTitle;
  private boolean categoryAll = false, weHaveAWinner = false;
  private String rightAnswer;
  private int nbAnswers = 0, nbPlayingPeers = 0;

  // SUBSCRIPTIONS
  protected Subscription rightAnswerSubscription, wrongAnswerSubscription;

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

    inflater.inflate(R.layout.view_game_trivia, this, true);
    unbinder = ButterKnife.bind(this);

    mapPlaylists = new HashMap<>();

    setClickable(true);

    progressBar.setProgressColor(Color.WHITE);
    progressBar.setProgressWidth(screenUtils.dpToPx(5));
    progressBar.setProgress((100 / NB_QUESTIONS));

    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onBattleMusicData(Map<String, BattleMusicPlaylist> map) {
        mapPlaylists.clear();
        mapPlaylists.putAll(map);
        showPlaylists();
      }
    };
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

  }

  private void showPlaylists() {
    //soundManager.playSound(SoundManager.TRIVIA_SOUNDTRACK, SoundManager.SOUND_MID);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_battle_music);
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
    //Collection<Integer> rankings = mapRanking.values();
    //List<String> leadersDisplayName = new ArrayList<>();
    //int maxRanking = rankings != null && rankings.size() > 0 ? Collections.max(rankings) : 0;
    //for (TribeGuest tribeGuest : mapRanking.keySet()) {
    //  if (mapRanking.get(tribeGuest) == maxRanking) {
    //    leadersDisplayName.add(tribeGuest.getDisplayName());
    //  }
    //}
    //
    //if (questions != null && questions.size() > 0) {
    //  TriviaQuestion question = questions.remove(0);
    //  rightAnswer = question.getAnswer();
    //  nbAnswers = 0;
    //
    //  int nbPlayersThatCanPlay = 0;
    //  for (TribeGuest guest : peerMap.values()) {
    //    if (guest.canPlayGames(game.getId())) nbPlayersThatCanPlay++;
    //  }
    //
    //  nbPlayingPeers = nbPlayersThatCanPlay;
    //  sendAction(ACTION_SHOW_QUESTION, getShowQuestionPayload(question));
    //} else if (leadersDisplayName != null && leadersDisplayName.size() > 0) {
    //  sendAction(ACTION_HIDE_GAME, getHideGamePayload(leadersDisplayName));
    //} else {
    //  sendAction(ACTION_HIDE_GAME, getHideGamePayload(null));
    //}
  }

  @Override protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    //if (jsonObject.has(game.getId())) {
    //  try {
    //
    //    JSONObject message = jsonObject.getJSONObject(game.getId());
    //
    //    if (message.has(ACTION_KEY)) {
    //      String actionKey = message.getString(ACTION_KEY);
    //      if (actionKey.equals(ACTION_PICK_PLAYLIST)) {
    //        categoryTitle = message.getString(TITLE_KEY);
    //        categoryAll = message.has(ALL_KEY) ? message.getBoolean(ALL_KEY) : false;
    //      } else if (actionKey.equals(ACTION_SHOW_QUESTION)) {
    //        soundManager.playSound(SoundManager.TRIVIA_SOUNDTRACK, SoundManager.SOUND_MID);
    //        currentMasterId = tribeSession == null ? currentUser.getId() : tribeSession.getUserId();
    //        game.setCurrentMaster(peerMap.get(currentMasterId));
    //        preloadQuestion(message, tribeSession);
    //        weHaveAWinner = false;
    //      } else if (actionKey.equals(ACTION_END_QUESTION)) {
    //        soundManager.playSound(SoundManager.TRIVIA_SOUNDTRACK_ANSWER, SoundManager.SOUND_MID);
    //
    //        if (message.has(WINNER_KEY)) {
    //          TribeGuest guest = peerMap.get(message.getString(WINNER_KEY));
    //          if (guest.getId().equals(currentUser.getId())) {
    //            onAddScore.onNext(Pair.create(game.getId(), getScore(guest.getId())));
    //          }
    //
    //          String text = getResources().getString(R.string.game_song_pop_status_winner,
    //              guest.getDisplayName());
    //          showInstructions(Arrays.asList(new String[] { text }), true, true, false,
    //              finished -> {
    //                if (finished) {
    //                  txtTitle.setText(text);
    //                  viewAnswers.animateAnswerResult();
    //                }
    //              });
    //        } else {
    //          String text = getResources().getString(R.string.game_song_pop_status_no_winner);
    //          showInstructions(Arrays.asList(new String[] { text }), true, true, false,
    //              finished -> {
    //                if (finished) {
    //                  txtTitle.setText(text);
    //                  viewAnswers.animateAnswerResult();
    //                }
    //              });
    //        }
    //      } else if (actionKey.equals(ACTION_HIDE_GAME)) {
    //        if (message.has(SHOW_WINNER_KEY)) {
    //          if (message.has(WINNERS_NAMES_KEY)) {
    //            JSONArray array = message.getJSONArray(WINNERS_NAMES_KEY);
    //            showResults(array);
    //          } else {
    //            showResults(null);
    //          }
    //
    //          return;
    //        }
    //
    //        subscriptionsRoom.clear();
    //        subscriptionsRoom.add(Observable.timer(10, TimeUnit.SECONDS)
    //            .observeOn(AndroidSchedulers.mainThread())
    //            .subscribe(aLong -> stop()));
    //      }
    //    } else if (message.has(ANSWER_KEY)) {
    //      String answer = message.getString(ANSWER_KEY);
    //      if (answer.equals(ANSWER_GUESS)) {
    //        nbAnswers++;
    //        if (message.getString(NAME_KEY).equals(rightAnswer)) {
    //          if (!weHaveAWinner) {
    //            weHaveAWinner = true;
    //            endQuestion(true, tribeSession);
    //          }
    //        } else if (nbAnswers == nbPlayingPeers) {
    //          weHaveAWinner = true;
    //          endQuestion(false, tribeSession);
    //        }
    //      }
    //    }
    //  } catch (JSONException ex) {
    //    ex.printStackTrace();
    //  }
    //}

    super.receiveMessage(tribeSession, jsonObject);
  }

  private void showInstructions(List<String> steps, boolean disappearAnimated,
      boolean automaticallyReappear, boolean showRays, InstructionsListener completionListener) {
    TextViewCompat.setTextAppearance(txtTitle, R.style.Title24_2_White);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    updateSteps(steps, false, () -> {
      if (completionListener != null) completionListener.finished(false);
      if (automaticallyReappear) {
        showTracks(true, showRays, () -> completionListener.finished(true));
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

  private void showTracks(boolean animated, boolean showRays, LabelListener completionListener) {
    TextViewCompat.setTextAppearance(txtTitle, R.style.Headline_White_2);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_question);
    if (!showRays) {
      constraintSet.connect(R.id.imgBackground, ConstraintSet.TOP, layoutConstraint.getId(),
          ConstraintSet.BOTTOM);
    }

    animateLayoutWithConstraintSet(constraintSet, animated, () -> {
      //if (rotationRaysAnimator != null) {
      //  rotationRaysAnimator.cancel();
      //  rotationRaysAnimator = null;
      //}
      //
      //imgBackground.setScaleType(ImageView.ScaleType.MATRIX);
      //rotationRaysAnimator = ValueAnimator.ofFloat(0, 360);
      //rotationRaysAnimator.setDuration(300);
      //rotationRaysAnimator.setInterpolator(new DecelerateInterpolator());
      //rotationRaysAnimator.setRepeatCount(ValueAnimator.INFINITE);
      //rotationRaysAnimator.setRepeatMode(ValueAnimator.RESTART);
      //rotationRaysAnimator.addUpdateListener(animation -> {
      //  float rotation = (float) animation.getAnimatedValue();
      //  imgBackground.setPivotX(imgBackground.getDrawable().getBounds().width() / 2);
      //  imgBackground.setPivotY(imgBackground.getDrawable().getBounds().height() / 2);
      //  imgBackground.setRotation(rotation);
      //});
      //rotationRaysAnimator.start();
      if (completionListener != null) completionListener.call();
    });
  }

  private void showInstructions(boolean animated, LabelListener completionListener) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_title_only);
    animateLayoutWithConstraintSet(constraintSet, animated, null);
    if (completionListener != null) completionListener.call();
  }

  private void preloadQuestion(JSONObject message, TribeSession tribeSession) {
    //if (message.has(QUESTION_KEY)) {
    //  try {
    //    TriviaQuestion currentQuestion = new TriviaQuestion(message.getJSONObject(QUESTION_KEY));
    //    int questionCount = message.has(NB_QUESTION_KEY) ? message.getInt(NB_QUESTION_KEY) : 1;
    //
    //    soundManager.playSound(SoundManager.TRIVIA_ANSWER_FIRST_WIN, SoundManager.SOUND_MAX);
    //    List<String> steps = Arrays.asList(
    //        new String[] { getResources().getString(R.string.game_trivia_status_guess) });
    //    showInstructions(steps, true, true, true, finished -> {
    //      if (!finished) setupAnswers(currentQuestion, tribeSession);
    //    });
    //
    //    txtQuestion.setText(currentQuestion.getQuestion());
    //    txtQuestionCount.setText("" + questionCount);
    //
    //    progressBar.setProgress((100 / NB_QUESTIONS) * questionCount);
    //  } catch (JSONException ex) {
    //    ex.printStackTrace();
    //  }
    //}
  }

  private void setupAnswers(TriviaQuestion triviaQuestion, TribeSession tribeSession) {
    //viewAnswers.initQuestion(triviaQuestion);
    //
    //if (rightAnswerSubscription != null) rightAnswerSubscription.unsubscribe();
    //if (wrongAnswerSubscription != null) wrongAnswerSubscription.unsubscribe();
    //
    //rightAnswerSubscription = viewAnswers.onAnsweredRight().subscribe(clickedAnswer -> {
    //  soundManager.playSound(SoundManager.TRIVIA_WON, SoundManager.SOUND_MAX);
    //  sendAnswer(tribeSession, ANSWER_GUESS, getAnswerPayload(clickedAnswer.getAnswer()));
    //  viewAnswers.computeAnswers(clickedAnswer, true);
    //});
    //
    //wrongAnswerSubscription = viewAnswers.onAnsweredWrong().subscribe(clickedAnswer -> {
    //  soundManager.playSound(SoundManager.TRIVIA_LOST, SoundManager.SOUND_MAX);
    //  txtTitle.setText(R.string.game_trivia_status_wrong_answer);
    //  sendAnswer(tribeSession, ANSWER_GUESS, getAnswerPayload(clickedAnswer.getAnswer()));
    //  viewAnswers.computeAnswers(clickedAnswer, false);
    //});
  }

  private void endQuestion(boolean isWinner, TribeSession tribeSession) {
    //if (isWinner) {
    //  String winnerId = tribeSession != null ? tribeSession.getUserId() : currentUser.getId();
    //  sendAction(ACTION_END_QUESTION, getWinnerPayload(winnerId));
    //  addPoints(1, winnerId, true);
    //} else {
    //  sendAction(ACTION_END_QUESTION, new JSONObject());
    //}
    //
    //subscriptions.add(Observable.timer(6, TimeUnit.SECONDS)
    //    .observeOn(AndroidSchedulers.mainThread())
    //    .subscribe(aLong -> nextQuestion()));
  }

  private void showResults(JSONArray winnerNames) {
    //soundManager.playSound(SoundManager.TRIVIA_ANSWER_FIRST_WIN, SoundManager.SOUND_MID);
    //
    //boolean isWinner = winnerNames != null && winnerNames.length() > 0;
    //String instruction = "";
    //
    //if (isWinner) {
    //  try {
    //    if (winnerNames.length() == 1) {
    //      instruction = getResources().getString(R.string.game_song_pop_status_winner_step,
    //          winnerNames.getString(0));
    //    } else {
    //      String winners = "";
    //
    //      for (int i = 0; i < winnerNames.length(); i++) {
    //        winners += winnerNames.get(i);
    //
    //        if (i < winnerNames.length() - 1) winners += ", ";
    //      }
    //
    //      instruction =
    //          getResources().getString(R.string.game_song_pop_status_winners_step, winners);
    //    }
    //  } catch (JSONException ex) {
    //    ex.printStackTrace();
    //  }
    //} else {
    //  instruction = getResources().getString(R.string.game_song_pop_status_no_winner_step);
    //}
    //
    //showInstructions(Arrays.asList(new String[] { instruction }), true, false, false,
    //    new InstructionsListener() {
    //      @Override public void finished(boolean finished) {
    //        if (!finished) {
    //          subscriptions.add(Observable.timer(1, TimeUnit.SECONDS)
    //              .observeOn(AndroidSchedulers.mainThread())
    //              .subscribe(aLong -> subscriptions.add(
    //                  DialogFactory.dialogMultipleChoices(getContext(), EmojiParser.demojizedText(
    //                      getContext().getString(R.string.game_song_pop_new_popup_title)),
    //                      EmojiParser.demojizedText(
    //                          getContext().getString(R.string.game_song_pop_new_popup_description)),
    //                      EmojiParser.demojizedText(
    //                          getContext().getString(R.string.game_song_pop_new_popup_again)),
    //                      EmojiParser.demojizedText(
    //                          getContext().getString(R.string.game_song_pop_new_popup_other)),
    //                      EmojiParser.demojizedText(
    //                          getContext().getString(R.string.game_song_pop_new_popup_stop)))
    //                      .subscribe(integer -> {
    //                        switch (integer) {
    //                          case 0:
    //                            onRestart.onNext(game);
    //                            game.getContextMap()
    //                                .put(SCORES_KEY, new HashMap<String, Integer>());
    //                            resetLiveScores();
    //                            updateRanking(null);
    //                            showCategories();
    //                            break;
    //                          case 1:
    //                            onPlayOtherGame.onNext(null);
    //                            break;
    //                          case 2:
    //                            onStop.onNext(game);
    //                            break;
    //                        }
    //                      }))));
    //        }
    //      }
    //    });
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

  private JSONObject getCategorySelectionPayload(BattleMusicPlaylist playlist) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, TITLE_KEY, playlist.getTitle());
    return obj;
  }

  private JSONObject getShowQuestionPayload(TriviaQuestion question) {
    JSONObject obj = new JSONObject();
    //JsonUtils.jsonPut(obj, QUESTION_KEY, question.asJSON());
    //JsonUtils.jsonPut(obj, NB_QUESTION_KEY, NB_QUESTIONS - questions.size());
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

    gamePresenter.getTriviaData();

    viewCategories.onPlaylistSelected().subscribe(playlist -> {
      sendAction(ACTION_PICK_PLAYLIST, getCategorySelectionPayload(playlist));
      //questions = playlist.getRandomTracks(NB_QUESTIONS);
      //nextQuestion();
    });
  }

  @Override public void stop() {
    super.stop();
    soundManager.cancelMediaPlayer();
  }

  @Override public void dispose() {
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */

}
