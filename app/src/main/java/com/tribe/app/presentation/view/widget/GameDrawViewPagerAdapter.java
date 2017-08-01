package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
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

  private static int COUNTER = 30;
  private Context context;
  private LayoutInflater mLayoutInflater;

  private User user;
  private String challenge;
  private TribeGuest guest;
  private GameManager gameManager;
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private View mCurrentView;

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

    TextViewFont drawDesc = (TextViewFont) itemView.findViewById(R.id.drawDesc);
    TextViewFont turn = (TextViewFont) itemView.findViewById(R.id.txtUsername);

    TextViewFont txtName = (TextViewFont) itemView.findViewById(R.id.txtName);
    AvatarView viewAvatar = (AvatarView) itemView.findViewById(R.id.viewAvatar);
    ImageView hand = (ImageView) itemView.findViewById(R.id.iconHand);

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
    } else {
      drawDesc.setText(
          EmojiParser.demojizedText(context.getString(R.string.game_draw_word_to_guess)));
      turn.setText(context.getString(R.string.game_draw_other_is_drawing));
      //  hand.setVisibility(View.INVISIBLE);
    }
    String displayName = guest != null ? guest.getDisplayName() : "";
    Timber.w("instangiate item "
        + draw.getCurrentDrawName()
        + " "
        + displayName
        + " position "
        + position);

    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }

  private void setCounter(TextViewFont counter) {
    CountDownTimer countDownTimer = new CountDownTimer(COUNTER * 1000, 1000) {
      public void onTick(long millisUntilFinished) {
        counter.setText("" + millisUntilFinished / 1000);
      }

      public void onFinish() {
        if (counter != null) counter.setText("0");
      }
    };

    countDownTimer.cancel();
    countDownTimer.start();
  }

  private void animateDiagonalPan(View v) {
    int duration = 1000;
    int translation = 80;
    v.animate()
        .translationX(-translation)
        .translationY(-translation)
        .setInterpolator(new LinearInterpolator())
        .setDuration(duration)
        .withEndAction(() -> {
          v.animate()
              .setInterpolator(new DecelerateInterpolator())
              .translationX(translation)
              .translationY(translation)
              .setDuration(duration)
              .withEndAction(() -> {
                animateDiagonalPan(v);
              })
              .start();
        })
        .start();
  }

  @Override public void setPrimaryItem(ViewGroup container, int position, Object object) {
    mCurrentView = (View) object;
    TextViewFont counter = (TextViewFont) mCurrentView.findViewById(R.id.counter);
    ImageView hand = (ImageView) mCurrentView.findViewById(R.id.iconHand);

    animateDiagonalPan(hand);
    setCounter(counter);
  }
}
