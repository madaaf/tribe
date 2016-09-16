package com.tribe.app.presentation.view.fragment;

import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.CameraType;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.FriendAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.component.MemberPhotoViewList;
import com.tribe.app.presentation.view.component.PrivatePublicView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ImageUtils;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Fragment that shows a list of media.
 */
public class GroupsGridFragment extends BaseFragment implements GroupView {

    public GroupsGridFragment() {
        setRetainInstance(true);
    }

    public static GroupsGridFragment newInstance(Bundle args) {

        GroupsGridFragment fragment = new GroupsGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // Bind view
    @BindView(R.id.imageGroup)
    ImageView imageGroup;
    @BindView(R.id.imageEditGroup)
    ImageView imageEditGroup;
    @BindView(R.id.imagePrivacyStatus)
    ImageView imagePrivacyStatus;
    @BindView(R.id.imageInvite)
    ImageView imageInvite;
    @BindView(R.id.imageDone)
    ImageView imageDone;
    @BindView(R.id.imageDoneEdit)
    ImageView imageDoneEdit;
    @BindView(R.id.textPrivacyStatus)
    TextViewFont textPrivacyStatus;
    @BindView(R.id.textCreateInvite)
    TextViewFont textCreateInvite;
    @BindView(R.id.textCreateInviteDesc)
    TextViewFont textCreateInviteDesc;
    @BindView(R.id.editTextGroupName)
    EditTextFont editTextGroupName;
    @BindView(R.id.editTextInviteSearch)
    EditTextFont editTextInviteSearch;
    @BindView(R.id.viewCreateGroupBg1)
    View viewCreateGroupBg1;
    @BindView(R.id.viewCreateGroupBg2)
    View viewCreateGroupBg2;
    @BindView(R.id.layoutDividerBackground)
    FrameLayout layoutDividerBackground;
    @BindView(R.id.layoutPrivacyStatus)
    LinearLayout layoutPrivacyStatus;
    @BindView(R.id.layoutCreateInvite)
    FrameLayout layoutCreateInvite;
    @BindView(R.id.layoutInvite)
    NestedScrollView layoutInvite;
    @BindView(R.id.recyclerViewInvite)
    RecyclerView recyclerViewInvite;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.privatePublicView)
    PrivatePublicView privatePublicView;
    @BindView(R.id.memberPhotoViewList)
    MemberPhotoViewList memberPhotoViewList;

    // Dagger Dependencies
    @Inject
    Navigator navigator;
    @Inject
    ScreenUtils screenUtils;
    @Inject
    FriendAdapter friendAdapter;
    @Inject
    GroupPresenter groupPresenter;

    // VARIABLES
    private BottomSheetDialog dialogCamera;
    private RecyclerView recyclerViewCameraType;
    private LabelSheetAdapter cameraTypeAdapter;
    private int animDuration = 300 * 2;
    private int loadingAnimDuration = 1000 * 2;
    private boolean privateGroup = true;
    private List<Friendship> friendshipsList;
    private List<Friendship> friendshipsListCopy;
    List<User> members;
    private LinearLayoutManager linearLayoutManager;
    private Group currentGroup;

    // Animation Variables
    int moveUpY = 138;
    int moveGroupName = 60;
    int privacyFinalPosition;
    int startTranslationEditIcons;
    int startTranslationDoneIcon;
    float groupPicScaleDownF = .7f;
    int layoutCreateInviteInfoPositionY = 255;
    int layoutViewBackgroundInfoPositionY = 185;
    int smallMargin = 5;
    /**
     * View lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_groups_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();

        privacyFinalPosition = -screenUtils.dpToPx(48);

        initPresenter();
        Bundle bundle = getArguments();
        if (bundle == null) initUi();
        else initGroupInfoUi(bundle.getString("groupId"));
        initFriendshipList();
        initSearchView();
        fragmentView.setTag(HomeActivity.GROUPS_FRAGMENT_PAGE);
        return fragmentView;
    }

    @Override
    public void onDestroy() {

        unbinder.unbind();

        if (subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDestroy();
    }

    /**
     * Setup UI
     * Includes subscription setup
     */

    private void initGroupInfoUi(String groupId) {
        // Set up initial position of view
        imageDoneEdit.setTranslationY(-500);
        // Setup invite button
        viewCreateGroupBg1.setEnabled(true);
        if (privateGroup) viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
        else viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
        textCreateInvite.setText(getString(R.string.group_button_share));
        textCreateInviteDesc.setText(getString(R.string.group_share_description));
        screenUtils.setTopMargin(layoutCreateInvite, screenUtils.dpToPx(layoutCreateInviteInfoPositionY));
        privatePublicView.setVisibility(View.INVISIBLE);
        imageDone.setVisibility(View.INVISIBLE);
        imageGroup.setEnabled(false);
        imageGroup.setScaleX(groupPicScaleDownF);
        imageGroup.setScaleY(groupPicScaleDownF);
        screenUtils.setTopMargin(layoutDividerBackground, screenUtils.dpToPx(layoutViewBackgroundInfoPositionY));
        editTextGroupName.bringToFront();
        editTextGroupName.setEnabled(false);
        editTextGroupName.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        editTextGroupName.setCursorVisible(false);
        editTextGroupName.setTranslationY(-screenUtils.dpToPx(moveGroupName));
        // setup privacy view
        layoutPrivacyStatus.setAlpha(AnimationUtils.ALPHA_FULL);
        layoutPrivacyStatus.setTranslationY(privacyFinalPosition);
        if (privateGroup) {
            textPrivacyStatus.setText(getString(R.string.group_private_title));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_grey));
        } else {
            textPrivacyStatus.setText(getString(R.string.group_public_title));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_grey));
        }
        layoutInvite.setTranslationY(screenUtils.dpToPx(smallMargin));
        recyclerViewInvite.setTranslationY(screenUtils.dpToPx(smallMargin));

        groupPresenter.getGroupMembers(groupId);
    }

    @Override
    public void setupGroupMembers(Group group) {
        members = group.getMembers();
        for (int i = 0; i < 5; i++) {
            String profPic = members.get(i).getProfilePicture();
            if (profPic != null) memberPhotoViewList.addMemberPhoto(profPic);
        }
    }


    private void initUi() {
        // Setup top-right icons
        startTranslationEditIcons = screenUtils.dpToPx(-200);
        startTranslationDoneIcon = screenUtils.dpToPx(200);

        imageEditGroup.setTranslationY(startTranslationEditIcons);
        imageDoneEdit.setTranslationY(startTranslationEditIcons);
        imageInvite.setScaleX(0);
        imageInvite.setScaleY(0);
        imageDone.setTranslationY(startTranslationDoneIcon);
        viewCreateGroupBg2.setEnabled(false);
        viewCreateGroupBg1.setEnabled(false);
        layoutInvite.setTranslationY(screenUtils.dpToPx(moveUpY));
        layoutInvite.setVisibility(View.INVISIBLE);
        recyclerViewInvite.setTranslationY(screenUtils.dpToPx(moveUpY));
        recyclerViewInvite.setVisibility(View.INVISIBLE);

        subscriptions.add(privatePublicView.isPrivate().subscribe(isPrivate -> {
                    privateGroup = isPrivate;
                    if (editTextGroupName.getText().toString().length() > 0) {
                        if (privateGroup) viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
                        else viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
                    }
                }));

        subscriptions.add(RxView.clicks(imageGroup).subscribe(aVoid -> {
            setupBottomSheetCamera();
        }));

        subscriptions.add(RxTextView.textChanges(editTextGroupName).subscribe(charSequence -> {
            if (editTextGroupName.getText().toString().length() > 0) {
                if (privateGroup) viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_enabled));
                else viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
                viewCreateGroupBg2.setEnabled(true);
            }
            else {
                viewCreateGroupBg1.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_disabled));
                viewCreateGroupBg2.setEnabled(false);
            }
        }));

        subscriptions.add(RxView.clicks(viewCreateGroupBg2).subscribe(aVoid -> {
            imageGroup.setEnabled(false);
            viewCreateGroupBg2.setEnabled(false);
            privatePublicView.setEnabled(false);

            createGroupLoadingAnim();
            Observable.timer(loadingAnimDuration, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                       animSet1();
                    });


            Observable.timer(loadingAnimDuration + animDuration, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                       animSet2();
                    });

            Observable.timer(loadingAnimDuration + animDuration*2, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        animSet3();
                    });
        }));

        subscriptions.add(RxView.clicks(imageEditGroup).subscribe(aVoid -> {
            presentEdit();
        }));

        subscriptions.add(RxView.clicks(imageDoneEdit).subscribe(aVoid -> {
            backFromEdit();
        }));

        subscriptions.add(RxView.clicks(viewCreateGroupBg1).subscribe(aVoid -> {
            showShareDialogFragment();
        }));

        subscriptions.add(RxView.clicks(editTextInviteSearch).subscribe(aVoid -> {
            appBarLayout.setExpanded(false);
        }));

        subscriptions.add(RxView.clicks(imageDone).subscribe(aVoid -> {
            // TODO: send data to create group
            ((HomeActivity) getActivity()).resetGroupsGridFragment();
        }));
    }

    private void resetLayoutInvite() {
        layoutInvite.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();
        recyclerViewInvite.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();

        AnimationUtils.animateBottomMargin(recyclerViewInvite, AnimationUtils.TRANSLATION_RESET, animDuration);
    }



    /**
     * Methods
     */

    private void backFromEdit() {
        removeEditGroup();

        layoutCreateInvite.animate()
                .translationY(-screenUtils.dpToPx(moveUpY))
                .setDuration(animDuration)
                .start();
        resetLayoutInvite();

        imageDone.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();
    }

    private void presentEdit() {
        // Setup top-right icon
        bringInIcon(imageDoneEdit);
        bringOutIcon(imageEditGroup);

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
        layoutPrivacyStatus.animate()
                .setDuration(animDuration)
                .alpha(AnimationUtils.ALPHA_NONE)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .start();

        // Small move of bottom half off view
        int presentEditTranslation = screenUtils.dpToPx(25);
        layoutCreateInvite.animate()
                .setDuration(animDuration)
                .y(presentEditTranslation+layoutCreateInvite.getY())
                .start();
        layoutInvite.animate()
                .setDuration(animDuration)
                .y(presentEditTranslation+layoutInvite.getY())
                .start();
        recyclerViewInvite.animate()
                .setDuration(animDuration)
                .y(presentEditTranslation+recyclerViewInvite.getY())
                .start();

        imageDone.animate()
                .translationY(startTranslationDoneIcon)
                .setDuration(animDuration)
                .start();

        AnimationUtils.animateBottomMargin(recyclerViewInvite, screenUtils.dpToPx(25), animDuration);
    }

    private void removeEditGroup() {
        screenUtils.hideKeyboard(getActivity());

        // Setup top right icons
        bringInIcon(imageEditGroup);
        bringOutIcon(imageDoneEdit);

        // Setup group image
        imageGroup.setEnabled(false);
        imageGroup.animate()
                .scaleY(groupPicScaleDownF)
                .scaleX(groupPicScaleDownF)
                .setDuration(animDuration)
                .start();

        // Setup group name
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
        layoutPrivacyStatus.animate()
                .setDuration(animDuration)
                .alpha(AnimationUtils.ALPHA_FULL)
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .translationY(privacyFinalPosition)
                .start();
    }

    /**
     * Animations performed after step 1
     */

    private void createGroupLoadingAnim() {
        screenUtils.hideKeyboard(getActivity());
        Rect rect = new Rect();
        viewCreateGroupBg2.getLocalVisibleRect(rect);
        Rect from = new Rect(rect);
        Rect to = new Rect(rect);
        from.right = 0;
        viewCreateGroupBg2.setAlpha(1f);
        ObjectAnimator anim = ObjectAnimator.ofObject(viewCreateGroupBg2,
                "clipBounds",
                new RectEvaluator(),
                from, to);
        anim.setDuration(loadingAnimDuration);
        anim.start();
        Observable.timer(loadingAnimDuration, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(time -> {
                    viewCreateGroupBg2.setVisibility(View.INVISIBLE);
                });
    }

    private void animSet1() {
        // remove private public selection
        AnimationUtils.collapseScale(privatePublicView, animDuration);
        privatePublicView.animate()
                .setStartDelay(AnimationUtils.NO_START_DELAY)
                .alpha(AnimationUtils.ALPHA_NONE)
                .setDuration(animDuration)
                .start();

        // setup privacy view
        if (privateGroup) {
            textPrivacyStatus.setText(getString(R.string.group_private_title));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_lock_grey));
        } else {
            textPrivacyStatus.setText(getString(R.string.group_public_title));
            imagePrivacyStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_megaphone_grey));
        }

        removeEditGroup();

        // Setup invite button
        textCreateInvite.setText(getString(R.string.group_button_share));
        textCreateInviteDesc.setText(getString(R.string.group_share_description));

        ((HomeActivity) getActivity()).disableNavigation();

    }

    private void animSet2() {
        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() - screenUtils.dpToPx(116),
                animDuration);
        layoutCreateInvite.animate()
                .translationY(-screenUtils.dpToPx(moveUpY))
                .setDuration(animDuration)
                .start();
        layoutInvite.setVisibility(View.VISIBLE);
        recyclerViewInvite.setVisibility(View.VISIBLE);
        layoutInvite.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();
        recyclerViewInvite.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();
    }

    private void animSet3() {
        AnimationUtils.scaleIn(imageInvite, animDuration);

        if (!privateGroup) imageDone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_done_purple));

        imageDone.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();

        viewCreateGroupBg1.setEnabled(true);

    }

    private void setHeightCoordinatorLayout(View coordinatorLayout, int height) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) coordinatorLayout.getLayoutParams();
        layoutParams.height = height;
        coordinatorLayout.setLayoutParams(layoutParams);
    }

    /**
     * Helper UI methods
     */

    private void bringInIcon(ImageView imageView) {
        imageView.setEnabled(true);
        imageView.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();
    }

    private void bringOutIcon(ImageView imageView) {
        imageView.setEnabled(false);
        imageView.animate()
                .translationY(startTranslationEditIcons)
                .setDuration(animDuration)
                .start();
    }

    private void showShareDialogFragment() {
        ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance();
        shareDialogFragment.show(getFragmentManager(), ShareDialogFragment.class.getName());

    }

    /**
     * Search for friend View
     */

    private void initFriendshipList() {
        User user = getCurrentUser();
        friendshipsList = user.getFriendships();
        friendshipsListCopy = new ArrayList<>();
        friendshipsListCopy.addAll(friendshipsList);
        friendAdapter.setItems(friendshipsList);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewInvite.setLayoutManager(linearLayoutManager);
        recyclerViewInvite.setAdapter(friendAdapter);
    }

    private void initSearchView() {
        editTextInviteSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                filter(editable.toString());
            }
        });
    }

    private void filter(String text) {
        if(text.isEmpty()){
            friendshipsList.clear();
            friendshipsList.addAll(friendshipsListCopy);
        } else{
            ArrayList<Friendship> result = new ArrayList<>();
            text = text.toLowerCase();
            for(Friendship item: friendshipsListCopy){
                if(item.getDisplayName().toLowerCase().contains(text) || item.getDisplayName().toLowerCase().contains(text)){
                    result.add(item);
                }
            }
            friendshipsList.clear();
            friendshipsList.addAll(result);
        }
        friendAdapter.setItems(friendshipsList);
    }

    /**
     * Bottom sheet set-up
     */

    private void prepareBottomSheetCamera(List<LabelType> items) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_camera_type, null);
        recyclerViewCameraType = (RecyclerView) view.findViewById(R.id.recyclerViewCameraType);
        recyclerViewCameraType.setHasFixedSize(true);
        recyclerViewCameraType.setLayoutManager(new LinearLayoutManager(getActivity()));
        cameraTypeAdapter = new LabelSheetAdapter(getContext(), items);
        cameraTypeAdapter.setHasStableIds(true);
        recyclerViewCameraType.setAdapter(cameraTypeAdapter);
        subscriptions.add(cameraTypeAdapter.clickLabelItem()
        .map(labelView -> cameraTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
        .subscribe(labelType -> {
            CameraType cameraType = (CameraType) labelType;
            if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_CAMERA)) {
                navigator.getImageFromCamera(getActivity(), HomeActivity.OPEN_CAMERA_RESULT);
            }
            if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_PHOTOS)) {
                navigator.getImageFromCameraRoll(getActivity(), HomeActivity.OPEN_GALLERY_RESULT);
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

    private void setupBottomSheetCamera() {
        if (dismissDialogSheetCamera()) {
            return;
        }

        List<LabelType> cameraTypes = new ArrayList<>();
        cameraTypes.add(new CameraType(getString(R.string.image_picker_camera), CameraType.OPEN_CAMERA));
        cameraTypes.add(new CameraType(getString(R.string.image_picker_library), CameraType.OPEN_PHOTOS));

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
     * Public methods
     */

    public void setGroupPicture(Bitmap bitmap) {
        imageGroup.setImageBitmap(formatBitmapforView(bitmap));
    }

    public Bitmap formatBitmapforView(Bitmap thumbnail) {
        RoundedCornersTransformation roundedCornersTransformation = new RoundedCornersTransformation(imageGroup.getWidth() >> 1, 0, RoundedCornersTransformation.CornerType.ALL);
        thumbnail = ImageUtils.centerCropBitmap(thumbnail);
        thumbnail = Bitmap.createScaledBitmap(thumbnail, imageGroup.getWidth(), imageGroup.getWidth(), false);
        thumbnail = roundedCornersTransformation.transform(thumbnail);
        return thumbnail;
    }

    /**
     * Dependency injection set-up
     */

    protected ApplicationComponent getApplicationComponent() {
        return ((AndroidApplication) getActivity().getApplication()).getApplicationComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(getActivity());
    }

    private void initDependencyInjector() {
        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    private void initPresenter() {
        groupPresenter.attachView(this);
    }

    @Override
    public void getGroupMembers(String groupId) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return null;
    }
}
