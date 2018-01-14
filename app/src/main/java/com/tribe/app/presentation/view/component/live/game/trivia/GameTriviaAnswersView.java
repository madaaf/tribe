package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaAnswersView extends LinearLayout {

  @Inject ScreenUtils screenUtils;

  @BindViews({
      R.id.viewAnswerFirst, R.id.viewAnswerSecond, R.id.viewAnswerThird, R.id.viewAnswerFourth
  }) List<GameTriviaAnswerView> listAnswerViews;

  // VARIABLES
  private Unbinder unbinder;
  private Integer[] colors;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private CompositeSubscription questionSubscriptions = new CompositeSubscription();
  private PublishSubject<Void> onAnsweredRight = PublishSubject.create();
  private PublishSubject<Void> onAnsweredWrong = PublishSubject.create();

  public GameTriviaAnswersView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaAnswersView(@NonNull Context context, @Nullable AttributeSet attrs) {
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
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_trivia_answers_view, this);
    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
    setGravity(Gravity.CENTER);
  }

  private void initSubscriptions() {

  }

  /**
   * PUBLIC
   */

  public void initQuestion(TriviaQuestion triviaQuestion) {
    int random = new Random().nextInt(listAnswerViews.size());
    GameTriviaAnswerView rightAnswer = listAnswerViews.get(random);
    rightAnswer.initAnswer(triviaQuestion.getAnswer());
    rightAnswer.setAnswerBackground(ContextCompat.getColor(getContext(), colors[random]));

    questionSubscriptions.add(
        rightAnswer.onClick().subscribe(aVoid -> onAnsweredRight.onNext(null)));

    for (int i = 0; i < listAnswerViews.size() - 1; i++) {
      if (i != random) {
        GameTriviaAnswerView answerView = listAnswerViews.get(i);
        answerView.initAnswer(triviaQuestion.getAlternativeAnswers().get(i));
        answerView.setAnswerBackground(ContextCompat.getColor(getContext(), colors[i]));

        questionSubscriptions.add(
            answerView.onClick().subscribe(aVoid -> onAnsweredWrong.onNext(null)));
      }
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onAnsweredRight() {
    return onAnsweredRight;
  }

  public Observable<Void> onAnsweredWrong() {
    return onAnsweredWrong;
  }
}
