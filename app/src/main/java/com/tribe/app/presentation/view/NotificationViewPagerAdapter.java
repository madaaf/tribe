package com.tribe.app.presentation.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by madaaflak on 09/02/2018.
 */

public class NotificationViewPagerAdapter extends PagerAdapter {
  private static final int DURATION_ANIMATION = 500;

  private List<NotificationModel> list;
  private Context context;
  private LayoutInflater layoutInflater;

  private PublishSubject<NotificationModel> onClickBtn1 = PublishSubject.create();

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
    NotificationModel model = list.get(position);

    if (model.getView() == null) {
      View itemView = layoutInflater.inflate(R.layout.item_notification, container, false);
      container.addView(itemView);

      TextViewFont txtChallenge = itemView.findViewById(R.id.title);
      TextViewFont subtitle = itemView.findViewById(R.id.subtitle);
      TextViewFont content = itemView.findViewById(R.id.content);
      TextViewFont btn1Content = itemView.findViewById(R.id.btn1Content);
      ImageView backImage = itemView.findViewById(R.id.backImage);
      ImageView btn1DrawableStart = itemView.findViewById(R.id.btn1DrawableStart);
      ImageView btn1DrawableEnd = itemView.findViewById(R.id.btn1DrawableEnd);
      AvatarView avatarView = itemView.findViewById(R.id.avatarView);
      LinearLayout btn1Container = itemView.findViewById(R.id.btn1Container);
      FrameLayout mask = itemView.findViewById(R.id.mask);

      btn1Container.setOnClickListener(v -> {
        switch (model.getType()) {
          case NotificationModel.POPUP_CHALLENGER:
            break;
          case NotificationModel.POPUP_FACEBOOK:
            break;
          case NotificationModel.POPUP_POKE:
            break;
          case NotificationModel.POPUP_UPLOAD_PICTURE:
            break;
        }
        onClickBtn1.onNext(model);
      });

      if (model.getBtn1DrawableStart() != null) {
        btn1DrawableStart.setImageResource(model.getBtn1DrawableStart());
      }
      if (model.getBtn1DrawableEnd() != null) {
        btn1DrawableEnd.setImageResource(model.getBtn1DrawableEnd());
      }

      if (model.getContent() != null) content.setText(model.getContent());
      if (model.getSubTitle() != null) subtitle.setText(model.getSubTitle());
      if (model.getTitle() != null) txtChallenge.setText(model.getTitle());
      if (model.getBtn1Content() != null) btn1Content.setText(model.getBtn1Content());
      if (model.getBackground() != null) {
        backImage.setImageDrawable(ContextCompat.getDrawable(context, model.getBackground()));
      }
      if (model.getProfilePicture() != null) avatarView.load(model.getProfilePicture());
      if (model.getLogoPicture() != null) {
        mask.setVisibility(View.GONE);
        avatarView.setBackground(content.getResources().getDrawable(model.getLogoPicture()));
      } else {
        mask.setVisibility(View.VISIBLE);
      }

      return itemView;
    } else {
      container.addView(model.getView());
      return model.getView();
    }
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object object) {
    container.removeView((View) object);
  }

  public Observable<NotificationModel> onClickBtn1() {
    return onClickBtn1;
  }
}
