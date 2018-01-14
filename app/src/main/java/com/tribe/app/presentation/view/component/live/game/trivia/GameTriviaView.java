package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionListenerAdapter;
import android.support.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.trivia.TriviaCategoryEnum;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaView extends GameViewWithRanking {

  private static final String ACTION_POP_ALIEN = "popAlien";
  private static final String ALIEN_KEY = "alien";

  private static final String ANSWER_KEY = "answer";
  private static final String QUESTION_KEY = "question";
  private static final String NB_QUESTION_KEY = "nbQuestion";
  private static final String NAME_KEY = "name";
  private static final String WINNER_KEY = "winner";
  private static final String TITLE_KEY = "title";
  private static final String ALL_KEY = "all";
  private static final String SHOW_WINNER_KEY = "showWinner";
  private static final String WINNERS_NAMES_KEY = "winnersNamesKey";
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
  @BindView(R.id.viewAnswers) GameTriviaAnswersView viewAnswers;

  // VARIABLES
  private TriviaCategoryEnum category;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private List<TriviaQuestion> questions;
  private String categoryTitle;
  private boolean categoryAll = false, weHaveAWinner = false;
  private String rightAnswer;
  private int nbAnswers = 0, nbPlayingPeers = 0;

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

    inflater.inflate(R.layout.view_game_trivia, this, true);
    unbinder = ButterKnife.bind(this);

    setClickable(true);

    progressBar.setProgressColor(Color.WHITE);
    progressBar.setProgressWidth(screenUtils.dpToPx(5));
    progressBar.setProgress(10);

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
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(layoutConstraint);
    constraintSet.setAlpha(R.id.groupInit, 1);
    animateLayoutWithConstraintSet(constraintSet, true);
  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet, boolean animated) {
    AutoTransition autoTransition = new AutoTransition();
    autoTransition.setDuration(animated ? 300 : 0);
    autoTransition.addListener(new TransitionListenerAdapter() {
      @Override public void onTransitionEnd(@NonNull Transition transition) {
        super.onTransitionEnd(transition);
      }
    });
    TransitionManager.beginDelayedTransition(layoutConstraint, autoTransition);
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
    if (questions != null && questions.size() > 0) {
      TriviaQuestion question = questions.remove(0);
      rightAnswer = question.getAnswer();
      nbAnswers = 0;

      int nbPlayersThatCanPlay = 0;
      for (TribeGuest guest : peerMap.values()) {
        if (guest.canPlayGames(game.getId())) nbPlayersThatCanPlay++;
      }

      nbPlayingPeers = 1 + nbPlayersThatCanPlay;
      sendAction(ACTION_SHOW_QUESTION, getShowQuestionPayload(question));
    }
  }

  @Override protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    if (jsonObject.has(game.getId())) {
      try {

        JSONObject message = jsonObject.getJSONObject(game.getId());

        if (message.has(ACTION_KEY)) {
          String actionKey = message.getString(ACTION_KEY);
          if (actionKey.equals(ACTION_PICK_CATEGORY)) {
            categoryTitle = message.getString(TITLE_KEY);
            categoryAll = message.has(ALL_KEY) ? message.getBoolean(ALL_KEY) : false;
          } else if (actionKey.equals(ACTION_SHOW_QUESTION)) {
            currentMasterId = tribeSession == null ? currentUser.getId() : tribeSession.getUserId();
            preloadQuestion(message, tribeSession);
            weHaveAWinner = false;
          } else if (actionKey.equals(ACTION_END_QUESTION)) {
            if (message.has(WINNER_KEY)) {
              TribeGuest guest = peerMap.get(message.getString(WINNER_KEY));
              if (guest.getId().equals(currentUser.getId())) {
                onAddScore.onNext(Pair.create(game.getId(), getScore(guest.getId())));
              }

              String text = getResources().getString(R.string.game_song_pop_status_winner,
                  guest.getDisplayName());
              showInstructions(Arrays.asList(new String[] { text }), true, true, false,
                  finished -> {

                  });
            } else {

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
    updateSteps(steps, false, () -> {
      if (completionListener != null) completionListener.finished(false);
      if (automaticallyReappear) {
        showQuestions(true, showRays, (LabelListener) () -> completionListener.finished(true));
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

  private void showQuestions(boolean animated, boolean showRays, LabelListener completionListener) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_question);
    animateLayoutWithConstraintSet(constraintSet, animated);
    if (completionListener != null) completionListener.call();
  }

  private void showInstructions(boolean animated, LabelListener completionListener) {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_title_only);
    animateLayoutWithConstraintSet(constraintSet, animated);
    if (completionListener != null) completionListener.call();
  }

  private void preloadQuestion(JSONObject message, TribeSession tribeSession) {
    if (message.has(QUESTION_KEY)) {
      try {
        TriviaQuestion currentQuestion = new TriviaQuestion(message.getJSONObject(QUESTION_KEY));
        int questionCount = message.has(NB_QUESTION_KEY) ? message.getInt(NB_QUESTION_KEY) : 1;

        List<String> steps = Arrays.asList(
            new String[] { getResources().getString(R.string.game_trivia_status_guess) });
        showInstructions(steps, true, true, true, finished -> {
          if (!finished) setupAnswers(currentQuestion, tribeSession);
        });

        txtQuestion.setText(currentQuestion.getQuestion());
        txtQuestionCount.setText("" + questionCount);
      } catch (JSONException ex) {
        ex.printStackTrace();
      }
    }
  }

  private void setupAnswers(TriviaQuestion triviaQuestion, TribeSession tribeSession) {
    viewAnswers.initQuestion(triviaQuestion);
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

  /**
   * PUBLIC
   */

  public void initSubscriptions() {
    subscriptions.add(viewAnswers.onAnsweredRight().subscribe(aVoid -> {

    }));

    subscriptions.add(viewAnswers.onAnsweredWrong().subscribe(aVoid -> {

    }));
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, mapObservable, liveViewsObservable, userId);

    gamePresenter.getTriviaData();
  }

  @Override public void stop() {
    super.stop();
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
