package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.scope.Theme;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingThemeView extends FrameLayout {

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

    @Inject
    @Theme
    Preference<Integer> theme;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public SettingThemeView(Context context) {
        super(context);
    }

    public SettingThemeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingThemeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingThemeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_theme, this);
        unbinder = ButterKnife.bind(this);

        initUi();
    }

    private void initUi() {
        initDependencyInjector();

        imageTheme1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                switch (theme.get()) {
                    case 0:
                        setUpUnderline(imageTheme1);
                        break;
                    case 1:
                        setUpUnderline(imageTheme2);
                        break;
                    case 2:
                        setUpUnderline(imageTheme3);
                        break;
                    default:
                        break;
                }
                imageTheme1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        subscriptions.add(RxView.clicks(imageTheme1).subscribe(aVoid -> {
            setUpUnderline(imageTheme1);
            theme.set(0);
        }));

//        subscriptions.add(RxView.clicks(imageTheme2).subscribe(aVoid -> {
//            setUpUnderline(imageTheme2);
//            theme.set(1);
//        }));
//
//        subscriptions.add(RxView.clicks(imageTheme3).subscribe(aVoid -> {
//            setUpUnderline(imageTheme3);
//            theme.set(2);
//        }));
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

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(((Activity) getContext()));
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }
}
