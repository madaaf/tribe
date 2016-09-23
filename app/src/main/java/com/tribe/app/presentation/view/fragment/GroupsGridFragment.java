package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jakewharton.rxbinding.view.RxView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.CameraType;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Group;
import com.tribe.app.domain.entity.GroupMember;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.GroupPresenter;
import com.tribe.app.presentation.mvp.view.GroupView;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.FriendAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.component.CreateInviteView;
import com.tribe.app.presentation.view.component.GroupInfoView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * Fragment that shows a list of media.
 */
public class GroupsGridFragment extends BaseFragment implements GroupView {

    public static GroupsGridFragment newInstance(Bundle args) {
        GroupsGridFragment fragment = new GroupsGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupsGridFragment() {
        setRetainInstance(true);
    }

    private Unbinder unbinder;
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
    ScreenUtils screenUtils;
    @Inject
    FriendAdapter friendAdapter;
    @Inject
    GroupPresenter groupPresenter;

    // VARIABLES
    private BottomSheetDialog dialogCamera;
    private LabelSheetAdapter cameraTypeAdapter;
    private List<Friendship> friendshipsList = new ArrayList<>();
    private List<Friendship> friendshipsListCopy = new ArrayList<>();
    List<User> members;
    private LinearLayoutManager linearLayoutManager;
    private boolean groupInfoValid = false;

    // Group Info
    private String groupId = null;
    private String groupName = null;
    private List<String> memberIds = new ArrayList<>();
    private String groupPictureUri = null;
    private boolean privateGroup = true;
    private List<String> groupMemberIds = new ArrayList<>();
    ArrayList<GroupMember> groupMemberList;

    // Animation Variables
    private int moveUpY = 138;
    private int presentEditInfoTranslation = 20;
    private int layoutCreateInviteInfoPositionY = 255;
    private int startTranslationDoneIcon = 200;
    private int animDuration = 300 * 2;
    private int loadingAnimDuration = 1000 * 2;
    private int smallMargin = 5;

    // Observables
    private PublishSubject<Void> imageGoToMembersClicked = PublishSubject.create();
    public Observable<Void> imageGoToMembersClicked() {
        return imageGoToMembersClicked;
    }

    /**
     * View lifecycle methods
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View fragmentView = inflater.inflate(R.layout.fragment_groups_grid, container, false);
        unbinder = ButterKnife.bind(this, fragmentView);
        initDependencyInjector();
        initForBoth();
        initPresenter();

        Bundle bundle = getArguments();
        if (bundle == null) {
            initUi();
            initFriendshipList();
        }
        else {
            initGroupInfoUi(bundle.getString("groupId"));
            recyclerViewInvite.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewInvite.setAdapter(new RecyclerView.Adapter() {
                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return null;
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

                }

                @Override
                public int getItemCount() {
                    return 0;
                }
            });
        }
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

    public ArrayList<GroupMember> getGroupMemberList() {
        return groupMemberList;
    }

    public String getGroupId() {
        return groupId;
    }

    /**
     * Setup UI
     * Includes subscription setup
     */

    private void initGroupInfoUi(String groupId) {
        this.groupId = groupId;
        screenUtils.setTopMargin(createInviteView, screenUtils.dpToPx(layoutCreateInviteInfoPositionY));
        createInviteView.setInvite(privateGroup);
        createInviteView.enableInvitePress();
        groupInfoView.setupGroupInfoUi(privateGroup);
        imageDone.setVisibility(View.VISIBLE);

        layoutInvite.setTranslationY(screenUtils.dpToPx(smallMargin));
        recyclerViewInvite.setTranslationY(screenUtils.dpToPx(smallMargin));

        subscriptions.add(groupInfoView.imageBackClicked().subscribe(aVoid -> {
            navigator.navigateToHome(getActivity(), false);
        }));
        subscriptions.add(groupInfoView.imageEditGroupClicked().subscribe(aVoid -> {
            presentEditInfo();
        }));
        subscriptions.add(groupInfoView.imageGoToMembersClicked().subscribe(aVoid -> {
            imageGoToMembersClicked.onNext(null);
        }));
        groupPresenter.getGroupMembers(groupId);
    }

    public void initForBoth() {
        subscriptions.add(groupInfoView.imageDoneEditClicked().subscribe(aVoid -> {
            groupName = groupInfoView.getGroupName();
            groupPresenter.updateGroup(groupId, groupName, groupPictureUri);
            backFromEditInfo();
        }));
        subscriptions.add(RxView.clicks(imageDone).subscribe(aVoid -> {
            groupPresenter.addMembersToGroup(groupId, memberIds);
        }));
        subscriptions.add(groupInfoView.imageGroupClicked().subscribe(aVoid -> {
            setupBottomSheetCamera();
        }));
    }

    @Override
    public void setupGroup(Group group) {
        groupName = group.getDisplayName();
        groupInfoView.setGroupName(groupName);
        if (group.getProfilePicture() != null && !group.getProfilePicture().isEmpty()) groupInfoView.setGroupPictureFromUrl(group.getProfilePicture());
        setGroupPrivacy(group.isPrivateGroup());

        members = group.getMembers();
        int memberPhotos;
        if (group.getMembers().size() < 5) memberPhotos = group.getMembers().size();
        else memberPhotos = 5;
        for (int i = 0; i < memberPhotos; i++) {
            String profPic = members.get(i).getProfilePicture();
            if (profPic != null) groupInfoView.addMemberPhoto(profPic);
        }
        initFriendshipListExcluding(members);
        friendAdapter.setItems(friendshipsList);
        friendAdapter.notifyDataSetChanged();

        // Setup Group Member View info
        List<User> admins = group.getAdmins();
        User user = getCurrentUser();
        List<Friendship> friendsList = user.getFriendships();
        List<User> friendsUsers = new ArrayList<>();
        for (Friendship friendship : friendsList) {
            friendsUsers.add(friendship.getFriend());
        }
        groupMemberList = new ArrayList<>();
        for (User member : members) {
            GroupMember groupMember = new GroupMember(
                    member.getId(),
                    member.getDisplayName(),
                    member.getUsername(),
                    member.getProfilePicture());
            if (groupMember.getUserId().equals(user.getId())) groupMember.setCurrentUser(true);
            for (int i = 0; i < friendsUsers.size(); i++) {
                if (friendsUsers.get(i).getId().equals(member.getId())) {
                    groupMember.setFriend(true);
                    groupMember.setFriendshipId(friendsList.get(i).getFriendshipId());
                }
            }
            for (User admin : admins) {
                if (admin.getId().equals(member.getId())) groupMember.setAdmin(true);
            }
            groupMemberList.add(groupMember);
        }
    }

    /**
     * Modify layout methods
     */

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
            groupName = groupInfoView.getGroupName();
            groupPresenter.createGroup(groupName, memberIds, privateGroup, groupPictureUri);

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

        subscriptions.add((createInviteView.invitePressed()).subscribe(aVoid -> {
            showShareDialogFragment();
        }));

        subscriptions.add(RxView.clicks(editTextInviteSearch).subscribe(aVoid -> {
            appBarLayout.setExpanded(false);
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

    private void setGroupPrivacy(boolean isPrivate) {
        groupInfoView.setPrivacy(isPrivate);
        createInviteView.switchColors(isPrivate);
        if (!isPrivate) imageDone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_done_purple));
    }

    /**
     * Methods to switch between edit and non-edit modes
     */
    private void backFromEdit() {
        groupInfoView.collapseInfo(animDuration, getActivity());
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

    private void backFromEditInfo() {
        groupInfoView.collapseInfo(animDuration, getActivity());

        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() - screenUtils.dpToPx(presentEditInfoTranslation),
                animDuration);

        createInviteView.animate()
                .setDuration(animDuration)
                .y(-screenUtils.dpToPx(presentEditInfoTranslation) + createInviteView.getY())
                .start();

        layoutInvite.animate()
                .setDuration(animDuration)
                .y(-screenUtils.dpToPx(presentEditInfoTranslation) + layoutInvite.getY())
                .start();
        recyclerViewInvite.animate()
                .setDuration(animDuration)
                .y(-screenUtils.dpToPx(presentEditInfoTranslation) + recyclerViewInvite.getY())
                .start();

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

    private void presentEditInfo() {
        groupInfoView.expandInfo(animDuration);

        appBarLayout.bringToFront();
        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() + screenUtils.dpToPx(presentEditInfoTranslation),
                animDuration);

        createInviteView.animate()
                .setDuration(animDuration)
                .y(screenUtils.dpToPx(presentEditInfoTranslation) + createInviteView.getY())
                .start();

        layoutInvite.animate()
                .setDuration(animDuration)
                .y(screenUtils.dpToPx(presentEditInfoTranslation) + layoutInvite.getY())
                .start();
        recyclerViewInvite.animate()
                .setDuration(animDuration)
                .y(screenUtils.dpToPx(presentEditInfoTranslation) + recyclerViewInvite.getY())
                .start();
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

    private void showShareDialogFragment() {
        ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance();
        shareDialogFragment.show(getFragmentManager(), ShareDialogFragment.class.getName());

    }

    public void setPictureUri(String pictureUri) {
        this.groupPictureUri = pictureUri;
    }

    /**
     * Search for friend View
     */

    private void initFriendshipList() {
        User user = getCurrentUser();
        friendshipsList = user.getFriendships();
        friendshipsListCopy = new ArrayList<>();
        if (friendshipsList != null) {
            friendshipsListCopy.addAll(friendshipsList);
            friendAdapter.setHasStableIds(true);
            friendAdapter.setItems(friendshipsList);
        }

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewInvite.setLayoutManager(linearLayoutManager);
        recyclerViewInvite.setAdapter(friendAdapter);

        subscriptions.add(friendAdapter.clickFriendItem()
            .subscribe(friendView -> {
                Friendship friendship = friendAdapter.getItemAtPosition((Integer) friendView.getTag(R.id.tag_position));
                String friendId = friendship.getFriend().getId();
                if ((Boolean) friendView.getTag(R.id.tag_selected)) {
                    memberIds.add(friendId);
                } else {
                    memberIds.remove(friendId);
                }
            }));
    }

    private void initFriendshipListExcluding(List<User> usersToExclude) {
        User user = getCurrentUser();
        friendshipsList.addAll(user.getFriendships());
        for(Iterator<Friendship> iterFriendship = friendshipsList.iterator(); iterFriendship.hasNext();) {
            final User frienshipUser = iterFriendship.next().getFriend();
            for (Iterator<User> iterMember = usersToExclude.iterator(); iterMember.hasNext();) {
                if (frienshipUser.getId().equals(iterMember.next().getId())) iterFriendship.remove();
            }
        }
        friendshipsListCopy = new ArrayList<>();
        if (friendshipsList != null) {
            friendshipsListCopy.addAll(friendshipsList);
            friendAdapter.setHasStableIds(true);
            friendAdapter.setItems(friendshipsList);
        }

        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewInvite.setLayoutManager(linearLayoutManager);
        recyclerViewInvite.setAdapter(friendAdapter);

        subscriptions.add(friendAdapter.clickFriendItem()
                .subscribe(friendView -> {
                    Friendship friendship = friendAdapter.getItemAtPosition((Integer) friendView.getTag(R.id.tag_position));
                    String friendId = friendship.getFriend().getId();
                    if ((Boolean) friendView.getTag(R.id.tag_selected)) {
                        memberIds.add(friendId);
                    } else {
                        memberIds.remove(friendId);
                    }
                }));
        friendAdapter.notifyDataSetChanged();
    }

    private void initSearchView() {
        setupSearchView();
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

    private void setupSearchView() {
        subscriptions.add(RxView.focusChanges(editTextInviteSearch).subscribe(aBoolean -> {
            if (aBoolean) appBarLayout.setExpanded(false);
        }));
        subscriptions.add(RxView.clicks(editTextInviteSearch).subscribe(aVoid -> {
            appBarLayout.setExpanded(false);
        }));
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
        RecyclerView recyclerViewCameraType = (RecyclerView) view.findViewById(R.id.recyclerViewCameraType);
        recyclerViewCameraType.setHasFixedSize(true);
        recyclerViewCameraType.setLayoutManager(new LinearLayoutManager(getActivity()));
        cameraTypeAdapter = new LabelSheetAdapter(getContext(), items);
        cameraTypeAdapter.setHasStableIds(true);
        recyclerViewCameraType.setAdapter(cameraTypeAdapter);
        subscriptions.add(cameraTypeAdapter.clickLabelItem()
                .map((View labelView) -> cameraTypeAdapter.getItemAtPosition((Integer) labelView.getTag(R.id.tag_position)))
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
    public void createGroup(String groupName, List<String> memberIds, boolean isPrivate, String pictureUri) {

    }

    @Override
    public void backToHome() {
        navigator.navigateToHome(getActivity(), false);
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
