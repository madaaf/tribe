package com.tribe.app.presentation.view.component.live.game.trivia;

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
import android.text.Html;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.trivia.TriviaCategoryEnum;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswerView;
import com.tribe.app.presentation.view.component.live.game.common.GameAnswersView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
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

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaView extends GameViewWithRanking {

  private static final String ANSWER_KEY = "answer";
  private static final String QUESTION_KEY = "question";
  private static final String NB_QUESTION_KEY = "nbQuestion";
  private static final String NAME_KEY = "name";
  private static final String WINNER_KEY = "winner";
  private static final String TITLE_KEY = "title";
  private static final String ALL_KEY = "all";
  private static final String SHOW_WINNER_KEY = "showWinner";
  private static final String WINNERS_NAMES_KEY = "winnersNamesKey";
  private static final String ACTION_DISPLAY_CATEGORIES = "displayCategories";
  private static final String ACTION_PICK_CATEGORY = "pickCategory";
  private static final String ACTION_SHOW_QUESTION = "showQuestion";
  private static final String ANSWER_GUESS = "guess";
  private static final String ACTION_END_QUESTION = "endQuestion";
  private static final String ACTION_HIDE_GAME = "hideGame";

  private static final int NB_QUESTIONS = 12;

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;
  @BindView(R.id.viewCategoryMovies) GameTriviaCategoryView viewCategorieMovies;
  @BindView(R.id.viewCategoryMusic) GameTriviaCategoryView viewCategorieMusic;
  @BindView(R.id.viewCategoryTV) GameTriviaCategoryView viewCategorieTV;
  @BindView(R.id.viewCategoryCelebs) GameTriviaCategoryView viewCategorieCelebs;
  @BindView(R.id.viewCategorySports) GameTriviaCategoryView viewCategorieSports;
  @BindView(R.id.viewCategoryGeeks) GameTriviaCategoryView viewCategorieGeeks;
  @BindView(R.id.viewCategoryGeneral) GameTriviaCategoryView viewCategorieGeneral;
  @BindView(R.id.viewCategoryWorld) GameTriviaCategoryView viewCategorieWorld;
  @BindView(R.id.viewCategoryGames) GameTriviaCategoryView viewCategorieGames;
  @BindView(R.id.txtTriviaTitle) TextViewFont txtTitle;
  @BindView(R.id.txtQuestion) TextViewFont txtQuestion;
  @BindView(R.id.txtQuestionCount) TextViewFont txtQuestionCount;
  @BindView(R.id.groupInit) Group groupInit;
  @BindView(R.id.progressBar) CircularProgressBar progressBar;
  @BindView(R.id.viewAnswers) GameAnswersView viewAnswers;
  @BindView(R.id.imgBackground) ImageView imgBackground;

  // VARIABLES
  private TriviaCategoryEnum category;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private List<TriviaQuestion> questions;
  private String categoryTitle;
  private boolean categoryAll = false, weHaveAWinner = false;
  private TriviaQuestion currentQuestion;
  private int nbAnswers = 0, nbPlayingPeers = 0;

  // SUBSCRIPTIONS
  protected Subscription rightAnswerSubscription, wrongAnswerSubscription;

  public GameTriviaView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaView(@NonNull Context context, @Nullable AttributeSet attrs) {
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

    inflater.inflate(R.layout.view_game_trivia_init, this, true);
    unbinder = ButterKnife.bind(this);

    setClickable(true);

    progressBar.setProgressColor(Color.WHITE);
    progressBar.setProgressWidth(screenUtils.dpToPx(5));
    progressBar.setProgress((100 / NB_QUESTIONS));

    gameMVPViewAdapter = new GameMVPViewAdapter() {
      @Override public void onTriviaData(Map<String, List<TriviaQuestion>> map) {
        for (String key : map.keySet()) {
          TriviaCategoryEnum.setQuestionsForCategory(key, map.get(key));
        }

        showCategories();
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

  private void showCategories() {
    soundManager.playSound(SoundManager.TRIVIA_SOUNDTRACK, SoundManager.SOUND_MID);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia);
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

  /**
   * ON CLICKS
   */

  @OnClick({
      R.id.viewCategoryCelebs, R.id.viewCategoryGames, R.id.viewCategoryGeeks,
      R.id.viewCategoryGeneral, R.id.viewCategoryMovies, R.id.viewCategoryMusic,
      R.id.viewCategorySports, R.id.viewCategoryTV, R.id.viewCategoryWorld
  }) void clickCategory(View view) {
    TriviaCategoryEnum categoryEnum = null;

    switch (view.getId()) {
      case R.id.viewCategoryCelebs:
        categoryEnum = TriviaCategoryEnum.CELEBS;
        break;

      case R.id.viewCategoryGames:
        categoryEnum = TriviaCategoryEnum.GAMES;
        break;

      case R.id.viewCategoryGeeks:
        categoryEnum = TriviaCategoryEnum.GEEKS;
        break;

      case R.id.viewCategoryGeneral:
        categoryEnum = TriviaCategoryEnum.GENERAL;
        break;

      case R.id.viewCategoryMovies:
        categoryEnum = TriviaCategoryEnum.MOVIES;
        break;

      case R.id.viewCategoryMusic:
        categoryEnum = TriviaCategoryEnum.MUSIC;
        break;

      case R.id.viewCategorySports:
        categoryEnum = TriviaCategoryEnum.SPORTS;
        break;

      case R.id.viewCategoryTV:
        categoryEnum = TriviaCategoryEnum.TV;
        break;

      case R.id.viewCategoryWorld:
        categoryEnum = TriviaCategoryEnum.WORLD;
        break;
    }

    sendAction(ACTION_PICK_CATEGORY, getCategorySelectionPayload(categoryEnum));
    questions = TriviaCategoryEnum.getRandomQuestions(NB_QUESTIONS, categoryEnum);
    nextQuestion();
  }

  private void nextQuestion() {
    Collection<Integer> rankings = mapRanking.values();
    List<String> leadersDisplayName = new ArrayList<>();
    int maxRanking = rankings != null && rankings.size() > 0 ? Collections.max(rankings) : 0;
    for (TribeGuest tribeGuest : mapRanking.keySet()) {
      if (mapRanking.get(tribeGuest) == maxRanking) {
        leadersDisplayName.add(tribeGuest.getDisplayName());
      }
    }

    if (questions != null && questions.size() > 0) {
      TriviaQuestion question = questions.remove(0);
      currentQuestion = question;
      nbAnswers = 0;

      int nbPlayersThatCanPlay = 0;
      for (TribeGuest guest : peerMap.values()) {
        if (guest.canPlayGames(game.getId())) nbPlayersThatCanPlay++;
      }

      nbPlayingPeers = nbPlayersThatCanPlay;
      sendAction(ACTION_SHOW_QUESTION, getShowQuestionPayload(question));
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
          if (actionKey.equals(ACTION_DISPLAY_CATEGORIES)) {
            gamePresenter.getTriviaData();
          } else if (actionKey.equals(ACTION_PICK_CATEGORY)) {
            categoryTitle = message.getString(TITLE_KEY);
            categoryAll = message.has(ALL_KEY) ? message.getBoolean(ALL_KEY) : false;
          } else if (actionKey.equals(ACTION_SHOW_QUESTION)) {
            soundManager.playSound(SoundManager.TRIVIA_SOUNDTRACK, SoundManager.SOUND_MID);
            currentMasterId = tribeSession == null ? currentUser.getId() : tribeSession.getUserId();
            game.setCurrentMaster(peerMap.get(currentMasterId));
            preloadQuestion(message, tribeSession);
            weHaveAWinner = false;
          } else if (actionKey.equals(ACTION_END_QUESTION)) {
            soundManager.playSound(SoundManager.TRIVIA_SOUNDTRACK_ANSWER, SoundManager.SOUND_MID);

            currentQuestion = null;

            if (message.has(WINNER_KEY)) {
              TribeGuest guest = peerMap.get(message.getString(WINNER_KEY));
              if (guest.getId().equals(currentUser.getId())) {
                onAddScore.onNext(Pair.create(game.getId(), getScore(guest.getId())));
              }

              String text = EmojiParser.demojizedText(
                  getResources().getString(R.string.game_song_pop_status_winner,
                      guest.getDisplayName()));
              showInstructions(Arrays.asList(new String[] { text }), true, true, false,
                  finished -> {
                    if (finished) {
                      txtTitle.setText(EmojiParser.demojizedText(text));
                      viewAnswers.animateAnswerResult();
                    }
                  });
            } else {
              String text = EmojiParser.demojizedText(
                  getResources().getString(R.string.game_song_pop_status_no_winner));
              showInstructions(Arrays.asList(new String[] { text }), true, true, false,
                  finished -> {
                    if (finished) {
                      txtTitle.setText(EmojiParser.demojizedText(text));
                      viewAnswers.animateAnswerResult();
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
          }
        } else if (message.has(ANSWER_KEY)) {
          String answer = message.getString(ANSWER_KEY);
          if (answer.equals(ANSWER_GUESS)) {
            nbAnswers++;
            if (message.getString(NAME_KEY).equals(currentQuestion.getAnswer())) {
              if (!weHaveAWinner) {
                weHaveAWinner = true;
                endQuestion(true, tribeSession);
              }
            } else if (nbAnswers == nbPlayingPeers) {
              weHaveAWinner = true;
              endQuestion(false, tribeSession);
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
      boolean automaticallyReappear, boolean showRays, InstructionsListener completionListener) {
    TextViewCompat.setTextAppearance(txtTitle, R.style.Title24_2_White);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    updateSteps(steps, false, () -> {
      if (completionListener != null) completionListener.finished(false);
      if (automaticallyReappear) {
        showQuestions(true, showRays, () -> {
          viewAnswers.showDone();
          viewAnswers.enableClicks(true);
          completionListener.finished(true);
        });
      }
    });

    showInstructions(true, null);
  }

  private void updateSteps(List<String> steps, boolean animated, LabelListener completionListener) {
    if (steps.size() > 0) {
      txtTitle.setText(EmojiParser.demojizedText(steps.get(0)));

      subscriptions.add(Observable.timer(2, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(
              aLong -> updateSteps(steps.subList(1, steps.size()), true, completionListener)));
    } else {
      completionListener.call();
    }
  }

  private void showQuestions(boolean animated, boolean showRays, LabelListener completionListener) {
    TextViewCompat.setTextAppearance(txtTitle, R.style.Headline_White_2);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_question);
    if (!showRays) {
      hideRays();
    } else {
      showRays();
    }

    animateLayoutWithConstraintSet(constraintSet, animated, () -> {
      if (completionListener != null) completionListener.call();
    });
  }

  private void showInstructions(boolean animated, LabelListener completionListener) {
    hideRays();

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_title_only);
    animateLayoutWithConstraintSet(constraintSet, animated, null);
    if (completionListener != null) completionListener.call();
  }

  private void hideRays() {
    AnimationUtils.fadeOut(imgBackground, 300);
    imgBackground.clearAnimation();
  }

  private void showRays() {
    AnimationUtils.fadeIn(imgBackground, 300);
    RotateAnimation rotate =
        new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
            0.5f);
    rotate.setDuration(10000);
    rotate.setRepeatCount(Animation.INFINITE);
    rotate.setFillAfter(true);
    imgBackground.startAnimation(rotate);
  }

  private void preloadQuestion(JSONObject message, TribeSession tribeSession) {
    viewAnswers.hide();
    viewAnswers.enableClicks(false);

    if (message.has(QUESTION_KEY)) {
      try {
        TriviaQuestion currentQuestion = new TriviaQuestion(message.getJSONObject(QUESTION_KEY));
        int questionCount = message.has(NB_QUESTION_KEY) ? message.getInt(NB_QUESTION_KEY) : 1;

        soundManager.playSound(SoundManager.TRIVIA_ANSWER_FIRST_WIN, SoundManager.SOUND_MAX);
        List<String> steps = Arrays.asList(
            new String[] { getResources().getString(R.string.game_trivia_status_guess) });
        showInstructions(steps, true, true, true, finished -> {
          if (!finished) setupAnswers(currentQuestion, tribeSession);
          viewAnswers.enableClicks(true);
        });

        txtQuestion.setText(Html.fromHtml(currentQuestion.getQuestion()));
        txtQuestionCount.setText("" + questionCount);

        progressBar.setProgress((100 / NB_QUESTIONS) * questionCount);
      } catch (JSONException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setupAnswers(TriviaQuestion triviaQuestion, TribeSession tribeSession) {
    viewAnswers.initQuestion(triviaQuestion.getAnswer(), triviaQuestion.getAlternativeAnswers(),
        GameAnswerView.TYPE_TRIVIA);

    if (rightAnswerSubscription != null) rightAnswerSubscription.unsubscribe();
    if (wrongAnswerSubscription != null) wrongAnswerSubscription.unsubscribe();

    rightAnswerSubscription = viewAnswers.onAnsweredRight().subscribe(clickedAnswer -> {
      viewAnswers.enableClicks(false);
      soundManager.playSound(SoundManager.TRIVIA_WON, SoundManager.SOUND_MAX);
      sendAnswer(tribeSession, ANSWER_GUESS, getAnswerPayload(clickedAnswer.getAnswer()));
      viewAnswers.computeAnswers(clickedAnswer, true);
    });

    wrongAnswerSubscription = viewAnswers.onAnsweredWrong().subscribe(clickedAnswer -> {
      viewAnswers.enableClicks(false);
      soundManager.playSound(SoundManager.TRIVIA_LOST, SoundManager.SOUND_MAX);
      txtTitle.setText(EmojiParser.demojizedText(
          getContext().getResources().getString(R.string.game_trivia_status_wrong_answer)));
      sendAnswer(tribeSession, ANSWER_GUESS, getAnswerPayload(clickedAnswer.getAnswer()));
      viewAnswers.computeAnswers(clickedAnswer, false);
    });
  }

  private void endQuestion(boolean isWinner, TribeSession tribeSession) {
    viewAnswers.enableClicks(false);

    if (isWinner) {
      String winnerId = tribeSession != null ? tribeSession.getUserId() : currentUser.getId();
      sendAction(ACTION_END_QUESTION, getWinnerPayload(winnerId));
      addPoints(1, winnerId, true);
    } else {
      sendAction(ACTION_END_QUESTION, new JSONObject());
    }

    subscriptions.add(Observable.timer(6, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> nextQuestion()));
  }

  private void showResults(JSONArray winnerNames) {
    soundManager.playSound(SoundManager.TRIVIA_ANSWER_FIRST_WIN, SoundManager.SOUND_MID);

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

    showInstructions(Arrays.asList(new String[] { instruction }), true, false, false,
        new InstructionsListener() {
          @Override public void finished(boolean finished) {
            if (!finished) {
              subscriptions.add(Observable.timer(1, TimeUnit.SECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(aLong -> subscriptions.add(
                      DialogFactory.dialogMultipleChoices(getContext(), EmojiParser.demojizedText(
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
                                game.getContextMap()
                                    .put(SCORES_KEY, new HashMap<String, Integer>());
                                resetLiveScores();
                                updateRanking(null);
                                showCategories();
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

  private JSONObject getCategorySelectionPayload(TriviaCategoryEnum category) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, TITLE_KEY, category.getCategory());
    return obj;
  }

  private JSONObject getShowQuestionPayload(TriviaQuestion question) {
    JSONObject obj = new JSONObject();
    JsonUtils.jsonPut(obj, QUESTION_KEY, question.asJSON());
    JsonUtils.jsonPut(obj, NB_QUESTION_KEY, NB_QUESTIONS - questions.size());
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

    currentMasterId = userId;

    if (userId.equals(currentUser.getId())) {
      sendAction(ACTION_DISPLAY_CATEGORIES, new JSONObject());
    }

    subscriptions.add(onNewPlayers.delay(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (currentMasterId.equals(currentUser.getId())) {
            if (questions != null) {
              if (currentQuestion != null) {
                for (TribeGuest tribeGuest : tribeGuests) {
                  JSONObject gameObj = new JSONObject();
                  JSONObject questionJSON = getShowQuestionPayload(currentQuestion);
                  JsonUtils.jsonPut(questionJSON, ACTION_KEY, ACTION_SHOW_QUESTION);
                  JsonUtils.jsonPut(gameObj, this.game.getId(), questionJSON);
                  webRTCRoom.sendToUser(tribeGuest.getId(), gameObj, true);
                }
              }
            } else {
              for (TribeGuest tribeGuest : tribeGuests) {
                JSONObject gameObj = new JSONObject();
                JSONObject obj = new JSONObject();
                JsonUtils.jsonPut(obj, ACTION_KEY, ACTION_DISPLAY_CATEGORIES);
                JsonUtils.jsonPut(gameObj, this.game.getId(), obj);
                webRTCRoom.sendToUser(tribeGuest.getId(), gameObj, true);
              }
            }
          }
        }));
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
