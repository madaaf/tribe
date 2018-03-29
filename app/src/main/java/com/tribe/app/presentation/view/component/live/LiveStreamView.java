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
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.entity.coolcams.CoolCamsModel;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.TextureViewRenderer;
import com.tribe.tribelivesdk.webrtc.Frame;
import java.util.List;
import javax.inject.Inject;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/12/17.
 */
public abstract class LiveStreamView extends LinearLayout {

  @IntDef({ TYPE_GRID, TYPE_LIST, TYPE_EMBED }) public @interface StreamType {
  }

  public static final int TYPE_GRID = 0;
  public static final int TYPE_LIST = 1;
  public static final int TYPE_EMBED = 2;

  public static final int MAX_HEIGHT_LIST = 45;
  public static final int MAX_HEIGHT_EMBED = 75;

  @Inject protected ScreenUtils screenUtils;

  @Inject protected User user;

  @BindView(R.id.viewPeerOverlay) LivePeerOverlayView viewPeerOverlay;

  @BindView(R.id.layoutStream) CardView layoutStream;

  @BindView(R.id.txtScore) TextViewFont txtScore;

  @BindView(R.id.txtEmoji) TextViewFont txtEmoji;

  @BindView(R.id.layoutCoolCams) FrameLayout layoutCoolCams;

  @BindView(R.id.imgCoolCamsWon) ImageView imgCoolCamsWon;

  @BindView(R.id.imgCoolCamsWonBg) ImageView imgCoolCamsWonBg;

  @BindView(R.id.imgCoolCams) ImageView imgCoolCams;

  @BindView(R.id.bgBattleRoyale) ImageView bgBattleRoyale;

  @BindView(R.id.layoutMasterStream) FrameLayout layoutMasterStream;

  // VARIABLES
  protected Unbinder unbinder;
  protected int score;
  protected float widthScaleFactor, heightScaleFactor;
  protected CoolCamsModel.CoolCamsStatusEnum previousStatus;
  protected RotateAnimation rotateAnimation;

  // OBSERVABLES
  protected CompositeSubscription subscriptions = new CompositeSubscription();
  protected PublishSubject<Pair<Integer, String>> onScoreChange = PublishSubject.create();
  protected PublishSubject<List<CoolCamsModel.CoolCamsFeatureEnum>> onFeaturesDetected =
      PublishSubject.create();
  protected PublishSubject<Pair<Integer, Integer>> onUpdateXYOffset = PublishSubject.create();

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

  private void scaleView(View v, boolean up, android.animation.AnimatorListenerAdapter listener) {
    v.animate()
        .scaleX(up ? 1 : 0)
        .scaleY(up ? 1 : 0)
        .setDuration(150)
        .setListener(listener)
        .start();
  }

  /**
   * PUBLIC
   */

  public void dispose() {
    subscriptions.clear();
    rotateAnimation.cancel();
    imgCoolCamsWonBg.clearAnimation();
  }

  public void setStyle(@LiveRoomView.RoomUIType int type) {
    if (type == TYPE_EMBED) {
      bgBattleRoyale.setVisibility(View.VISIBLE);
      layoutStream.setRadius(screenUtils.dpToPx((float) MAX_HEIGHT_LIST / 2f));
      UIUtils.changeWidthHeightOfView(layoutMasterStream, screenUtils.dpToPx(MAX_HEIGHT_EMBED),
          screenUtils.dpToPx(MAX_HEIGHT_EMBED));
      UIUtils.changeWidthHeightOfView(layoutStream, screenUtils.dpToPx(MAX_HEIGHT_LIST),
          screenUtils.dpToPx(MAX_HEIGHT_LIST));
      txtScore.setVisibility(GONE);
      txtEmoji.setVisibility(GONE);
    } else if (type == TYPE_LIST) {
      bgBattleRoyale.setVisibility(View.GONE);
      layoutStream.setRadius(screenUtils.dpToPx(5));
      UIUtils.changeWidthHeightOfView(layoutMasterStream, screenUtils.dpToPx(MAX_HEIGHT_LIST),
          screenUtils.dpToPx(MAX_HEIGHT_LIST));
      UIUtils.changeWidthHeightOfView(layoutStream, screenUtils.dpToPx(MAX_HEIGHT_LIST),
          screenUtils.dpToPx(MAX_HEIGHT_LIST));
      txtScore.setVisibility(VISIBLE);
      txtEmoji.setVisibility(VISIBLE);
    } else {
      bgBattleRoyale.setVisibility(View.GONE);
      layoutStream.setRadius(0);
      getPeerView().setPadding(0, 0, 0, 0);
      UIUtils.changeWidthHeightOfView(layoutStream, ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
      UIUtils.changeWidthHeightOfView(layoutMasterStream, ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.MATCH_PARENT);
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

  public Pair<Double, Double> computeFrameAndFacePosition(Frame frame, PointF middleEyesPosition,
      CoolCamsModel.CoolCamsStatusEnum statusEnum) {
    widthScaleFactor = (float) getMeasuredWidth() / (float) frame.rotatedWidth();
    heightScaleFactor = (float) getMeasuredHeight() / (float) frame.rotatedHeight();

    PointF middleEyesTranslatedPosition = null;

    if (middleEyesPosition != null) {
      middleEyesTranslatedPosition =
          new PointF(translateX(middleEyesPosition.x, frame.isFrontCamera()),
              translateY(middleEyesPosition.y));
      middleEyesTranslatedPosition.x = (int) middleEyesTranslatedPosition.x;
      middleEyesTranslatedPosition.y = (int) middleEyesTranslatedPosition.y;

      updatePositionOfSticker(middleEyesTranslatedPosition, statusEnum);

      return Pair.create(middleEyesPosition.x / (double) frame.rotatedWidth(),
          middleEyesPosition.y / (double) frame.rotatedHeight());
    }

    updatePositionOfSticker(middleEyesTranslatedPosition, statusEnum);
    return null;
  }

  public void updatePositionOfSticker(PointF pointF, CoolCamsModel.CoolCamsStatusEnum status) {
    if (pointF == null || status == null) {
      previousStatus = null;
      layoutCoolCams.setVisibility(View.INVISIBLE);
    } else {
      layoutCoolCams.setVisibility(View.VISIBLE);

      if (status.equals(CoolCamsModel.CoolCamsStatusEnum.STEP) ||
          status.equals(CoolCamsModel.CoolCamsStatusEnum.LOST)) {
        int img = 0;
        if (status.equals(CoolCamsModel.CoolCamsStatusEnum.LOST)) {
          img = R.drawable.picto_coolcams_lost;
        } else {
          img = status.getStep().getIcon();
        }

        final int res = img;

        if (previousStatus == null || !previousStatus.equals(status)) {
          if (previousStatus != null &&
              previousStatus.equals(CoolCamsModel.CoolCamsStatusEnum.WON)) {
            scaleView(imgCoolCamsWon, false, new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                animation.cancel();
                imgCoolCams.setImageResource(res);
                scaleView(imgCoolCams, true, null);
                imgCoolCamsWon.setVisibility(View.INVISIBLE);
                imgCoolCamsWonBg.setVisibility(View.INVISIBLE);
              }
            });

            scaleView(imgCoolCamsWonBg, false, null);
          } else {
            scaleView(imgCoolCams, false, new AnimatorListenerAdapter() {
              @Override public void onAnimationEnd(Animator animation) {
                animation.cancel();
                imgCoolCams.setImageResource(res);
                scaleView(imgCoolCams, true, null);
              }
            });
          }

          if (rotateAnimation != null) rotateAnimation.cancel();
          imgCoolCamsWonBg.clearAnimation();
          imgCoolCams.setVisibility(View.VISIBLE);
        } else {
          imgCoolCams.setImageResource(res);
        }
      } else if (status.equals(CoolCamsModel.CoolCamsStatusEnum.WON)) {
        if (previousStatus == null || !previousStatus.equals(status)) {
          scaleView(imgCoolCams, false, null);
          imgCoolCamsWonBg.setScaleX(0);
          imgCoolCamsWonBg.setScaleY(0);
          imgCoolCamsWon.setScaleX(0);
          imgCoolCamsWon.setScaleY(0);
          imgCoolCamsWon.setVisibility(View.VISIBLE);
          imgCoolCamsWonBg.setVisibility(View.VISIBLE);
          scaleView(imgCoolCamsWon, true, null);
          scaleView(imgCoolCamsWonBg, true, null);

          imgCoolCamsWonBg.getViewTreeObserver()
              .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override public void onGlobalLayout() {
                  imgCoolCamsWonBg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                  imgCoolCamsWonBg.setPivotX(imgCoolCamsWonBg.getMeasuredWidth() >> 1);
                  imgCoolCamsWonBg.setPivotY(imgCoolCamsWonBg.getMeasuredHeight() >> 1);

                  if (rotateAnimation != null) rotateAnimation.cancel();

                  rotateAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f,
                      Animation.RELATIVE_TO_SELF, 0.5f);
                  rotateAnimation.setDuration(2500);
                  rotateAnimation.setRepeatCount(Animation.INFINITE);
                  rotateAnimation.setInterpolator(new LinearInterpolator());
                  rotateAnimation.setRepeatMode(Animation.RESTART);
                  rotateAnimation.setFillAfter(true);
                  imgCoolCamsWonBg.startAnimation(rotateAnimation);
                  imgCoolCamsWonBg.setVisibility(View.GONE);
                }
              });
        }
      }

      AnimationUtils.animateLeftMargin(layoutCoolCams,
          (int) pointF.x - (layoutCoolCams.getMeasuredWidth() >> 1), 300,
          new DecelerateInterpolator());
      AnimationUtils.animateTopMargin(layoutCoolCams,
          (int) pointF.y - (layoutCoolCams.getMeasuredHeight() >> 1), 300,
          new DecelerateInterpolator());
    }

    previousStatus = status;
  }

  public void updatePositionRatioOfSticker(double xRatio, double yRatio,
      CoolCamsModel.CoolCamsStatusEnum statusEnum) {
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

    updatePositionOfSticker(pointEnd, statusEnum);
  }

  public void updateXYOffset(int left, int top) {
    bgBattleRoyale.setVisibility(View.GONE);
    UIUtils.changeWidthHeightOfView(layoutMasterStream, screenUtils.dpToPx(MAX_HEIGHT_LIST),
        screenUtils.dpToPx(MAX_HEIGHT_LIST));
    onUpdateXYOffset.onNext(Pair.create(left, top));
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Pair<Integer, String>> onScoreChange() {
    return onScoreChange;
  }

  public Observable<List<CoolCamsModel.CoolCamsFeatureEnum>> onFeaturesDetected() {
    return onFeaturesDetected;
  }

  public Observable<Pair<Integer, Integer>> onUpdateXYOffset() {
    return onUpdateXYOffset;
  }
}
