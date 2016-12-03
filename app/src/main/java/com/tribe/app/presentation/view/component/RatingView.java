package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/26/2016.
 */
public class RatingView extends FrameLayout {

    private static final float OVERSHOOT_TENSION_LIGHT = 0.7f;
    private static final int DURATION = 600;
    private static final int DURATION_SMALL = 300;
    private static final int START_DELAY_BIG = 1500;
    private static final int START_DELAY_SMALL = 800;

    @Inject
    ScreenUtils screenUtils;

    @BindViews({ R.id.txtReviewOne, R.id.txtReviewTwo })
    List<TextViewFont> txtReviews;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private int currentTxtReviewAnimation = 0;
    private int currentTxtReviewWording = 0;
    private String [] wordings;
    private int initialDelay = START_DELAY_SMALL;

    public RatingView(Context context) {
        super(context);
    }

    public RatingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RatingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    /**
     * Lifecycle methods
     */

    public void onDestroy() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_rating, this);
        unbinder = ButterKnife.bind(this);

        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initUI();
    }

    private void initUI() {
        for (View txtRating : txtReviews) {
            txtRating.setTranslationX(screenUtils.getWidthPx());
            txtRating.setAlpha(0);
        }

        wordings = getContext().getResources().getString(R.string.rating_descriptions).split("\\+");

        subscriptions.add(
                Observable
                        .timer(DURATION_SMALL, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            showTxtRating(currentTxtReviewAnimation, currentTxtReviewWording);
                        })
        );
    }

    private void showTxtRating(final int index, final int indexWording) {
        txtReviews.get(index).setText(wordings[indexWording]);
        txtReviews.get(index).animate()
                .translationX(0)
                .alpha(1)
                .setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION_LIGHT))
                .setDuration(DURATION)
                .withEndAction(() -> hideTxtRating(index))
                .start();
    }

    private void hideTxtRating(int index) {
        txtReviews.get(index).animate()
                .translationX(-screenUtils.getWidthPx())
                .alpha(0)
                .setInterpolator(new OvershootInterpolator(OVERSHOOT_TENSION_LIGHT))
                .setDuration(DURATION)
                .setStartDelay(START_DELAY_SMALL)
                .withEndAction(() -> txtReviews.get(index).setTranslationX(screenUtils.getWidthPx()))
                .start();

        subscriptions.add(Observable.timer(initialDelay, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(aLong -> {
            if (currentTxtReviewAnimation == txtReviews.size() - 1) currentTxtReviewAnimation = 0;
            else currentTxtReviewAnimation++;

            if (currentTxtReviewWording == wordings.length - 1) currentTxtReviewWording = 0;
            else currentTxtReviewWording++;

            initialDelay = 0;
            showTxtRating(currentTxtReviewAnimation, currentTxtReviewWording);
        }));
    }
}
