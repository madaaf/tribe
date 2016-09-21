package com.tribe.app.presentation.view.component;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class ProfileInfoView extends FrameLayout {

    private final static String AVATAR = "AVATAR";
    public final static int RESULT_LOAD_IMAGE = 5, CAMERA_REQUEST = 6;

    @BindView(R.id.imgProfilePic)
    ImageView imgProfilePic;

    @BindView(R.id.txtOpenCameraRoll)
    TextViewFont txtOpenCameraRoll;

    @BindView(R.id.txtTakeASelfie)
    TextViewFont txtTakeASelfie;

    @BindView(R.id.editDisplayName)
    EditTextFont editDisplayName;

    @BindView(R.id.viewDisplayNameValid)
    View viewDisplayNameValid;

    @BindView(R.id.editUsername)
    EditTextFont editUsername;

    @BindView(R.id.viewUsernameValid)
    View viewUsernameValid;

    @Inject
    Navigator navigator;

    @Inject
    ScreenUtils screenUtils;

    // VARIABLES
    private boolean profilePictureSelected = false, usernameSelected = false,
            displayNameSelected = false, displayNameChanged = false;
    private int profilePicSize;
    private String imgUri;

    // OBSERVABLES
    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<String> usernameInput = PublishSubject.create();
    private PublishSubject<String> displayNameInput = PublishSubject.create();

    private static final String[] PERMISSIONS_CAMERA = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public ProfileInfoView(Context context) {
        super(context);
    }

    public ProfileInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProfileInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_profile_info, this);
        unbinder = ButterKnife.bind(this);
        initDimens();
        initDependencyInjector();
        initUi();
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

    public void setImgProfilePic(Bitmap bitmap, String imgUri) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(bitmap.getWidth() >> 1, 0, RoundedCornersTransformation.CornerType.ALL);
        imgProfilePic.setImageBitmap(roundedCornersTransformation.transform(bitmap));

        this.imgUri = imgUri;
        profilePictureSelected = true;
    }

    public void setUrlProfilePic(String imageUrl) {
        Glide.with(getContext()).load(imageUrl)
                .override(profilePicSize, profilePicSize)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgProfilePic);
    }

    public void setEditDisplayName(String displayName) {
        editDisplayName.setText(displayName);
    }

    public void setEditUsername(String username) {
        editUsername.setText(username);
    }

    public String getDisplayName() {
        return editDisplayName.getText().toString();
    }

    public String getUsername() {
        return editUsername.getText().toString();
    }

    public boolean isProfilePictureSelected() {
        return profilePictureSelected;
    }

    public boolean isDisplayNameSelected() {
        return displayNameSelected;
    }

    public boolean isUsernameSelected() {
        return usernameSelected;
    }

    private void initDimens() {
        profilePicSize = getResources().getDimensionPixelSize(R.dimen.avatar_size);
    }

    private void initUi() {
        subscriptions.add(RxView.clicks(txtOpenCameraRoll).subscribe(aVoid -> {
            RxPermissions.getInstance(getContext())
                    .request(PERMISSIONS_CAMERA)
                    .subscribe(granted -> {
                        if (granted) navigator.getImageFromCameraRoll((Activity) getContext(), RESULT_LOAD_IMAGE);
                        else
                            // TODO: get string from laurent
                            Toast.makeText(getContext(), "You must grant permissions to access your pictures", Toast.LENGTH_LONG).show();
                    });
        }));

        subscriptions.add(RxView.clicks(txtTakeASelfie).subscribe(aVoid -> {
            RxPermissions.getInstance(getContext())
                    .request(PERMISSIONS_CAMERA)
                    .subscribe(granted -> {
                        if (granted) navigator.getImageFromCamera((Activity) getContext(), CAMERA_REQUEST);
                        else
                            // TODO: get string from laurent
                            Toast.makeText(getContext(), "You must grant permissions to access the camera", Toast.LENGTH_LONG).show();
                    });
        }));

        subscriptions.add(
                RxTextView.textChanges(editUsername)
                        .filter(charSequence -> charSequence.length() > 1)
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(s -> setUsernameValid(false))
                        .map(CharSequence::toString)
                        .subscribe(usernameInput)
        );

        subscriptions.add(
                RxTextView.textChanges(editDisplayName)
                        .doOnNext(s -> displayNameSelected = false)
                        .subscribe(s -> {
                            if (s.length() > 1) {
                                setDisplayNameValid(true);
                            } else if (displayNameChanged) {
                                displayNameChanged = true;
                                setDisplayNameValid(false);
                            } else {
                                displayNameChanged = true;
                            }
                        })
        );
    }

    public void setInfoFromFacebook(FacebookEntity facebookEntity) {
        Glide.with(getContext()).load(facebookEntity.getProfilePicture())
                .override(profilePicSize, profilePicSize)
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgProfilePic);

        Glide.with(getContext()).load(facebookEntity.getProfilePicture())
                .override(ImageUtils.IMG_SIZE, ImageUtils.IMG_SIZE)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        imgUri = Uri.fromFile(FileUtils.bitmapToFile(AVATAR, ((GlideBitmapDrawable) resource.getCurrent()).getBitmap(), getContext())).toString();
                    }
                });

        profilePictureSelected = true;
        editDisplayName.setText(facebookEntity.getName());
        editUsername.setText(facebookEntity.getUsername());
    }

    public void setUsernameValid(boolean valid) {
        if (valid) {
            usernameSelected = true;
            viewUsernameValid.setBackgroundResource(R.drawable.shape_oval_green);
            viewUsernameValid.setVisibility(View.VISIBLE);
        } else {
            usernameSelected = false;
            viewUsernameValid.setBackgroundResource(R.drawable.shape_oval_red);
            viewUsernameValid.setVisibility(View.VISIBLE);
        }
    }

    public void setDisplayNameValid(boolean valid) {
        if (valid) {
            displayNameSelected = true;
            viewDisplayNameValid.setBackgroundResource(R.drawable.shape_oval_green);
            viewDisplayNameValid.setVisibility(View.VISIBLE);
        } else {
            displayNameSelected = false;
            viewDisplayNameValid.setBackgroundResource(R.drawable.shape_oval_red);
            viewDisplayNameValid.setVisibility(View.VISIBLE);
        }
    }

    public String getImgUri() {
        return imgUri;
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

    /**
     * OBSERVABLES
     */
    public Observable<String> onUsernameInput() {
        return usernameInput;
    }

    public Observable<String> onDisplayNameInput() {
        return displayNameInput;
    }
}
