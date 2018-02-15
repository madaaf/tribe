package com.tribe.app.presentation.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class NotificationViewPagerAdapter extends PagerAdapter {

  private List<NotificationModel> list;
  private Context context;
  private LayoutInflater layoutInflater;

  private PublishSubject<String> onClickBtn1 = PublishSubject.create();


  public NotificationViewPagerAdapter(Context context, List<NotificationModel> dataObjectList) {
    this.context = context;
    this.list = dataObjectList;
    layoutInflater =
        (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return list.size();
  }

  @Override public int getItemPosition(@NonNull Object object) {
    return super.getItemPosition(object);
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    View itemView = layoutInflater.inflate(R.layout.item_notification, container, false);
    container.addView(itemView);
    NotificationModel model = list.get(position);

    TextViewFont txtChallenge = itemView.findViewById(R.id.title);
    TextViewFont subtitle = itemView.findViewById(R.id.subtitle);
    TextViewFont content = itemView.findViewById(R.id.content);
    TextViewFont btn1Content = itemView.findViewById(R.id.btn1Content);
    ImageView backImage = itemView.findViewById(R.id.backImage);
    NewAvatarView avatarView = itemView.findViewById(R.id.avatarView);

    btn1Content.setOnClickListener(v -> {
      //newChatPresenter.createShortcut(model.getUserId());
      onClickBtn1.onNext(model.getUserId());
    });

    if (model.getDrawableBtn1() != null) {
      Drawable img = context.getResources().getDrawable(model.getDrawableBtn1());
      img.setBounds(0, 0, 60, 60);
      btn1Content.setCompoundDrawables(img, null, null, null);
    }

    if (model.getContent() != null) content.setText(model.getContent());
    if (model.getSubTitle() != null) subtitle.setText(model.getSubTitle());
    if (model.getTitle() != null) txtChallenge.setText(model.getTitle());
    if (model.getBtn1Content() != null) btn1Content.setText(model.getBtn1Content());
    if (model.getBackground() != null) {
      backImage.setImageDrawable(ContextCompat.getDrawable(context, model.getBackground()));
    }
    if (model.getProfilePicture() != null) avatarView.load(model.getProfilePicture());
    return itemView;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  public Observable<String> onClickBtn1() {
    return onClickBtn1;
  }
}
