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
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageAudio;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageAudioAdapterDelegate extends BaseMessageAdapterDelegate {

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

    vh.timerVoiceNote.setText(m.getOriginal().getDurationFormatted());

    vh.playBtn.setOnClickListener(view -> {

      if (position == currentPlayingPosition) {
        if (mediaPlayer.isPlaying()) {
          mediaPlayer.pause();
          stopAnimation();
        } else {
          mediaPlayer.start();
          startAnimation();
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
        startMediaPlayer(Uri.parse(m.getOriginal().getUrl()));
        animePlayerIndicator(vh, m.getOriginal().getDurationMs());
      }
      updatePlayingView();
    });
    setPendingBehavior(m, vh.container);
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
  }

  void stopPlayer() {
    if (null != mediaPlayer) {
      releaseMediaPlayer();
    }
  }

  private void updatePlayingView() {
    if (mediaPlayer.isPlaying()) {
      playingHolder.playBtn.animate()
          .scaleX(0)
          .scaleY(0)
          .setDuration(DURATION)
          .setInterpolator(new OvershootInterpolator(3))
          .withEndAction(() -> {
            playingHolder.playBtn.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.picto_pause_recording));
            playingHolder.playBtn.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION)
                .setInterpolator(new OvershootInterpolator(3))
                .start();
          })
          .start();

      Timber.e("OK 1 ");
    } else {

      playingHolder.playBtn.animate()
          .scaleX(0)
          .scaleY(0)
          .setDuration(DURATION)
          .setInterpolator(new OvershootInterpolator(3))
          .withEndAction(() -> {
            playingHolder.playBtn.setImageDrawable(
                ContextCompat.getDrawable(context, R.drawable.picto_play_recording));
            playingHolder.playBtn.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(DURATION)
                .setInterpolator(new OvershootInterpolator(3))
                .start();
          })
          .start();
      Timber.e("OK 2 ");
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
    vh.playerIndicator.setVisibility(View.VISIBLE);

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

  private void stopAnimation() {
    currentPlayTime = anim.getCurrentPlayTime();
    anim.cancel();
  }

  private void startAnimation() {
    anim.start();
    anim.setCurrentPlayTime(currentPlayTime);
  }

  static class MessageAudioViewHolder extends BaseTextViewHolder {

    @BindView(R.id.container) public RelativeLayout container;
    @BindView(R.id.timerVoiceNote) public TextViewFont timerVoiceNote;
    @BindView(R.id.playBtn) public ImageView playBtn;
    @BindView(R.id.playerIndicator) public FrameLayout playerIndicator;
    @BindView(R.id.recordingView) public FrameLayout recordingView;

    MessageAudioViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
