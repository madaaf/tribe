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
import android.util.Pair;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.coolcams.CoolCamsModel;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.GamePresenter;
import com.tribe.app.presentation.mvp.view.adapter.GameMVPViewAdapter;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.component.live.LiveStreamView;
import com.tribe.app.presentation.view.component.live.game.common.GameViewWithRanking;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.SoundManager;
import com.tribe.app.presentation.view.widget.CircularProgressBar;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.facetracking.VisionAPIManager;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeGuest;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 14/03/2018.
 */

public class GameCoolCamsView extends GameViewWithRanking {

  private static final int DURATION = 500;
  private static final float OVERSHOOT = 0.45f;
  private static final int NB_STEPS = 12;

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
  protected VisionAPIManager visionAPIManager;
  private List<CoolCamsModel.CoolCamsFeatureEnum> featuresDetected;
  private GameMVPViewAdapter gameMVPViewAdapter;
  private Set<String> playingIds;
  private CoolCamsModel.CoolCamsStatusEnum currentStatus;
  private PointF previousOrigin = null;
  private CoolCamsModel.CoolCamsStatusEnum previousStatus = null;
  private Date statusSince = new Date();
  private int roundScore = 0;
  private double remainingDuration;

  // SUBSCRIPTIONS
  private CompositeSubscription subscriptionsGame = new CompositeSubscription();
  protected Subscription visionSubscription;
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

    featuresDetected = new ArrayList<>();
    visionAPIManager = VisionAPIManager.getInstance(context);
    gameMVPViewAdapter = new GameMVPViewAdapter() {

    };

    setClickable(true);

    txtCoolCamsInstructions.setTranslationY(screenUtils.dpToPx(50));
    txtCoolCamsInstructions.setAlpha(0f);

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
            if (message.has(X_KEY) && message.has(Y_KEY)) {
              liveViewsMap.get(tribeSession.getUserId())
                  .updatePositionRatioOfSticker(message.getDouble(X_KEY), message.getDouble(Y_KEY),
                      CoolCamsModel.CoolCamsStatusEnum.with(message.getString(STATUS_CODE_KEY),
                          message.has(STEP_ID_KEY) ? message.getString(STEP_ID_KEY) : ""));
            } else {
              liveViewsMap.get(tribeSession.getUserId()).updatePositionOfSticker(null, null);
            }
          } else if (actionKey.equals(ACTION_NEW_GAME)) {
            long timestamp = message.getLong(TIMESTAMP_KEY) * 1000;
            Date date = new Date();
            date.setTime(timestamp);
            JSONArray array = message.getJSONArray(STEPS_IDS_KEY);
            List<CoolCamsModel.CoolCamsStepsEnum> stepsEnums = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
              stepsEnums.add(CoolCamsModel.CoolCamsStepsEnum.getStepById(array.getString(i)));
            }
            double stepDuration = message.getDouble(STEP_DURATION);
            double stepResultDuration = message.getDouble(STEP_RESULT_DURATION);
            newGame(timestamp, stepsEnums, stepDuration, stepResultDuration);
          } else if (actionKey.equals(ACTION_GAME_OVER)) {
            gameOver(message.getJSONArray(WINNERS_KEY));
          } else if (actionKey.equals(ACTION_USER_GAME_OVER)) {
            userGameOver(tribeSession.getUserId());
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
    userGameOver(userId);
  }

  private void userGameOver(String userId) {
    if (!StringUtils.isEmpty(userId)) {
      playingIds.remove(userId);
      liveViewsMap.get(userId).updatePositionOfSticker(null, null);
    } else if (!StringUtils.isEmpty(currentUser.getId())) {
      playingIds.remove(currentUser.getId());
      liveViewsMap.get(currentUser.getId()).updatePositionOfSticker(null, null);
    }

    if (playingIds.isEmpty() && currentMasterId.equals(currentUser.getId())) {
      Collection<Integer> rankings = mapRankingById.values();
      List<String> winnersIds = new ArrayList<>();
      int maxRanking = rankings != null && rankings.size() > 0 ? Collections.max(rankings) : 0;
      for (String id : mapRankingById.keySet()) {
        if (mapRankingById.get(id) == maxRanking) {
          winnersIds.add(id);
        }
      }

      if (winnersIds.size() > 0) {
        JSONArray winnersIdsArray = new JSONArray();
        for (String winnerId : winnersIds) {
          winnersIdsArray.put(winnerId);
        }

        gameOver(winnersIdsArray);
        broadcast(getGameOverPayload(winnersIdsArray));
      } else {
        gameOver(null);
        broadcast(getGameOverPayload(null));
      }
    }
  }

  private void gameOver(JSONArray winnersIds) {
    subscriptionsGame.clear();
    visionSubscription.unsubscribe();
    visionSubscription = null;
    progressBarRound.setProgress(0);
    progressBarTotal.setProgress(0);

    for (LiveStreamView lvs : liveViewsMap.values()) lvs.updatePositionOfSticker(null, null);

    if (winnersIds != null && winnersIds.length() > 0) {
      List<TribeGuest> winners = new ArrayList<>();

      for (int i = 0; i < winnersIds.length(); i++) {
        try {
          winners.add(peerMap.get(winnersIds.getString(i)));

          if (winners.size() == 0) {
            launchNewGameIfMaster();
          } else {
            String message;

            if (winners.size() == 1) {
              TribeGuest winner = winners.get(0);
              if (winner.getId().equals(currentUser.getId())) {
                message = getContext().getString(R.string.game_coolcams_you_won);
              } else {
                message = getContext().getString(R.string.game_coolcams_someone_won,
                    winner.getDisplayName());
              }
            } else {
              String winnersConcat = "";

              for (int j = 0; j < winners.size(); j++) {
                winnersConcat += winners.get(j).getDisplayName();

                if (i < winners.size() - 1) winnersConcat += ", ";
              }

              message = getResources().getString(R.string.game_coolcams_someone_won_plural,
                  winnersConcat);
            }

            showInstructions(message, 1000,
                finished -> hideLabels(1000, finished1 -> launchNewGameIfMaster()));
          }
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    } else {
      launchNewGameIfMaster();
    }
  }

  private void launchNewGameIfMaster() {
    subscriptions.add(Observable.timer(500, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          if (currentMasterId.equals(currentUser.getId())) {
            broadcastNewGame();
          }
        }));
  }

  private void broadcastNewGame() {
    long timestamp = startGameTimestamp();
    List<CoolCamsModel.CoolCamsStepsEnum> steps =
        CoolCamsModel.CoolCamsStepsEnum.generateGame(NB_STEPS);
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
              sendTo(trg.getId(), getNewGamePayload(steps, timestamp, stepDuration, stepResultDuration));
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
          showInstructions(getContext().getString(R.string.game_coolcams_lets_go), delayOneThird,
              finished1 -> {
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

    previousOrigin = null;
    previousStatus = null;
    statusSince = new Date();
    roundScore = 0;
    remainingDuration = (stepDuration + stepResultDuration) * (steps.size() + 1);

    txtSessionTime.setText("" + Math.round(remainingDuration));
    txtSessionTime.animate()
        .scaleX(1)
        .scaleY(1)
        .setDuration(DURATION)
        .setInterpolator(new OvershootInterpolator())
        .start();

    subscriptionsGame.add(Observable.interval(1, TimeUnit.SECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(aLong -> {
          remainingDuration -= 1;
          txtSessionTime.setText("" + Math.round(remainingDuration));
        }));

    subscriptionsGame.add(onStatusChange.subscribe(coolCamsStatusEnum -> {
      currentStatus = coolCamsStatusEnum;

      if (currentStatus.equals(CoolCamsModel.CoolCamsStatusEnum.STEP)) {
        int duration = (int) (stepDuration * 1000);

        progressBarRound.stop();
        progressBarRound.setProgress(100, duration, 0, new LinearInterpolator(), null, null);
        progressBarTotal.setProgress(progressBarTotal.getProgress() + (100 / NB_STEPS), duration, 0,
            new LinearInterpolator(), null, null);

        subscriptionsGame.add(Observable.timer((int) (stepDuration * 1000), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(aLong -> {
              progressBarRound.stop();

              if (currentStatus.equals(CoolCamsModel.CoolCamsStatusEnum.STEP) ||
                  currentStatus.equals(CoolCamsModel.CoolCamsStatusEnum.LOST)) {
                soundManager.playSound(SoundManager.TRIVIA_LOST, SoundManager.SOUND_MID);
                onStatusChange.onNext(CoolCamsModel.CoolCamsStatusEnum.LOST);
              }

              subscriptionsGame.add(
                  Observable.timer((int) (stepResultDuration * 1000), TimeUnit.MILLISECONDS)
                      .observeOn(AndroidSchedulers.mainThread())
                      .subscribe(aLong2 -> {
                        progressBarRound.stop();
                        progressBarRound.setProgress(0);

                        if (steps.size() > 0) {
                          CoolCamsModel.CoolCamsStepsEnum nextStep = steps.remove(0);
                          onStatusChange.onNext(CoolCamsModel.CoolCamsStatusEnum.with(
                              CoolCamsModel.CoolCamsStatusEnum.STEP.getCode(), nextStep.getStep()));
                        } else {
                          onAddScore.onNext(Pair.create(game.getId(), roundScore));
                          broadcast(getUserGameOverPayload());
                          userGameOver(null);
                          subscriptionsGame.clear();
                        }
                      }));
            }));
      }
    }));

    onStatusChange.onNext(
        CoolCamsModel.CoolCamsStatusEnum.with(CoolCamsModel.CoolCamsStatusEnum.STEP.getCode(),
            step.getStep()));

    if (visionSubscription != null) {
      visionSubscription.unsubscribe();
      visionSubscription = null;
    }

    visionSubscription = visionAPIManager.onComputeFaceDone()
        .onBackpressureDrop()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(frame -> {
          featuresDetected.clear();

          if (visionAPIManager.getFace() == null) {
            featuresDetected.add(CoolCamsModel.CoolCamsFeatureEnum.NO_FACE);
          }

          if ((!visionAPIManager.isLeftEyeOpen() && visionAPIManager.isRightEyeOpen()) ||
              (visionAPIManager.isLeftEyeOpen() && !visionAPIManager.isRightEyeOpen())) {
            featuresDetected.add(CoolCamsModel.CoolCamsFeatureEnum.ONE_EYE_CLOSED);
          } else if (!visionAPIManager.isLeftEyeOpen() && !visionAPIManager.isLeftEyeOpen()) {
            featuresDetected.add(CoolCamsModel.CoolCamsFeatureEnum.TWO_EYES_CLOSED);
          }

          if (visionAPIManager.isSmiling()) {
            featuresDetected.add(CoolCamsModel.CoolCamsFeatureEnum.HAS_SMILE);
          }

          if (Math.abs(visionAPIManager.getEulerY()) >= 15) {
            featuresDetected.add(CoolCamsModel.CoolCamsFeatureEnum.HAS_ANGLE);
          }

          if (currentStatus.equals(CoolCamsModel.CoolCamsStatusEnum.STEP) &&
              currentStatus.getStep() != null) {
            if ((new Date().getTime() - statusSince.getTime()) / 1000 > 1) {
              Timber.d("Features needed : " + currentStatus.getStep().getFeatures());
              Timber.d("Features done : " + featuresDetected);

              boolean equals = currentStatus.getStep().getFeatures().equals(featuresDetected);
              Timber.d("Are features completed : " + equals);
              if (equals) {
                statusSince = new Date();
                onStatusChange.onNext(CoolCamsModel.CoolCamsStatusEnum.WON);
                roundScore++;
                addPoints(1, currentUser.getId(), true);
                soundManager.playSound(SoundManager.TRIVIA_WON, SoundManager.SOUND_MID);
              }
            }
          } else if (currentStatus.equals(CoolCamsModel.CoolCamsStatusEnum.WON) ||
              currentStatus.equals(CoolCamsModel.CoolCamsStatusEnum.LOST)) {
            if ((new Date().getTime() - statusSince.getTime()) / 1000 > 1) {
              statusSince = new Date();
            }
          }

          PointF origin = visionAPIManager.findXYMiddleEye();

          if (previousOrigin == null ||
              previousOrigin != origin ||
              previousStatus == null ||
              !currentStatus.getCode().equals(previousStatus.getCode())) {
            previousOrigin = origin;
            previousStatus = currentStatus;

            Pair<Double, Double> positionRatio = liveViewsMap.get(currentUser.getId())
                .computeFrameAndFacePosition(frame, origin, currentStatus);

            if (positionRatio == null) {
              broadcast(getEmptyOriginPayload());
            } else {
              broadcast(getOriginPayload(currentStatus,
                  frame.isFrontCamera() ? 1 - positionRatio.first : positionRatio.first,
                  positionRatio.second));
            }
          } else if (previousOrigin != null) {
            previousOrigin = null;
            liveViewsMap.get(currentUser.getId())
                .computeFrameAndFacePosition(frame, null, currentStatus);
            broadcast(getEmptyOriginPayload());
          }
        });
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

  private void showInstructions(String text, int duration, CompletionListener completionListener) {
    txtCoolCamsInstructions.setText(text);

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

  private JSONObject getUserGameOverPayload() {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_USER_GAME_OVER);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getGameOverPayload(JSONArray jsonArray) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_GAME_OVER);
    if (jsonArray != null) JsonUtils.jsonPut(game, WINNERS_KEY, jsonArray);
    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getOriginPayload(CoolCamsModel.CoolCamsStatusEnum status, double x, double y) {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_POSITION);
    JsonUtils.jsonPut(game, X_KEY, x);
    JsonUtils.jsonPut(game, Y_KEY, y);
    JsonUtils.jsonPut(game, STATUS_CODE_KEY, status.getCode());

    if (status.equals(CoolCamsModel.CoolCamsStatusEnum.STEP)) {
      JsonUtils.jsonPut(game, STEP_ID_KEY, status.getStep().getStep());
    }

    JsonUtils.jsonPut(obj, this.game.getId(), game);
    return obj;
  }

  private JSONObject getEmptyOriginPayload() {
    JSONObject obj = new JSONObject();
    JSONObject game = new JSONObject();
    JsonUtils.jsonPut(game, ACTION_KEY, ACTION_POSITION);
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

    soundManager.playSound(SoundManager.COOL_CAMS_SOUNDTRACK, SoundManager.SOUND_MID);

    playingIds = new HashSet<>();

    currentMasterId = userId;
    game.setCurrentMaster(peerMap.get(currentMasterId));

    if (currentMasterId.equals(currentUser.getId())) {
      layoutConstraint.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override public void onGlobalLayout() {
          layoutConstraint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          broadcastNewGame();
        }
      });
    }
  }

  @Override public void stop() {
    if (visionSubscription != null) {
      visionSubscription.unsubscribe();
      visionSubscription = null;
    }

    soundManager.cancelMediaPlayer();

    for (LiveStreamView str : liveViewsMap.values()) str.updatePositionOfSticker(null, null);

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
