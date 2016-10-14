package com.tribe.app.presentation.view.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
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
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.activity.GroupInfoActivity;
import com.tribe.app.presentation.view.activity.HomeActivity;
import com.tribe.app.presentation.view.adapter.FriendAdapter;
import com.tribe.app.presentation.view.adapter.LabelSheetAdapter;
import com.tribe.app.presentation.view.component.CreateInviteView;
import com.tribe.app.presentation.view.component.GroupInfoView;
import com.tribe.app.presentation.view.component.GroupSuggestionsView;
import com.tribe.app.presentation.view.component.SearchFriendsView;
import com.tribe.app.presentation.view.dialog_fragment.ShareDialogFragment;
import com.tribe.app.presentation.view.utils.AnimationUtils;
import com.tribe.app.presentation.view.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.Date;
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
    private FriendAdapter friendAdapter;

    // Bind view
    @BindView(R.id.imageDone)
    ImageView imageDone;
    @BindView(R.id.circularProgressViewDone)
    CircularProgressView circularProgressView;
    @BindView(R.id.layoutInvite)
    NestedScrollView layoutInvite;
    @BindView(R.id.recyclerViewInvite)
    RecyclerView recyclerViewInvite;
    @BindView(R.id.appBarLayout)
    AppBarLayout appBarLayout;
    @BindView(R.id.main_collapsing)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.createInviteView)
    CreateInviteView createInviteView;
    @BindView(R.id.groupInfoView)
    GroupInfoView groupInfoView;
    @BindView(R.id.searchFriendsView)
    SearchFriendsView searchFriendsView;
    @BindView(R.id.groupSuggestionsView)
    GroupSuggestionsView groupSuggestionsView;

    // Dagger Dependencies
    @Inject
    ScreenUtils screenUtils;

    @Inject
    GroupPresenter groupPresenter;

    @Inject
    RxImagePicker rxImagePicker;

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
    private String membershipId = null;
    private String groupName = null;
    private String groupLink = null;
    private boolean isCurrentUserAdmin = false;
    private long groupLinkExpirationDate;
    private List<String> memberIds = new ArrayList<>();
    private String groupPictureUri = null;
    private boolean privateGroup = true;
    private List<String> groupMemberIds = new ArrayList<>();
    ArrayList<GroupMember> groupMemberList;

    // Animation Variables
    private int moveUpY = 138;
    private int currentEditTranslation;
    private int appBarLayoutTranslation = 130;
    private int layoutCreateInviteInfoPositionY = 255;
    private int startTranslationDoneIcon = 200;
    private int animDuration = AnimationUtils.ANIMATION_DURATION_MID;
    private int smallMargin = 5;
    private int orignalGroupSuggestionsMargin;
    private boolean friendAdapterClickable = true;


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
        initPresenter();

        Bundle bundle = getArguments();
        if (bundle == null) {
            initUi();
            initFriendshipListExcluding(new ArrayList<>());
        }
        else {
            initGroupInfoUi(bundle.getString("membershipId"), bundle.getBoolean("isCurrentUserAdmin"), bundle.getString("groupId"), bundle.getString("groupName"), bundle.getString("groupPicture"), bundle.getString("privateGroupLink"), bundle.getLong("privateGroupLinkExpiresAt"));
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
        initForBoth();
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

        if  (groupPresenter != null) groupPresenter.onDestroy();

        super.onDestroy();
    }

    public ArrayList<GroupMember> getGroupMemberList() {
        return groupMemberList;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void setMembershipId(String membershipId) {
        this.membershipId = membershipId;
    }

    @Override
    public void setGroupLink(String groupLink) {
        this.groupLink = groupLink;
        createInviteView.setInviteLink(groupLink);
        createInviteView.enableInvite();
        createInviteView.loaded();

    }

    @Override
    public void setGroupLinkExpirationDate(Date groupLinkExpirationDate) {
        this.groupLinkExpirationDate = groupLinkExpirationDate.getTime();
        if (groupLink != null) createInviteView.setExpirationDesc(timeRemaining(this.groupLinkExpirationDate));
        if (privateGroup) showShareDialogFragment();
    }

    private boolean isLinkExpired(long groupLinkExpirationDate) {
        return timeRemaining(groupLinkExpirationDate) <= 0;
    }

    private long timeRemaining(long groupLinkExpirationDate) {
        Date now = new Date();
        return groupLinkExpirationDate - now.getTime();
    }

    /**
     * Setup UI
     * Includes subscription setup
     */

    private void initUi() {
        groupInfoView.setBringGroupNameToTopEnabled(true);
        currentEditTranslation = 25;
        groupInfoView.setUpInitialUi();
        imageDone.setTranslationY(screenUtils.dpToPx(startTranslationDoneIcon));

        createInviteView.disable();
        enableScrolling(false);
        layoutInvite.setTranslationY(screenUtils.dpToPx(moveUpY));
        layoutInvite.setVisibility(View.INVISIBLE);
        recyclerViewInvite.setTranslationY(screenUtils.dpToPx(moveUpY));
        recyclerViewInvite.setVisibility(View.INVISIBLE);

        subscriptions.add(groupInfoView.isPrivate().subscribe(isPrivate -> {
            privateGroup = isPrivate;
            if (privateGroup) groupSuggestionsView.setPrivate();
            else groupSuggestionsView.setPublic();
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
            setGroupSuggestionsViewVisible(false);
            groupInfoView.bringGroupNameDown(animDuration);
            createInviteView.disable();
            groupInfoView.disableButtons();
            groupName = groupInfoView.getGroupName();
            groupPresenter.createGroup(groupName, memberIds, privateGroup, groupPictureUri);
            createInviteView.loadingAnimation(CreateInviteView.STATUS_CREATING_GROUP, AnimationUtils.ANIMATION_DURATION_EXTRA_SHORT, screenUtils, getActivity());
        }));

    }

    private void initGroupInfoUi(String membershipId, boolean isCurrentUserAdmin, String groupId, String groupName, String groupPicture, String groupLink, long groupLinkExpirationDate) {
        this.membershipId = membershipId;
        this.groupId = groupId;
        this.groupLink = groupLink;
        this.isCurrentUserAdmin = isCurrentUserAdmin;
        groupInfoView.setBringGroupNameToTopEnabled(false);
        if  (!isCurrentUserAdmin) groupInfoView.bringOutIcons(0);
        this.groupLinkExpirationDate = groupLinkExpirationDate;
        createInviteView.disableCreate();
        createInviteView.setInvite(privateGroup);
        if (!privateGroup) groupSuggestionsView.setPublic();
        if (groupLink != null && !isLinkExpired(groupLinkExpirationDate)) {
            createInviteView.setInviteLink(groupLink);
            createInviteView.setExpirationDesc(timeRemaining(groupLinkExpirationDate));
        }
        groupInfoView.setGroupName(groupName);
        if (groupPicture != null) groupInfoView.setGroupPictureFromUrl(groupPicture);
        currentEditTranslation = 20;
        screenUtils.setTopMargin(createInviteView, screenUtils.dpToPx(layoutCreateInviteInfoPositionY));

        createInviteView.enableInvite();
        groupInfoView.setupGroupInfoUi(privateGroup, 1);
        imageDone.setVisibility(View.VISIBLE);
        appBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                AnimationUtils.animateHeightCoordinatorLayout(appBarLayout, appBarLayout.getHeight(), appBarLayout.getHeight() + screenUtils.dpToPx(smallMargin), 0);
                appBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        subscriptions.add(groupInfoView.imageBackClicked().subscribe(aVoid -> {
            Intent resultIntent = new Intent();
            getActivity().setResult(BaseActivity.RESULT_OK, resultIntent);
            getActivity().finish();
        }));

        subscriptions.add(groupInfoView.imageGoToMembersClicked().subscribe(aVoid -> {
            imageGoToMembersClicked.onNext(null);
        }));
        groupPresenter.getGroupMembers(groupId);
        appBarLayout.setExpanded(true);
    }

    public void initForBoth() {
        subscriptions.add(RxView.clicks(imageDone).subscribe(aVoid -> {
            groupPresenter.addMembersToGroup(groupId, memberIds);
            imageDone.setEnabled(false);
            circularProgressView.setVisibility(View.VISIBLE);
        }));
        subscriptions.add(groupInfoView.imageGroupClicked().subscribe(aVoid -> {
            setupBottomSheetCamera();
        }));
        subscriptions.add(groupInfoView.imageEditGroupClicked().subscribe(aVoid -> {
            presentEdit(currentEditTranslation);
        }));
        subscriptions.add(groupInfoView.imageDoneEditClicked().subscribe(aVoid -> {
            groupName = groupInfoView.getGroupName();
            groupPresenter.updateGroup(groupId, groupName, groupPictureUri);
            groupInfoView.setLoading(true);
        }));
        subscriptions.add(groupInfoView.isEditingGroupName().subscribe(this::setGroupSuggestionsViewVisible));
        subscriptions.add(groupSuggestionsView.groupSuggestionClicked().subscribe(suggestionName -> {
            groupInfoView.setGroupName(suggestionName);
            groupInfoView.setCursorEndGroupName();
            groupInfoView.bringGroupNameDown(animDuration);
            setGroupSuggestionsViewVisible(false);
        }));
        subscriptions.add((createInviteView.invitePressed()).subscribe(aVoid -> {
            tagManager.trackEvent(TagManagerConstants.KPI_GROUP_LINK_SHARED);

            if (privateGroup && groupLink != null && !isLinkExpired(groupLinkExpirationDate)) showShareDialogFragment();
            else if (privateGroup) {
                groupPresenter.modifyPrivateGroupLink(membershipId, true);
                createInviteView.disableInvite();
                createInviteView.loadingAnimation(CreateInviteView.STATUS_CREATING_LINK, AnimationUtils.ANIMATION_DURATION_EXTRA_SHORT, screenUtils, getActivity());
            }
            else navigator.shareGenericText(getString(R.string.share_group_public_link, groupName, groupLink), getContext());
        }));

        subscriptions.add(groupInfoView.hideKeyboard().subscribe(aVoid -> {
            screenUtils.hideKeyboard(getActivity());
        }));

        subscriptions.add(searchFriendsView.editTextSearchTextChanged().subscribe(this::filter));
        setupSearchView();
    }

    @Override
    public void setupGroup(Group group) {
        groupName = group.getName();
        privateGroup = group.isPrivateGroup();
        if (group.getGroupLink() != null) groupLink = group.getGroupLink();
        groupInfoView.setGroupName(groupName);
        if (group.getPicture() != null && !group.getPicture().isEmpty()) groupInfoView.setGroupPictureFromUrl(group.getPicture());
        members = group.getMembers();
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
                    groupMember.setFriendshipId(friendsList.get(i).getId());
                }
            }
            for (User admin : admins) {
                if (admin.getId().equals(member.getId())) groupMember.setAdmin(true);
            }
            groupMemberList.add(groupMember);
        }
        addMemberPhotos(groupMemberList);
        initFriendshipListExcluding(groupMemberList);
        friendAdapter.setItems(friendshipsList);
        friendAdapter.notifyDataSetChanged();
        if (!privateGroup)circularProgressView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
        setGroupPrivacy(privateGroup, groupMemberList.size());
    }

    public void addMemberPhotos(List<GroupMember> groupMemberList) {
        int memberPhotos;
        if (groupMemberList.size() < 5) memberPhotos = groupMemberList.size();
        else memberPhotos = 5;
        for (int i = 0; i < memberPhotos; i++) {
            String profPic = groupMemberList.get(i).getProfilePicture();
            if (profPic != null) groupInfoView.addMemberPhoto(profPic);
            if (profPic.equals(getString(R.string.no_profile_picture_url))) groupInfoView.addMemberPhotoDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_placeholder_avatar));
        }
    }

    public void setGroupMemberList(ArrayList<GroupMember> groupMemberList) {
        this.groupMemberList = groupMemberList;
        friendshipsList.clear();
        friendshipsListCopy.clear();
        initFriendshipListExcluding(groupMemberList);
        groupInfoView.clearMemberPhotos();
        addMemberPhotos(groupMemberList);
    }

    /**
     * Modify layout methods
     */

    private void setGroupPrivacy(boolean isPrivate, int memberCount) {
        groupInfoView.setPrivacy(isPrivate, memberCount);
        createInviteView.switchColors(isPrivate);
        if (!isPrivate) imageDone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_done_purple));
    }

    /**
     * Methods to switch between edit and non-edit modes
     */

    private void backFromEdit(int translation) {
        groupInfoView.setBringGroupNameToTopEnabled(false);
        if (translation > 20) groupInfoView.collapse(animDuration, getActivity());
        else groupInfoView.collapseInfo(animDuration, getActivity());

        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() - screenUtils.dpToPx(translation),
                animDuration);

        createInviteView.animate()
                .setDuration(animDuration)
                .y(-screenUtils.dpToPx(translation) + createInviteView.getY())
                .start();
    }

    private void presentEdit(int translation) {
        groupInfoView.setBringGroupNameToTopEnabled(true);
        if (translation > 20) groupInfoView.expand(animDuration);
        else groupInfoView.expandInfo(animDuration);

        appBarLayout.bringToFront();
        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() + screenUtils.dpToPx(translation),
                animDuration);

        createInviteView.animate()
                .setDuration(animDuration)
                .y(screenUtils.dpToPx(translation) + createInviteView.getY())
                .start();
    }

    /**
     * Animations performed after step 1
     */
    private void animSet1() {
        groupInfoView.collapsePrivatePublic(privateGroup, animDuration, 1);
        groupInfoView.collapse(animDuration, getActivity());
        createInviteView.disable();
        createInviteView.setInvite(privateGroup);
        ((HomeActivity) getActivity()).disableNavigation();
    }

    private void resetCreateGroupView() {
        groupInfoView.expand(0);
        groupInfoView.setBringGroupNameToTopEnabled(true);
        groupInfoView.setGroupName("");
        groupInfoView.bringOutIcons(0);
        groupInfoView.enableButtons();
        groupInfoView.setGroupPictureInvisible();
        groupPictureUri = null;
        createInviteView.setDefault();
        createInviteView.disable();
        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout, appBarLayout.getHeight(), appBarLayout.getHeight() + screenUtils.dpToPx(appBarLayoutTranslation), 0);
        groupInfoView.resetPrivatePublic();
        imageDone.setEnabled(true);
        createInviteView.setTranslationY(AnimationUtils.TRANSLATION_RESET);
        layoutInvite.setVisibility(View.INVISIBLE);
        recyclerViewInvite.setVisibility(View.INVISIBLE);
        imageDone.setTranslationY(screenUtils.dpToPx(startTranslationDoneIcon));
        enableScrolling(false);
        circularProgressView.setVisibility(View.INVISIBLE);
    }

    private void animSet2() {
        AnimationUtils.animateHeightCoordinatorLayout(appBarLayout,
                appBarLayout.getHeight(), appBarLayout.getHeight() - screenUtils.dpToPx(appBarLayoutTranslation),
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

        if (!privateGroup) {
            imageDone.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.picto_done_purple));
            circularProgressView.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_group_public));
        }

        imageDone.animate()
                .translationY(AnimationUtils.TRANSLATION_RESET)
                .setDuration(animDuration)
                .start();;

        createInviteView.enableInvite();
        enableScrolling(true);
    }

    private void showShareDialogFragment() {
        ShareDialogFragment shareDialogFragment = ShareDialogFragment.newInstance();
        shareDialogFragment.setExpirationTime(groupName, groupLink, timeRemaining(groupLinkExpirationDate));
        if (groupLink != null) shareDialogFragment.show(getFragmentManager(), ShareDialogFragment.class.getName());
        subscriptions.add(shareDialogFragment.deletePressed().subscribe(aVoid -> {
            groupPresenter.modifyPrivateGroupLink(membershipId, false);
            createInviteView.disableInvite();
            createInviteView.loadingAnimation(CreateInviteView.STATUS_DELETING_LINK, AnimationUtils.ANIMATION_DURATION_EXTRA_SHORT, screenUtils, getActivity());
        }));

    }

    /**
     * Search for friend View
     */


    private void initFriendshipListExcluding(List<GroupMember> usersToExclude) {
        User user = getCurrentUser();
        friendshipsList.addAll(user.getFriendships());
        for (Iterator<Friendship> iterFriendship = friendshipsList.iterator(); iterFriendship.hasNext();) {
            Friendship friendship = iterFriendship.next();
            friendship.setSelected(false);
            final User frienshipUser = friendship.getFriend();
            for (Iterator<GroupMember> iterMember = usersToExclude.iterator(); iterMember.hasNext();) {
                if (frienshipUser.getId().equals(iterMember.next().getUserId())) iterFriendship.remove();
            }
        }
        friendAdapter = new FriendAdapter(getContext(), privateGroup);
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
                    recyclerViewInvite.setEnabled(false);
                    subscriptions.add(
                            Observable.timer(animDuration, TimeUnit.MILLISECONDS)
                                    .onBackpressureDrop()
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(time -> {
                                        recyclerViewInvite.setEnabled(true);
                                    }));
                    if (friendship.isSelected()) {
                        memberIds.add(friendId);
                        searchFriendsView.insertFriend(friendship.getId(), friendship.getProfilePicture());
                    } else {
                        memberIds.remove(friendId);
                        searchFriendsView.deleteFriend(friendship.getId());
                    }

                }));
        friendAdapter.notifyDataSetChanged();
    }

    private void setupSearchView() {
        subscriptions.add(searchFriendsView.editTextSearchClicked().subscribe(aVoid -> {
            appBarLayout.setExpanded(false);
        }));
    }

    private void setGroupSuggestionsViewVisible(boolean visible) {
        if (visible) {
            groupSuggestionsView.setVisibility(View.VISIBLE);
            orignalGroupSuggestionsMargin = (int) groupSuggestionsView.getY();
            groupSuggestionsView.animate()
                    .y(getResources().getDimension(R.dimen.group_info_bar_height))
                    .setDuration(AnimationUtils.ANIMATION_DURATION_SHORT)
                    .setStartDelay(AnimationUtils.NO_START_DELAY)
                    .start();
        } else {
            groupSuggestionsView.animate()
                    .y(orignalGroupSuggestionsMargin)
                    .setDuration(AnimationUtils.ANIMATION_DURATION_SHORT)
                    .setStartDelay(AnimationUtils.NO_START_DELAY)
                    .start();
            subscriptions.add(
                    Observable.timer(AnimationUtils.ANIMATION_DURATION_SHORT, TimeUnit.MILLISECONDS)
                            .onBackpressureDrop()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(time -> {
                                groupSuggestionsView.setVisibility(View.INVISIBLE);
                            }));
        }
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
                        subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA)
                                .subscribe(uri -> {
                                    groupInfoView.setGroupPictureFromUrl(uri.toString());
                                    this.groupPictureUri = uri.toString();
                                }));
                    } else if (cameraType.getCameraTypeDef().equals(CameraType.OPEN_PHOTOS)) {
                        subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY)
                                .subscribe(uri -> {
                                    groupInfoView.setGroupPictureFromUrl(uri.toString());
                                    this.groupPictureUri = uri.toString();
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
        dialogCamera.setOnCancelListener(dialog -> {
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

    private void enableScrolling(boolean scrollable) {
        AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();
        if (scrollable) layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL|AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        else layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP);
        collapsingToolbarLayout.setLayoutParams(layoutParams);
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
    public void groupCreatedSuccessfully() {
        Bundle bundle = new Bundle();
        bundle.putString(TagManagerConstants.TYPE_TRIBE_GROUP, privateGroup ? TagManagerConstants.TYPE_GROUP_PRIVATE : TagManagerConstants.TYPE_GROUP_PUBLIC);
        tagManager.trackEvent(TagManagerConstants.KPI_GROUP_CREATED);

        tagManager.increment(TagManagerConstants.COUNT_GROUPS_CREATED);

        createInviteView.loaded();
        animSet1();
        subscriptions.add(
                Observable.timer(animDuration, TimeUnit.MILLISECONDS)
                        .onBackpressureDrop()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(time -> {
                            animSet2();
                        }));
        subscriptions.add(
                Observable.timer(animDuration * 2, TimeUnit.MILLISECONDS)
                        .onBackpressureDrop()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(time -> {
                            animSet3();
                        }));
        subscriptions.add(
                Observable.timer(animDuration * 3, TimeUnit.MILLISECONDS)
                        .onBackpressureDrop()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(time -> {
                            groupPresenter.updateScore();
                        }));
    }

    @Override
    public void groupCreationFailed() {
        createInviteView.creationFailed(privateGroup);
        groupInfoView.enableButtons();
    }

    @Override
    public void groupUpdatedSuccessfully() {
        groupInfoView.setLoading(false);
        backFromEdit(currentEditTranslation);
    }

    @Override
    public void groupUpdatedFailed() {
        groupInfoView.setLoading(false);
    }

    @Override
    public void memberAddedSuccessfully() {
        tagManager.trackEvent(TagManagerConstants.KPI_GROUP_MEMBERS_ADDED);

        //TODO: navigate to home fragment
        if (getActivity() instanceof GroupInfoActivity) {
            Intent resultIntent = new Intent();
            getActivity().setResult(BaseActivity.RESULT_OK, resultIntent);
            getActivity().finish();
        }
        if (getActivity() instanceof  HomeActivity) {
            resetCreateGroupView();
            ((HomeActivity) getActivity()).goToHome();
        }
    }

    @Override
    public void memberAddedFailed() {
        circularProgressView.setVisibility(View.INVISIBLE);
        imageDone.setEnabled(true);
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
