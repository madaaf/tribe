package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import java.util.List;

/**
 * Created by madaaflak on 05/10/2017.
 */

public class PicturePagerAdapter extends PagerAdapter {

  private Context context;
  private LayoutInflater mLayoutInflater;
  private List<Message> messages;

  public PicturePagerAdapter(Context context, List<Message> messages) {
    this.context = context;
    this.messages = messages;
    mLayoutInflater =
        (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return messages.size();
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    View itemView = mLayoutInflater.inflate(R.layout.item_picture, container, false);

    ImageView imageView = (ImageView) itemView.findViewById(R.id.picture);
    MessageImage m = (MessageImage) messages.get(position);


    Glide.with(context)
        .load(m.getOriginal().getUrl())
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(imageView);

    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }
}
