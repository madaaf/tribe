package com.tribe.app.presentation.view.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by madaaflak on 19/07/2017.
 */

public class GameDrawViewPagerAdapter extends PagerAdapter {

  private static int COUNTER = 30;
  private Context context;
  private LayoutInflater mLayoutInflater;
  private int currentPosition;

  private User user;
  private TribeGuest guest;
  private GameManager gameManager;
  private View mCurrentView;

  private DrawingView dv;
  private Paint mPaint;

  private PublishSubject<Boolean> onBlockOpenInviteView = PublishSubject.create();
  private PublishSubject<Boolean> onNextDraw = PublishSubject.create();
  private PublishSubject<Game> onCurrentGame = PublishSubject.create();

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

    TribeGuest guest = draw.getCurrentDrawer();
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
          // onNextDraw.onNext(true); // SOEF TODO DECOMMENT
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

    dv = new DrawingView(context, hand, clearBtn);
    drawContainer.addView(dv);
    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setColor(ContextCompat.getColor(context, R.color.yellow_draw));
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth(12);
    animateDiagonalPan(hand);

    GameDraw draw = (GameDraw) gameManager.getCurrentGame();
    Timber.w("SOEF setPrimaryItem item "
        + draw.getCurrentDrawName()
        + " "
        + " position "
        + position
        + " "
        + draw.isUserAction());

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

  private class DrawingView extends View {

    public int width;
    public int height;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;
    private Context context;
    private ImageView hand;
    private TextViewFont clearBtn;

    public DrawingView(Context c, ImageView hand, TextViewFont clearBtn) {
      super(c);
      this.hand = hand;
      this.clearBtn = clearBtn;
      context = c;
      mPath = new Path();
      mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
      width = w;
      height = h;

      mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
      mCanvas = new Canvas(mBitmap);
    }

    @Override protected void onDraw(Canvas canvas) {
      super.onDraw(canvas);

      canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
      canvas.drawPath(mPath, mPaint);

      clearBtn.setOnClickListener(v -> {
        setDrawingCacheEnabled(false);
        onSizeChanged(width, height, width, height);
        invalidate();
        setDrawingCacheEnabled(true);
      });
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 20;

    private void touch_start(float x, float y) {
      Timber.e("MADA touch_start " + x + " " + y);
      mPath.reset();
      mPath.moveTo(x, y);
      mX = x;
      mY = y;
    }

    private void touch_move(float x, float y) {
      // Timber.e("MADA touch_move " + x + " " + y);
      float dx = Math.abs(x - mX);
      float dy = Math.abs(y - mY);
      if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mX = x;
        mY = y;
      }
    }

    private void touch_up() {
      Timber.e("MADA touch_up ");
      mPath.lineTo(mX, mY);
      // commit the path to our offscreen
      mCanvas.drawPath(mPath, mPaint);
      // kill this so we don't double draw
      mPath.reset();
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
      float x = event.getX();
      float y = event.getY();

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          hand.setVisibility(GONE);
          onBlockOpenInviteView.onNext(true);
          touch_start(x, y);
          invalidate();
          break;
        case MotionEvent.ACTION_MOVE:
          touch_move(x, y);
          invalidate();
          break;
        case MotionEvent.ACTION_UP:
          //onBlockOpenInviteView.onNext(false);
          touch_up();
          // invalidate();
          break;
      }
      return true;
    }
  }
}
