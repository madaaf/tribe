package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.trivia.TriviaQuestion;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
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
  private String title;
  private Drawable icon;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public GameTriviaAnswersView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaAnswersView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GameTriviaCategoryView);
    icon = a.getDrawable(R.styleable.GameTriviaCategoryView_categoryIcon);
    title = a.getString(R.styleable.GameTriviaCategoryView_categoryTitle);
    a.recycle();

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

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_trivia_answers_view, this);
    unbinder = ButterKnife.bind(this);

    setOrientation(VERTICAL);
  }

  private void initSubscriptions() {

  }

  /**
   * PUBLIC
   */

  public void initQuestion(TriviaQuestion triviaQuestion) {
    Collections.shuffle(listAnswerViews);
    GameTriviaAnswerView rightAnswer = listAnswerViews.get(0);
    rightAnswer.initAnswer(triviaQuestion.getAnswer());

    for (int i = 1; i < listAnswerViews.size() - 1; i++)
      listAnswerViews.get(i).initAnswer(triviaQuestion.getAlternativeAnswers().get(i));
  }

  /**
   * OBSERVABLES
   */

}
