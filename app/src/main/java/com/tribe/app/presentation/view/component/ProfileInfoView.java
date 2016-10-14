package com.tribe.app.presentation.view.component;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.CameraType;
import com.tribe.app.domain.entity.FacebookEntity;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/6/16.
 */
public class ProfileInfoView extends LinearLayout {

    private static final int DURATION = 100;

    @BindView(R.id.imgAvatar)
    ImageView imgAvatar;

    @BindView(R.id.editDisplayName)
    EditTextFont editDisplayName;

    @BindView(R.id.imgDisplayNameInd)
    ImageView imgDisplayNameInd;

    @BindView(R.id.editUsername)
    EditTextFont editUsername;

    @BindView(R.id.imgUsernameInd)
    ImageView imgUsernameInd;

    @BindView(R.id.txtArobase)
    TextViewFont txtArobase;

    @BindView(R.id.circularProgressUsername)
    CircularProgressView circularProgressUsername;

    @Inject
    RxImagePicker rxImagePicker;

    @Inject
    ScreenUtils screenUtils;

    // VARIABLES
    private String usernameInit;
    private String displayNameInit;
    private boolean avatarSelected = false, usernameSelected = false,
            displayNameSelected = false, displayNameChanged = false;
    private int avatarSize;
    private String imgUri;
    private BottomSheetDialog dialogCamera;
    private LabelSheetAdapter cameraTypeAdapter;

    // OBSERVABLES
    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<String> usernameInput = PublishSubject.create();
    private PublishSubject<String> displayNameInput = PublishSubject.create();
    private PublishSubject<Boolean> infoValid = PublishSubject.create();

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

    public void loadAvatar(String url) {
        avatarSelected = true;
        refactorInfosValid();
        Glide.with(getContext()).load(url)
                .override(avatarSize, avatarSize)
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgAvatar);
    }

    public void setEditDisplayName(String displayName) {
        this.displayNameInit = displayName;
        editDisplayName.setText(displayName);
    }

    public void setEditUsername(String username) {
        this.usernameInit = username;
        editUsername.setText(username);
    }

    public String getDisplayName() {
        return editDisplayName.getText().toString();
    }

    public String getUsername() {
        return editUsername.getText().toString();
    }

    public boolean isAvatarSelected() {
        return avatarSelected;
    }

    public boolean isDisplayNameSelected() {
        return displayNameSelected;
    }

    public boolean isUsernameSelected() {
        return usernameSelected;
    }

    private void initDimens() {
        avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size_with_shadow);
    }

    private void initUi() {
        setOrientation(VERTICAL);

        ArrayList<InputFilter> inputFilters = new ArrayList<InputFilter>(Arrays.asList(editUsername.getFilters()));
        inputFilters.add(0, filterAlphanumeric);
        inputFilters.add(1, filterSpace);
        inputFilters.add(2, filterLowercase);
        InputFilter[] newInputFilters = inputFilters.toArray(new InputFilter[inputFilters.size()]);
        editUsername.setFilters(newInputFilters);

        editUsername.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) editUsername.setSelection(editUsername.getText().length());
            }
        });

        subscriptions.add(
                RxTextView.textChanges(editUsername)
                        .map(CharSequence::toString)
                        .doOnNext(s -> {
                            if (!StringUtils.isEmpty(s)) {
                                editUsername.setHint("");
                                showArobase();
                            } else {
                                editUsername.setHint(R.string.onboarding_user_username_placeholder);
                                hideArobase();
                            }
                        })
                        .debounce(200, TimeUnit.MILLISECONDS)
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(s -> {
                            if (!StringUtils.isEmpty(s)) {
                                if (s.equals(usernameInit)) {
                                    setUsernameValid(true);
                                } else if (!StringUtils.isEmpty(s)) {
                                    showUsernameProgress();
                                }
                            } else {
                                setUsernameValid(false);
                            }
                        })
                        .subscribe(s -> {
                            if (!StringUtils.isEmpty(s)) usernameInput.onNext(s);
                        })
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

        subscriptions.add(
                RxView.clicks(imgAvatar)
                        .subscribe(aVoid -> setupBottomSheetCamera())
        );
    }

    public void setInfoFromFacebook(FacebookEntity facebookEntity) {
        Glide.with(getContext()).load(facebookEntity.getProfilePicture())
                .override(avatarSize, avatarSize)
                .centerCrop()
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imgAvatar);

        Glide.with(getContext()).load(facebookEntity.getProfilePicture())
                .override(ImageUtils.IMG_SIZE, ImageUtils.IMG_SIZE)
                .into(new SimpleTarget<GlideDrawable>() {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                        imgUri = Uri.fromFile(FileUtils.bitmapToFile("avatar", ((GlideBitmapDrawable) resource.getCurrent()).getBitmap(), getContext())).toString();
                    }
                });

        avatarSelected = true;
        editDisplayName.setText(facebookEntity.getName());
        editUsername.setText(facebookEntity.getUsername());
    }

    public void setUsernameValid(boolean valid) {
        hideUsernameProgress();

        if (valid) {
            usernameSelected = true;
            imgUsernameInd.setImageResource(R.drawable.picto_valid);
        } else {
            usernameSelected = false;
            imgUsernameInd.setImageResource(R.drawable.picto_wrong);
        }

        if (editUsername.getText().length() > 0) {
            showUsernameInd();
        } else {
            hideUsernameInd();
        }

        refactorInfosValid();
    }

    public void showUsernameInd() {
        if (imgUsernameInd.getScaleX() == 0) {
            AnimationUtils.scaleUp(imgUsernameInd, DURATION, new DecelerateInterpolator());
        }
    }

    public void hideUsernameInd() {
        if (imgUsernameInd.getScaleX() == 1) {
            AnimationUtils.scaleDown(imgUsernameInd, DURATION, new DecelerateInterpolator());
        }
    }

    public void hideArobase() {
        txtArobase.setVisibility(View.GONE);
    }

    public void showArobase() {
        txtArobase.setVisibility(View.VISIBLE);
    }

    public void showUsernameProgress() {
        usernameSelected = false;
        refactorInfosValid();

        if (circularProgressUsername.getScaleX() == 0) {
            hideUsernameInd();
            AnimationUtils.scaleUp(circularProgressUsername, DURATION, new DecelerateInterpolator());
        }
    }

    public void hideUsernameProgress() {
        if (circularProgressUsername.getScaleX() == 1) {
            showUsernameInd();
            AnimationUtils.scaleDown(circularProgressUsername, DURATION, new DecelerateInterpolator());
        }
    }

    public void setDisplayNameValid(boolean valid) {
        if (valid) {
            displayNameSelected = true;
            imgDisplayNameInd.setImageResource(R.drawable.picto_valid);
        } else {
            displayNameSelected = false;
            imgDisplayNameInd.setImageResource(R.drawable.picto_wrong);
        }

        if (editDisplayName.getText().length() > 0)
            showDisplayNameInd();
        else hideDisplayNameInd();

        refactorInfosValid();
    }

    public void showDisplayNameInd() {
        if (imgDisplayNameInd.getScaleX() == 0) AnimationUtils.scaleUp(imgDisplayNameInd, DURATION, new DecelerateInterpolator());
    }

    public void hideDisplayNameInd() {
        if (imgDisplayNameInd.getScaleX() == 1) AnimationUtils.scaleDown(imgDisplayNameInd, DURATION, new DecelerateInterpolator());
    }

    public String getImgUri() {
        return imgUri;
    }

    private void refactorInfosValid() {
        infoValid.onNext(displayNameSelected && avatarSelected && usernameSelected);
    }

    @OnClick(R.id.layoutUsername)
    void clickLayoutUsername() {
        editUsername.requestFocus();
        editUsername.setSelection(editUsername.getText().length());
        editUsername.postDelayed(() -> {
            InputMethodManager keyboard = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            keyboard.showSoftInput(editUsername, 0);
        }, 200);
    }

    private static InputFilter filterSpace = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (Character.isSpaceChar(source.charAt(i))) {
                return "";
            }
        }

        return null;
    };

    private static InputFilter filterAlphanumeric = (source, start, end, dest, dstart, dend) -> {
        StringBuilder builder = new StringBuilder();
        for (int i = start; i < end; i++) {
            char c = source.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                builder.append(c);
            }
        }

        // If all characters are valid, return null, otherwise only return the filtered characters
        boolean allCharactersValid = (builder.length() == end - start);
        return allCharactersValid ? null : builder.toString();
    };

    private static InputFilter filterLowercase = (source, start, end, dest, dstart, dend) -> {
        for (int i = start; i < end; i++) {
            if (!Character.isLowerCase(source.charAt(i))) {
                char[] v = new char[end - start];
                TextUtils.getChars(source, start, end, v, 0);
                String s = new String(v).toLowerCase();

                if (source instanceof Spanned) {
                    SpannableString sp = new SpannableString(s);
                    TextUtils.copySpansFrom((Spanned) source,
                            start, end, null, sp, 0);
                    return sp;
                } else {
                    return s;
                }
            }
        }

        return null; // keep original
    };

    /**
     * Bottom sheet set-up
     */
    private void prepareBottomSheetCamera(List<LabelType> items) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_camera_type, null);
        RecyclerView recyclerViewCameraType = (RecyclerView) view.findViewById(R.id.recyclerViewCameraType);
        recyclerViewCameraType.setHasFixedSize(true);
        recyclerViewCameraType.setLayoutManager(new LinearLayoutManager(getContext()));
        cameraTypeAdapter = new LabelSheetAdapter(getContext(), items);
        cameraTypeAdapter.setHasStableIds(true);
        recyclerViewCameraType.setAdapter(cameraTypeAdapter);
        subscriptions.add(cameraTypeAdapter.clickLabelItem()
                .map((View labelView) -> cameraTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
                .subscribe(labelType -> {
                    CameraType cameraType = (CameraType) labelType;

                    if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_CAMERA)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA)
                                .subscribe(uri -> {
                                    loadUri(uri);
                                }));
                    } else if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_PHOTOS)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY)
                                .subscribe(uri -> {
                                    loadUri(uri);
                                }));
                    }

                    dismissDialogSheetCamera();
                }));

        dialogCamera = new BottomSheetDialog(getContext());
        dialogCamera.setContentView(view);
        dialogCamera.show();
        dialogCamera.setOnDismissListener(dialog -> {
            cameraTypeAdapter.releaseSubscriptions();
            dialogCamera = null;
        });
    }

    public void loadUri(Uri uri) {
        imgUri = uri.toString();
        avatarSelected = true;
        loadAvatar(uri.toString());
    }

    private void setupBottomSheetCamera() {
        if (dismissDialogSheetCamera()) {
            return;
        }

        List<LabelType> cameraTypes = new ArrayList<>();
        cameraTypes.add(new CameraType(getContext().getString(R.string.image_picker_camera), CameraType.OPEN_CAMERA));
        cameraTypes.add(new CameraType(getContext().getString(R.string.image_picker_library), CameraType.OPEN_PHOTOS));

        prepareBottomSheetCamera(cameraTypes);
    }

    private boolean dismissDialogSheetCamera() {
        if (dialogCamera != null && dialogCamera.isShowing()) {
            dialogCamera.dismiss();
            return true;
        }

        return false;
    }

    /**
     * DEPENDENCIES
     */
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

    public Observable<Boolean> onInfoValid() {
        return infoValid;
    }
}
