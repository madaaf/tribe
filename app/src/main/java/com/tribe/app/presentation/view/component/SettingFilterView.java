package com.tribe.app.presentation.view.component;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.f2prateek.rx.preferences.Preference;
import com.jakewharton.rxbinding.view.RxView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.internal.di.scope.Filter;
import com.tribe.app.presentation.view.widget.CameraWrapper;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class SettingFilterView extends FrameLayout {

    public static final String[] PERMISSIONS_CAMERA = new String[]{ Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE };

    @BindView(R.id.imgFilterUnderline)
    ImageView imgFilterUnderline;

    @BindView(R.id.imageFilter1)
    ImageView imageFilter1;

    @BindView(R.id.imageFilter2)
    ImageView imageFilter2;

    @BindView(R.id.imageFilter3)
    ImageView imageFilter3;

    @BindView(R.id.imageFilter4)
    ImageView imageFilter4;

    @BindView(R.id.cameraWrapper)
    CameraWrapper cameraWrapper;

    @Inject
    @Filter
    Preference<Integer> filter;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    public SettingFilterView(Context context) {
        super(context);
    }

    public SettingFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SettingFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SettingFilterView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_setting_filter, this);
        unbinder = ButterKnife.bind(this);

        initUi();
    }

    private void initUi() {
        initDependencyInjector();

        imageFilter1.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                switch (filter.get()) {
                    case 0:
                        setUpUnderline(imageFilter1);
                        break;

                    case 1:
                        setUpUnderline(imageFilter2);
                        break;

                    case 2:
                        setUpUnderline(imageFilter3);
                        break;

                    case 3:
                        setUpUnderline(imageFilter4);
                        break;

                    default:
                        break;
                }

                imageFilter1.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        subscriptions.add(RxView.clicks(imageFilter1).subscribe(aVoid -> {
            setUpUnderline(imageFilter1);
            filter.set(0);
        }));

        subscriptions.add(RxView.clicks(imageFilter2).subscribe(aVoid -> {
            setUpUnderline(imageFilter2);
            filter.set(1);
        }));

        subscriptions.add(RxView.clicks(imageFilter3).subscribe(aVoid -> {
            setUpUnderline(imageFilter3);
            filter.set(2);
        }));

        subscriptions.add(RxView.clicks(imageFilter4).subscribe(aVoid -> {
            setUpUnderline(imageFilter4);
            filter.set(3);
        }));
    }

    private void setUpUnderline(ImageView imageView) {
        int location[] = new int[2];
        imageView.getLocationOnScreen(location);

        imgFilterUnderline.animate()
                .x(location[0])
                .start();
    }

    public void onResume() {
        subscriptions.add(Observable.
            from(PERMISSIONS_CAMERA)
            .map(permission -> RxPermissions.getInstance(getContext()).isGranted(permission))
            .toList()
            .subscribe(grantedList -> {
                boolean areAllGranted = true;

                for (Boolean granted : grantedList) {
                    if (!granted) areAllGranted = false;
                }

                if (areAllGranted) cameraWrapper.onResume(false);
                else cameraWrapper.showPermissions();
            }));
    }

    public void onPause() {
        if (cameraWrapper != null) cameraWrapper.onPause(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        if (unbinder != null) unbinder.unbind();

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
