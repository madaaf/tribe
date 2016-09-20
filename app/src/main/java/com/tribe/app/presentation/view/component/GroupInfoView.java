package com.tribe.app.presentation.view.component;

import android.app.Activity;
import android.app.VoiceInteractor;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
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

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
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

    public GroupInfoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @BindView(R.id.imageGroup)
    ImageView imageGroup;
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
    int moveGroupName = 60;
    int startTranslationEditIcons = -200;
    int privacyFinalPosition = -48;
    float groupPicScaleDownF = .7f;
    int layoutViewBackgroundInfoPositionY = 185;
    int groupLayoutOffSet = 20;

    private PublishSubject<Boolean> isPrivate = PublishSubject.create();
    private PublishSubject<Boolean> isGroupNameValid = PublishSubject.create();
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



    public void addMemberPhoto(String profPic) {
        memberPhotoViewList.addMemberPhoto(profPic);
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

    public void disableButtons() {
        imageGroup.setEnabled(false);
        privatePublicView.setEnabled(false);
    }



    public void collapsePrivatePublic(Boolean privateGroup, int animDuration) {
        AnimationUtils.collapseScale(privatePublicView, animDuration);
        privatePublicView.animate()
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .alpha(AnimationUtils.ALPHA_NONE)
                .setDuration(animDuration)
                .start();

        viewPrivacyStatus.setup(privateGroup);
    }

    public void expand(int animDuration) {
        // Setup group image
        imageGroup.setEnabled(true);
        imageGroup.animate()
                .scaleY(AnimationUtils.SCALE_RESET)
                .scaleX(AnimationUtils.SCALE_RESET)
                .setDuration(animDuration)
                .start();

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
        // Setup group image
        imageGroup.setEnabled(false);
        imageGroup.animate()
                .scaleY(groupPicScaleDownF)
                .scaleX(groupPicScaleDownF)
                .setDuration(animDuration)
                .start();

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

    public void presentFinishEditIcons(int animDuration) {
        bringInIcon(imageDoneEdit, animDuration);
        bringOutIcon(imageEditGroup, animDuration);
    }

    public void presentEditIcons(int animDuration) {
        bringInIcon(imageEditGroup, animDuration);
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

    public void setupGroupInfoUi(boolean privateGroup) {
        imageDoneEdit.setTranslationY(-500);
        privatePublicView.setVisibility(View.INVISIBLE);

        imageGroup.setEnabled(false);
        imageGroup.setScaleX(groupPicScaleDownF);
        imageGroup.setScaleY(groupPicScaleDownF);
        layoutDividerBackground.setVisibility(INVISIBLE);
        layoutGroupMembers.setVisibility(VISIBLE);
        editTextGroupName.bringToFront();
        editTextGroupName.setEnabled(false);
        editTextGroupName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        editTextGroupName.setCursorVisible(false);
        editTextGroupName.setTranslationY(-screenUtils.dpToPx(moveGroupName));
        viewPrivacyStatus.setAlpha(AnimationUtils.ALPHA_FULL);
        viewPrivacyStatus.setTranslationY(screenUtils.dpToPx(privacyFinalPosition));
        viewPrivacyStatus.setup(privateGroup);
        imageBackIcon.setVisibility(View.VISIBLE);
        imageBackIcon.setClickable(true);

        memberPhotoViewList.bringToFront();

        subscriptions.add(RxView.clicks(imageBackIcon).subscribe(aVoid -> {
            imageBackClicked.onNext(null);
        }));
    }

    public String getGroupName() {
        return editTextGroupName.getText().toString();
    }

    public void setGroupName(String groupName) {
        editTextGroupName.setText(groupName);
    }

    public void setPrivacy(boolean isPrivate) {
            viewPrivacyStatus.setup(isPrivate);
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
