package com.tribe.app.presentation.view.fragment;

import android.animation.ObjectAnimator;
import android.animation.RectEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.Image;
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
import com.tribe.app.presentation.view.component.CreateInviteView;
import com.tribe.app.presentation.view.component.GroupInfoView;
import com.tribe.app.presentation.view.component.MemberPhotoViewList;
import com.tribe.app.presentation.view.component.PrivatePublicView;
import com.tribe.app.presentation.view.component.ViewPrivacyStatus;
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
    @BindView(R.id.imageDone)
    ImageView imageDone;
    @BindView(R.id.editTextInviteSearch)
    EditTextFont editTextInviteSearch;
    @BindView(R.id.layoutInvite)
    NestedScrollView layoutInvite;
    @BindView(R.id.recyclerViewInvite)
    RecyclerView recyclerViewInvite;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.createInviteView)
    CreateInviteView createInviteView;
    @BindView(R.id.groupInfoView)
    GroupInfoView groupInfoView;

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
    private boolean groupInfoValid = false;

    // Animation Variables
    int moveUpY = 138;
    int moveGroupName = 60;


    int layoutCreateInviteInfoPositionY = 255;
    int startTranslationDoneIcon = 200;

    int smallMargin = 5;

    /**
     * View lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_groups_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();

        initPresenter();
        Bundle bundle = getArguments();
        if (bundle == null) initUi();
        else initGroupInfoUi(bundle.getString("groupId"));
        initFriendshipList();
        initSearchView();
        fragmentView.setTag(HomeActivity.GROUPS_FRAGMENT_PAGE);
        return fragmentView;
    }

    public GroupInfoView getGroupInfoView() {
        return groupInfoView;
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
        screenUtils.setTopMargin(createInviteView, screenUtils.dpToPx(layoutCreateInviteInfoPositionY));
        createInviteView.setInvite(privateGroup);
        createInviteView.enableInvitePress();
        groupInfoView.setupGroupInfoUi(privateGroup);
        imageDone.setVisibility(View.INVISIBLE);

        layoutInvite.setTranslationY(screenUtils.dpToPx(smallMargin));
        recyclerViewInvite.setTranslationY(screenUtils.dpToPx(smallMargin));

        subscriptions.add(groupInfoView.imageBackClicked().subscribe(aVoid -> {
            navigator.navigateToHome(getActivity());
        }));
        groupPresenter.getGroupMembers(groupId);
    }

    @Override
    public void setupGroupMembers(Group group) {
        members = group.getMembers();
        for (int i = 0; i < 5; i++) {
            String profPic = members.get(i).getProfilePicture();
            if (profPic != null) groupInfoView.addMemberPhoto(profPic);
        }
    }


    private void initUi() {
        // Setup top-right icons
        groupInfoView.setUpInitialUi();
        imageDone.setTranslationY(screenUtils.dpToPx(startTranslationDoneIcon));

        createInviteView.disable();
        layoutInvite.setNestedScrollingEnabled(false);
        layoutInvite.setTranslationY(screenUtils.dpToPx(moveUpY));
        layoutInvite.setVisibility(View.INVISIBLE);
        recyclerViewInvite.setTranslationY(screenUtils.dpToPx(moveUpY));
        recyclerViewInvite.setVisibility(View.INVISIBLE);

        subscriptions.add(groupInfoView.isPrivate().subscribe(isPrivate -> {
            privateGroup = isPrivate;
            if (groupInfoValid) {
                createInviteView.switchColors(privateGroup);
            }
        }));

        subscriptions.add(groupInfoView.imageGroupClicked().subscribe(aVoid -> {
            setupBottomSheetCamera();
        }));

        subscriptions.add(groupInfoView.isGroupNameValid().subscribe(isValid -> {
            groupInfoValid = isValid;
            groupInfoView.enableDoneEdit(isValid);
            if (isValid) {
                createInviteView.enableCreate(privateGroup);
            } else {
                createInviteView.disable();
                createInviteView.setCreateGrey();
            }
        }));

        subscriptions.add(createInviteView.createPressed().subscribe(aVoid -> {
            createInviteView.disable();
            groupInfoView.disableButtons();

            createInviteView.loadingAnimation(loadingAnimDuration, screenUtils, getActivity());
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

            Observable.timer(loadingAnimDuration + animDuration * 2, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(time -> {
                        animSet3();
                    });
        }));

        subscriptions.add(groupInfoView.imageEditGroupClicked().subscribe(aVoid -> {
            presentEdit();
        }));

        subscriptions.add(groupInfoView.imageDoneEditClicked().subscribe(aVoid -> {
            backFromEdit();
        }));

        subscriptions.add((createInviteView.invitePressed()).subscribe(aVoid -> {
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
        groupInfoView.collapse(animDuration, getActivity());
        imageDone.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();

        createInviteView.animate()
                .translationY(-screenUtils.dpToPx(moveUpY))
                .setDuration(animDuration)
                .start();
        resetLayoutInvite();

    }

    private void presentEdit() {
        // Setup top-right icon
        imageDone.animate()
                .translationY(screenUtils.dpToPx(startTranslationDoneIcon))
                .setDuration(animDuration)
                .start();

        groupInfoView.expand(animDuration);

        // Small move of bottom half off view
        int presentEditTranslation = screenUtils.dpToPx(25);
        createInviteView.animate()
                .setDuration(animDuration)
                .y(presentEditTranslation + createInviteView.getY())
                .start();
        layoutInvite.animate()
                .setDuration(animDuration)
                .y(presentEditTranslation + layoutInvite.getY())
                .start();
        recyclerViewInvite.animate()
                .setDuration(animDuration)
                .y(presentEditTranslation + recyclerViewInvite.getY())
                .start();


        AnimationUtils.animateBottomMargin(recyclerViewInvite, screenUtils.dpToPx(25), animDuration);
    }

    /**
     * Animations performed after step 1
     */

    private void animSet1() {
        groupInfoView.collapsePrivatePublic(privateGroup, animDuration);
        groupInfoView.collapse(animDuration, getActivity());
        createInviteView.disable();
        createInviteView.setInvite(privateGroup);
        ((HomeActivity) getActivity()).disableNavigation();
    }

    private void animSet2() {
        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() - screenUtils.dpToPx(116),
                animDuration);
        createInviteView.animate()
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
        createInviteView.scaleInInviteImage(animDuration);

        if (!privateGroup) imageDone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_done_purple));;

        imageDone.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();;

        createInviteView.enableInvitePress();
        layoutInvite.setNestedScrollingEnabled(true);
    }

    private void setHeightCoordinatorLayout(View coordinatorLayout, int height) {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) coordinatorLayout.getLayoutParams();
        layoutParams.height = height;
        coordinatorLayout.setLayoutParams(layoutParams);
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
        if (text.isEmpty()) {
            friendshipsList.clear();
            friendshipsList.addAll(friendshipsListCopy);
        } else {
            ArrayList<Friendship> result = new ArrayList<>();
            text = text.toLowerCase();
            for (Friendship item : friendshipsListCopy) {
                if (item.getDisplayName().toLowerCase().contains(text) || item.getDisplayName().toLowerCase().contains(text)) {
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
