package com.tribe.app.presentation.view.widget.chat.adapterDelegate;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import java.util.List;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageImageAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessageImageAdapterDelegate(Context context, int type) {
    super(context, type);
    this.context = context;
    this.layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public boolean isForViewType(@NonNull List<Message> items, int position) {
    Message message = items.get(position);
    return message instanceof MessageImage;
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items, int position,
      @NonNull RecyclerView.ViewHolder holder) {
    super.onBindViewHolder(items, position, holder);

    MessageImageViewHolder vh = (MessageImageViewHolder) holder;
    MessageImage m = (MessageImage) items.get(position);

    Image o = m.getOriginal();
    String lowUrlImage =
        (m.getRessources() != null && m.getRessources().get(0) != null) ? m.getRessources()
            .get(0)
            .getUrl() : null;
    String url = (lowUrlImage != null) ? lowUrlImage : o.getUrl();
    Uri uri = m.getUri();

    Glide.with(context).load(url).asBitmap().into(new BitmapImageViewTarget(vh.image) {
      @Override protected void setResource(Bitmap resource) {
        RoundedBitmapDrawable circularBitmapDrawable =
            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
        circularBitmapDrawable.setCornerRadius(40);
        vh.image.setImageDrawable(circularBitmapDrawable);
      }
    });

    if (uri != null) setPendingBehavior(m, vh.container); // is pending
  }

  @Override public void onBindViewHolder(@NonNull List<Message> items,
      @NonNull RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
    MessageImageViewHolder vh = (MessageImageViewHolder) holder;
    MessageImage m = (MessageImage) items.get(position);
    if (m.isPending()) {
      vh.container.setAlpha(0.4f);
    } else {
      vh.container.setAlpha(1f);
    }
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    return new MessageImageViewHolder(
        layoutInflater.inflate(R.layout.item_message_image, parent, false));
  }

  static class MessageImageViewHolder extends BaseTextViewHolder {
    @BindView(R.id.image) public ImageView image;
    @BindView(R.id.container) public LinearLayout container;

    @Override protected ViewGroup getLayoutContent() {
      return container;
    }

    MessageImageViewHolder(View itemView) {
      super(itemView);
    }
  }
}
