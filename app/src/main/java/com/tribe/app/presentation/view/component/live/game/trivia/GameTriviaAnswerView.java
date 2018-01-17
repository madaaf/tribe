package com.tribe.app.presentation.view.component.live.game.trivia;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
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
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/21/2017.
 */

public class GameTriviaAnswerView extends LinearLayout {

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

  // RESOURCES
  private int paddingHorizontalWithIcon, paddingVerticalWithIcon, paddingHorizontal,
      paddingVertical;
  private int redColor, greenColor;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<String> onClick = PublishSubject.create();

  public GameTriviaAnswerView(@NonNull Context context) {
    super(context);
  }

  public GameTriviaAnswerView(@NonNull Context context, @Nullable AttributeSet attrs) {
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
    paddingHorizontal = screenUtils.dpToPx(25);
    paddingVertical = screenUtils.dpToPx(15);
    paddingHorizontalWithIcon = screenUtils.dpToPx(15);
    paddingVerticalWithIcon = screenUtils.dpToPx(10);
    redColor = ContextCompat.getColor(getContext(), R.color.red);
    greenColor = ContextCompat.getColor(getContext(), R.color.green_status);
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
    setGravity(Gravity.CENTER);
    setOrientation(HORIZONTAL);
    setOnClickListener(v -> {
      onClick.onNext(answer);
      setClickable(false);
    });
    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
  }

  private void initSubscriptions() {

  }

  private void setDefaultStyle() {
    TextViewCompat.setTextAppearance(txtAnswer, R.style.Headline_White_2);
    txtAnswer.setCustomFont(getContext(), FontUtils.PROXIMA_BOLD);
    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
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

  public void initAnswer(String answer, int color) {
    setLayoutTransition(null);

    this.answer = answer;
    this.color = color;
    layoutIcon.setVisibility(View.GONE);
    setAlpha(1);
    txtAnswer.setText(answer);
    background.setColor(color);
    setDefaultStyle();
    setClickable(true);
  }

  public void showRightAnswer() {
    setAlpha(1f);
    background.setColor(Color.WHITE);
    layoutIcon.setVisibility(View.VISIBLE);
    setGreenStyle();

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

    setPadding(paddingHorizontalWithIcon, paddingVerticalWithIcon, paddingHorizontalWithIcon,
        paddingVerticalWithIcon);
  }

  public void showWrongAnswer() {
    setAlpha(1f);
    background.setColor(Color.WHITE);
    layoutIcon.setVisibility(View.VISIBLE);
    setRedStyle();

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

    setPadding(paddingHorizontalWithIcon, paddingVerticalWithIcon, paddingHorizontalWithIcon,
        paddingVerticalWithIcon);
  }

  public void showBogusAnswer() {
    setDefaultStyle();
    background.setColor(color);
    layoutIcon.setVisibility(View.GONE);
    setAlpha(0.2f);
  }

  public void animateBogusAnswer() {
    //LayoutTransition layoutTransition = new LayoutTransition();
    //layoutTransition.setDuration(DURATION);
    //layoutTransition.addTransitionListener(new LayoutTransition.TransitionListener() {
    //  @Override
    //  public void startTransition(LayoutTransition transition, ViewGroup container, View view,
    //      int transitionType) {
    //  }
    //
    //  @Override
    //  public void endTransition(LayoutTransition transition, ViewGroup container, View view,
    //      int transitionType) {
    //
    //  }
    //});
    //setLayoutTransition(layoutTransition);
    //layoutIcon.setVisibility(View.GONE);

    //animate().alpha(0.2f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
    //
    //AnimationUtils.animateBGColor(this, Color.WHITE, color, DURATION);
    //AnimationUtils.animateTextColor(txtAnswer, redColor, Color.WHITE, DURATION);
    //

    setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);
    layoutIcon.setVisibility(View.GONE);
    setAlpha(0.2f);
    background.setColor(color);
    txtAnswer.setTextColor(Color.WHITE);
  }

  public void animateRightAnswer() {
    setAlpha(1f);
    background.setColor(Color.WHITE);
    txtAnswer.setTextColor(greenColor);

    //animate().alpha(1f).setDuration(300).setInterpolator(new DecelerateInterpolator()).start();
    //
    //AnimationUtils.animateBGColor(this, color, Color.WHITE, DURATION);
    //AnimationUtils.animateTextColor(txtAnswer, Color.WHITE, greenColor, DURATION);

    imgGuessed.setAlpha(1f);
    imgNotGuessed.setAlpha(0f);
    layoutIcon.setVisibility(View.VISIBLE);

    setPadding(paddingHorizontalWithIcon, paddingVerticalWithIcon, paddingHorizontalWithIcon,
        paddingVerticalWithIcon);
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
