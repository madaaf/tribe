package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengeViewPagerAdapter extends PagerAdapter {

  private Context mContext;
  private LayoutInflater mLayoutInflater;
  private List<String> items = new ArrayList<>();
  private List<TribeGuest> guestList = new ArrayList<>();
  private GameChallenge gameChallenge;
  private PublishSubject<GameChallenge> onNextChallenge = PublishSubject.create();
  private User user;

  public GameChallengeViewPagerAdapter(Context context, User user) {
    mContext = context;
    this.user = user;
    Timber.e("SOEF GameChallengeViewPagerAdapter items: "
        + items.size()
        + " guest :"
        + guestList.size());
    mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  public void setGameChallenge(GameChallenge gameChallenge) {
    this.gameChallenge = gameChallenge;
    this.items = gameChallenge.getNameList();
    this.guestList = gameChallenge.getGuestList();
  }

  @Override public int getCount() {
    return items.size();
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {
    Timber.e("SOEF GET instantiateItem " + getCount());
    String challenge = "";
    TribeGuest guest = null;
    if (items != null && !items.isEmpty()) {
      challenge = items.get(position);
      if (!guestList.isEmpty()) guest = guestList.get(0);
    }

    View itemView = mLayoutInflater.inflate(R.layout.item_game_challenges, container, false);

    TextViewFont txtChallenge = (TextViewFont) itemView.findViewById(R.id.txtChallenge);
    TextViewFont txtName = (TextViewFont) itemView.findViewById(R.id.txtName);
    TextViewFont txtUsername = (TextViewFont) itemView.findViewById(R.id.txtUsername);
    AvatarView viewAvatar = (AvatarView) itemView.findViewById(R.id.viewAvatar);

    Timber.e("SOEF message challenge " + challenge);
    gameChallenge.setCurrentChallenge(challenge);
    gameChallenge.setPeerId(guest.getId());
    // gameChallenge.setName("message send by  : " + user.getDisplayName() + " " + user.getId());

    onNextChallenge.onNext(gameChallenge);

    txtChallenge.setText(challenge);
    if (guest != null) {
      txtName.setText(guest.getDisplayName());
      viewAvatar.load(guest.getPicture());
    }
    txtUsername.setText("Your turn to be challenged");

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

  public Observable<GameChallenge> onNextChallenge() {
    return onNextChallenge;
  }
}
