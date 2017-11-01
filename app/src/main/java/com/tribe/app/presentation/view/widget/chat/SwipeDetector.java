package com.tribe.app.presentation.view.widget.chat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.media.MediaRecorder;
import android.support.v4.content.ContextCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import com.tribe.app.R;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.tribe.app.presentation.view.widget.chat.ChatView.ANIM_DURATION;
import static com.tribe.app.presentation.view.widget.chat.ChatView.ANIM_DURATION_FAST;

/**
 * Created by madaaflak on 11/10/2017.
 */

public class SwipeDetector implements View.OnTouchListener {

  private static float VOICE_NOTE_SCALE_RATIO = 0.75f;
  private static final int SWIPE_MIN_DISTANCE = 1;

  private GestureDetector mGestureDetector;
  private View mView, recordingView;
  private ChatView context;
  private float initialPosition, initPos, ratio;
  private boolean isLongTap = false, isDown = false;
  private ScreenUtils screenUtils;
  private MediaRecorder recorder = null;
  public Subscription timerVoiceSub, subscribe;

  public SwipeDetector(ChatView context, View view, View recordingView, float initPos,
      ScreenUtils screenUtils) {
    mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
    this.mView = view;
    this.context = context;
    this.initPos = initPos;
    this.recordingView = recordingView;
    this.initialPosition = view.getX();
    this.screenUtils = screenUtils;

    mGestureDetector.setIsLongpressEnabled(false);
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    if (event.getAction() == MotionEvent.ACTION_UP) {
      isDown = false;
      Timber.i("ACTION_UP");
      onActionUp(mView, getRatio());
      isLongTap = false;
    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {

      if (subscribe == null) {
        subscribe = Observable.interval(1, TimeUnit.SECONDS)
            .timeInterval()
            .observeOn(AndroidSchedulers.mainThread())
            .onBackpressureDrop()
            .subscribe(avoid -> {
              if (isDown) {
                Timber.i("LONG CLICK");
                isLongTap = true;
                onActionDown(mView);
                isDown = false;
              }
            });
        context.subscriptions.add(subscribe);
      }

      isDown = true;
    }
    return mGestureDetector.onTouchEvent(event);
  }

  private float getRatio() {
    float dist = initialPosition - initPos;
    float r = mView.getX() - initPos;
    return (r / dist) + 1;
  }

  private GestureDetector.OnGestureListener mGestureListener =
      new GestureDetector.SimpleOnGestureListener() {
        private float viewMotionDownX, recordingViewMotionDownX;

        @Override public boolean onSingleTapUp(MotionEvent e) {
          Timber.i("onSingleTapUp " + isLongTap);
          if (!isLongTap) {
            onSingleTap();
          }
          isLongTap = false;
          return super.onSingleTapUp(e);
        }

        @Override public boolean onSingleTapConfirmed(MotionEvent e) {
          Timber.i("onSingleTapConfirmed");

          return super.onSingleTapConfirmed(e);
        }

        @Override public boolean onDown(MotionEvent e) {
          viewMotionDownX = e.getRawX() - mView.getTranslationX();
          recordingViewMotionDownX = e.getRawX() - recordingView.getTranslationX();

          return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
          if (isLongTap) {
            float x2 = mView.getX() + (mView.getWidth() / 2);
            float x1 = recordingView.getX() + (recordingView.getWidth() / 2);

            ratio = getRatio();
            try {
              if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && mView.getX() > initPos) {
                mView.setTranslationX(e2.getRawX() - viewMotionDownX);
              } else if (mView.getX() < initPos) { // TRASH BTN POSTITION
                mView.setX(initPos);
              } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                  && mView.getX() < initialPosition) {
                mView.setTranslationX(e2.getRawX() - viewMotionDownX);
              } else if (mView.getX() > initialPosition) {
                mView.setX(initialPosition);
              }

              if (e2.getRawX() > initialPosition) {
                recordingView.setTranslationX(initialPosition - recordingViewMotionDownX);
              } else if (e2.getRawX() >= (screenUtils.getWidthPx() / 2)) {
                recordingView.setTranslationX(e2.getRawX() - recordingViewMotionDownX);
              }

              right2left(mView, ratio);
            } catch (Exception e) {
              // nothing
            }
          }
          return true;
        }
      };

  private void initRecordingView() {
    recordingView.setScaleX(1f);
    recordingView.setScaleY(1f);

    ViewGroup.LayoutParams p = recordingView.getLayoutParams();
    p.width = context.recordingViewInitWidth;
    recordingView.setLayoutParams(p);

    context.timerVoiceNote.setAlpha(1f);
    context.loadingRecordView.setAlpha(1f);
    context.playerBtn.setAlpha(1f);

    context.playerBtn.setVisibility(VISIBLE);
    context.loadingRecordView.setVisibility(VISIBLE);
    context.timerVoiceNote.setVisibility(VISIBLE);
  }

  public void onActionUp(View v, float ratio) {
    Timber.i("ON ACTION :onActionUp! " + ratio + "  " + context.audioDuration);
    if (ratio == 1) {
      context.viewPlayerProgress.setVisibility(View.INVISIBLE);
      context.cardViewIndicator.setVisibility(View.INVISIBLE);

      context.voiceNoteBtn.setBackground(
          ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_orange));
      context.voiceNoteBtn.setImageDrawable(
          ContextCompat.getDrawable(context.getContext(), R.drawable.picto_trash_white));

      context.playerBtn.setImageDrawable(
          ContextCompat.getDrawable(context.getContext(), R.drawable.picto_trash_white));

      context.btnContainer.setImageDrawable(null);

      context.recordingFrame.setBackground(
          ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_orange));

      context.recordingFrame.setScaleX(0.8f);
      context.recordingFrame.setScaleY(0.8f);

      context.playerBtn.animate()
          .setDuration(ANIM_DURATION)
          .scaleX(1.2f)
          .scaleY(1.2f)
          .setInterpolator(new OvershootInterpolator(3f))
          .withEndAction(() -> context.playerBtn.animate()
              .setDuration(ANIM_DURATION)
              .scaleX(1f)
              .scaleY(1f)
              .setInterpolator(new OvershootInterpolator(3f))
              .start())
          .start();

      context.voiceNoteBtn.postDelayed(() -> {
        context.voiceNoteBtn.setImageDrawable(null);
        context.voiceNoteBtn.animate()
            .translationX(context.widthRefInit)
            .setDuration(ANIM_DURATION)
            .setInterpolator(new AccelerateInterpolator())
            .withStartAction(() -> {
              context.editText.getLayoutParams().width = context.widthRefInit;
              recordingView.animate()
                  .scaleX(0)
                  .scaleY(0)
                  .setDuration(ANIM_DURATION)
                  .setInterpolator(new OvershootInterpolator(3f))
                  .withEndAction(() -> {
                    initRecordingView();
                    recordingView.setVisibility(View.INVISIBLE);
                  })
                  .withEndAction(() -> {
                    recordingView.clearAnimation();
                    context.voiceNoteBtn.clearAnimation();

                    ViewGroup.LayoutParams p = recordingView.getLayoutParams();
                    p.width = context.recordingViewInitWidth;
                    recordingView.setLayoutParams(p);

                    recordingView.setX(context.recordingViewX);
                    recordingView.setY(screenUtils.getHeightPx() + recordingView.getHeight());
                    context.playerBtn.setScaleX(1);
                    context.playerBtn.setScaleY(1);
                    context.loadingRecordView.setVisibility(VISIBLE);
                    context.timerVoiceNote.setVisibility(VISIBLE);
                    context.recordingFrame.setBackground(
                        ContextCompat.getDrawable(context.getContext(),
                            R.drawable.shape_rect_voice_note));
                    context.voiceNoteBtn.setX(context.voiceNoteBtnX);
                    context.voiceNoteBtn.setAlpha(1f);
                    context.playerBtn.setAlpha(1f);
                    context.playerBtn.setImageDrawable(
                        ContextCompat.getDrawable(context.getContext(),
                            R.drawable.picto_recording_voice));
                    context.playerBtn.setBackground(null);
                    context.voiceNoteBtn.setBackground(
                        ContextCompat.getDrawable(context.getContext(),
                            R.drawable.shape_circle_grey));

                    context.btnContainer.setImageDrawable(
                        ContextCompat.getDrawable(context.getContext(),
                            R.drawable.shape_circle_blue_recording));

                    context.recordingFrame.setScaleX(1f);
                    context.recordingFrame.setScaleY(1f);
                  })
                  .start();
              context.voiceNoteBtn.setBackground(
                  ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_grey));
            })
            .start();
        stopVoiceNote(false, true);
      }, 1000);
    } else {
      Timber.i("ACTION UP RATIO 0! " + ratio);
      stopVoiceNote(true, true);
    }
  }

  private void animePlayerIndicator(int duration) {
    context.cardViewIndicator.setVisibility(VISIBLE);
    context.viewPlayerProgress.setVisibility(View.VISIBLE);
    context.viewPlayerProgress.clearAnimation();

    ValueAnimator anim = ValueAnimator.ofInt(0, context.recordingViewInitWidth);
    anim.addUpdateListener(valueAnimator -> {
      int val = (Integer) valueAnimator.getAnimatedValue();
      ViewGroup.LayoutParams layoutParams = context.viewPlayerProgress.getLayoutParams();
      layoutParams.width = val;
      context.viewPlayerProgress.setLayoutParams(layoutParams);
    });
    anim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        context.cardViewIndicator.setVisibility(View.INVISIBLE);
        context.viewPlayerProgress.setVisibility(View.INVISIBLE);
        context.viewPlayerProgress.clearAnimation();
      }
    });
    anim.setDuration(duration);
    anim.start();

    anim.getCurrentPlayTime();
  }

  private void animHintView() {
    int translation = screenUtils.dpToPx(30);
    context.hintEditText.animate()
        .translationX(translation)
        .alpha(1f)
        .setDuration(3000)
        .withStartAction(() -> {
          context.hintEditText.setAlpha(0f);
          context.editText.setHintTextColor(
              ContextCompat.getColor(context.getContext(), R.color.grey_chat_grey_hint));
          context.hintEditText.setText(
              context.getContext().getString(R.string.chat_placeholder_slide_to_cancel));
        })
        .withEndAction(() -> context.hintEditText.animate()
            .translationX(-translation)
            .alpha(0)
            .setDuration(3000)
            .withEndAction(() -> animHintView())
            .start())
        .start();
  }

  private void startVoiceNote() {
    Timber.i("startVoiceNote");
    timerVoiceSub = Observable.interval(1, TimeUnit.SECONDS)
        .timeInterval()
        .observeOn(AndroidSchedulers.mainThread())
        .onBackpressureDrop()
        .map(ok -> {
          long value = ok.getValue() + 1;
          context.audioDuration = Float.valueOf(value);
          String formatTime = "";
          if (value < 10) {
            formatTime = "0:0" + value;
          } else {
            int sec = (int) (value % 60);
            String secF;
            if (sec < 10) {
              secF = "0" + sec;
            } else {
              secF = String.valueOf(sec);
            }
            formatTime = (int) (value / 60) + ":" + secF;
          }

          return formatTime;
        })
        .subscribe(formatTime -> {
          context.timerVoiceNote.setText(formatTime);
        });

    animePlayerIndicator(1000 * 60 * 3); // 3min
    recordingView.setVisibility(View.VISIBLE);
    initRecordingView();
    context.voiceNoteBtn.animate()
        .scaleX(2f * VOICE_NOTE_SCALE_RATIO)
        .scaleY(2f * VOICE_NOTE_SCALE_RATIO)
        .setInterpolator(new OvershootInterpolator())
        .setDuration(ANIM_DURATION)
        .withStartAction(() -> {
          context.trashBtn.setVisibility(VISIBLE);
          context.btnSendLikeContainer.setVisibility(GONE);
          context.uploadImageBtn.setVisibility(GONE);
          context.layoutPulse.setVisibility(GONE);
          context.videoCallBtn.setVisibility(GONE);
          recordingView.animate()
              .translationY(-(recordingView.getHeight() * 2.5f))
              .setInterpolator(new AccelerateInterpolator())
              .setDuration(ANIM_DURATION)
              .start();
          context.editText.setHint("");
          context.hintEditText.setVisibility(VISIBLE);
          context.hintEditText.setAlpha(0);
          context.editText.setCursorVisible(false);

          animHintView();
        })

        .start();

    ResizeAnimation a = new ResizeAnimation(context.editText);
    a.setDuration(500);
    a.setInterpolator(new LinearInterpolator());
    a.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationStart(Animation animation) {
        context.uploadImageBtn.setAlpha(0f);
        context.uploadImageBtn.setVisibility(GONE);
      }
    });
    a.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationEnd(Animation animation) {
        super.onAnimationEnd(animation);
        context.trashBtn.animate().alpha(1).setDuration(500).start();
      }
    });
    a.setParams(context.widthRefInit, screenUtils.getWidthPx(),
        FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    context.editText.startAnimation(a);

    ResizeAnimation a2 = new ResizeAnimation(recordingView);
    a2.setDuration(500);
    a2.setInterpolator(new LinearInterpolator());
    a2.setAnimationListener(new AnimationListenerAdapter() {
      @Override public void onAnimationStart(Animation animation) {
        super.onAnimationStart(animation); // TODO
        context.playerBtn.setVisibility(VISIBLE);
        context.loadingRecordView.setVisibility(VISIBLE);
        context.timerVoiceNote.setVisibility(VISIBLE);
      }
    });
    a2.setParams(context.recordingViewInitWidth, context.recordingViewInitWidth + 100,
        recordingView.getHeight(), recordingView.getHeight());
    recordingView.startAnimation(a2);
  }

  public void onActionDown(View v) {
    context.onRecord = true;
    startRecording();
    startVoiceNote();
  }

  private void stopRecording() {
    Timber.i("stopRecording");
    if (timerVoiceSub != null) timerVoiceSub.unsubscribe();
    timerVoiceSub = null;
    context.onRecord = false;
    context.audioDuration = 0f;

    if (recorder == null) return;
    try {
      recorder.stop();
    } catch (RuntimeException ex) {
      //Ignore
    }
    recorder.release();
    recorder = null;
  }

  private void startRecording() {

    context.fileName = context.getContext().getExternalCacheDir().getAbsolutePath()
        + File.separator
        + context.dateUtils.getUTCDateAsString()
        + context.user.getId()
        + "audiorecord.mp4";
    context.fileName = context.fileName.replaceAll(" ", "_").replaceAll(":", "-");

    Timber.i("startRecording" + context.fileName);
    recorder = new MediaRecorder();
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    recorder.setOutputFile(context.fileName);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC);

    try {
      recorder.prepare();
    } catch (IOException e) {
      Timber.e("prepare() failed");
    }
    recorder.start();
  }

  private void stopVoiceNote(boolean sendMessage, boolean withAnim) {
    Timber.i("stopVoiceNote " + sendMessage + "  " + context.audioDuration + "s");
    context.editText.getLayoutParams().width = context.widthRefInit;
    context.trashBtn.setVisibility(GONE);
    String time = String.valueOf(context.timerVoiceNote.getText());
    context.timerVoiceNote.setText("0:01");

    if (sendMessage && context.audioDuration > 0) {
      context.audioCount++;
      context.sendMessageToAdapter(Message.MESSAGE_AUDIO, time, null);
    }

    context.editText.clearAnimation();
    context.voiceNoteBtn.clearAnimation();
    ViewGroup.LayoutParams params = context.editText.getLayoutParams();
    params.width = context.widthRefInit;
    context.editText.setLayoutParams(params);

    recordingView.setVisibility(View.INVISIBLE);

    context.voiceNoteBtn.animate()
        .scaleX(1f)
        .scaleY(1f)
        .translationX(context.voiceNoteBtnX - context.voiceNoteBtn.getLeft())
        .setInterpolator(new LinearInterpolator())
        .setDuration(ANIM_DURATION_FAST)
        .withStartAction(() -> {
          context.editText.setHint(
              context.getResources().getString(R.string.chat_placeholder_message));
          context.uploadImageBtn.setAlpha(1f);
          context.uploadImageBtn.setVisibility(VISIBLE);
          context.sendBtn.setVisibility(VISIBLE);
          context.likeBtn.setVisibility(VISIBLE);
          context.btnSendLikeContainer.setVisibility(VISIBLE);
          context.uploadImageBtn.setVisibility(VISIBLE);
          context.layoutPulse.setVisibility(VISIBLE);
          context.videoCallBtn.setVisibility(VISIBLE);
          context.editText.setCursorVisible(true);
          context.hintEditText.setVisibility(View.INVISIBLE);
        })
        .withEndAction(() -> {
          recordingView.clearAnimation();
          context.voiceNoteBtn.clearAnimation();

          ViewGroup.LayoutParams p = recordingView.getLayoutParams();
          p.width = context.recordingViewInitWidth;
          recordingView.setLayoutParams(p);

          recordingView.setX(context.recordingViewX);
          recordingView.setY(screenUtils.getHeightPx() + recordingView.getHeight());
          context.playerBtn.setScaleX(1);
          context.playerBtn.setScaleY(1);
          context.loadingRecordView.setVisibility(VISIBLE);
          context.timerVoiceNote.setVisibility(VISIBLE);
          context.recordingFrame.setBackground(
              ContextCompat.getDrawable(context.getContext(), R.drawable.shape_rect_voice_note));
          context.voiceNoteBtn.setX(context.voiceNoteBtnX);
          context.voiceNoteBtn.setAlpha(1f);
          context.playerBtn.setAlpha(1f);
          context.playerBtn.setImageDrawable(
              ContextCompat.getDrawable(context.getContext(), R.drawable.picto_recording_voice));
          context.playerBtn.setBackground(null);
          context.voiceNoteBtn.setBackground(
              ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_grey));
        })
        .start();
    stopRecording();
  }

  public void bottom2top(View v) {
    Timber.i("ON ACTION : bottom2top!");
  }

  public void left2right(View v) {
    Timber.i("ON ACTION : left2right!");
  }

  public void right2left(View v, float ratio) {
    context.voiceNoteBtn.setScaleX(ratio * VOICE_NOTE_SCALE_RATIO);
    context.voiceNoteBtn.setScaleY(ratio * VOICE_NOTE_SCALE_RATIO);

    float newRatio = ratio - 1;
    float val = (newRatio * context.recordingViewInitWidth) + ((1 - newRatio)
        * (context.playerBtn.getWidth()));

    ViewGroup.LayoutParams layoutParams = recordingView.getLayoutParams();
    layoutParams.width = (int) val;
    recordingView.setLayoutParams(layoutParams);

    context.timerVoiceNote.setAlpha(newRatio);
    context.loadingRecordView.setAlpha(newRatio);

    if (newRatio == 1) {
      context.loadingRecordView.animate()
          .alpha(1f)
          .withStartAction(() -> context.loadingRecordView.setVisibility(VISIBLE))
          .setDuration(context.ANIM_DURATION_FAST)
          .start();
      context.timerVoiceNote.animate()
          .alpha(1f)
          .withStartAction(() -> context.timerVoiceNote.setVisibility(VISIBLE))
          .setDuration(context.ANIM_DURATION_FAST)
          .start();
    }

    if (ratio == 1) {
      context.voiceNoteBtn.setImageDrawable(
          ContextCompat.getDrawable(context.getContext(), R.drawable.picto_cancel_voice_note));
    } else {
      context.voiceNoteBtn.setImageDrawable(null);
    }

    context.voiceNoteBtn.setAlpha(ratio);
  }

  public void onSingleTap() {
    int translation = screenUtils.dpToPx(30);
    context.hintEditText.setTranslationX(-translation);
    context.editText.setCursorVisible(false);
    recordingView.setVisibility(View.INVISIBLE);
    context.hintEditText.setVisibility(VISIBLE);
    context.hintEditText.setAlpha(0f);
    context.hintEditText.setText(
        context.getContext().getString(R.string.chat_placeholder_tap_and_hold));
    context.hintEditText.setTextColor(
        ContextCompat.getColor(context.getContext(), R.color.blue_voice_bck));
    context.editText.setHintTextColor(
        ContextCompat.getColor(context.getContext(), R.color.transparent));

    context.hintEditText.animate()
        .translationX(0)
        .alpha(1f)
        .setDuration(ANIM_DURATION)
        .withEndAction(() -> context.hintEditText.postDelayed(() -> {
          context.editText.setCursorVisible(true);
          context.hintEditText.animate().translationX(-translation).withStartAction(() -> {
            context.hintEditText.setText("");
            context.editText.setHintTextColor(
                ContextCompat.getColor(context.getContext(), R.color.grey_chat_grey_hint));
            context.hintEditText.setTextColor(
                ContextCompat.getColor(context.getContext(), R.color.grey_chat_grey_hint));
            context.editText.setHint(
                context.getResources().getString(R.string.chat_placeholder_message));
          }).setDuration(ANIM_DURATION_FAST).start();
        }, 1000))
        .start();
  }
}