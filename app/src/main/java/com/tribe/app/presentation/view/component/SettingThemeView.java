package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingThemeView extends FrameLayout {

    public SettingThemeView(Context context) {
        super(context);
    }

    public SettingThemeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingThemeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingThemeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    @BindView(R.id.imgThemeUnderline)
    ImageView imgThemeUnderline;

    @BindView(R.id.imageTheme1)
    ImageView imageTheme1;

    @BindView(R.id.imageTheme2)
    ImageView imageTheme2;

    @BindView(R.id.imageTheme3)
    ImageView imageTheme3;

    @BindView(R.id.layoutTheme)
    LinearLayout layoutTheme;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_theme, this);
        unbinder = ButterKnife.bind(this);

        initUi();

    }

    private void initUi() {

        // TODO: get theme selected & set highlighter based on theme selected

        subscriptions.add(RxView.clicks(imageTheme1).subscribe(aVoid -> {
            setUpUnderline(imageTheme1);
        }));

        subscriptions.add(RxView.clicks(imageTheme2).subscribe(aVoid -> {
            setUpUnderline(imageTheme2);
        }));

        subscriptions.add(RxView.clicks(imageTheme3).subscribe(aVoid -> {
            setUpUnderline(imageTheme3);
        }));

    }

    private void setUpUnderline(ImageView imageView) {

        int location[] = new int[2];
        imageView.getLocationOnScreen(location);

        imgThemeUnderline.animate()
                .x(location[0])
                .start();
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


    public int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources().getDisplayMetrics());
    }

}
