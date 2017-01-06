package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.f2prateek.rx.preferences.Preference;
import com.facebook.AccessToken;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.tribe.app.BuildConfig;
import com.tribe.app.R;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.components.UserComponent;
import com.tribe.app.presentation.internal.di.scope.HasComponent;
import com.tribe.app.presentation.mvp.presenter.HomeGridPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.utils.EmojiParser;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManagerConstants;
import com.tribe.app.presentation.utils.preferences.AddressBook;
import com.tribe.app.presentation.utils.preferences.LastOnlineNotification;
import com.tribe.app.presentation.utils.preferences.LastSync;
import com.tribe.app.presentation.utils.preferences.LastVersionCode;
import com.tribe.app.presentation.view.adapter.HomeGridAdapter;
import com.tribe.app.presentation.view.adapter.manager.HomeLayoutManager;
import com.tribe.app.presentation.view.component.TopBarContainer;
import com.tribe.app.presentation.view.tutorial.Tutorial;
import com.tribe.app.presentation.view.tutorial.TutorialManager;
import com.tribe.app.presentation.view.utils.Constants;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.SoundManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class HomeActivity extends BaseActivity implements HasComponent<UserComponent>, HomeGridMVPView, GoogleApiClient.OnConnectionFailedListener {

    private static final long TWENTY_FOUR_HOURS = 86400000;

    public static final int SETTINGS_RESULT = 101;

    public static Intent getCallingIntent(Context context) {
        return new Intent(context, HomeActivity.class);
    }

    @Inject
    HomeGridPresenter homeGridPresenter;

    @Inject
    HomeGridAdapter homeGridAdapter;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    PaletteGrid paletteGrid;

    @Inject
    TutorialManager tutorialManager;

    @Inject
    @LastOnlineNotification
    Preference<Long> lastOnlineNotification;

    @Inject
    @AddressBook
    Preference<Boolean> addressBook;

    @Inject
    @LastVersionCode
    Preference<Integer> lastVersion;

    @Inject
    @LastSync
    Preference<Long> lastSync;

    @Inject
    SoundManager soundManager;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.topBarContainer)
    TopBarContainer topBarContainer;

    // OBSERVABLES
    private UserComponent userComponent;
    private CompositeSubscription subscriptions = new CompositeSubscription();

    // VARIABLES
    private HomeLayoutManager layoutManager;
    private Tutorial tutorial;
    private List<Recipient> latestRecipientList;
    private boolean shouldOverridePendingTransactions = false;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    // DIMEN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.black);

        super.onCreate(savedInstanceState);

        initDependencyInjector();
        init();
        initUi();
        initDimensions();
        initRegistrationToken();
        initRecyclerView();
        initPullToRefresh();
        initRemoteConfig();
        manageDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        manageDeepLink(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        tagManager.onStart(this);
        homeGridPresenter.onViewAttached(this);

        if (System.currentTimeMillis() - lastSync.get() > TWENTY_FOUR_HOURS) {
            syncContacts();
        }
    }

    @Override
    protected void onStop() {
        tagManager.onStop(this);
        homeGridPresenter.onViewDetached();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (shouldOverridePendingTransactions) {
            overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
            shouldOverridePendingTransactions = false;
        }

        //viewFilter.updateSync();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        firebaseRemoteConfig.fetch(BuildConfig.DEBUG ? 1 : 3600)
            .addOnSuccessListener(aVoid -> {
                firebaseRemoteConfig.activateFetched();
                sendOnlineNotification();
            })
            .addOnFailureListener(exception -> Log.d("Tribe", "Fetch failed"));
    }

    @Override
    protected void onPause() {
        cleanTutorial();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        recyclerViewFriends.setAdapter(null);

        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();

        super.onDestroy();
    }

    private void init() {
        latestRecipientList = new ArrayList<>();
    }

    private void initUi() {
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
    }

    private void initDimensions() {
    }

    private void initRecyclerView() {
        this.layoutManager = new HomeLayoutManager(context());
        this.recyclerViewFriends.setLayoutManager(layoutManager);
        this.recyclerViewFriends.setItemAnimator(null);
        homeGridAdapter.setItems(new ArrayList<>());
        this.recyclerViewFriends.setAdapter(homeGridAdapter);

        // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
        this.recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

        this.layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(homeGridAdapter.getItemViewType(position)) {
                    case HomeGridAdapter.EMPTY_HEADER_VIEW_TYPE:
                        return layoutManager.getSpanCount();
                    default:
                        return 1;
                }
            }
        });

        subscriptions.add(homeGridAdapter.onClickMore()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .flatMap(recipient -> {
                            if (recipient instanceof Membership) {
                                Membership membership = (Membership) recipient;
                                navigator.navigateToGroupDetails(this, membership);
                                return Observable.empty();
                            } else {
                                return DialogFactory.showBottomSheetForRecipient(this, recipient);
                            }
                        },
                        ((recipient, labelType) -> {
                            if (labelType != null) {
                                if (labelType.getTypeDef().equals(LabelType.HIDE) || labelType.getTypeDef().equals(LabelType.BLOCK_HIDE)) {
                                    tagManager.trackEvent(TagManagerConstants.USER_TILE_HIDDEN);
                                    homeGridPresenter.updateFriendship((Friendship) recipient, labelType.getTypeDef().equals(LabelType.BLOCK_HIDE) ? FriendshipRealm.BLOCKED : FriendshipRealm.HIDDEN);
                                } else if (labelType.getTypeDef().equals(LabelType.GROUP_INFO)) {
                                    Membership membership = (Membership) recipient;
                                    navigator.navigateToGroupDetails(this, membership);
                                } else if (labelType.getTypeDef().equals(LabelType.GROUP_LEAVE)) {
                                    homeGridPresenter.leaveGroup(recipient.getId());
                                }
                            }

                            return recipient;
                        }))
                .subscribe()
        );

        subscriptions.add(homeGridAdapter.onClick()
                .map(view -> homeGridAdapter.getItemAtPosition(recyclerViewFriends.getChildLayoutPosition(view)))
                .subscribe(recipient -> {
                    navigator.navigateToLiveTest(this, recipient);
                })
        );
    }

    private void initDependencyInjector() {
        this.userComponent = DaggerUserComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build();

        DaggerUserComponent.builder()
                .activityModule(getActivityModule())
                .applicationComponent(getApplicationComponent())
                .build().inject(this);
    }

    private void initPullToRefresh() {
        subscriptions.add(topBarContainer.onClickSettings()
                .subscribe(aVoid -> {
                    navigateToSettings();
                }));

        subscriptions.add(topBarContainer.onClickNew()
                .flatMap(aVoid -> DialogFactory.showBottomSheetForInvites(this),
                        ((aVoid, labelType) -> {
                            if (labelType.getTypeDef().equals(LabelType.SEARCH)) {
                                navigateToSearch(null);
                            } else if (labelType.getTypeDef().equals(LabelType.INVITE_SMS)) {
                                navigateToInvitesSMS();
                            } else if (labelType.getTypeDef().equals(LabelType.INVITE_WHATSAPP)) {
                                navigateToInvitesWhatsapp();
                            } else if (labelType.getTypeDef().equals(LabelType.INVITE_MESSENGER)) {
                                navigateToInvitesMessenger();
                            }

                            return null;
                        }))
                .subscribe()
        );
    }

    private void initRemoteConfig() {
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        firebaseRemoteConfig.setConfigSettings(configSettings);
        firebaseRemoteConfig.setDefaults(R.xml.firebase_default_config);
    }

    @Override
    public void onDeepLink(String url) {
        if (!StringUtils.isEmpty(url)) {
            Uri uri = Uri.parse(url);

            if (uri != null && !StringUtils.isEmpty(uri.getPath())) {
                if (uri.getPath().startsWith("/u/")) {
                    navigateToSearch(StringUtils.getLastBitFromUrl(url));
                } else if (uri.getPath().startsWith("/g/")) {
                    homeGridPresenter.createMembership(StringUtils.getLastBitFromUrl(url));
                }
            }
        }
    }

    @Override
    public void renderRecipientList(List<Recipient> recipientList) {
        if (recipientList != null && tutorial == null) {
            Bundle bundle = new Bundle();
            bundle.putInt(TagManagerConstants.COUNT_FRIENDS, getCurrentUser().getFriendships().size());
            bundle.putInt(TagManagerConstants.COUNT_GROUPS, getCurrentUser().getFriendships().size());
            tagManager.setProperty(bundle);

            homeGridAdapter.setItems(recipientList);
        }
    }

    @Override
    public void refreshGrid() {

    }

    @Override
    public void onFriendshipUpdated(Friendship friendship) {

    }

    @Override
    public void successFacebookLogin() {
        homeGridPresenter.updateUserFacebook(AccessToken.getCurrentAccessToken().getUserId());
        syncContacts();
    }

    @Override
    public void errorFacebookLogin() {
    }

    @Override
    public void onMembershipCreated(Membership membership) {

    }

    private void initRegistrationToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        if (token != null) homeGridPresenter.sendToken(token);
    }

    private void manageDeepLink(Intent intent) {
        if (intent != null) {
            if (intent.getData() != null) {
                homeGridPresenter.getHeadDeepLink(intent.getDataString());
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (!topBarContainer.isSearchMode()) {
            // This is important : Hack to open a dummy activity for 200-500ms (cannot be noticed by user as it is for 500ms
            // and transparent floating activity and auto finishes)
            startActivity(new Intent(this, DummyActivity.class));
            finish();
        } else {
            topBarContainer.closeSearch();
        }
    }

    @Override
    public UserComponent getComponent() {
        return userComponent;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {
    }

    @Override
    public void showError(String message) {
    }

    @Override
    public Context context() {
        return this;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w("TRIBE", "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services Error: " + connectionResult.getErrorCode(),
                Toast.LENGTH_SHORT).show();
    }

    private void cleanTutorial() {
        if (tutorial != null) {
            tutorial.cleanUp();
            tutorial = null;
        }
    }

    private void navigateToSettings() {
        navigator.navigateToSettings(HomeActivity.this, SETTINGS_RESULT);
    }

    private void navigateToSearch(String username) {
        navigator.navigateToSearchUser(HomeActivity.this, username);
    }

    private void navigateToInvitesSMS() {
        shouldOverridePendingTransactions = true;
        HomeActivity.this.navigator.openSms(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), this);
    }

    private void navigateToInvitesMessenger() {
        shouldOverridePendingTransactions = true;
        HomeActivity.this.navigator.openFacebookMessenger(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), this);
    }

    private void navigateToInvitesWhatsapp() {
        shouldOverridePendingTransactions = true;
        HomeActivity.this.navigator.openWhatsApp(EmojiParser.demojizedText(getString(R.string.share_add_friends_handle)), this);
    }

    private void navigateToCreateGroup() {
        HomeActivity.this.navigator.navigateToCreateGroup(this);
    }

    private void sendOnlineNotification() {
        boolean canSendOnlineNotification = !firebaseRemoteConfig.getBoolean(Constants.FIREBASE_DISABLE_ONLINE_NOTIFICATIONS);

        int onlineNotificationIntervalMs = Integer.valueOf(firebaseRemoteConfig.getString(Constants.FIREBASE_DELAY_ONLINE_NOTIFICATIONS)) * 60 * 1000;

        if (canSendOnlineNotification && (System.currentTimeMillis() - lastOnlineNotification.get()) > onlineNotificationIntervalMs) {
            //homeGridPresenter.sendOnlineNotification();
            lastOnlineNotification.set(System.currentTimeMillis());
        }
    }

    private void syncContacts() {
        homeGridPresenter.lookupContacts();
        lastSync.set(System.currentTimeMillis());
    }
}