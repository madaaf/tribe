package com.tribe.app.presentation.view.component;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.view.transformer.CropCircleTransformation;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by horatiothomas on 9/16/16.
 */
public class GroupInfoView extends FrameLayout {
    public GroupInfoView(Context context) {
        super(context);
    }

    public GroupInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GroupInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @BindView(R.id.imageGroup)
    ImageView imageGroup;
    @BindView(R.id.imageGroupBg)
    ImageView imageGroupBg;
    @BindView(R.id.imageEditGroup)
    ImageView imageEditGroup;
    @BindView(R.id.imageDoneEdit)
    ImageView imageDoneEdit;
    @BindView(R.id.imageBackIcon)
    ImageView imageBackIcon;
    @BindView(R.id.imageGoToMembers)
    ImageView imageGoToMembers;
    @BindView(R.id.editTextGroupName)
    EditTextFont editTextGroupName;
    @BindView(R.id.layoutDividerBackground)
    FrameLayout layoutDividerBackground;
    @BindView(R.id.layoutGroupMembers)
    FrameLayout layoutGroupMembers;
    @BindView(R.id.circularProgressView)
    CircularProgressView circularProgressView;
    @BindView(R.id.privatePublicView)
    PrivatePublicView privatePublicView;
    @BindView(R.id.viewPrivacyStatus)
    ViewPrivacyStatus viewPrivacyStatus;
    @BindView(R.id.memberPhotoViewList)
    MemberPhotoViewList memberPhotoViewList;

    @Inject ScreenUtils screenUtils;

    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // view vars
    private int moveGroupName = 55;
    private int startTranslationEditIcons = -200;
    private int privacyFinalPosition = -48;
    private float groupPicScaleDownF = .7f;
    private int layoutViewBackgroundInfoPositionY = 185;
    private int groupLayoutOffSet = 20;
    private float originalGroupNameMargin;
    private float originalGroupBackgroundMargin;
    private int animDuration = AnimationUtils.ANIMATION_DURATION_SHORT;


    private PublishSubject<Boolean> isPrivate = PublishSubject.create();
    private PublishSubject<Boolean> isGroupNameValid = PublishSubject.create();
    private PublishSubject<Boolean> isEditingGroupName = PublishSubject.create();
    private PublishSubject<Void> imageGroupClicked = PublishSubject.create();
    private PublishSubject<Void> imageEditGroupClicked = PublishSubject.create();
    private PublishSubject<Void> imageDoneEditClicked = PublishSubject.create();
    private PublishSubject<Void> imageBackClicked = PublishSubject.create();
    private PublishSubject<Void> imageGoToMembersClicked = PublishSubject.create();


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        LayoutInflater.from(getContext()).inflate(R.layout.view_group_info, this);
        unbinder = ButterKnife.bind(this);

        initDependencyInjector();

        // View Subscriptions
        subscriptions.add(RxTextView.textChanges(editTextGroupName).subscribe(charSequence -> {
            if (editTextGroupName.getText().toString().length() > 0) isGroupNameValid.onNext(true);
            else isGroupNameValid.onNext(false);
        }));
        subscriptions.add(RxView.clicks(editTextGroupName).subscribe(aVoid -> {
            bringGroupNameToTop(animDuration);
            isEditingGroupName.onNext(true);
        }));
        subscriptions.add(RxView.clicks(imageGroup).subscribe(aVoid -> {
            imageGroupClicked.onNext(null);
        }));
        subscriptions.add(RxView.clicks(imageEditGroup).subscribe(aVoid -> {
            imageEditGroupClicked.onNext(null);
        }));
        subscriptions.add(RxView.clicks(imageDoneEdit).subscribe(aVoid -> {
            imageDoneEditClicked.onNext(null);
        }));

        subscriptions.add(privatePublicView.isPrivate().subscribe(aBoolean -> {
           isPrivate.onNext(aBoolean);
        }));
        subscriptions.add(RxView.clicks(imageGoToMembers).subscribe(aVoid -> {
            imageGoToMembersClicked.onNext(null);
        }));

        subscriptions.add(editTextGroupName.keyBackPressed().subscribe(aVoid -> {
            bringGroupNameDown(animDuration);
            isEditingGroupName.onNext(false);
        }));

        editTextGroupName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    bringGroupNameDown(animDuration);
                    isEditingGroupName.onNext(false);
                }
                return false;
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

    public Observable<Void> imageBackClicked() {
        return imageBackClicked;
    }
    public Observable<Void> imageEditGroupClicked() {
        return imageEditGroupClicked;
    }
    public Observable<Void> imageDoneEditClicked() {
        return imageDoneEditClicked;
    }
    public Observable<Void> imageGroupClicked() {
        return imageGroupClicked;
    }
    public Observable<Void> imageGoToMembersClicked() {
        return imageGoToMembersClicked;
    }
    public Observable<Boolean> isPrivate() {
        return isPrivate;
    }
    public Observable<Boolean> isGroupNameValid() {
        return isGroupNameValid;
    }
    public Observable<Boolean> isEditingGroupName() {
        return isEditingGroupName;
    }



    public void addMemberPhoto(String profPic) {
        memberPhotoViewList.addMemberPhoto(profPic);
    }

    public void addMemberPhotoDrawable(Drawable drawable) {
        memberPhotoViewList.addMemberPhotoDrawable(drawable);
    }

    public void clearMemberPhotos() {
        memberPhotoViewList.clearPhotos();
    }

    public void setGroupPicture(Bitmap bitmap) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(bitmap.getWidth() >> 1, 0, RoundedCornersTransformation.CornerType.ALL);
        imageGroup.setImageBitmap(roundedCornersTransformation.transform(bitmap));
    }

    public void setGroupPictureFromUrl(String pictureFromUrl) {
        Glide.with(getContext()).load(pictureFromUrl)
                .fitCenter()
                .bitmapTransform(new CropCircleTransformation(getContext()))
                .crossFade()
                .into(imageGroup);
    }

    public void enableButtons() {
        imageGroup.setEnabled(true);
        privatePublicView.setEnabled(true);
    }

    public void disableButtons() {
        imageGroup.setEnabled(false);
        privatePublicView.setEnabled(false);
    }

    public void setLoading(boolean isLoading) {
        if (isLoading) {
            imageDoneEdit.setVisibility(INVISIBLE);
            circularProgressView.setVisibility(VISIBLE);
        } else {
            circularProgressView.setVisibility(INVISIBLE);
            imageDoneEdit.setVisibility(VISIBLE);
        }
    }

    public void collapsePrivatePublic(Boolean privateGroup, int animDuration, int memberCount) {
        AnimationUtils.collapseScale(privatePublicView, animDuration);
        privatePublicView.animate()
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .alpha(AnimationUtils.ALPHA_NONE)
                .setDuration(animDuration)
                .start();
        viewPrivacyStatus.setup(privateGroup, memberCount);
    }

    public void resetPrivatePublic() {
        AnimationUtils.expandScale(privatePublicView, 0);
        privatePublicView.setAlpha(AnimationUtils.ALPHA_FULL);
        privatePublicView.bringToFront();
    }

    public void expand(int animDuration) {
        enableIcons(false);
        Observable.timer(animDuration, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    enableIcons(true);
                        });

        // Setup group image
        imageGroup.setEnabled(true);
        imageGroupExpanded(true, imageGroupBg);
        imageGroupExpanded(true, imageGroup);


        // Setup edit group name
        layoutDividerBackground.setVisibility(View.VISIBLE);
        editTextGroupName.bringToFront();
        editTextGroupName.setEnabled(true);
        editTextGroupName.setCursorVisible(true);
        editTextGroupName.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();

        // Privacy status
        viewPrivacyStatus.animate()
                .setDuration(animDuration)
                .alpha(AnimationUtils.ALPHA_NONE)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .start();

        presentFinishEditIcons(animDuration);
    }

    public void expandInfo(int animDuration) {
        expand(animDuration);
        layoutGroupMembers.animate()
                .setDuration(animDuration)
                .translationY(screenUtils.dpToPx(groupLayoutOffSet))
                .start();
    }

    public void enableDoneEdit(Boolean enable) {
        imageDoneEdit.setClickable(enable);
    }

    public void collapse(int animDuration, Activity activity) {
        screenUtils.hideKeyboard(activity);
        enableIcons(false);
        Observable.timer(animDuration, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    enableIcons(true);
                });
        // Setup group image
        imageGroup.setEnabled(false);
        imageGroupExpanded(false, imageGroupBg);
        imageGroupExpanded(false, imageGroup);

        layoutDividerBackground.setVisibility(View.INVISIBLE);
        editTextGroupName.bringToFront();
        editTextGroupName.setEnabled(false);
        editTextGroupName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        editTextGroupName.setCursorVisible(false);
        editTextGroupName.animate()
                .translationY(-screenUtils.dpToPx(moveGroupName))
                .setDuration(animDuration)
                .start();

        // Setup privacy view
        viewPrivacyStatus.animate()
                .setDuration(animDuration)
                .alpha(AnimationUtils.ALPHA_FULL)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .translationY(screenUtils.dpToPx(privacyFinalPosition))
                .start();

        presentEditIcons(animDuration);
    }

    public void collapseInfo(int animDuration, Activity activity) {
        collapse(animDuration, activity);
        layoutGroupMembers.animate()
                .setDuration(animDuration)
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .start();
    }

    private void imageGroupExpanded(boolean expand, ImageView image) {
        if (expand) {
            image.animate()
                    .scaleY(AnimationUtils.SCALE_RESET)
                    .scaleX(AnimationUtils.SCALE_RESET)
                    .setDuration(animDuration)
                    .start();
        } else {
            image.animate()
                    .scaleY(groupPicScaleDownF)
                    .scaleX(groupPicScaleDownF)
                    .setDuration(animDuration)
                    .start();
        }
    }

    public void presentFinishEditIcons(int animDuration) {
        bringInIcon(imageDoneEdit, animDuration);
        bringOutIcon(imageEditGroup, animDuration);
    }

    public void presentEditIcons(int animDuration) {
        bringInIcon(imageEditGroup, animDuration);
        bringOutIcon(imageDoneEdit, animDuration);
    }

    public void enableIcons(boolean enabled) {
            imageDoneEdit.setEnabled(enabled);
            imageEditGroup.setEnabled(enabled);
    }

    public void bringOutIcons(int animDuration) {
        bringOutIcon(imageEditGroup, animDuration);
        bringOutIcon(imageDoneEdit, animDuration);
    }


    private void bringInIcon(ImageView imageView, int animDuration ) {
        imageView.setEnabled(true);
        imageView.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();
    }

    private void bringOutIcon(ImageView imageView, int animDuration) {
        imageView.setEnabled(false);
        imageView.animate()
                .translationY(screenUtils.dpToPx(startTranslationEditIcons))
                .setDuration(animDuration)
                .start();
    }

    public void setUpInitialUi() {
        imageEditGroup.setTranslationY(startTranslationEditIcons);
        imageDoneEdit.setTranslationY(startTranslationEditIcons);

    }

    public void setupGroupInfoUi(boolean privateGroup, int memberCount) {
        imageDoneEdit.setTranslationY(-500);
        privatePublicView.setVisibility(View.INVISIBLE);

        imageGroup.setEnabled(false);
        imageGroup.setScaleX(groupPicScaleDownF);
        imageGroup.setScaleY(groupPicScaleDownF);
        imageGroupBg.setScaleX(groupPicScaleDownF);
        imageGroupBg.setScaleY(groupPicScaleDownF);
        layoutDividerBackground.setVisibility(INVISIBLE);
        layoutGroupMembers.setVisibility(VISIBLE);
        editTextGroupName.bringToFront();
        editTextGroupName.setEnabled(false);
        editTextGroupName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        editTextGroupName.setCursorVisible(false);
        editTextGroupName.setTranslationY(-screenUtils.dpToPx(moveGroupName));
        viewPrivacyStatus.setAlpha(AnimationUtils.ALPHA_FULL);
        viewPrivacyStatus.setTranslationY(screenUtils.dpToPx(privacyFinalPosition));
        viewPrivacyStatus.setup(privateGroup, memberCount);
        imageBackIcon.setVisibility(View.VISIBLE);
        imageBackIcon.setClickable(true);

        memberPhotoViewList.bringToFront();

        subscriptions.add(RxView.clicks(imageBackIcon).subscribe(aVoid -> {
            imageBackClicked.onNext(null);
        }));
    }

    private void bringGroupNameToTop(int animDuration) {
        originalGroupBackgroundMargin =layoutDividerBackground.getY();
        originalGroupNameMargin = editTextGroupName.getY();
        layoutDividerBackground.animate()
                .y(0)
                .setDuration(animDuration)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .start();
        int editTextGroupTopMargin = 22;
        editTextGroupName.animate()
                .y(screenUtils.dpToPx(editTextGroupTopMargin))
                .setDuration(animDuration)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .start();
        layoutDividerBackground.bringToFront();
        editTextGroupName.bringToFront();
    }

    public void bringGroupNameDown(int animDuration) {
        layoutDividerBackground.animate()
                .y(originalGroupBackgroundMargin)
                .setDuration(animDuration)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .start();
        editTextGroupName.animate()
                .y(originalGroupNameMargin)
                .setDuration(animDuration)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .start();
    }



    public String getGroupName() {
        return editTextGroupName.getText().toString();
    }

    public void setGroupName(String groupName) {
        editTextGroupName.setText(groupName);
    }

    public void setCursorEndGroupName() {
        editTextGroupName.setSelection(editTextGroupName.getText().length());
    }

    public void setPrivacy(boolean isPrivate, int memberCount) {
            viewPrivacyStatus.setup(isPrivate, memberCount);
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
