package com.tribe.app.presentation.view.adapter.delegate.grid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.fernandocejas.frodo.annotation.RxLogObservable;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.widget.AvatarView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 18/05/2016.
 */
public class UserGridAdapterDelegate extends RxAdapterDelegate<List<Friendship>> {

    private final float BOUNCINESS_INSIDE = 15f;
    private final float SPEED_INSIDE = 12.5f;
    private final float BOUNCINESS_OUTSIDE = 1f;
    private final float SPEED_OUTSIDE = 20f;
    private final int LONG_PRESS = 200;
    private final int FADE_DURATION = 200;
    private final int SCALE_DURATION = 200;
    private final int END_RECORD_DELAY = 1000;
    private final float OVERSHOOT = 3f;

    private LayoutInflater layoutInflater;
    private Context context;

    private long longDown = 0L;
    private boolean isDown = false;

    // RESOURCES
    private int transitionGridPressed;
    private int timeTapToCancel;
    private int sizePressedBorder;
    private int sizeAvatar;
    private int sizeAvatarIntermediate;
    private int sizeAvatarScaled;
    private int sizeAvatarInner;
    private int sizeAvatarInnerIntermediate;
    private int sizeAvatarInnerScaled;
    private int colorBlackOpacity20;
    private int diffSizeForScale;

    // RX SUBSCRIPTIONS / SUBJECTS
    private final PublishSubject<View> clickChatView = PublishSubject.create();
    private final PublishSubject<View> clickMoreView = PublishSubject.create();
    private final PublishSubject<View> recordStarted = PublishSubject.create();
    private final PublishSubject<View> recordEnded = PublishSubject.create();

    public UserGridAdapterDelegate(Context context) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        transitionGridPressed = context.getResources().getDimensionPixelSize(R.dimen.transition_grid_pressed);
        sizePressedBorder = context.getResources().getDimensionPixelSize(R.dimen.inside_cell_border);
        sizeAvatarScaled = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_scaled);
        sizeAvatar = context.getResources().getDimensionPixelSize(R.dimen.avatar_size);
        sizeAvatarIntermediate = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_intermediate);
        sizeAvatarInner = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner);
        sizeAvatarInnerIntermediate = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner_intermediate);
        sizeAvatarInnerScaled = context.getResources().getDimensionPixelSize(R.dimen.avatar_size_inner_scaled);
        timeTapToCancel = context.getResources().getInteger(R.integer.time_tap_to_cancel);
        colorBlackOpacity20 = context.getResources().getColor(R.color.black_opacity_20);
        diffSizeForScale = (int) (context.getResources().getDisplayMetrics().density * 0.5);
        ((AndroidApplication) context.getApplicationContext()).getApplicationComponent().inject(this);
    }

    @Override
    public boolean isForViewType(@NonNull List<Friendship> items, int position) {
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

        subscriptions.add(RxView.clicks(userGridViewHolder.btnMore)
                .takeUntil(RxView.detaches(parent))
                .map(aVoid -> userGridViewHolder.itemView)
                .subscribe(clickMoreView));

        userGridViewHolder.itemView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (isDown) return false;

                boolean isTapToCancel = false;

                if (userGridViewHolder.itemView.getTag(R.id.is_tap_to_cancel) != null)
                    isTapToCancel = (Boolean) userGridViewHolder.itemView.getTag(R.id.is_tap_to_cancel);

                if (isTapToCancel) {
                    resetViewAfterTapToCancel(userGridViewHolder, false);
                    return false;
                } else {
                    longDown = System.currentTimeMillis();
                    isDown = true;
                    Observable.timer(LONG_PRESS, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> {
                                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                                    recordStarted.onNext(userGridViewHolder.itemView);

                                    Spring springInside = (Spring) v.getTag(R.id.spring_inside);
                                    springInside.setEndValue(1f);

                                    Spring springAvatar = (Spring) v.getTag(R.id.spring_avatar);
                                    springAvatar.setEndValue(1f);

                                    Spring springOutside = (Spring) v.getTag(R.id.spring_outside);
                                    springOutside.setEndValue(1f);
                                }
                            });
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                if ((System.currentTimeMillis() - longDown) >= LONG_PRESS && isDown) {
                    AnimationUtils.fadeIn(userGridViewHolder.viewForeground, 0);
                    ((TransitionDrawable) ((LayerDrawable) userGridViewHolder.viewForeground.getBackground()).getDrawable(0)).startTransition(FADE_DURATION);
                    AnimationUtils.scaleUp(userGridViewHolder.imgCancel, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

                    Spring springOutside = (Spring) userGridViewHolder.itemView.getTag(R.id.spring_outside);
                    springOutside.setEndValue(0f);

                    showSending(userGridViewHolder);

                    recordEnded.onNext(userGridViewHolder.itemView);

                    userGridViewHolder.itemView.setTag(R.id.is_tap_to_cancel, true);

                    ObjectAnimator animation = ObjectAnimator.ofInt(userGridViewHolder.progressBar, "progress", 0, timeTapToCancel);
                    animation.setDuration(timeTapToCancel);
                    animation.setInterpolator(new DecelerateInterpolator());
                    animation.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            AnimationUtils.scaleDown(userGridViewHolder.imgCancel, SCALE_DURATION);
                            AnimationUtils.scaleUp(userGridViewHolder.imgDone, SCALE_DURATION, SCALE_DURATION, new OvershootInterpolator(OVERSHOOT));

                            userGridViewHolder.txtSending.setText(R.string.Grid_User_Sent);

                            Observable.timer(END_RECORD_DELAY, TimeUnit.MILLISECONDS)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(time -> {
                                        resetViewAfterTapToCancel(userGridViewHolder, true);
                                    });
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {
                            animation.removeAllListeners();
                        }
                    });
                    animation.start();

                    userGridViewHolder.itemView.setTag(R.id.progress_bar_animation, animation);
                }

                isDown = false;
            }

            return true;
        });

        SpringSystem springSystem = SpringSystem.create();

        // SPRING INSIDE CONFIGURATION
        Spring springInside = springSystem.createSpring();
        SpringConfig configInside = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_INSIDE, SPEED_INSIDE);
        springInside.setSpringConfig(configInside);
        springInside.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                float alpha = 1 - value;
                userGridViewHolder.txtName.setAlpha(alpha);
                userGridViewHolder.txtStatus.setAlpha(alpha);
                userGridViewHolder.btnText.setAlpha(alpha);
                userGridViewHolder.btnMore.setAlpha(alpha);
            }
        });

        springInside.setEndValue(0f);
        userGridViewHolder.itemView.setTag(R.id.spring_inside, springInside);

        // SPRING INSIDE CONFIGURATION
        Spring springAvatar = springSystem.createSpring();
        SpringConfig configAvatar = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_INSIDE, SPEED_INSIDE);
        springAvatar.setSpringConfig(configAvatar);
        springAvatar.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                int scaleUp = (int) (sizeAvatar + ((sizeAvatarScaled - sizeAvatar) * value));
                ViewGroup.LayoutParams paramsAvatar = userGridViewHolder.avatar.getLayoutParams();

                if (Math.abs(scaleUp - paramsAvatar.height) > diffSizeForScale) {
                    paramsAvatar.height = scaleUp;
                    paramsAvatar.width = scaleUp;
                    userGridViewHolder.avatar.setLayoutParams(paramsAvatar);

                    float scale = 1f + (value * (((float) sizeAvatarInnerScaled / sizeAvatarInner) - 1));
                    userGridViewHolder.progressBar.setScaleX(scale);
                    userGridViewHolder.progressBar.setScaleY(scale);

                    userGridViewHolder.viewForeground.setScaleX(scale);
                    userGridViewHolder.viewForeground.setScaleY(scale);

                    userGridViewHolder.viewShadow.setScaleX(scale);
                    userGridViewHolder.viewShadow.setScaleY(scale);
                }
            }
        });

        springAvatar.setEndValue(0f);
        userGridViewHolder.itemView.setTag(R.id.spring_avatar, springAvatar);

        final GradientDrawable drawable = (GradientDrawable) userGridViewHolder.viewPressedForeground.getBackground();

        // SPRING OUTSIDE CONFIGURATION
        Spring springOutside = springSystem.createSpring();
        SpringConfig configOutside = SpringConfig.fromBouncinessAndSpeed(BOUNCINESS_OUTSIDE, SPEED_OUTSIDE);
        springOutside.setSpringConfig(configOutside);
        springOutside.addListener(new SimpleSpringListener() {
            @Override
            public void onSpringUpdate(Spring spring) {
                float value = (float) spring.getCurrentValue();

                int borderSize = (int) (sizePressedBorder * value);
                drawable.setStroke(borderSize, colorBlackOpacity20);
            }
        });

        springOutside.setEndValue(0f);
        userGridViewHolder.itemView.setTag(R.id.spring_outside, springOutside);

        return userGridViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull List<Friendship> items, int position, @NonNull RecyclerView.ViewHolder holder) {
        UserGridViewHolder vh = (UserGridViewHolder) holder;
        User user = (User) items.get(position);

        vh.txtName.setText(user.getDisplayName());
        vh.layoutContent.setBackgroundColor(PaletteGrid.get(position - 1));
        vh.avatar.load(user.getProfilePicture());

        user.setPosition(position);
    }

    private void resetViewAfterTapToCancel(UserGridViewHolder viewHolder, boolean hasFinished) {
        if (hasFinished) AnimationUtils.scaleDown(viewHolder.imgDone, SCALE_DURATION);
        else AnimationUtils.scaleDown(viewHolder.imgCancel, SCALE_DURATION);

        AnimationUtils.fadeOut(viewHolder.viewForeground, 0);
        AnimationUtils.fadeOut(viewHolder.txtSending, 0);

        if (viewHolder.itemView.getTag(R.id.progress_bar_animation) != null) {
            ObjectAnimator animator = (ObjectAnimator) viewHolder.itemView.getTag(R.id.progress_bar_animation);
            animator.cancel();
            viewHolder.itemView.setTag(R.id.progress_bar_animation, null);
        }

        viewHolder.progressBar.clearAnimation();
        viewHolder.progressBar.setProgress(0);

        Spring springInside = (Spring) viewHolder.itemView.getTag(R.id.spring_inside);
        springInside.setEndValue(0f);

        Spring springAvatar = (Spring) viewHolder.itemView.getTag(R.id.spring_avatar);
        springAvatar.setEndValue(0f);

        viewHolder.itemView.setTag(R.id.is_tap_to_cancel, false);

        ((TransitionDrawable) ((LayerDrawable) viewHolder.viewForeground.getBackground()).getDrawable(0)).reverseTransition(FADE_DURATION);
    }

    private void showSending(UserGridViewHolder viewHolder) {
        AnimationUtils.fadeIn(viewHolder.txtSending, 0);

        Spring springOutside = (Spring) viewHolder.itemView.getTag(R.id.spring_avatar);
        springOutside.setEndValue(0.75f);
    }

    public Observable<View> onClickChat() {
        return clickChatView;
    }

    public Observable<View> onClickMore() {
        return clickMoreView;
    }

    public Observable<View> onRecordStart() {
        return recordStarted;
    }

    public Observable<View> onRecordEnd() {
        return recordEnded;
    }

    static class UserGridViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtName) public TextViewFont txtName;
        @BindView(R.id.btnText) public ImageView btnText;
        @BindView(R.id.btnMore) public ImageView btnMore;
        @BindView(R.id.txtStatus) public TextViewFont txtStatus;
        @BindView(R.id.txtSending) public TextViewFont txtSending;
        @BindView(R.id.layoutContent) public ViewGroup layoutContent;
        @BindView(R.id.viewShadow) public View viewShadow;
        @BindView(R.id.avatar) public AvatarView avatar;
        @BindView(R.id.progressBar) public ProgressBar progressBar;
        @BindView(R.id.viewForeground) public View viewForeground;
        @BindView(R.id.imgCancel) public ImageView imgCancel;
        @BindView(R.id.imgDone) public ImageView imgDone;
        @BindView(R.id.viewPressedForeground) public View viewPressedForeground;

        public UserGridViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
