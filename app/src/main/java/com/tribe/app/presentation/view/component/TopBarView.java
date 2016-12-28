package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 11/15/2016.
 */
public class TopBarView extends FrameLayout {

    private static final int DURATION_FADE = 100;
    private static final int DURATION = 250;
    private static final int CLICK_ACTION_THRESHOLD = 5;
    private static final int SPINNER_THRESHOLD = 20;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    User user;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.btnNew)
    View btnNew;

    // VARIABLES
    private float startX, startY = 0;
    private int currentBGColor;

    // RESOURCES
    private int avatarSize;
    private int clickActionThreshold;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> clickSettings = PublishSubject.create();
    private PublishSubject<Void> clickNew = PublishSubject.create();

    public TopBarView(Context context) {
        super(context);
        init(context, null);
    }

    public TopBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_top_bar, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initUI();

        super.onFinishInflate();
    }

    private void init(Context context, AttributeSet attrs) {
        currentBGColor = getResources().getColor(R.color.white_opacity_20);
    }

    private void initUI() {
        Glide.with(getContext()).load(user.getProfilePicture())
                .override(avatarSize, avatarSize)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgAvatar);
    }

    private void initResources() {
        avatarSize = getContext().getResources().getDimensionPixelSize(R.dimen.avatar_size_smaller);
        clickActionThreshold = screenUtils.dpToPx(CLICK_ACTION_THRESHOLD);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP: {
                if (isAClick(startX, event.getX(), startY, event.getY())) {
                    if (isAClickInView(imgAvatar, (int) startX, (int) startY)) {
                        imgAvatar.onTouchEvent(event);
                        imgAvatar.performClick();
                    } else if (isAClickInView(btnNew, (int) startX, (int) startY)) {
                        btnNew.onTouchEvent(event);
                        btnNew.performClick();
                    }
                }

                break;
            }

            case MotionEvent.ACTION_DOWN: {
                startX = event.getX();
                startY = event.getY();
            }

            default:
                if (isAClickInView(imgAvatar, (int) event.getX(), (int) event.getY())) imgAvatar.onTouchEvent(event);
                else if (isAClickInView(btnNew, (int) event.getX(), (int) event.getY())) btnNew.onTouchEvent(event);
                break;
        }

        return false;
    }

    @OnClick(R.id.imgAvatar)
    void launchSettings() {
        clickSettings.onNext(null);
    }

    @OnClick(R.id.btnNew)
    void launchInvites() {
        clickNew.onNext(null);
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);

        if (differenceX > clickActionThreshold || differenceY > clickActionThreshold) {
            return false;
        }

        return true;
    }

    private boolean isAClickInView(View v, int x, int y) {
        final int location[] = {0, 0};
        v.getLocationOnScreen(location);
        Rect rect = new Rect(location[0], location[1], location[0] + v.getWidth(), location[1] + v.getHeight());

        if (!rect.contains(x, y)) {
            return false;
        }

        return true;
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////

    public Observable<Void> onClickSettings() {
        return clickSettings;
    }

    public Observable<Void> onClickNew() { return clickNew; }
}

