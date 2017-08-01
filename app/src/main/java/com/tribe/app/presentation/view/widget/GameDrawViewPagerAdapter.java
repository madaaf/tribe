package com.tribe.app.presentation.view.widget;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.game.GameDraw;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameDrawViewPagerAdapter extends PagerAdapter {
  private static int nbrPage = 1;
  private static int COUNTER = 30;
  private Context context;
  private LayoutInflater mLayoutInflater;

  private User user;
  private String challenge;
  private TribeGuest guest;
  private GameManager gameManager;
  private CountDownTimer countDownTimer;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();

  public GameDrawViewPagerAdapter(Context context, User user) {
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
    View itemView = mLayoutInflater.inflate(R.layout.item_game_draw, container, false);
    GameDraw draw = (GameDraw) gameManager.getCurrentGame();

    TextViewFont gameName = (TextViewFont) itemView.findViewById(R.id.gameName);
    TextViewFont nextInLabel = (TextViewFont) itemView.findViewById(R.id.nextInLabel);
    TextViewFont counter = (TextViewFont) itemView.findViewById(R.id.counter);
    TextViewFont drawDesc = (TextViewFont) itemView.findViewById(R.id.drawDesc);
    TextViewFont turn = (TextViewFont) itemView.findViewById(R.id.txtUsername);
    ImageView hand = (ImageView) itemView.findViewById(R.id.iconHand);
    TextViewFont txtName = (TextViewFont) itemView.findViewById(R.id.txtName);
    AvatarView viewAvatar = (AvatarView) itemView.findViewById(R.id.viewAvatar);

    TribeGuest guest = draw.getCurrentGuest();
    if (guest != null) {
      viewAvatar.load(guest.getPicture());
      txtName.setText(guest.getDisplayName());
    }

    nextInLabel.setText(
        EmojiParser.demojizedText(context.getString(R.string.game_draw_timer_instructions)));

    if (draw.isMyTurn()) {
      gameName.setText(draw.getCurrentDrawName());
      drawDesc.setText(
          EmojiParser.demojizedText(context.getString(R.string.game_draw_word_to_draw)));
      turn.setText(context.getString(R.string.game_draw_my_turn));
      animateDiagonalPan(hand);
    } else {
      drawDesc.setText(
          EmojiParser.demojizedText(context.getString(R.string.game_draw_word_to_guess)));
      turn.setText(context.getString(R.string.game_draw_other_is_drawing));
      hand.setVisibility(View.INVISIBLE);
    }
    String displayName = guest != null ? guest.getDisplayName() : "";
    Timber.w("instangiate item " + draw.getCurrentDrawName() + displayName);

    setCounter(counter);

    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }

  private void setCounter(TextViewFont counter) {
    // if (countDownTimer != null) countDownTimer.cancel();
    countDownTimer = new CountDownTimer(COUNTER * 1000, 1000) {
      public void onTick(long millisUntilFinished) {
        counter.setText("" + millisUntilFinished / 1000);
      }

      public void onFinish() {
        counter.setText("0");
      }
    }.start();
  }

  private void animateDiagonalPan(View v) {
    AnimatorSet animSetXY = new AnimatorSet();

    ObjectAnimator y = ObjectAnimator.ofFloat(v, "translationY", v.getY(), v.getY() + 200);

    ObjectAnimator x = ObjectAnimator.ofFloat(v, "translationX", v.getX(), v.getX() + 200);

    animSetXY.playTogether(x, y);
    animSetXY.setInterpolator(new LinearInterpolator());
    animSetXY.setDuration(300);
    animSetXY.start();
  }
}
