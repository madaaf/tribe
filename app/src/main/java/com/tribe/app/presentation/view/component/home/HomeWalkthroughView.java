package com.tribe.app.presentation.view.component.home;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/22/2017.
 */
public class HomeWalkthroughView extends FrameLayout {

  private static final int DURATION = 300;
  private static final float OVERSHOOT = 0.65f;

  private static final int STEP_BEGIN = 0;
  private static final int STEP_SLIDE_TO_CHAT = 1;
  private static final int STEP_SLIDE_TO_VIDEO = 2;
  private static final int STEP_HAVE_FUN_GAMES = 3;

  @Inject ScreenUtils screenUtils;

  @BindView(R.id.layoutVideo) FrameLayout layoutVideo;

  @BindView(R.id.viewVideo) HomeWalkthroughVideoView viewVideo;

  @BindView(R.id.txtWalkthrough) TextViewFont txtWalkthrough;

  @BindView(R.id.txtWalkthrough2) TextViewFont txtWalkthrough2;

  @BindView(R.id.btnNext) TextViewFont btnNext;

  @BindView(R.id.layoutInd) FrameLayout layoutInd;

  @BindView(R.id.viewInd) View viewInd;

  @BindView(R.id.viewBG) View viewBG;

  // VARIABLES
  private GradientDrawable gradientDrawable;
  private int step = STEP_BEGIN;

  // RESOURCES

  // OBSERVABLES
  private Unbinder unbinder;
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public HomeWalkthroughView(Context context) {
    super(context);
    init(context, null);
  }

  public HomeWalkthroughView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  @Override protected void onDetachedFromWindow() {
    unbinder.unbind();

    if (subscriptions != null && subscriptions.hasSubscriptions()) {
      subscriptions.unsubscribe();
    }

    super.onDetachedFromWindow();
  }

  @Override protected void onFinishInflate() {
    LayoutInflater.from(getContext()).inflate(R.layout.view_home_walkthrough, this);
    unbinder = ButterKnife.bind(this);
    ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent()
        .inject(this);

    initResources();
    initUI();
    initSubscriptions();

    super.onFinishInflate();
  }

  private void init(Context context, AttributeSet attrs) {

  }

  private void initUI() {
    setVisibility(View.GONE);

    gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.RECTANGLE);
    gradientDrawable.setCornerRadius(screenUtils.dpToPx(5));
    gradientDrawable.setColor(Color.WHITE);
    viewInd.setBackground(gradientDrawable);

    txtWalkthrough.setTranslationY(screenUtils.dpToPx(25));
    txtWalkthrough.setAlpha(0);
    txtWalkthrough.setText(
        EmojiParser.demojizedText(getResources().getString(R.string.walkthrough_message_step1)));

    txtWalkthrough2.setTranslationX(-screenUtils.getWidthPx() >> 1);
    txtWalkthrough2.setAlpha(0);

    btnNext.setEnabled(false);
    btnNext.setTranslationY(screenUtils.dpToPx(100));

    UIUtils.changeWidthOfView(layoutVideo, (int) (screenUtils.getWidthPx() * 0.7f));
    layoutVideo.setTranslationY(screenUtils.dpToPx(10));
    layoutVideo.setScaleX(1.2f);
    layoutVideo.setScaleY(1.2f);
    layoutVideo.setAlpha(0);
  }

  private void initResources() {

  }

  private void initSubscriptions() {
    subscriptions.add(viewVideo.onProgress().subscribe(time -> {
      if ((time >= 4200 && step == STEP_SLIDE_TO_CHAT) ||
          (time >= 6495 && step == STEP_SLIDE_TO_VIDEO) ||
          (time >= 13000 && step == STEP_HAVE_FUN_GAMES)) {
        btnNext.setEnabled(true);
        viewVideo.onPause(false);

        if (step == STEP_HAVE_FUN_GAMES) hide();
      }
    }));
  }

  private void translateIndToStep(int step, int delay) {
    int translateX = 0;
    final int color;

    if (step == STEP_SLIDE_TO_CHAT) {
      color = ContextCompat.getColor(getContext(), R.color.blue_text);
      translateX = -((layoutInd.getWidth() >> 1) - (viewInd.getWidth() >> 1));
    } else if (step == STEP_SLIDE_TO_VIDEO) {
      color = Color.WHITE;
      translateX = 0;
    } else if (step == STEP_HAVE_FUN_GAMES) {
      color = ContextCompat.getColor(getContext(), R.color.red);
      translateX = (layoutInd.getWidth() >> 1) - (viewInd.getWidth() >> 1);
    } else {
      color = Color.WHITE;
    }

    viewInd.animate()
        .translationX(translateX)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setStartDelay(delay)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationStart(Animator animation) {
            gradientDrawable.setColor(color);
            animation.removeAllListeners();
          }
        })
        .start();
  }

  @OnClick(R.id.btnNext) void clickNext() {
    viewVideo.play();
    btnNext.setEnabled(false);

    int delay = 0;

    if (step == STEP_BEGIN) {
      delay = 1500;
      step = STEP_SLIDE_TO_CHAT;
      btnNext.setText(R.string.walkthrough_action_step2);
      showTitle(highlightTextInText(
          EmojiParser.demojizedText(getResources().getString(R.string.walkthrough_message_step2)),
          getResources().getString(R.string.walkthrough_message_highlight_step2),
          R.style.Headline_BlueText_2), false, delay);
    } else if (step == STEP_SLIDE_TO_CHAT) {
      delay = 1050;
      step = STEP_SLIDE_TO_VIDEO;
      btnNext.setText(R.string.walkthrough_action_step3);
      showTitle(highlightTextInText(
          EmojiParser.demojizedText(getResources().getString(R.string.walkthrough_message_step3)),
          getResources().getString(R.string.walkthrough_message_highlight_step3),
          R.style.Headline_Red_2), true, delay);
    } else if (step == STEP_SLIDE_TO_VIDEO) {
      delay = 1550;
      step = STEP_HAVE_FUN_GAMES;
      showTitle(highlightTextInText(
          EmojiParser.demojizedText(getResources().getString(R.string.walkthrough_message_step4)),
          getResources().getString(R.string.walkthrough_message_highlight_step4),
          R.style.Headline_Red_2), true, delay);
    } else {
      hide();
      return;
    }

    translateIndToStep(step, delay);
  }

  private void showTitle(SpannableString title, boolean forward, int delay) {
    if (txtWalkthrough.getTranslationX() == 0) {
      txtWalkthrough2.setText(title);
      showTitle(txtWalkthrough2, forward, delay);
      hideTitle(txtWalkthrough, forward, delay);
    } else {
      txtWalkthrough.setText(title);
      hideTitle(txtWalkthrough2, forward, delay);
      showTitle(txtWalkthrough, forward, delay);
    }
  }

  private void hideTitle(View view, boolean forward, int delay) {
    if (forward) {
      view.animate()
          .translationX(-screenUtils.getWidthPx() >> 1)
          .alpha(0)
          .setDuration(DURATION)
          .setStartDelay(delay)
          .start();
    } else {
      view.animate()
          .alpha(0)
          .translationX(screenUtils.getWidthPx() >> 1)
          .setDuration(DURATION)
          .setStartDelay(delay)
          .start();
    }
  }

  private void showTitle(View view, boolean forward, int delay) {
    if (forward) {
      view.setTranslationX(screenUtils.getWidthPx() >> 1);
      view.setAlpha(0);
    } else {
      view.setTranslationX(-screenUtils.getWidthPx() >> 1);
      view.setAlpha(0);
    }

    view.animate().translationX(0).alpha(1).setDuration(DURATION).setStartDelay(delay).start();
  }

  private SpannableString highlightTextInText(String fullText, String highlightedText, int style) {
    SpannableString string = new SpannableString(fullText);
    int indexOf = fullText.indexOf(highlightedText);
    string.setSpan(new TextAppearanceSpan(getContext(), style), indexOf,
        indexOf + highlightedText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    return string;
  }

  //////////////
  //  PUBLIC  //
  //////////////

  public void show() {
    setVisibility(View.VISIBLE);

    viewBG.animate()
        .alpha(1)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    txtWalkthrough.animate()
        .alpha(1)
        .translationY(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setDuration(DURATION)
        .start();

    btnNext.animate()
        .translationY(0)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .setDuration(DURATION)
        .start();

    layoutVideo.animate()
        .translationY(0)
        .alpha(1)
        .scaleX(1)
        .scaleY(1)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator(OVERSHOOT))
        .start();

    viewVideo.play();
    viewVideo.onPause(false);
    btnNext.setEnabled(true);
  }

  public void hide() {
    viewVideo.releasePlayer();
    animate().alpha(0)
        .setDuration(DURATION)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationEnd(Animator animation) {
            setVisibility(View.GONE);
          }
        })
        .start();
  }

  //////////////////////
  //   OBSERVABLES    //
  //////////////////////
}

