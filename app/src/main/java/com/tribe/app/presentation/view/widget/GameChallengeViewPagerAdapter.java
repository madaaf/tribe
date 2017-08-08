package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.view.utils.MoveViewTouchListener;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameChallenge;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameChallengeViewPagerAdapter extends PagerAdapter {

  private Context context;
  private LayoutInflater mLayoutInflater;

  private User user;
  private GameManager gameManager;
  private int currentPosition;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private PublishSubject<Game> onCurrentGame = PublishSubject.create();

  public GameChallengeViewPagerAdapter(Context context, User user) {
    this.context = context;
    this.user = user;
    mLayoutInflater =
        (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    gameManager = GameManager.getInstance(context);
  }

  @Override public int getCount() {
    return 100;
  }

  @Override public boolean isViewFromObject(View view, Object object) {
    return view == object;
  }

  @Override public Object instantiateItem(ViewGroup container, int position) {

    View itemView = mLayoutInflater.inflate(R.layout.item_game_challenges, container, false);

    GameChallenge game = (GameChallenge) gameManager.getCurrentGame();

    TextViewFont txtChallenge = (TextViewFont) itemView.findViewById(R.id.txtChallenge);
    TextViewFont txtName = (TextViewFont) itemView.findViewById(R.id.txtName);
    TextViewFont txtUsername = (TextViewFont) itemView.findViewById(R.id.txtUsername);
    AvatarView viewAvatar = (AvatarView) itemView.findViewById(R.id.viewAvatar);
    CardView card = (CardView) itemView.findViewById(R.id.cardview);

    MoveViewTouchListener moveListener = new MoveViewTouchListener(card);
    card.setOnTouchListener(moveListener);
    subscriptions.add(moveListener.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));

    TribeGuest guest = game.getCurrentChallenger();
    String challenge = game.getCurrentChallenge();

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
    } else {
      Timber.e("SOEF guest = null");
    }

    container.addView(itemView);
    return itemView;
  }

  @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
    if (position == currentPosition) return;
    currentPosition = position;

    GameChallenge challenge = (GameChallenge) gameManager.getCurrentGame();
    if (challenge.isUserAction()) {
      onCurrentGame.onNext(challenge);
    }
  }

  public Observable<Game> onCurrentGame() {
    return onCurrentGame;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }

  public Observable<Boolean> onBlockOpenInviteView() {
    return onBlockOpenInviteView;
  }
}
