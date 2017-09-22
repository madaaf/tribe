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
import butterknife.BindView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import java.util.List;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class MessageImageAdapterDelegate extends BaseMessageAdapterDelegate {

  public MessageImageAdapterDelegate(Context context) {
    super(context);
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
    Uri uri = m.getUri();

    Glide.with(context).load(o.getUrl()).asBitmap().into(new BitmapImageViewTarget(vh.image) {
      @Override protected void setResource(Bitmap resource) {
        RoundedBitmapDrawable circularBitmapDrawable =
            RoundedBitmapDrawableFactory.create(context.getResources(), resource);
        circularBitmapDrawable.setCornerRadius(30);
        vh.image.setImageDrawable(circularBitmapDrawable);
      }
    });
  }

  @Override protected BaseTextViewHolder getViewHolder(ViewGroup parent) {
    MessageImageViewHolder vh = new MessageImageViewHolder(
        layoutInflater.inflate(R.layout.item_message_image, parent, false));
    return vh;
  }

  static class MessageImageViewHolder extends BaseTextViewHolder {
    @BindView(R.id.image) public ImageView image;

    /**/
    MessageImageViewHolder(View itemView) {
      super(itemView);
    }

    @Override protected ViewGroup getLayoutContent() {
      return null;
    }
  }
}
