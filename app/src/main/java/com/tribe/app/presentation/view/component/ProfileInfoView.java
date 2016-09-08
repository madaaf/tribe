package com.tribe.app.presentation.view.component;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.squareup.picasso.Picasso;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class ProfileInfoView extends FrameLayout {
    public ProfileInfoView(Context context) {
        super(context);
    }

    public ProfileInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProfileInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProfileInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.imgProfilePic)
    ImageView imgProfilePic;

    @BindView(R.id.txtOpenCameraRoll)
    TextViewFont txtOpenCameraRoll;

    @BindView(R.id.txtTakeASelfie)
    TextViewFont txtTakeASelfie;

    @BindView(R.id.editDisplayName)
    EditTextFont editDisplayName;

    @BindView(R.id.editUsername)
    EditTextFont editUsername;

    @Inject
    Navigator navigator;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    Picasso picasso;

    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Boolean> infoValid = PublishSubject.create();
    public boolean profilePictureSelected = false;
    public final static int RESULT_LOAD_IMAGE = 5, CAMERA_REQUEST = 6;
    private int profilePicSize = 65;

    private static final String[] PERMISSIONS_CAMERA = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_profile_info, this);
        unbinder = ButterKnife.bind(this);
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

    public void setImgProfilePic(Bitmap bitmap) {
        imgProfilePic.setImageBitmap(formatBitmapforView(bitmap));

    }

    public void setUrlProfilePic(String imageUrl) {
        picasso.load(imageUrl)
                .resize(screenUtils.dpToPx(profilePicSize), screenUtils.dpToPx(profilePicSize))
                .centerCrop()
                .transform(new RoundedCornersTransformation(screenUtils.dpToPx(100), 0, RoundedCornersTransformation.CornerType.ALL))
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
            navigator.getImageFromCamera((Activity) getContext(), CAMERA_REQUEST);
        }));

        Observable.combineLatest(RxTextView.textChanges(editDisplayName),
                RxTextView.textChanges(editUsername),
                (charSequence1, charSequence2) -> charSequence1.length() > 1 && charSequence2.length() > 1).subscribe(infoValid);


    }

    public Observable<Boolean> infoValid() {
        return infoValid;
    }




    public void setInfoFromFacebook(String profilePicLink, String username, String realName) {
        picasso.load(profilePicLink)
                .transform(new RoundedCornersTransformation(screenUtils.dpToPx(100), 0))
                .resize(screenUtils.dpToPx(profilePicSize), screenUtils.dpToPx(profilePicSize))
                .centerInside()
                .into(imgProfilePic);
        profilePictureSelected = true;
        editDisplayName.setText(realName);
        editUsername.setText(username);

    }

    public Bitmap formatBitmapforView(Bitmap thumbnail) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(screenUtils.dpToPx(100), 0, RoundedCornersTransformation.CornerType.ALL);
        thumbnail = ImageUtils.centerCropBitmap(thumbnail);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, screenUtils.dpToPx(profilePicSize), screenUtils.dpToPx(profilePicSize), false);
        thumbnail = roundedCornersTransformation.transform(thumbnail);
        return thumbnail;
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
