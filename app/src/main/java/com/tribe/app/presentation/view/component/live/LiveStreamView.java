package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/12/17.
 */
public abstract class LiveStreamView extends LinearLayout {

  @IntDef({ TYPE_GRID, TYPE_LIST }) public @interface StreamType {
  }

  public static final int TYPE_GRID = 0;
  public static final int TYPE_LIST = 1;

  public static final int MAX_HEIGHT_LIST = 45;

  @Inject protected ScreenUtils screenUtils;

  @Inject protected User user;

  @BindView(R.id.viewPeerOverlay) LivePeerOverlayView viewPeerOverlay;

  @BindView(R.id.layoutStream) CardView layoutStream;

  @BindView(R.id.txtScore) TextViewFont txtScore;

  @BindView(R.id.txtEmoji) TextViewFont txtEmoji;

  // VARIABLES
  protected Unbinder unbinder;
  protected int score;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();

  public LiveStreamView(Context context) {
    super(context);
    init();
  }

  public LiveStreamView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public LiveStreamView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  protected abstract void init();

  protected void endInit() {
    setOrientation(HORIZONTAL);

    layoutStream.setPreventCornerOverlap(false);
    layoutStream.setMaxCardElevation(0);
    layoutStream.setCardElevation(0);
  }

  public void dispose() {
    subscriptions.clear();
  }

  public void setStyle(@StreamType int type) {
    if (type == TYPE_LIST) {
      layoutStream.setRadius(screenUtils.dpToPx(5));
      UIUtils.changeWidthOfView(layoutStream, screenUtils.dpToPx(MAX_HEIGHT_LIST));
      txtScore.setVisibility(VISIBLE);
      txtEmoji.setVisibility(VISIBLE);
    } else {
      layoutStream.setRadius(0);
      UIUtils.changeWidthOfView(layoutStream, ViewGroup.LayoutParams.MATCH_PARENT);
      txtScore.setVisibility(GONE);
      txtEmoji.setVisibility(GONE);
    }
  }

  public void updateScoreWithEmoji(int score, String emoji) {
    this.score = score;

    if (score == 0) {
      txtScore.setAlpha(0.5f);
    } else {
      txtScore.setAlpha(1f);
    }

    txtScore.setText("" + score);
    txtEmoji.setText(emoji);
  }

  public void bounceView() {
    clearAnimation();
    setScaleX(1);
    setScaleY(1);

    animate().scaleX(1.1f)
        .scaleY(1.1f)
        .setInterpolator(new OvershootInterpolator(0.75f))
        .setDuration(300)
        .setListener(new AnimatorListenerAdapter() {
          @Override public void onAnimationCancel(Animator animation) {
            super.onAnimationCancel(animation);
            animation.removeAllListeners();
          }

          @Override public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            animation.removeAllListeners();
            animate().setListener(null)
                .scaleX(1)
                .scaleY(1)
                .setInterpolator(new DecelerateInterpolator())
                .setDuration(300)
                .start();
          }
        })
        .start();
  }

  public int getScore() {
    return score;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////
}
