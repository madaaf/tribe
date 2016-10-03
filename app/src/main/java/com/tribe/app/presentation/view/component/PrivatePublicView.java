package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.fragment.GroupsGridFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import org.w3c.dom.Text;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/8/16.
 */
public class PrivatePublicView extends FrameLayout {
    public PrivatePublicView(Context context) {
        super(context);
    }

    public PrivatePublicView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PrivatePublicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Lifecycle methods
     */

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private boolean publicSelected;

    @BindView(R.id.layoutPrivate)
    FrameLayout layoutPrivate;
    @BindView(R.id.layoutPublic)
    FrameLayout layoutPublic;

    @BindView(R.id.imgTriangle)
    ImageView imgTriangle;
    @BindView(R.id.imgMegaphone)
    ImageView imgMegaphone;
    @BindView(R.id.imgLock)
    ImageView imgLock;

    @BindView(R.id.txtDescription)
    TextViewFont txtDescription;
    @BindView(R.id.txtPublic)
    TextViewFont txtPublic;
    @BindView(R.id.txtPrivate)
    TextViewFont txtPrivate;

    int animationDuration = 300;
    int moveLeft = -20;
    int moveRight = 20;
    int reset = 0;
    private PublishSubject<Boolean> isPrivate = PublishSubject.create();


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_private_public, this);
        unbinder = ButterKnife.bind(this);

        subscriptions.add(RxView.clicks(layoutPrivate).subscribe(aVoid -> {
            setPrivate();
        }));

        subscriptions.add(RxView.clicks(layoutPublic).subscribe(aVoid -> {
            setPublic();
        }));

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setPrivate();
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
    }

    public void setEnabled(boolean enabled) {
        layoutPrivate.setEnabled(enabled);
        layoutPublic.setEnabled(enabled);
    }

    private void setPrivate() {
        txtDescription.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_group));
        imgTriangle.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_triangle_green));
        txtDescription.setText(getContext().getString(R.string.group_private_description));
        int layoutWidth = layoutPrivate.getWidth();
        int triangleLoc = (layoutWidth)/2;
        imgTriangle.animate()
                .x(triangleLoc)
                .setDuration(animationDuration)
                .start();
        imgLock.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_green));
        imgMegaphone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_grey));
        moveImg(imgLock, moveLeft);
        moveImg(imgMegaphone, reset);
        moveTxt(txtPublic, reset, reset);
        moveTxt(txtPrivate, moveRight, 1);
        isPrivate.onNext(true);
    }

    private void setPublic() {
        txtDescription.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.purple_group));
        imgTriangle.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_triangle_purple));
        txtDescription.setText(getContext().getString(R.string.group_public_description));
        int location[] = new int[2];
        layoutPublic.getLocationOnScreen(location);
        int layoutWidth = layoutPublic.getWidth();
        int triangleLoc = (location[0] + layoutWidth + location[0])/2;
        imgTriangle.animate()
                .x(triangleLoc)
                .setDuration(animationDuration)
                .start();

        imgLock.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_grey));
        imgMegaphone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_purple));
        moveImg(imgMegaphone, moveLeft);
        moveImg(imgLock, reset);
        moveTxt(txtPrivate, reset, reset);
        moveTxt(txtPublic, moveRight, 1);
        isPrivate.onNext(false);
    }

    public Observable<Boolean> isPrivate() {
     return isPrivate;
    }

    private void moveImg(ImageView imageView, int dp) {
        imageView.animate()
                .setDuration(animationDuration)
                .translationX(dpToPx(dp))
                .start();
    }

    private void moveTxt(TextViewFont txt, int dp, int alpha) {
        if (alpha == AnimationUtils.ALPHA_FULL) txt.animate()
                .setDuration(animationDuration)
                .setInterpolator(new AccelerateInterpolator(2))
                .setStartDelay(0)
                .alpha(alpha)
                .start();
        else txt.animate()
                .setDuration(animationDuration)
                .setInterpolator(new DecelerateInterpolator(2))
                .setStartDelay(0)
                .alpha(alpha)
                .start();
        txt.animate()
                .setDuration(animationDuration)
                .translationX(dpToPx(dp))
                .start();
    }

    public int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

}
