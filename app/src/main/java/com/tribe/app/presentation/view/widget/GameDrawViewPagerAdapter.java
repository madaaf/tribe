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
import android.widget.RelativeLayout;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameDraw;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.model.TribeGuest;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameDrawViewPagerAdapter extends PagerAdapter {

  private static int COUNTER = 300;
  private Context context;
  private LayoutInflater mLayoutInflater;
  private int currentPosition;

  private User user;
  private GameManager gameManager;
  private View mCurrentView;
  private DrawerView dv;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private PublishSubject<Boolean> onNextDraw = PublishSubject.create();
  private PublishSubject<Game> onCurrentGame = PublishSubject.create();
  private PublishSubject<Void> onClearDraw = PublishSubject.create();
  private PublishSubject<List<Float[]>> onDrawing = PublishSubject.create();

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
    RelativeLayout drawContainer = (RelativeLayout) itemView.findViewById(R.id.drawContainer);
    TribeGuest guest = draw.getCurrentDrawer();

    viewAvatar.load(guest.getPicture());
    nextInLabel.setText(
        EmojiParser.demojizedText(context.getString(R.string.game_draw_timer_instructions)));

    if (guest.getId().equals(user.getId())) {
      txtName.setText(user.getDisplayName());
      gameName.setText(draw.getCurrentDrawName());
      drawDesc.setText(
          EmojiParser.demojizedText(context.getString(R.string.game_draw_word_to_draw)));
      turn.setText(context.getString(R.string.game_draw_my_turn));
    } else {
      txtName.setText(guest.getDisplayName());
      drawDesc.setText(
          EmojiParser.demojizedText(context.getString(R.string.game_draw_word_to_guess)));
      turn.setText(context.getString(R.string.game_draw_other_is_drawing));
      hand.setVisibility(View.INVISIBLE);
      drawContainer.setOnTouchListener((v, event) -> true);
    }

    container.addView(itemView);
    return itemView;
  }

  @Override public void destroyItem(ViewGroup container, int position, Object view) {
    container.removeView((View) view);
  }

  private void setCounter(TextViewFont counter, int position) {
    CountDownTimer countDownTimer = new CountDownTimer(COUNTER * 1000, 1000) {
      public void onTick(long millisUntilFinished) {
        counter.setText("" + millisUntilFinished / 1000);
      }

      public void onFinish() {
        Timber.v("SOEF ON FINISH " + position + " " + currentPosition);
        if (counter != null) counter.setText("0");
        if (position == currentPosition) {
          onNextDraw.onNext(true);
        }
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
    if (position == currentPosition) return;
    currentPosition = position;

    TextViewFont counter = (TextViewFont) mCurrentView.findViewById(R.id.counter);
    TextViewFont clearBtn = (TextViewFont) mCurrentView.findViewById(R.id.clearBtn);
    ImageView hand = (ImageView) mCurrentView.findViewById(R.id.iconHand);
    RelativeLayout drawContainer = (RelativeLayout) mCurrentView.findViewById(R.id.drawContainer);

    GameDraw draw = (GameDraw) gameManager.getCurrentGame();
    TribeGuest guest = draw.getCurrentDrawer();

    dv = new DrawerView(context, hand, clearBtn);
    onBlockOpenInviteView.onNext(true);
    dv.setOnDrawerListener(path -> {
      List<Float[]> points = new ArrayList<>();
      for (TrackablePath.Point p : path.getPathData()) {
        points.add(new Float[] { p.x, p.y });
      }
      onDrawing.onNext(points);
    });

    drawContainer.addView(dv);
    animateDiagonalPan(hand);
    subscriptions.add(dv.onClearDraw().subscribe(onClearDraw));
    if (draw.isUserAction()) {
      onCurrentGame.onNext(draw);
    }

    setCounter(counter, position);
  }

  public Observable<Boolean> onBlockOpenInviteView() {
    return onBlockOpenInviteView;
  }

  public Observable<Boolean> onNextDraw() {
    return onNextDraw;
  }

  public Observable<Game> onCurrentGame() {
    return onCurrentGame;
  }

  public Observable<Void> onClearDraw() {
    return onClearDraw;
  }

  public Observable<List<Float[]>> onDrawing() {
    return onDrawing;
  }

  public void onPointsDrawReceived(Float[][] str) {
    TrackablePath path = new TrackablePath();
    path.moveTo(str[0][0], str[0][1]);

    for (int i = 1; i < str.length; i++) {
      path.lineTo(str[i][0], str[i][1]);
    }

    dv.draw(path);
  }

  public void onClearDrawReceived() {
    Timber.e("SOEF onClearDrawReceived");
    dv.clear();
    dv.invalidate();
  }
}
