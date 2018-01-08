package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.tribe.app.R;
import com.tribe.app.domain.entity.trivia.TriviaCategoryEnum;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaView extends GameViewWithRanking {

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
  @BindView(R.id.groupInit) Group groupInit;
  @BindView(R.id.progressBar) CircularProgressBar progressBar;
  @BindView(R.id.viewAnswers) GameTriviaAnswersView viewAnswers;

  // VARIABLES
  private TriviaCategoryEnum category;

  public GameTriviaView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_trivia, this, true);
    unbinder = ButterKnife.bind(this);

    setClickable(true);

    progressBar.setProgressColor(Color.WHITE);
    progressBar.setProgressWidth(screenUtils.dpToPx(5));
    progressBar.setProgress(10);
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(jsonObject -> {
          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
              if (message.has(ACTION_KEY)) {

              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  @Override protected void takeOverGame() {

  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet) {
    AutoTransition autoTransition = new AutoTransition();
    autoTransition.setDuration(300);
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
    selectCategory(TriviaCategoryEnum.CELEBS);
  }

  /**
   * JSON PAYLOAD
   */

  /**
   * PUBLIC
   */

  public void showCategories() {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(layoutConstraint);
    constraintSet.setAlpha(R.id.groupInit, 1);
    animateLayoutWithConstraintSet(constraintSet);
  }

  public void selectCategory(TriviaCategoryEnum triviaCategoryEnum) {
    category = triviaCategoryEnum;

    TextViewCompat.setTextAppearance(txtTitle, R.style.Headline_White_2);
    txtTitle.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_title_only);
    animateLayoutWithConstraintSet(constraintSet);

    subscriptions.add(Observable.timer(1000, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> startFirstQuestion()));
  }

  public void startFirstQuestion() {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_trivia_question);
    animateLayoutWithConstraintSet(constraintSet);

    Collections.shuffle(category.getQuestions());
    TriviaQuestion triviaQuestion = category.getQuestions().get(0);
    txtTitle.setText(triviaQuestion.getQuestion());
    viewAnswers.initQuestion(triviaQuestion);
  }

  @Override public void start(Game game, Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, mapObservable, liveViewsObservable, userId);
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
