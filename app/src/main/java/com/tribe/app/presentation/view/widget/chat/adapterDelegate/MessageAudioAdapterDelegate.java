package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.animation.ValueAnimator;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
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
import com.tribe.app.presentation.view.widget.chat.model.Media;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageAudio;
import com.wang.avi.AVLoadingIndicatorView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
  private List<Message> items;
  private HashMap<Integer, BaseTextViewHolder> itemsView;

  public MessageAudioAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    currentPlayingPosition = -1;
    items = new ArrayList<>();
    itemsView = new HashMap<>();
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
    this.items = items;

    MessageAudio m = (MessageAudio) items.get(position);
    MessageAudioViewHolder vh = (MessageAudioViewHolder) holder;
    itemsView.put(position, vh);

    Media o = m.getOriginal();
    String time =
        (m.getTime() != null && !m.getTime().isEmpty()) ? m.getTime() : o.getDurationFormatted();

    vh.timerVoiceNote.setText(time);

    vh.pauseBtn.setOnClickListener(view -> {
      onClickBtn(position);
    });
    vh.playBtn.setOnClickListener(view -> onClickBtn(position));

    setPendingBehavior(m, vh.container);
  }

  private void onClickBtn(int position) {
    MessageAudioViewHolder vh = null;
    if (position > items.size() - 1) {
      return;
    }

    if (itemsView.get(position) instanceof MessageAudioViewHolder) {
      vh = (MessageAudioViewHolder) itemsView.get(position);
    }

    if (!(items.get(position) instanceof MessageAudio)) {
      onClickBtn(position + 1);
      return;
    }

    MessageAudio m = (MessageAudio) items.get(position);
    Media o = m.getOriginal();
    if (position == currentPlayingPosition) {
      if (mediaPlayer.isPlaying()) {
        mediaPlayer.pause();
        stopAnimation(vh);
        setEqualizerAnim(vh, false);
      } else {
        mediaPlayer.start();
        startAnimation(vh);
        setEqualizerAnim(vh, true);
        MessageAudioViewHolder finalVh = vh;
        mediaPlayer.setOnCompletionListener(mediaPlayer1 -> {
          updateNonPlayingView(finalVh);
        });
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
      startMediaPlayer(Uri.parse(o.getUrl()), position);
      animePlayerIndicator(vh, o.getDurationMs());
    }
    updatePlayingView();
  }

  private void startMediaPlayer(Uri audioResId, int position) {
    mediaPlayer = MediaPlayer.create(context.getApplicationContext(), audioResId);

    mediaPlayer.setOnCompletionListener(mp -> {
      releaseMediaPlayer();
      onClickBtn(position + 1);
    });
    mediaPlayer.start();
  }

  private void releaseMediaPlayer() {
    if (null != playingHolder) {
      updateNonPlayingView(playingHolder);
    }
    if (mediaPlayer != null) {
      mediaPlayer.release();
    }
    mediaPlayer = null;
    currentPlayingPosition = -1;
  }

  private void updateNonPlayingView(MessageAudioViewHolder holder) {
    holder.cardViewIndicator.setVisibility(View.INVISIBLE);
    holder.equalizer.setVisibility(VISIBLE);
    holder.loadingRecordView.setVisibility(View.INVISIBLE);
    switchPauseToPlayBtn(playingHolder, true);
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

  private void resetPauseBtn(MessageAudioViewHolder vh, boolean hide) {
    float i = 1f;
    if (hide) {
      i = 0f;
    }
    vh.pauseBtn.setScaleX(i);
    vh.pauseBtn.setScaleY(i);
    vh.pauseBtn.setAlpha(i);
  }

  private void resetPlayBtn(MessageAudioViewHolder vh, boolean hide) {
    float i = 1f;
    if (hide) {
      i = 0f;
    }
    vh.playBtn.setScaleX(i);
    vh.playBtn.setScaleY(i);
    vh.playBtn.setAlpha(i);
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
            resetPlayBtn(playingHolder, false);

            playingHolder.pauseBtn.setRotation(90);
            resetPauseBtn(playingHolder, true);

            playingHolder.pauseBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(1f)
                .withEndAction(() -> {

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

            playingHolder.playBtn.setRotation(45);
            playingHolder.playBtn.setScaleX(0);
            playingHolder.playBtn.setScaleY(0);
            playingHolder.playBtn.setAlpha(0f);

            playingHolder.playBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIM_DURATION)
                .alpha(1f)
                .withEndAction(() -> {

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

    if (m.isPending()) {
      vh.container.setAlpha(0.4f);
    } else {
      vh.container.setAlpha(1f);
    }
  }

  private void animePlayerIndicator(MessageAudioViewHolder vh, int duration) {
    vh.cardViewIndicator.setVisibility(VISIBLE);

    anim = ValueAnimator.ofInt(0, vh.recordingView.getWidth());
    anim.addUpdateListener(valueAnimator -> {
      int val = (Integer) valueAnimator.getAnimatedValue();
      ViewGroup.LayoutParams layoutParams = vh.viewPlayerProgress.getLayoutParams();
      layoutParams.width = val;
      vh.viewPlayerProgress.setLayoutParams(layoutParams);
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

  public void stopListenVoiceNote() {
    releaseMediaPlayer();
  }

  static class MessageAudioViewHolder extends BaseTextViewHolder {

    @BindView(R.id.container) public RelativeLayout container;
    @BindView(R.id.timerVoiceNote) public TextViewFont timerVoiceNote;
    @BindView(R.id.playBtn) public ImageView playBtn;
    @BindView(R.id.pauseBtn) ImageView pauseBtn;
    @BindView(R.id.cardViewIndicator) public CardView cardViewIndicator;
    @BindView(R.id.viewPlayerProgress) public View viewPlayerProgress;
    @BindView(R.id.recordingView) public FrameLayout recordingView;
    @BindView(R.id.loadingRecordView) public AVLoadingIndicatorView loadingRecordView;
    @BindView(R.id.equalizer) ImageView equalizer;

    MessageAudioViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
