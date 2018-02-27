package com.tribe.app.presentation.view.widget.avatar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 02/27/18
 */
public class PictoAvatarView extends LinearLayout {

  @BindView(R.id.imgLive) ImageView imgLive;
  @BindView(R.id.txtEmoji) TextViewFont txtEmoji;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public PictoAvatarView(Context context) {
    super(context);
    init();
  }

  public PictoAvatarView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void init() {
    initResources();

    LayoutInflater inflater =
        (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_picto_avatar, this, true);
    ButterKnife.bind(this);

    setGravity(Gravity.CENTER);
    setBackgroundResource(R.drawable.shape_circle_white);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    subscriptions.clear();
  }

  private void initResources() {

  }

  /**
   * PUBLIC
   */

  public void setLive() {
    imgLive.setVisibility(View.VISIBLE);
    txtEmoji.setVisibility(View.GONE);
  }

  public void setPlaying(String emoji) {
    txtEmoji.setVisibility(View.VISIBLE);
    imgLive.setVisibility(View.GONE);
    txtEmoji.setText(emoji);
  }
}
