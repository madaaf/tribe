package com.tribe.app.presentation.view.widget.avatar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import com.tribe.app.presentation.view.widget.TextViewFont;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 12/13/17.
 */
public class EmojiGameView extends TextViewFont {

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Subscription emojiSubscription;

  private int currentEmojiIndex = -1;

  public EmojiGameView(Context context) {
    super(context);
    init();
  }

  public EmojiGameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public void init() {
    initResources();
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
  }

  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    subscriptions.clear();
  }

  private void initResources() {

  }

  public void clear() {
    if (emojiSubscription != null) emojiSubscription.unsubscribe();
    currentEmojiIndex = -1;

    setText("");
  }

  public void setEmojiList(List<String> emojiLeaderGameList) {
    if (emojiLeaderGameList == null || emojiLeaderGameList.size() == 0) {
      setText("");
      if (emojiSubscription != null) emojiSubscription.unsubscribe();
      return;
    }

    if (emojiLeaderGameList.size() > 0) {
      if (emojiLeaderGameList.size() == 1) {
        setText(emojiLeaderGameList.get(0));
        return;
      }

      if (emojiSubscription != null) emojiSubscription.unsubscribe();

      emojiSubscription = Observable.timer(1, TimeUnit.SECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnUnsubscribe(() -> clearAnimation())
          .subscribe(aLong -> {
            currentEmojiIndex++;
            if (emojiLeaderGameList.size() <= currentEmojiIndex) {
              currentEmojiIndex = 0;
            }

            animate().alpha(0)
                .setStartDelay(0)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                  @Override public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    animation.removeAllListeners();
                  }

                  @Override public void onAnimationEnd(Animator animation) {
                    animation.removeAllListeners();
                    clearAnimation();
                    setText(emojiLeaderGameList.get(currentEmojiIndex));
                    animate().alpha(1)
                        .setDuration(300)
                        .setStartDelay(0)
                        .setListener(new AnimatorListenerAdapter() {
                          @Override public void onAnimationCancel(Animator animation) {
                            super.onAnimationCancel(animation);
                            clearAnimation();
                            animation.removeAllListeners();
                          }

                          @Override public void onAnimationEnd(Animator animation) {
                            animation.removeAllListeners();
                            clearAnimation();
                            animate().setStartDelay(0).setListener(null).start();
                            setEmojiList(emojiLeaderGameList);
                          }
                        })
                        .start();
                  }
                })
                .start();
          });

      subscriptions.add(emojiSubscription);
    }
  }
}
