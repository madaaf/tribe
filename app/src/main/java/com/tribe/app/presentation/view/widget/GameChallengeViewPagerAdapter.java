package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.model.TribeGuest;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengeViewPagerAdapter extends PagerAdapter {
  private static int nbrPage = 1;
  private Context mContext;
  private LayoutInflater mLayoutInflater;

  private User user;
  private String challenge;
  private TribeGuest guest;

  public GameChallengeViewPagerAdapter(Context context, User user) {
    mContext = context;
    this.user = user;
    mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return 100;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    View itemView = mLayoutInflater.inflate(R.layout.item_game_challenges, container, false);

    TextViewFont txtChallenge = (TextViewFont) itemView.findViewById(R.id.txtChallenge);
    TextViewFont txtName = (TextViewFont) itemView.findViewById(R.id.txtName);
    TextViewFont txtUsername = (TextViewFont) itemView.findViewById(R.id.txtUsername);
    AvatarView viewAvatar = (AvatarView) itemView.findViewById(R.id.viewAvatar);

    txtChallenge.setText(challenge);

    if (guest != null) {
      if (guest.getId().equals(user.getId())) {
        txtName.setText(user.getDisplayName());
        viewAvatar.load(user.getProfilePicture());
        txtUsername.setText("Your turn to be challenged");
      } else {
        txtName.setText(guest.getDisplayName());
        viewAvatar.load(guest.getPicture());
        txtUsername.setText("is challenged");
      }
      Timber.i("SOEF  1: "
          + guest.getDisplayName()
          + " "
          + guest.getId()
          + " "
          + user.getId()
          + " "
          + user.getDisplayName()
          + " "
          + challenge);
    }
    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }

  public void setChallenge(String challenge, TribeGuest guest) {
    Timber.i("SET CHALLENGE " + guest.getDisplayName());
    this.challenge = challenge;
    this.guest = guest;
  }
}
