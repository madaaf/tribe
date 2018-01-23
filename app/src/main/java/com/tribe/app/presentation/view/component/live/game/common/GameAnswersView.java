package com.tribe.app.presentation.view.component.live.game.common;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameAnswersView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindViews({
      R.id.viewAnswerFirst, R.id.viewAnswerSecond, R.id.viewAnswerThird, R.id.viewAnswerFourth
  }) List<GameAnswerView> listAnswerViews;

  // VARIABLES
  private Unbinder unbinder;
  private Integer[] colors;
  private GameAnswerView rightAnswerView, clickedAnswerView;
  private boolean enabled = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription questionSubscriptions = new CompositeSubscription();
  private PublishSubject<GameAnswerView> onAnsweredRight = PublishSubject.create();
  private PublishSubject<GameAnswerView> onAnsweredWrong = PublishSubject.create();

  public GameAnswersView(@NonNull Context context) {
    super(context);
  }

  public GameAnswersView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void init() {
    initResources();
    initDependencyInjector();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {
    colors = new Integer[] {
        R.color.trivia_answer_1, R.color.trivia_answer_2, R.color.trivia_answer_3,
        R.color.trivia_answer_4
    };
  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_answers_view, this);
    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
  }

  private void initSubscriptions() {

  }

  @Override public boolean onInterceptTouchEvent(MotionEvent ev) {
    return !enabled || super.onInterceptTouchEvent(ev);
  }

  /**
   * PUBLIC
   */

  public void initQuestion(String answer, List<String> alternativeAnswers,
      @GameAnswerView.AnswerType int answerType) {
    if (questionSubscriptions != null) questionSubscriptions.clear();

    int random = new Random().nextInt(listAnswerViews.size());
    rightAnswerView = listAnswerViews.get(random);
    rightAnswerView.initAnswer(answer, answerType == GameAnswerView.TYPE_BATTLE_MUSIC ? Color.WHITE
        : ContextCompat.getColor(getContext(), colors[random]), answerType);

    questionSubscriptions.add(
        rightAnswerView.onClick().subscribe(value -> onAnsweredRight.onNext(rightAnswerView)));

    for (int i = 0; i < listAnswerViews.size(); i++) {
      if (i != random) {
        GameAnswerView answerView = listAnswerViews.get(i);
        String alternativeAnswer =
            (i > random ? alternativeAnswers.get(i - 1) : alternativeAnswers.get(i));
        if (alternativeAnswer.equals(answer)) return;
        answerView.initAnswer(alternativeAnswer,
            answerType == GameAnswerView.TYPE_BATTLE_MUSIC ? Color.WHITE
                : ContextCompat.getColor(getContext(), colors[i]), answerType);

        questionSubscriptions.add(
            answerView.onClick().subscribe(value -> onAnsweredWrong.onNext(answerView)));
      }
    }
  }

  public void computeAnswers(GameAnswerView clickedAnswer, boolean isRight) {
    this.clickedAnswerView = clickedAnswer;
    if (isRight) {
      clickedAnswerView.showRightAnswer();
    } else {
      clickedAnswerView.showWrongAnswer();
    }

    for (int i = 0; i < listAnswerViews.size(); i++) {
      GameAnswerView answerView = listAnswerViews.get(i);
      if (answerView != clickedAnswerView) answerView.showBogusAnswer();
    }
  }

  public void animateAnswerResult() {
    if (clickedAnswerView == rightAnswerView) return;
    if (clickedAnswerView != null) clickedAnswerView.animateBogusAnswer();

    subscriptions.add(Observable.timer(300, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> rightAnswerView.animateRightAnswer()));
  }

  public void showDone() {
    for (GameAnswerView v : listAnswerViews) v.showDone();
  }

  public void hide() {
    for (GameAnswerView v : listAnswerViews) v.hide();
  }

  public void enableClicks(boolean enable) {
    this.enabled = enable;
  }

  /**
   * OBSERVABLES
   */

  public Observable<GameAnswerView> onAnsweredRight() {
    return onAnsweredRight;
  }

  public Observable<GameAnswerView> onAnsweredWrong() {
    return onAnsweredWrong;
  }
}
