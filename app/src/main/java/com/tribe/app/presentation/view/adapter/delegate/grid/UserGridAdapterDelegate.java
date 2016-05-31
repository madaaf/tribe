package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.MarvelCharacter;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserGridAdapterDelegate extends RxAdapterDelegate<List<MarvelCharacter>> {

    private final float BOUNCINESS = 15f;
    private final float SPEED = 12.5f;
    private final int LONG_PRESS = 200;
    private final int FADE_DURATION = 200;
    private final int SCALE_DURATION = 150;
    private final int END_RECORD_DELAY = 1000;

    @Inject PaletteGrid paletteGrid;
    private LayoutInflater layoutInflater;

    private long longDown = 0L;
    private boolean isDown = false;

    // RESOURCES
    private int transitionGridPressed;
    private int timeTapToCancel;

    private final PublishSubject<View> clickChatView = PublishSubject.create();

    public UserGridAdapterDelegate(Context context) {
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        transitionGridPressed = context.getResources().getDimensionPixelSize(R.dimen.transition_grid_pressed);
        timeTapToCancel = context.getResources().getInteger(R.integer.time_tap_to_cancel);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<MarvelCharacter> items, int position) {
        return position != 0;
    }

    @NonNull
    @RxLogObservable
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        UserGridViewHolder userGridViewHolder = new UserGridViewHolder(layoutInflater.inflate(R.layout.item_user_grid, parent, false));

        subscriptions.add(RxView.clicks(userGridViewHolder.btnText)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> userGridViewHolder.itemView)
                .subscribe(clickChatView));

        userGridViewHolder.itemView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDown) return false;

                longDown = System.currentTimeMillis();
                isDown = true;
                Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(time -> {
                            if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                                Spring spring = (Spring) v.getTag(R.id.spring);
                                spring.setEndValue(1f);
                            }
                        });
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                    ((TransitionDrawable) ((LayerDrawable) userGridViewHolder.viewForeground.getBackground()).getDrawable(1)).startTransition(FADE_DURATION);
                    AnimationUtils.scaleUp(userGridViewHolder.imgCancel, SCALE_DURATION);

                    ObjectAnimator animation = ObjectAnimator.ofInt(userGridViewHolder.progressBar, "progress", 0, timeTapToCancel);
                    animation.setDuration(timeTapToCancel);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            AnimationUtils.scaleDown(userGridViewHolder.imgCancel, SCALE_DURATION);
                            AnimationUtils.scaleUp(userGridViewHolder.imgDone, SCALE_DURATION, SCALE_DURATION);

                            Observable.timer(END_RECORD_DELAY, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(time -> {
                                        AnimationUtils.scaleDown(userGridViewHolder.imgDone, SCALE_DURATION);
                                        userGridViewHolder.progressBar.setProgress(0);
                                        Spring spring = (Spring) v.getTag(R.id.spring);
                                        spring.setEndValue(0f);

                                        ((TransitionDrawable) ((LayerDrawable) userGridViewHolder.viewForeground.getBackground()).getDrawable(1)).reverseTransition(FADE_DURATION);
                                    });

                        }
                    });
                    animation.start();
                }

                isDown = false;
            }

            return true;
        });

        SpringSystem springSystem = SpringSystem.create();
        Spring spring = springSystem.createSpring();
        SpringConfig config = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS, SPEED);
        spring.setSpringConfig(config);
        spring.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                float scaleDown = 1f - (value * 0.10f);
                userGridViewHolder.itemView.setScaleX(scaleDown);
                userGridViewHolder.itemView.setScaleY(scaleDown);

                float scaleUp = 1f + (value * 0.8f);
                userGridViewHolder.avatar.setScaleX(scaleUp);
                userGridViewHolder.avatar.setScaleY(scaleUp);

                userGridViewHolder.progressBar.setScaleX(scaleUp);
                userGridViewHolder.progressBar.setScaleY(scaleUp);

                userGridViewHolder.viewForeground.setScaleX(scaleUp);
                userGridViewHolder.viewForeground.setScaleY(scaleUp);

                float transitionItems = transitionGridPressed * value;
                userGridViewHolder.txtName.setTranslationX(-transitionItems);
                userGridViewHolder.txtName.setTranslationY(-transitionItems);
                userGridViewHolder.txtStatus.setTranslationX(-transitionItems);
                userGridViewHolder.txtStatus.setTranslationY(transitionItems);
                userGridViewHolder.btnText.setTranslationX(transitionItems);
                userGridViewHolder.btnText.setTranslationY(transitionItems);
                userGridViewHolder.btnMore.setTranslationX(transitionItems);
                userGridViewHolder.btnMore.setTranslationY(-transitionItems);
            }
        });
        spring.setEndValue(0f);
        userGridViewHolder.itemView.setTag(R.id.spring, spring);

        return userGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<MarvelCharacter> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserGridViewHolder vh = (UserGridViewHolder) holder;
        MarvelCharacter marvelCharacter = items.get(position);

        vh.txtName.setText(marvelCharacter.getName());
        vh.layoutContent.setBackgroundColor(PaletteGrid.get(position - 1));
        vh.avatar.load("");
    }

    public Observable<View> onClickChat() {
        return clickChatView;
    }

    static class UserGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.btnText) public ImageView btnText;
        @BindView(R.id.btnMore) public ImageView btnMore;
        @BindView(R.id.txtStatus) public TextViewFont txtStatus;
        @BindView(R.id.layoutContent) public ViewGroup layoutContent;
        @BindView(R.id.avatar) public AvatarView avatar;
        @BindView(R.id.progressBar) public ProgressBar progressBar;
        @BindView(R.id.viewForeground) public View viewForeground;
        @BindView(R.id.imgCancel) public ImageView imgCancel;
        @BindView(R.id.imgDone) public ImageView imgDone;

        public UserGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
