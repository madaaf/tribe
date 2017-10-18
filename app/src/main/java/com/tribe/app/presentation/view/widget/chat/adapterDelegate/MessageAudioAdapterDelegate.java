package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageAudio;
import com.wang.avi.AVLoadingIndicatorView;
import java.util.List;
import timber.log.Timber;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAudioAdapterDelegate extends BaseMessageAdapterDelegate {
  private static int ANIM_DURATION = 300;
  private static int DURATION = 200;

  private int currentPlayingPosition;
  private MediaPlayer mediaPlayer;
  private MessageAudioViewHolder playingHolder;
  private ValueAnimator anim;
  private long currentPlayTime;

  public MessageAudioAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    currentPlayingPosition = -1;
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessageAudio;
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    return new MessageAudioViewHolder(
        layoutInflater.inflate(R.layout.item_message_audio, parent, false));
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessageAudio m = (MessageAudio) items.get(position);
    MessageAudioViewHolder vh = (MessageAudioViewHolder) holder;
    Image o = m.getOriginal();
    String time =
        (m.getTime() != null && !m.getTime().isEmpty()) ? m.getTime() : o.getDurationFormatted();
    // String time = o.getDurationFormatted();
    vh.timerVoiceNote.setText(time);

    vh.pauseBtn.setOnClickListener(view -> onClickBtn(position, o, vh));
    vh.playBtn.setOnClickListener(view -> onClickBtn(position, o, vh));

    setPendingBehavior(m, vh.container);
  }

  private void onClickBtn(int position, Image o, MessageAudioViewHolder vh) {

    if (position == currentPlayingPosition) {
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.pause();
        stopAnimation(vh);
        setEqualizerAnim(vh, false);
      } else {
        mediaPlayer.start();
        startAnimation(vh);
        setEqualizerAnim(vh, true);
        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> Timber.e("onCompletion"));
      }
    } else {
      currentPlayingPosition = position;
      if (mediaPlayer != null) {
        if (null != playingHolder) {
          updateNonPlayingView(playingHolder);
        }
        mediaPlayer.release();
      }
      playingHolder = vh;

      setEqualizerAnim(vh, true);
      startMediaPlayer(Uri.parse(o.getUrl()));
      animePlayerIndicator(vh, o.getDurationMs());
    }
    updatePlayingView();
  }

  private void startMediaPlayer(Uri audioResId) {
    mediaPlayer = MediaPlayer.create(context.getApplicationContext(), audioResId);
    mediaPlayer.setOnCompletionListener(mp -> releaseMediaPlayer());
    mediaPlayer.start();
  }

  private void releaseMediaPlayer() {
    if (null != playingHolder) {
      updateNonPlayingView(playingHolder);
    }
    mediaPlayer.release();
    mediaPlayer = null;
    currentPlayingPosition = -1;
  }

  private void updateNonPlayingView(MessageAudioViewHolder holder) {
    holder.playBtn.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.picto_play_recording));

    holder.playerIndicator.setVisibility(View.INVISIBLE);
    holder.equalizer.setVisibility(VISIBLE);
    holder.loadingRecordView.setVisibility(View.INVISIBLE);
  }

  void stopPlayer() {
    if (null != mediaPlayer) {
      releaseMediaPlayer();
    }
  }

  private void updatePlayingView() {
    if (mediaPlayer.isPlaying()) {
      switchPauseToPlayBtn(playingHolder, false);
    } else {
      switchPauseToPlayBtn(playingHolder, true);
    }
  }

  private void switchPauseToPlayBtn(MessageAudioViewHolder playingHolder, boolean fromPauseToPlay) {
    if (!fromPauseToPlay) {
      playingHolder.playBtn.animate()
          .scaleX(0f)
          .scaleY(0f)
          .rotation(45)
          .setDuration(ANIM_DURATION)
          .alpha(0f)
          .withStartAction(() -> {

            playingHolder.playBtn.setRotation(0);
            playingHolder.playBtn.setScaleX(1);
            playingHolder.playBtn.setScaleY(1);
            playingHolder.playBtn.setAlpha(1f);
            playingHolder.playBtn.setVisibility(VISIBLE);

            playingHolder.pauseBtn.setRotation(90);
            playingHolder.pauseBtn.setScaleX(0);
            playingHolder.pauseBtn.setScaleY(0);
            playingHolder.pauseBtn.setAlpha(0f);
            playingHolder.pauseBtn.setVisibility(VISIBLE);

            playingHolder.pauseBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(1f)
                .withEndAction(() -> {
                  playingHolder.playBtn.setVisibility(GONE);
                })
                .start();
          })
          .start();
    } else {
      playingHolder.pauseBtn.animate()
          .scaleX(0f)
          .scaleY(0f)
          .rotation(0)
          .setDuration(ANIM_DURATION)
          .alpha(0f)
          .withStartAction(() -> {
            playingHolder.pauseBtn.setRotation(90);
            playingHolder.pauseBtn.setScaleX(1);
            playingHolder.pauseBtn.setScaleY(1);
            playingHolder.pauseBtn.setAlpha(1f);
            playingHolder.pauseBtn.setVisibility(VISIBLE);

            playingHolder.playBtn.setRotation(45);
            playingHolder.playBtn.setScaleX(0);
            playingHolder.playBtn.setScaleY(0);
            playingHolder.playBtn.setAlpha(0f);
            playingHolder.playBtn.setVisibility(VISIBLE);

            playingHolder.playBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIM_DURATION)
                .alpha(1f)
                .withEndAction(() -> {
                  playingHolder.pauseBtn.setVisibility(GONE);
                })
                .start();
          })
          .start();
    }
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MessageAudioViewHolder vh = (MessageAudioViewHolder) holder;
    MessageAudio m = (MessageAudio) items.get(position);
    Timber.w("PUT 1f " + m.toString() + ((MessageAudio) payloads.get(0)).toString());
    if (m.isPending()) {
      vh.container.setAlpha(0.4f);
    } else {
      vh.container.setAlpha(1f);
    }
  }

  private void animePlayerIndicator(MessageAudioViewHolder vh, int duration) {
    vh.playerIndicator.setVisibility(VISIBLE);

    anim = ValueAnimator.ofInt(0, vh.recordingView.getWidth());
    anim.addUpdateListener(valueAnimator -> {
      int val = (Integer) valueAnimator.getAnimatedValue();
      ViewGroup.LayoutParams layoutParams = vh.playerIndicator.getLayoutParams();
      layoutParams.width = val;
      vh.playerIndicator.setLayoutParams(layoutParams);
    });
    anim.setDuration(duration);
    anim.start();

    anim.getCurrentPlayTime();
  }

  private void stopAnimation(MessageAudioViewHolder vh) {
    setEqualizerAnim(vh, false);
    currentPlayTime = anim.getCurrentPlayTime();
    anim.cancel();
  }

  private void setEqualizerAnim(MessageAudioViewHolder vh, boolean isPlaying) {
    if (isPlaying) {
      vh.equalizer.setVisibility(View.INVISIBLE);
      vh.loadingRecordView.setVisibility(VISIBLE);
    } else {
      vh.equalizer.setVisibility(VISIBLE);
      vh.loadingRecordView.setVisibility(View.INVISIBLE);
    }
  }

  private void startAnimation(MessageAudioViewHolder vh) {
    setEqualizerAnim(vh, true);
    anim.start();
    anim.setCurrentPlayTime(currentPlayTime);
  }

  static class MessageAudioViewHolder extends BaseTextViewHolder {

    @BindView(R.id.container) public RelativeLayout container;
    @BindView(R.id.timerVoiceNote) public TextViewFont timerVoiceNote;
    @BindView(R.id.playBtn) public ImageView playBtn;
    @BindView(R.id.playerIndicator) public FrameLayout playerIndicator;
    @BindView(R.id.recordingView) public FrameLayout recordingView;
    @BindView(R.id.loadingRecordView) public AVLoadingIndicatorView loadingRecordView;
    @BindView(R.id.equalizer) ImageView equalizer;
    @BindView(R.id.pauseBtn) ImageView pauseBtn;

    MessageAudioViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
