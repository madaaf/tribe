package com.tribe.app.presentation.view.component.live;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.annotation.IntDef;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.entity.coolcams.CoolCamsModel;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.facetracking.VisionAPIManager;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.TextureViewRenderer;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

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

  @BindView(R.id.imgCoolCams) ImageView imgCoolCams;

  // VARIABLES
  protected Unbinder unbinder;
  protected int score;
  protected VisionAPIManager visionAPIManager;
  protected float widthScaleFactor, heightScaleFactor;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected Subscription visionSubscription;
  protected PublishSubject<Pair<Integer, String>> onScoreChange = PublishSubject.create();

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

  protected abstract PeerView getPeerView();

  protected void endInit() {
    visionAPIManager = VisionAPIManager.getInstance(getContext());

    setOrientation(HORIZONTAL);
    layoutStream.setCardBackgroundColor(PaletteGrid.getRandomColorExcluding(Color.BLACK));
    layoutStream.setPreventCornerOverlap(false);
    layoutStream.setMaxCardElevation(0);
    layoutStream.setCardElevation(0);
    layoutStream.setRadius(0);
  }

  /**
   * Adjusts a horizontal value of the supplied value from the preview scale to the view
   * scale.
   */
  protected float scaleX(float horizontal) {
    return horizontal * widthScaleFactor;
  }

  /**
   * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
   */
  protected float scaleY(float vertical) {
    return vertical * heightScaleFactor;
  }

  /**
   * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
   * system.
   */
  protected float translateX(float x, boolean isFrontFacing) {
    if (isFrontFacing) {
      return getMeasuredWidth() - scaleX(x);
    } else {
      return scaleX(x);
    }
  }

  /**
   * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
   * system.
   */
  protected float translateY(float y) {
    return scaleY(y);
  }

  /**
   * PUBLIC
   */

  public void dispose() {
    subscriptions.clear();
    if (visionSubscription != null) visionSubscription.unsubscribe();
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
    onScoreChange.onNext(Pair.create(score, emoji));
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

  public void setStep(CoolCamsModel.CoolCamsStepsEnum step) {
    if (step != null) {
      Timber.d("Setting stepId to : " + step.getStep());
      imgCoolCams.setImageResource(step.getIcon());
      imgCoolCams.setVisibility(View.VISIBLE);
    } else {
      if (visionSubscription != null) {
        visionSubscription.unsubscribe();
        visionSubscription = null;
      }

      imgCoolCams.setImageDrawable(null);
      imgCoolCams.setVisibility(View.GONE);
    }
  }

  public void updatePositionOfSticker(PointF pointF) {
    UIUtils.changeLeftMarginOfView(imgCoolCams,
        (int) pointF.x - (imgCoolCams.getMeasuredWidth() >> 1));
    UIUtils.changeTopMarginOfView(imgCoolCams,
        (int) pointF.y - (imgCoolCams.getMeasuredHeight() >> 1));
  }

  public void updatePositionRatioOfSticker(double xRatio, double yRatio) {
    TextureViewRenderer renderer = getPeerView().getTextureViewRenderer();
    PointF pointF = new PointF(renderer.getFrameWidth() * (float) xRatio,
        renderer.getFrameHeight() * (float) yRatio);

    widthScaleFactor = (float) getMeasuredWidth() / (float) renderer.getFrameWidth();
    heightScaleFactor = (float) getMeasuredHeight() / (float) renderer.getFrameHeight();

    PointF pointEnd;

    if (pointF != null) {
      pointEnd = new PointF(translateX(pointF.x, !getPeerView().isMirror()), translateY(pointF.y));
    } else {
      pointEnd = new PointF();
      pointEnd.x = getMeasuredWidth() >> 1;
      pointEnd.y = getMeasuredHeight() >> 1;
    }

    updatePositionOfSticker(pointEnd);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Pair<Integer, String>> onScoreChange() {
    return onScoreChange;
  }
}
