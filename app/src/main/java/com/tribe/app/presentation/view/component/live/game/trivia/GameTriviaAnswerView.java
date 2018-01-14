package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaAnswerView extends FrameLayout {

  private static final int DURATION = 300;

  @IntDef({ ANSWER, RIGHT_ANSWER_GUESSED, RIGHT_ANSWER_NOT_GUESSED, WRONG_ANSWER })
  public @interface CellType {
  }

  public static final int ANSWER = 0;
  public static final int RIGHT_ANSWER_GUESSED = 1;
  public static final int RIGHT_ANSWER_NOT_GUESSED = 2;
  public static final int WRONG_ANSWER = 3;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.imgNotGuessed) ImageView imgNotGuessed;

  @BindView(R.id.imgGuessed) ImageView imgGuessed;

  @BindView(R.id.txtAnswer) TextViewFont txtAnswer;

  // VARIABLES
  private Unbinder unbinder;
  private GradientDrawable background;
  private String answer;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onClick = PublishSubject.create();

  public GameTriviaAnswerView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaAnswerView(@NonNull Context context, @Nullable AttributeSet attrs) {
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

  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_trivia_answer, this);
    unbinder = ButterKnife.bind(this);

    background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    background.setCornerRadius(screenUtils.dpToPx(6));
    setBackground(background);

    setClickable(true);
    setForeground(ContextCompat.getDrawable(getContext(), R.drawable.selectable_button));
    setOnClickListener(v -> onClick.onNext(null));
  }

  private void initSubscriptions() {

  }

  /**
   * PUBLIC
   */

  public void initAnswer(String answer) {
    this.answer = answer;
    setType(ANSWER);
    txtAnswer.setText(answer);
  }

  public void setAnswerBackground(int color) {
    background.setColor(color);
  }

  public void setType(@CellType int type) {
    if (type != WRONG_ANSWER) {
      TextViewCompat.setTextAppearance(txtAnswer, R.style.Headline_White_2);
      txtAnswer.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
    } else {
      TextViewCompat.setTextAppearance(txtAnswer, R.style.Headline_Black_2);
      txtAnswer.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
    }

    switch (type) {
      case ANSWER:
        imgGuessed.setAlpha(0f);
        imgNotGuessed.setAlpha(0f);
        break;

      case RIGHT_ANSWER_GUESSED:
        background.setColor(Color.WHITE);
        imgGuessed.animate()
            .alpha(1)
            .setInterpolator(new DecelerateInterpolator())
            .setDuration(DURATION)
            .start();
        imgNotGuessed.animate()
            .alpha(0)
            .setInterpolator(new DecelerateInterpolator())
            .setDuration(DURATION)
            .start();
        break;

      case RIGHT_ANSWER_NOT_GUESSED:
        background.setColor(Color.WHITE);
        imgGuessed.animate()
            .alpha(0)
            .setInterpolator(new DecelerateInterpolator())
            .setDuration(DURATION)
            .start();

        imgNotGuessed.animate()
            .alpha(1)
            .setInterpolator(new DecelerateInterpolator())
            .setDuration(DURATION)
            .start();
        break;

      case WRONG_ANSWER:
        imgGuessed.setAlpha(0f);
        imgNotGuessed.setAlpha(0f);
        background.setColor(Color.WHITE);
        animate().alpha(0.2f)
            .setInterpolator(new DecelerateInterpolator())
            .setDuration(DURATION)
            .start();
        break;
    }
  }

  /**
   * OBSERVABLES
   */

  public Observable<Void> onClick() {
    return onClick;
  }
}
