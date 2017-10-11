package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

  public MessageAudioAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    vh.timerVoiceNote.setText(m.getTime());

    vh.playBtn.setOnClickListener(view -> {
      Drawable d = vh.playBtn.getDrawable();
      Timber.e("DRAWABLE " + d + " " + ContextCompat.getDrawable(context,
          R.drawable.picto_play_recording) + " " + ContextCompat.getDrawable(context,
          R.drawable.picto_play_recording));
      if (d == ContextCompat.getDrawable(context, R.drawable.picto_play_recording)) {
        vh.playBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_pause_recording));
      } else {
        vh.playBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_play_recording));
      }
    });
    setPendingBehavior(m, vh.container);
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

  static class MessageAudioViewHolder extends BaseTextViewHolder {

    @BindView(R.id.container) public LinearLayout container;
    @BindView(R.id.timerVoiceNote) public TextViewFont timerVoiceNote;
    @BindView(R.id.playBtn) public ImageView playBtn;

    MessageAudioViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }
  }
}
