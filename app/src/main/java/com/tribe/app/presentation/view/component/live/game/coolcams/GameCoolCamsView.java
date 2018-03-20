package com.tribe.app.presentation.view.component.live.game.coolcams;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionListenerAdapter;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.coolcams.CoolCamsModel;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 14/03/2018.
 */

public class GameCoolCamsView extends GameViewWithRanking {

  private static final int DURATION = 500;
  private static final float OVERSHOOT = 0.45f;

  private static final String ACTION_POSITION = "position";
  private static final String ACTION_NEW_GAME = "newGame";
  private static final String ACTION_USER_GAME_OVER = "userGameOver";
  private static final String ACTION_GAME_OVER = "gameOver";

  private static final String STEPS_IDS_KEY = "stepsIds";
  private static final String STEP_DURATION = "stepDuration";
  private static final String STEP_RESULT_DURATION = "stepResultDuration";
  private static final String TIMESTAMP_KEY = "timestamp";
  private static final String STATUS_CODE_KEY = "statusCode";
  private static final String STEP_ID_KEY = "stepId";

  private static final String X_KEY = "x";
  private static final String Y_KEY = "y";

  private static final String WINNERS_KEY = "winners";

  @Inject GamePresenter gamePresenter;

  @BindView(R.id.layoutConstraint) ConstraintLayout layoutConstraint;

  @BindView(R.id.imgCoolCamsBottom) ImageView imgCoolCamsBottom;

  @BindView(R.id.imgCoolCamsTop) ImageView imgCoolCamsTop;

  @BindView(R.id.txtCoolCamsInstructions) TextViewFont txtCoolCamsInstructions;

  @BindView(R.id.progressBarBg) CircularProgressBar progressBarBg;

  @BindView(R.id.viewBgBlack) View viewBgBlack;

  @BindView(R.id.progressBarTotal) CircularProgressBar progressBarTotal;

  @BindView(R.id.progressBarRound) CircularProgressBar progressBarRound;

  @BindView(R.id.viewGradientBg) View viewGradientBg;

  @BindView(R.id.txtSessionTime) TextViewFont txtSessionTime;

  // VARIABLES
  private GameMVPViewAdapter gameMVPViewAdapter;
  private Set<String> playingIds;

  // SUBSCRIPTIONS
  private CompositeSubscription subscriptionsGame = new CompositeSubscription();
  private PublishSubject<CoolCamsModel.CoolCamsStatusEnum> onStatusChange = PublishSubject.create();

  public GameCoolCamsView(@NonNull Context context) {
    super(context);
  }

  public GameCoolCamsView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    gamePresenter.onViewAttached(gameMVPViewAdapter);
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    gamePresenter.onViewDetached();
  }

  @Override protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  @Override protected void initView(Context context) {
    super.initView(context);

    inflater.inflate(R.layout.view_game_cool_cams_init, this, true);
    unbinder = ButterKnife.bind(this);

    setClickable(true);

    txtCoolCamsInstructions.setTranslationY(screenUtils.dpToPx(50));
    txtCoolCamsInstructions.setAlpha(0f);

    gameMVPViewAdapter = new GameMVPViewAdapter() {

    };

    GradientDrawable background = new GradientDrawable();
    background.setShape(GradientDrawable.OVAL);
    background.setColor(ContextCompat.getColor(getContext(), R.color.black_opacity_30));
    viewBgBlack.setBackground(background);

    background = new GradientDrawable(GradientDrawable.Orientation.TR_BL, new int[] {
        ContextCompat.getColor(context, R.color.coolcams_top),
        ContextCompat.getColor(context, R.color.coolcams_bottom)
    });
    viewGradientBg.setBackground(background);

    progressBarBg.useRoundedCorners(false);
    progressBarBg.setProgressColor(ContextCompat.getColor(getContext(), R.color.white_opacity_50));
    progressBarBg.setProgressWidth(screenUtils.dpToPx(3));

    progressBarRound.useRoundedCorners(false);
    progressBarRound.setProgressColor(Color.WHITE);
    progressBarRound.setProgressWidth(screenUtils.dpToPx(3));

    progressBarTotal.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            progressBarTotal.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            progressBarTotal.useRoundedCorners(false);
            progressBarTotal.setProgressColor(Color.WHITE);
            progressBarTotal.setProgressWidth(progressBarTotal.getMeasuredWidth() >> 1);
          }
        });
  }

  @Override protected void initWebRTCRoomSubscriptions() {
    super.initWebRTCRoomSubscriptions();
    subscriptionsRoom.add(webRTCRoom.onGameMessage()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(pair -> {
          JSONObject jsonObject = pair.second;
          TribeSession tribeSession = pair.first;

          if (jsonObject.has(game.getId())) {
            try {
              JSONObject message = jsonObject.getJSONObject(game.getId());
              receiveMessage(tribeSession, message);
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }
        }));
  }

  @Override protected void takeOverGame() {

  }

  @Override protected void receiveMessage(TribeSession tribeSession, JSONObject jsonObject) {
    if (jsonObject.has(game.getId())) {
      try {
        JSONObject message = jsonObject.getJSONObject(game.getId());
        if (message.has(ACTION_KEY)) {
          String actionKey = message.getString(ACTION_KEY);
          if (actionKey.equals(ACTION_POSITION)) {
            if (message.has(STEP_ID_KEY)) {
              liveViewsMap.get(tribeSession.getUserId())
                  .setStep(
                      CoolCamsModel.CoolCamsStepsEnum.getStepById(message.getString(STEP_ID_KEY)));
            }

            if (message.has(X_KEY) && message.has(Y_KEY)) {
              liveViewsMap.get(tribeSession.getUserId())
                  .updatePositionRatioOfSticker(message.getDouble(X_KEY), message.getDouble(Y_KEY));
            }
          }
        }
      } catch (JSONException ex) {
        ex.printStackTrace();
      }
    }

    super.receiveMessage(tribeSession, jsonObject);
  }

  @Override public void userLeft(String userId) {
    super.userLeft(userId);

    playingIds.remove(userId);
    //userGameOver(userId);
  }

  private void broadcastNewGame() {
    long timestamp = startGameTimestamp();
    List<CoolCamsModel.CoolCamsStepsEnum> steps = CoolCamsModel.CoolCamsStepsEnum.generateGame(12);
    double stepDuration = 5.0D;
    double stepResultDuration = 1.0D;

    resetScores(false);

    JSONObject message = getNewGamePayload(steps, timestamp, stepDuration, stepResultDuration);
    broadcast(message);

    newGame(timestamp, steps, stepDuration, stepResultDuration);

    subscriptionsGame.add(onNewPlayers.delay(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(tribeGuests -> {
          if (tribeGuests == null) return;
          for (TribeGuest trg : tribeGuests) {
            if (trg.canPlayGames(game.getId())) playingIds.add(trg.getId());

            if (currentMasterId.equals(currentUser.getId())) {
              sendTo(trg.getId(), message);
            }
          }
        }));
  }

  private void newGame(long timestamp, List<CoolCamsModel.CoolCamsStepsEnum> steps,
      double stepDuration, double stepResultDuration) {
    subscriptionsGame.clear();

    setupSessionTimer();

    long delayBeforeGame = Math.max(0, timestamp - new Date().getTime());

    int delayOneThird = (int) (delayBeforeGame / 3);
    if (delayBeforeGame > 1000) {
      showTitle(delayOneThird, finished -> {
        if (finished) {
          showInstructions(delayOneThird, finished1 -> {
            if (finished1) {
              hideLabels(delayOneThird, null);
            }
          });
        }
      });
    }

    subscriptionsGame.add(Observable.timer(delayBeforeGame, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> launchGame(steps, stepDuration, stepResultDuration)));
  }

  private void launchGame(List<CoolCamsModel.CoolCamsStepsEnum> steps, double stepDuration,
      double stepResultDuration) {
    if (steps == null || steps.size() == 0) return;
    CoolCamsModel.CoolCamsStepsEnum step = steps.remove(0);

    PointF previousOrigin = null;
    CoolCamsModel.CoolCamsStatusEnum status = null;

    Date statusSince = new Date();

    String userId = currentUser.getId();
    int roundScore = 0;

    double remainingDuration = (stepDuration + stepResultDuration) * steps.size();
    txtSessionTime.setText("" + (int) remainingDuration);
    txtSessionTime.animate()
        .scaleX(1)
        .scaleY(1)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator())
        .start();

    subscriptionsGame.add(Observable.interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          double time = remainingDuration - new Long(aLong).doubleValue();
          txtSessionTime.setText("" + (int) time);
        }));

    progressBarTotal.setProgress(progressBarTotal.getProgress() + (100 / steps.size()));
    progressBarRound.setProgress(100, (int) stepDuration * 1000, 0, null, null);
  }

  private void showTitle(int duration, CompletionListener completionListener) {
    AnimationUtils.fadeIn(viewGradientBg, DURATION);

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_cool_cams_title_only);
    animateLayoutWithConstraintSet(constraintSet, true, () -> subscriptionsGame.add(
        Observable.timer(duration, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              if (completionListener != null) completionListener.finished(true);
            })));
  }

  private void showInstructions(int duration, CompletionListener completionListener) {
    AnimationUtils.fadeIn(txtCoolCamsInstructions, DURATION);
    txtCoolCamsInstructions.animate()
        .translationY(0)
        .setDuration(DURATION >> 1)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_cool_cams_instructions_only);
    animateLayoutWithConstraintSet(constraintSet, true, () -> subscriptionsGame.add(
        Observable.timer(duration, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              if (completionListener != null) completionListener.finished(true);
            })));
  }

  private void hideLabels(int duration, CompletionListener completionListener) {
    AnimationUtils.fadeOut(viewGradientBg, DURATION);
    AnimationUtils.fadeOut(txtCoolCamsInstructions, DURATION >> 1);
    txtCoolCamsInstructions.animate()
        .translationY(screenUtils.dpToPx(50))
        .setDuration(DURATION >> 1)
        .setInterpolator(new DecelerateInterpolator())
        .start();

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(getContext(), R.layout.view_game_cool_cams_init);
    animateLayoutWithConstraintSet(constraintSet, true, () -> subscriptionsGame.add(
        Observable.timer(duration, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              if (completionListener != null) completionListener.finished(true);
            })));
  }

  private void animateLayoutWithConstraintSet(ConstraintSet constraintSet, boolean animated,
      LabelListener labelListener) {
    Transition transition = new ChangeBounds();
    transition.setDuration(animated ? DURATION : 0);
    transition.setInterpolator(new OvershootInterpolator(0.75f));
    transition.addListener(new TransitionListenerAdapter() {
      @Override public void onTransitionEnd(@NonNull Transition transition) {
        if (labelListener != null) labelListener.call();
      }
    });
    TransitionManager.beginDelayedTransition(layoutConstraint, transition);
    constraintSet.applyTo(layoutConstraint);
  }

  private void setupSessionTimer() {
    progressBarRound.setProgress(0);
    progressBarTotal.setProgress(0);
  }

  private void broadcast(JSONObject jsonObject) {
    webRTCRoom.sendToPeers(jsonObject, true);
  }

  private void sendTo(String userId, JSONObject jsonObject) {
    webRTCRoom.sendToUser(userId, jsonObject, true);
  }

  protected interface CompletionListener {
    void finished(boolean finished);
  }

  /**
   * JSON PAYLOAD
   */

  private JSONObject getNewGamePayload(List<CoolCamsModel.CoolCamsStepsEnum> steps, long timestamp,
      double stepDuration, double stepResultDuration) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_NEW_GAME);
    JsonUtils.jsonPut(game, TIMESTAMP_KEY, Long.valueOf(timestamp).doubleValue() / 1000);
    JsonUtils.jsonPut(game, STEP_DURATION, stepDuration);
    JsonUtils.jsonPut(game, STEP_RESULT_DURATION, stepResultDuration);
    JSONArray stepsArray = new JSONArray();
    for (CoolCamsModel.CoolCamsStepsEnum step : steps) stepsArray.put(step.getStep());
    JsonUtils.jsonPut(game, STEPS_IDS_KEY, stepsArray);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  /**
   * PUBLIC
   */

  public void initSubscriptions() {

  }

  @Override public void start(Game game,
      Observable<ObservableRxHashMap.RxHashMap<String, TribeGuest>> masterMapObs,
      Observable<Map<String, TribeGuest>> mapObservable,
      Observable<Map<String, TribeGuest>> mapInvitedObservable,
      Observable<Map<String, LiveStreamView>> liveViewsObservable, String userId) {
    super.start(game, masterMapObs, mapObservable, mapInvitedObservable, liveViewsObservable,
        userId);
    playingIds = new HashSet<>();

    currentMasterId = userId;
    game.setCurrentMaster(peerMap.get(currentMasterId));

    if (currentMasterId.equals(currentUser.getId())) {
      subscriptions.add(Observable.timer(1, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(aLong -> broadcastNewGame()));
    }
  }

  @Override public void stop() {
    for (LiveStreamView lsv : liveViewsMap.values()) lsv.setStep(null);
    super.stop();
  }

  @Override public void dispose() {
    subscriptionsGame.clear();
    super.dispose();
  }

  @Override public void setNextGame() {

  }

  /**
   * OBSERVABLES
   */
}
