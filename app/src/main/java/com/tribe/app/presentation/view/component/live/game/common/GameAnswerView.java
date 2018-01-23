package com.tribe.app.presentation.view.component.live.game.common;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.text.Html;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.FontUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameAnswerView extends LinearLayout {

  @IntDef({ TYPE_TRIVIA, TYPE_BATTLE_MUSIC }) @Retention(RetentionPolicy.SOURCE)
  public @interface AnswerType {
  }

  public static final int TYPE_TRIVIA = 0;
  public static final int TYPE_BATTLE_MUSIC = 1;

  private static final int DURATION = 300;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutIcon) FrameLayout layoutIcon;

  @BindView(R.id.imgNotGuessed) ImageView imgNotGuessed;

  @BindView(R.id.imgGuessed) ImageView imgGuessed;

  @BindView(R.id.txtAnswer) TextViewFont txtAnswer;

  // VARIABLES
  private Unbinder unbinder;
  private GradientDrawable background;
  private String answer;
  private int color;
  private @AnswerType int type;

  // RESOURCES
  private int redColor, greenColor;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onClick = PublishSubject.create();

  public GameAnswerView(@NonNull Context context) {
    super(context);
  }

  public GameAnswerView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void init() {
    initDependencyInjector();
    initResources();
    initUI();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  private void initResources() {
    redColor = ContextCompat.getColor(getContext(), R.color.red);
    greenColor = ContextCompat.getColor(getContext(), R.color.green_status);
  }

  private void initDependencyInjector() {
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);
  }

  private void initUI() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_game_answer, this);
    unbinder = ButterKnife.bind(this);

    background = new GradientDrawable();
    background.setShape(GradientDrawable.RECTANGLE);
    setBackground(background);

    setClickable(true);
    setGravity(Gravity.CENTER);
    setOrientation(HORIZONTAL);

    setMinimumHeight(screenUtils.dpToPx(51));

    setOnClickListener(v -> {
      onClick.onNext(answer);
      setClickable(false);
    });
  }

  private void initSubscriptions() {

  }

  private void setDefaultStyle() {
    if (type == TYPE_TRIVIA) {
      TextViewCompat.setTextAppearance(txtAnswer, R.style.Headline_White_2);
    } else {
      TextViewCompat.setTextAppearance(txtAnswer, R.style.Headline_Black_2);
    }

    txtAnswer.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
    layoutIcon.setVisibility(View.GONE);
    UIUtils.changeLeftMarginOfView(txtAnswer, screenUtils.dpToPx(25));
    setClickable(true);
  }

  private void setRedStyle() {
    txtAnswer.setTextColor(redColor);
  }

  private void setGreenStyle() {
    txtAnswer.setTextColor(greenColor);
  }

  /**
   * PUBLIC
   */

  public void initAnswer(String answer, int color, @AnswerType int type) {
    this.type = type;

    this.answer = answer;
    this.color = color;
    setAlpha(1);
    txtAnswer.setText(Html.fromHtml(answer));
    background.setColor(color);

    if (type == TYPE_BATTLE_MUSIC) {
      background.setCornerRadius(screenUtils.dpToPx(400));
    } else {
      background.setCornerRadius(screenUtils.dpToPx(6));
    }

    setDefaultStyle();
  }

  public void showRightAnswer() {
    setAlpha(1f);
    background.setColor(Color.WHITE);
    setGreenStyle();

    imgGuessed.setVisibility(View.VISIBLE);
    imgNotGuessed.setVisibility(View.GONE);
    layoutIcon.setVisibility(View.VISIBLE);
    UIUtils.changeLeftMarginOfView(txtAnswer, 0);
  }

  public void showWrongAnswer() {
    setAlpha(1f);
    background.setColor(Color.WHITE);
    setRedStyle();

    imgGuessed.setVisibility(View.GONE);
    imgNotGuessed.setVisibility(View.VISIBLE);
    layoutIcon.setVisibility(View.VISIBLE);
    UIUtils.changeLeftMarginOfView(txtAnswer, 0);
  }

  public void showBogusAnswer() {
    setDefaultStyle();
    background.setColor(color);
    layoutIcon.setVisibility(View.GONE);
    setAlpha(0.2f);
  }

  public void animateBogusAnswer() {
    layoutIcon.setVisibility(View.GONE);
    UIUtils.changeLeftMarginOfView(txtAnswer, screenUtils.dpToPx(25));
    setAlpha(0.2f);
    background.setColor(color);

    if (type == TYPE_TRIVIA) {
      txtAnswer.setTextColor(Color.WHITE);
    } else {
      txtAnswer.setTextColor(Color.BLACK);
    }
  }

  public void animateRightAnswer() {
    setAlpha(1f);
    background.setColor(Color.WHITE);
    txtAnswer.setTextColor(greenColor);
    imgGuessed.setVisibility(View.VISIBLE);
    imgNotGuessed.setVisibility(View.GONE);
    layoutIcon.setVisibility(View.VISIBLE);
    UIUtils.changeLeftMarginOfView(txtAnswer, 0);
  }

  public void showDone() {
    LayoutTransition transition = new LayoutTransition();
    transition.setDuration(300);
    setLayoutTransition(transition);
  }

  public void hide() {
    setLayoutTransition(null);
  }

  public String getAnswer() {
    return answer;
  }

  /**
   * OBSERVABLES
   */

  public Observable<String> onClick() {
    return onClick;
  }
}
