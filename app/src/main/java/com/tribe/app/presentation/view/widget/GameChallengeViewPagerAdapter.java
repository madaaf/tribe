package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.List;
import java.util.Random;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengeViewPagerAdapter extends PagerAdapter {

  private Context mContext;
  private LayoutInflater mLayoutInflater;
  private List<String> items;
  private List<TribeGuest> guestList;

  public GameChallengeViewPagerAdapter(Context context, GameChallenge gameChallenge) {
    mContext = context;
    this.items = gameChallenge.getNameList();
    this.guestList = gameChallenge.getGuestList();
    Timber.e("SOEF GameChallengeViewPagerAdapter items: "
        + items.size()
        + " guest :"
        + guestList.size());
    mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override public int getCount() {
    return 3;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    String ok = "";
    TribeGuest guest = null;
    if (items != null && !items.isEmpty()) {
      ok = items.get(position);
      guest = guestList.get(0);
    }

    View itemView = mLayoutInflater.inflate(R.layout.item_game_challenges, container, false);

    TextViewFont txt = (TextViewFont) itemView.findViewById(R.id.txtChallenge);
    TextViewFont txtName = (TextViewFont) itemView.findViewById(R.id.txtName);
    TextViewFont txtUsername = (TextViewFont) itemView.findViewById(R.id.txtUsername);
    AvatarView viewAvatar = (AvatarView) itemView.findViewById(R.id.viewAvatar);

    txt.setText(ok);
    txtName.setText(guest.getDisplayName());
    txtUsername.setText("Your turn to be challenged");
    viewAvatar.load(guest.getPicture());
    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }

  private static int getRandom(int[] array) {
    int rnd = new Random().nextInt(array.length);
    return array[rnd];
  }
}
