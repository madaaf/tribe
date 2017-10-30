package com.tribe.app.presentation.view.widget.chat;

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

/**
 * Created by madaaflak on 11/10/2017.
 */

public class SwipeDetector2 implements View.OnTouchListener {

  private static float VOICE_NOTE_SCALE_RATIO = 0.75f;

  private static final int SWIPE_MIN_DISTANCE = 1;
  private static final int MAX_CLICK_DURATION = 200;
  private long startClickTime;
  private long lastTouchDown;
  private long lastTouchMove;
  private static int CLICK_ACTION_THRESHHOLD = 200;

  private GestureDetector mGestureDetector;
  private View mView;
  private View recordingView;
  private ChatView context;
  private float initialPosition, initPos, ratio;
  private ScreenUtils screenUtils;
  private MediaRecorder recorder = null;
  public Subscription timerVoiceSub;

  public SwipeDetector2(ChatView context, View view, View recordingView, float initPos,
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
      onActionUp(mView, getRatio());
      return false;
    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
      onActionDown(mView);
      if (System.currentTimeMillis() - lastTouchMove > CLICK_ACTION_THRESHHOLD) {

      }
      lastTouchDown = System.currentTimeMillis();
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
        private float mMotionDownX, mMotionDownY;

        @Override public boolean onSingleTapUp(MotionEvent e) {
          Timber.e("MADA onSingleTapUp");
          return super.onSingleTapUp(e);
        }

        @Override public boolean onSingleTapConfirmed(MotionEvent e) {
          Timber.e("MADA onSingleTapConfirmed ");
          onSingleTap();
          return super.onSingleTapConfirmed(e);
        }

        @Override public boolean onContextClick(MotionEvent e) {
          Timber.e("MADA onContextClick");
          return super.onContextClick(e);
        }

        @Override public boolean onDown(MotionEvent e) {
          mMotionDownX = e.getRawX() - mView.getTranslationX();
          mMotionDownY = e.getRawY() - mView.getTranslationY();

          return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
          lastTouchMove = System.currentTimeMillis();
          float x2 = mView.getX() + (mView.getWidth() / 2);
          ratio = getRatio();

          try {

            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && mView.getX() > initPos) {
              mView.setTranslationX(e2.getRawX() - mMotionDownX);
            } else if (mView.getX() < initPos) { // TRASH POSTITION
              mView.setX(initPos);
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                && mView.getX() < initialPosition) {
              mView.setTranslationX(e2.getRawX() - mMotionDownX);
            } else if (mView.getX() > initialPosition) {
              mView.setX(initialPosition);
            }

            if (x2 < (screenUtils.getWidthPx() / 2) || x2 > initialPosition) {
              recordingView.setX(screenUtils.getWidthPx() / 2 - (recordingView.getWidth() / 2));
            } else {
              recordingView.setTranslationX(
                  e2.getRawX() - mMotionDownX - (screenUtils.getWidthPx() / 2));
            }

            right2left(mView, ratio);
          } catch (Exception e) {
            // nothing
          }
          return true;
        }
      };

  public void onActionUp(View v, float ratio) {
    Timber.i("ON ACTION :onActionUp! " + ratio + "  " + context.audioDuration);
    if (ratio == 1) {
      context.voiceNoteBtn.setBackground(
          ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_orange));
      context.voiceNoteBtn.setImageDrawable(
          ContextCompat.getDrawable(context.getContext(), R.drawable.picto_trash_white));
      context.playerBtn.setImageDrawable(
          ContextCompat.getDrawable(context.getContext(), R.drawable.picto_trash_white));
      context.playerBtn.setBackground(
          ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_orange));

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
                  .setInterpolator(new OvershootInterpolator(2.5f))
                  .withEndAction(() -> {
                    recordingView.setScaleX(1f);
                    recordingView.setScaleY(1f);
                    recordingView.setVisibility(GONE);
                  })
                  .start();
              context.voiceNoteBtn.setBackground(
                  ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_grey));
            })
            .start();
        stopVoiceNote(false, true);
      }, 1500);
    } else {
      Timber.i("ACTION UP RATIO 0! " + ratio);
      stopVoiceNote(true, true);
    }
  }

  private void startVoiceNote() {
    Timber.e("startVoiceNote");
    timerVoiceSub = Observable.interval(1, TimeUnit.SECONDS)
        .timeInterval()
        .observeOn(AndroidSchedulers.mainThread())
        .onBackpressureDrop()
        .map(ok -> {
          long value = ok.getValue() + 1;
          context.audioDuration = Float.valueOf(value);
          Timber.e("SOEF DURATION " + context.audioDuration);
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

    recordingView.setVisibility(VISIBLE);

    context.voiceNoteBtn.setBackground(
        ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_blue));
    context.editText.setCursorVisible(false);

    context.voiceNoteBtn.animate() // TODO SOEF
        .scaleX(2f * VOICE_NOTE_SCALE_RATIO)
        .scaleY(2f * VOICE_NOTE_SCALE_RATIO)
        .setInterpolator(new OvershootInterpolator())
        .setDuration(ANIM_DURATION)
        .withStartAction(() -> {
          context.btnSendLikeContainer.setVisibility(GONE);
          context.uploadImageBtn.setVisibility(GONE);
          context.layoutPulse.setVisibility(GONE);
          context.videoCallBtn.setVisibility(GONE);
          recordingView.animate()
              .translationY(-(recordingView.getHeight() * 2.5f))
              .setInterpolator(new OvershootInterpolator())
              .setDuration(ANIM_DURATION)
              .start();
          context.editText.setHint("");
          context.hintEditText.setAlpha(0);
          context.hintEditText.setText(
              context.getContext().getString(R.string.chat_placeholder_slide_to_cancel));
          context.hintEditText.animate().alpha(0.75f).setDuration(ANIM_DURATION).start(); // TODO
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
    a2.setParams(recordingView.getWidth(), recordingView.getWidth() + 100,
        recordingView.getHeight(), recordingView.getHeight());
    recordingView.startAnimation(a2);
  }

  public void onActionDown(View v) {
    Timber.e("onActionDown");
    context.onRecord = true;
    startRecording();
    startVoiceNote();
  }

  private void stopRecording() {
    Timber.e("stopRecording");

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
    Timber.e("startRecording");
    context.fileName = context.getContext().getExternalCacheDir().getAbsolutePath()
        + File.separator
        + context.dateUtils.getUTCDateAsString()
        + context.user.getId()
        + "audiorecord.mp4";
    context.fileName = context.fileName.replaceAll(" ", "_").replaceAll(":", "-");

    Timber.w("SOEF " + context.fileName);
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

  private void setRecordingViewVisibility() {
   /* if (context.audioDuration > 0) {
      recordingView.setVisibility(VISIBLE);
    } else {
      recordingView.setVisibility(INVISIBLE);
    }*/
  }

  public void bottom2top(View v) {
    Timber.i("ON ACTION : bottom2top!");
  }

  public void left2right(View v) {
    Timber.i("ON ACTION : left2right!");
  }

  public void right2left(View v, float ratio) {
    setRecordingViewVisibility();
    context.voiceNoteBtn.setScaleX(ratio * VOICE_NOTE_SCALE_RATIO);
    context.voiceNoteBtn.setScaleY(ratio * VOICE_NOTE_SCALE_RATIO);

    float newRatio = ratio - 1;
    float val = (newRatio * context.recordingViewInitWidth) + ((1 - newRatio)
        * (context.playerBtn.getWidth()));

    ViewGroup.LayoutParams layoutParams = recordingView.getLayoutParams();
    layoutParams.width = (int) val;
    recordingView.setLayoutParams(layoutParams);
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
    } else {
      context.loadingRecordView.setVisibility(GONE);
      context.timerVoiceNote.setVisibility(GONE);
      context.timerVoiceNote.setAlpha(0f);
      context.loadingRecordView.setAlpha(0f);
    }
    Timber.i("right2left! " + ratio + " " + newRatio + " " + val + " " + (int) val);
    if (ratio == 1) {
      context.voiceNoteBtn.setImageDrawable(
          ContextCompat.getDrawable(context.getContext(), R.drawable.picto_cancel_voice_note));
    } else {
      context.voiceNoteBtn.setImageDrawable(null);
    }

    context.voiceNoteBtn.setAlpha(ratio);
  }

  public void onSingleTap() {
    Timber.e(
        "ON ACTION : ON SINGLE TAP " + context.audioDuration + " " + (context.audioDuration > 1f));
    if (context.audioDuration > 1f) {
      context.fileName = null;
      context.audioDuration = 0f;
      stopRecording();
      return;
    }
    context.fileName = null;
    context.audioDuration = 0f;
    recordingView.setVisibility(GONE);
    context.hintEditText.setAlpha(0);
    context.hintEditText.setText(
        context.getContext().getString(R.string.chat_placeholder_tap_and_hold));
    context.hintEditText.setTextColor(
        ContextCompat.getColor(context.getContext(), R.color.blue_voice_bck));
    context.editText.setHint("");
    context.hintEditText.animate()
        .translationX(screenUtils.dpToPx(30))
        .alpha(1f)
        .setDuration(ANIM_DURATION)
        .withEndAction(() -> context.hintEditText.postDelayed(() -> {
          context.hintEditText.setText("");
          context.editText.setHint(
              context.getResources().getString(R.string.chat_placeholder_message));
          context.hintEditText.setTextColor(
              ContextCompat.getColor(context.getContext(), R.color.grey_chat_grey_hint));

          ViewGroup.LayoutParams params = context.editText.getLayoutParams();
          params.width = context.widthRefInit;
          context.editText.setLayoutParams(params);
          context.editText.clearAnimation();

          context.voiceNoteBtn.setScaleX(1f);
          context.voiceNoteBtn.setScaleY(1f);
          ViewGroup.LayoutParams p = context.voiceNoteBtn.getLayoutParams();
          p.width = context.voiceNoteBtnWidth;
          p.height = context.voiceNoteBtnWidth;
          context.voiceNoteBtn.setLayoutParams(p);
          context.voiceNoteBtn.clearAnimation();
        }, 1000))
        .start();


    ViewGroup.LayoutParams p = context.recordingView.getLayoutParams();
    p.width = context.recordingViewInitWidth;
    context.recordingView.setLayoutParams(p);



    context.uploadImageBtn.setAlpha(1f);
    context.uploadImageBtn.setVisibility(VISIBLE);
    context.sendBtn.setVisibility(VISIBLE);
    context.likeBtn.setVisibility(VISIBLE);
    context.btnSendLikeContainer.setVisibility(VISIBLE);
    context.uploadImageBtn.setVisibility(VISIBLE);
    context.layoutPulse.setVisibility(VISIBLE);
    context.videoCallBtn.setVisibility(VISIBLE);
    context.editText.setCursorVisible(true);

    recordingView.clearAnimation();

    context.voiceNoteBtn.setBackground(
        ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_grey));
  }

  private void stopVoiceNote(boolean sendMessage, boolean withAnim) {
    Timber.e("stopVoiceNote  + send Message " + sendMessage + "  " + context.audioDuration);

    context.editText.getLayoutParams().width = context.widthRefInit;
    context.trashBtn.setVisibility(GONE);
    String time = String.valueOf(context.timerVoiceNote.getText());
    context.timerVoiceNote.setText("0:01");
    timerVoiceSub.unsubscribe();
    timerVoiceSub = null;
    context.onRecord = false;
    stopRecording();
    if (context.audioDuration < 1f) {
      return;
    }

    if (sendMessage && context.audioDuration > 0) {
      context.audioCount++;
      context.sendMessageToAdapter(Message.MESSAGE_AUDIO, time, null);
    }

    context.trashBtn.setAlpha(0f);
    context.hintEditText.setAlpha(0f);
    context.editText.clearAnimation();

    context.voiceNoteBtn.animate()
        .scaleX(1f)
        .scaleY(1f)
        .setInterpolator(new LinearInterpolator())
        .setDuration(ANIM_DURATION)
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
        })
        .withEndAction(() -> {
          recordingView.clearAnimation();
          context.voiceNoteBtn.clearAnimation();


          ViewGroup.LayoutParams p = context.recordingView.getLayoutParams();
          p.width = context.recordingViewInitWidth;
          context.recordingView.setLayoutParams(p);

          recordingView.setX(context.recordingViewX);
          recordingView.setY(screenUtils.getHeightPx() + recordingView.getHeight());
          context.playerBtn.setScaleX(1);
          context.playerBtn.setScaleY(1);
          context.loadingRecordView.setVisibility(VISIBLE);
          context.timerVoiceNote.setVisibility(VISIBLE);
          recordingView.setBackground(
              ContextCompat.getDrawable(context.getContext(), R.drawable.shape_rect_voice_note));
          context.voiceNoteBtn.setX(context.voiceNoteBtnX);
          context.voiceNoteBtn.setAlpha(1f);
          context.playerBtn.setAlpha(1f);
          context.playerBtn.setImageDrawable(
              ContextCompat.getDrawable(context.getContext(), R.drawable.picto_play_recording));
          context.playerBtn.setBackground(null);
          context.voiceNoteBtn.setBackground(
              ContextCompat.getDrawable(context.getContext(), R.drawable.shape_circle_grey));
        })
        .start();
  }
}