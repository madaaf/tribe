package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import org.w3c.dom.Text;

import java.util.Observable;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
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

    public PrivatePublicView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_private_public, this);
        unbinder = ButterKnife.bind(this);

        setPrivate();

        subscriptions.add(RxView.clicks(layoutPrivate).subscribe(aVoid -> {
            setPrivate();
        }));

        subscriptions.add(RxView.clicks(layoutPublic).subscribe(aVoid -> {
            setPublic();
        }));
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

    private void setPrivate() {
        txtDescription.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.green_group));
        imgTriangle.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_triangle_green));
        txtDescription.setText(getContext().getString(R.string.group_private_description));
        int location[] = new int[2];
        layoutPrivate.getLocationOnScreen(location);
        int layoutWidth = layoutPrivate.getWidth();
        int triangleLoc = (location[0] + layoutWidth)/2;
        imgTriangle.animate()
                .x(triangleLoc)
                .setDuration(animationDuration)
                .start();
        imgLock.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_green));
        imgMegaphone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_grey));
        moveImg(imgLock, -20);
        moveImg(imgMegaphone, 0);
        moveTxt(txtPublic, 0, 0);
        moveTxt(txtPrivate, 20, 1);
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
        moveImg(imgMegaphone, -20);
        moveImg(imgLock, 0);
        moveTxt(txtPrivate, 0, 0);
        moveTxt(txtPublic, 20, 1);
    }

    private void moveImg(ImageView imageView, int dp) {
        imageView.animate()
                .setDuration(animationDuration)
                .translationX(dpToPx(dp))
                .start();
    }

    private void moveTxt(TextViewFont txt, int dp, int alpha) {
        txt.animate()
                .setDuration(animationDuration/2)
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
