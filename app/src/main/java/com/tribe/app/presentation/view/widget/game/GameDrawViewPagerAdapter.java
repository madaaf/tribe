package com.tribe.app.presentation.view.widget.game;

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
import com.tribe.app.presentation.view.widget.TextViewFont;
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

  private static int COUNTER = 45;
  public static int LENGTH_POINTS_PACKAGE = 10;

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

    GameDraw draw = (GameDraw) gameManager.getCurrentGame();
    TribeGuest guest = draw.getCurrentDrawer();

    TextViewFont counter = (TextViewFont) mCurrentView.findViewById(R.id.counter);
    TextViewFont clearBtn = (TextViewFont) mCurrentView.findViewById(R.id.clearBtn);
    TextViewFont gameName = (TextViewFont) mCurrentView.findViewById(R.id.gameName);
    TextViewFont nextInLabel = (TextViewFont) mCurrentView.findViewById(R.id.nextInLabel);
    TextViewFont drawDesc = (TextViewFont) mCurrentView.findViewById(R.id.drawDesc);
    TextViewFont turn = (TextViewFont) mCurrentView.findViewById(R.id.txtUsername);
    TextViewFont txtName = (TextViewFont) mCurrentView.findViewById(R.id.txtName);
    AvatarView viewAvatar = (AvatarView) mCurrentView.findViewById(R.id.viewAvatar);
    ImageView hand = (ImageView) mCurrentView.findViewById(R.id.iconHand);
    RelativeLayout drawContainer = (RelativeLayout) mCurrentView.findViewById(R.id.drawContainer);

    boolean userIsDrawer = guest.getId().equals(user.getId());

    hand.setVisibility(View.VISIBLE);
    viewAvatar.load(guest.getPicture());
    nextInLabel.setText(
        EmojiParser.demojizedText(context.getString(R.string.game_draw_timer_instructions)));

    if (userIsDrawer) {
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
      clearBtn.setVisibility(View.INVISIBLE);
    }

    dv = new DrawerView(context, hand, clearBtn);
    dv.setOnDrawerListener(path -> {
      List<Float[]> points = new ArrayList<>();
      for (TrackablePath.Point p : path.getPathData()) {
        points.add(new Float[] { p.x, p.y });
      }
      onDrawing.onNext(points);
    }, LENGTH_POINTS_PACKAGE);

    if (!userIsDrawer) {
      dv.setOnTouchListener((v, event) -> true);
    }

    drawContainer.addView(dv);
    subscriptions.add(dv.onClearDraw().subscribe(onClearDraw));
    subscriptions.add(dv.onBlockOpenInviteView().subscribe(onBlockOpenInviteView));

    animateDiagonalPan(hand);
    if (draw.isUserAction()) onCurrentGame.onNext(draw);
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

  public void onPointsDrawReceived(Float[][] points) {
    int width = dv.getWidth();
    int height = dv.getHeight();
    TrackablePath path = new TrackablePath();

    if (isAllEqual(points)) {
      path.moveTo(points[0][0] * width, points[0][1] * height);
      path.lineTo(points[0][0] * width + 1, points[0][1] * height);
    } else {
      path.moveTo(points[0][0] * width, points[0][1] * height);
      for (int i = 1; i < points.length; i++) {
        path.lineTo(points[i][0] * width, points[i][1] * height);
      }
    }

    dv.draw(path);
  }

  public boolean isAllEqual(Float[][] points) {
    Float[] x = new Float[points.length];
    Float[] y = new Float[points.length];

    for (int i = 0; i < points.length; i++) {
      x[i] = points[i][0];
      y[i] = points[i][1];
    }
    return (isEquals(x) && isEquals(y));
  }

  private boolean isEquals(Float[] a) {
    for (int i = 1; i < a.length; i++) {
      if (!a[0].equals(a[i])) {
        return false;
      }
    }
    return true;
  }

  public void onClearDrawReceived() {
    if (dv != null) dv.clear();
  }
}
