package com.tribe.app.presentation.mvp.presenter;

import android.content.Context;
import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.RemoveNewStatusContactJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.game.GetCloudGames;
import com.tribe.app.domain.interactor.game.GetGamesData;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.user.CreateShortcut;
import com.tribe.app.domain.interactor.user.GetDiskContactInviteList;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.domain.interactor.user.GetDiskFBContactInviteList;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.SendInvitations;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.domain.interactor.user.SynchroContactList;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.adapter.delegate.contact.UserToAddAdapterDelegate;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.game.GameManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import timber.log.Timber;

public class HomePresenter implements Presenter {

  private ShortcutPresenter shortcutPresenter;

  // VIEW ATTACHED
  private HomeGridMVPView homeGridView;

  // TEMP VARIABLES
  private String feature, phone;
  private boolean shouldOpenSMS;

  // USECASES
  private JobManager jobManager;
  private GetDiskUserInfos diskUserInfosUsecase;
  private SendToken sendTokenUseCase;
  private UseCase cloudUserInfos;
  private UpdateUserFacebook updateUserFacebook;
  private RxFacebook rxFacebook;
  private SynchroContactList synchroContactList;
  private GetDiskContactOnAppList getDiskContactOnAppList;
  private GetDiskContactInviteList getDiskContactInviteList;
  private GetDiskFBContactInviteList getDiskFBContactInviteList;
  private GetGamesData getGamesData;
  private GetCloudGames getGames;
  private GameManager gameManager;

  private DeclineInvite declineInvite;
  private SendInvitations sendInvitations;
  private CreateRoom createRoom;
  private CreateShortcut createShortcut;

  // SUBSCRIBERS
  private CreateShortcutSubscriber createShortcutSubscriber;
  private RecipientListSubscriber diskRecipientListSubscriber;
  private RecipientListSubscriber cloudRecipientListSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;
  private ContactsOnAppSubscriber contactsOnAppSubscriber;
  private ContactListInviteSubscriber contactListInviteSubscriber;
  private FBContactListInviteSubscriber fbContactListInviteSubscriber;

  @Inject
  public HomePresenter(Context context, ShortcutPresenter shortcutPresenter, JobManager jobManager,
      @Named("diskUserInfos") GetDiskUserInfos diskUserInfos,
      @Named("sendToken") SendToken sendToken, @Named("cloudUserInfos") UseCase cloudUserInfos,
      UpdateUserFacebook updateUserFacebook, RxFacebook rxFacebook,
      SynchroContactList synchroContactList, GetDiskContactOnAppList getDiskContactOnAppList,
      GetDiskContactInviteList getDiskContactInviteList, DeclineInvite declineInvite,
      SendInvitations sendInvitations, CreateRoom createRoom,
      GetDiskFBContactInviteList getDiskFBContactInviteList, GetGamesData getGamesData,
      GetCloudGames getGames, CreateShortcut createShortcut) {

    this.shortcutPresenter = shortcutPresenter;
    this.jobManager = jobManager;
    this.diskUserInfosUsecase = diskUserInfos;
    this.sendTokenUseCase = sendToken;
    this.cloudUserInfos = cloudUserInfos;
    this.updateUserFacebook = updateUserFacebook;
    this.rxFacebook = rxFacebook;
    this.synchroContactList = synchroContactList;
    this.getDiskContactOnAppList = getDiskContactOnAppList;
    this.getDiskContactInviteList = getDiskContactInviteList;
    this.declineInvite = declineInvite;
    this.sendInvitations = sendInvitations;
    this.createRoom = createRoom;
    this.getDiskFBContactInviteList = getDiskFBContactInviteList;
    this.getGamesData = getGamesData;
    this.getGames = getGames;
    this.gameManager = GameManager.getInstance(context);
    this.createShortcut = createShortcut;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    cloudUserInfos.unsubscribe();
    updateUserFacebook.unsubscribe();
    diskUserInfosUsecase.unsubscribe();
    synchroContactList.unsubscribe();
    getDiskContactOnAppList.unsubscribe();
    getDiskContactInviteList.unsubscribe();
    getDiskFBContactInviteList.unsubscribe();
    declineInvite.unsubscribe();
    sendInvitations.unsubscribe();
    createRoom.unsubscribe();
    getGamesData.unsubscribe();
    getGames.unsubscribe();
    createShortcut.unsubscribe();
    homeGridView = null;
  }

  @Override public void onViewAttached(MVPView v) {
    homeGridView = (HomeGridMVPView) v;
    shortcutPresenter.onViewAttached(v);
  }

  public void reload(boolean sync) {
    showViewLoading();
    loadFriendList();
    loadContactsOnApp();
    loadContactsInvite();
    loadFBContactsInvite();
    if (!sync) syncFriendList();
  }

  public void loadFriendList() {
    if (diskRecipientListSubscriber != null) {
      diskRecipientListSubscriber.unsubscribe();
    }

    diskRecipientListSubscriber = new RecipientListSubscriber(false);
    diskUserInfosUsecase.execute(diskRecipientListSubscriber);
  }

  public void syncFriendList() {
    if (cloudRecipientListSubscriber != null) {
      cloudRecipientListSubscriber.unsubscribe();
    }

    cloudRecipientListSubscriber = new RecipientListSubscriber(true);
    cloudUserInfos.execute(cloudRecipientListSubscriber);
  }

  private void showRecipients(List<Recipient> recipientList) {
    this.homeGridView.renderRecipientList(recipientList);
  }

  public void sendToken(String token) {
    sendTokenUseCase.setToken(token);
    sendTokenUseCase.execute(new SendTokenSubscriber());
  }

  public void createRoom(String feature, String phone, boolean shouldOpenSMS) {
    this.feature = feature;
    this.phone = phone;
    this.shouldOpenSMS = shouldOpenSMS;
    createRoom.execute(new CreateRoomSubscriber());
  }

  protected void showViewLoading() {
    homeGridView.showLoading();
  }

  protected void hideViewLoading() {
    homeGridView.hideLoading();
  }

  protected void showErrorMessage(ErrorBundle errorBundle) {
    if (homeGridView != null && homeGridView.context() != null) {
      String errorMessage =
          ErrorMessageFactory.create(homeGridView.context(), errorBundle.getException());
      homeGridView.showError(errorMessage);
    }
  }

  private final class RecipientListSubscriber extends DefaultSubscriber<User> {

    private boolean cloud = false;

    public RecipientListSubscriber(boolean cloud) {
      this.cloud = cloud;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      if (cloud) showErrorMessage(new DefaultErrorBundle((Exception) e));
    }

    @Override public void onNext(User user) {
      if (!cloud) {
        if (user.getRandom_banned_until() != null || user.isRandom_banned_permanently()) {
          homeGridView.onBannedUser(user);
        }
        try {
          List<Recipient> recipientList = user.getRecipientList();
          showRecipients(recipientList);
        } catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
  }

  private final class SendTokenSubscriber extends DefaultSubscriber<Installation> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(Installation installation) {
    }
  }

  public void updateUserFacebook(String userId, String accessToken) {

    updateUserFacebook.prepare(userId, accessToken);
    updateUserFacebook.execute(new DefaultSubscriber() {
      @Override public void onError(Throwable e) {
        super.onError(e);
        System.out.println("ON ERROR" + e.getMessage());
      }

      @Override public void onNext(Object o) {
        super.onNext(o);
        System.out.println("ON NEXT");
      }
    });
  }

  public void loginFacebook() {
    if (!FacebookUtils.isLoggedIn()) {
      rxFacebook.requestLogin().subscribe(loginResult -> {
        if (FacebookUtils.isLoggedIn()) {
          homeGridView.successFacebookLogin();
        } else {
          homeGridView.errorFacebookLogin();
        }
      });
    } else {
      homeGridView.successFacebookLogin();
    }
  }

  public void lookupContacts() {
    if (lookupContactsSubscriber != null) lookupContactsSubscriber.unsubscribe();
    lookupContactsSubscriber = new LookupContactsSubscriber();
    synchroContactList.execute(lookupContactsSubscriber);
    if (homeGridView != null) homeGridView.onSyncStart();
  }

  private class LookupContactsSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
      Timber.e("on error lookup contact " + e.getMessage());
      homeGridView.onSyncError();
    }

    @Override public void onNext(List<Contact> contactList) {
      if (homeGridView != null) homeGridView.onSyncDone();
    }
  }

  public void removeNewStatusContact() {
    jobManager.addJobInBackground(new RemoveNewStatusContactJob());
  }

  public void loadContactsOnApp() {
    if (contactsOnAppSubscriber != null) {
      contactsOnAppSubscriber.unsubscribe();
      getDiskContactOnAppList.unsubscribe();
    }

    contactsOnAppSubscriber = new ContactsOnAppSubscriber();
    getDiskContactOnAppList.execute(contactsOnAppSubscriber);
  }

  private final class ContactsOnAppSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(List<Contact> contactList) {
      homeGridView.renderContactsOnApp(contactList);
    }
  }

  public void loadContactsInvite() {
    if (contactListInviteSubscriber != null) {
      contactListInviteSubscriber.unsubscribe();
    }

    contactListInviteSubscriber = new ContactListInviteSubscriber();
    getDiskContactInviteList.execute(contactListInviteSubscriber);
  }

  private final class ContactListInviteSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Contact> contactList) {
      homeGridView.renderContactsInvite(contactList);
    }
  }

  public void loadFBContactsInvite() {
    if (fbContactListInviteSubscriber != null) {
      fbContactListInviteSubscriber.unsubscribe();
    }

    fbContactListInviteSubscriber = new FBContactListInviteSubscriber();
    getDiskFBContactInviteList.execute(fbContactListInviteSubscriber);
  }

  private final class FBContactListInviteSubscriber extends DefaultSubscriber<List<Contact>> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {

    }

    @Override public void onNext(List<Contact> contactList) {
      homeGridView.renderContactsFBInvite(contactList);
    }
  }

  public void declineInvite(String roomId) {
    declineInvite.setup(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }

  public void sendInvitations() {
    sendInvitations.execute(new DefaultSubscriber());
  }

  private class CreateRoomSubscriber extends DefaultSubscriber<Room> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      e.printStackTrace();
    }

    @Override public void onNext(Room room) {
      homeGridView.onCreateRoom(room, feature, phone, shouldOpenSMS);
    }
  }

  public void getGames() {
    getGames.execute(new DefaultSubscriber<List<Game>>() {
      @Override public void onNext(List<Game> gameList) {
        gameManager.addGames(gameList);
      }
    });
  }

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  private class CreateShortcutSubscriber extends DefaultSubscriber<Shortcut> {
    UserToAddAdapterDelegate.UserToAddViewHolder vh;

    public CreateShortcutSubscriber(UserToAddAdapterDelegate.UserToAddViewHolder vh) {
      this.vh = vh;
    }

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      Timber.e(e.toString());
    }

    @Override public void onNext(Shortcut shortcut) {
      if (homeGridView != null) {
        homeGridView.onShortcutCreatedFromSuggestedFriendSuccess(shortcut, vh);
      }
    }
  }

  public void createShortcutFromSuggestedFriend(UserToAddAdapterDelegate.UserToAddViewHolder vh,
      String... userIds) {
    if (createShortcutSubscriber != null) createShortcutSubscriber.unsubscribe();
    createShortcutSubscriber = new CreateShortcutSubscriber(vh);
    if (ShortcutUtil.isNotSupport(userIds)) {
      createShortcut.setup(userIds);
      createShortcut.execute(createShortcutSubscriber);
    }
  }

  public void muteShortcut(String shortcutId, boolean mute) {
    shortcutPresenter.muteShortcut(shortcutId, mute);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status, null);
  }

  public void updateShortcutName(String shortcutId, String name) {
    shortcutPresenter.updateShortcutName(shortcutId, name);
  }

  public void updateShortcutPicture(String shortcutId, String imageUri) {
    shortcutPresenter.updateShortcutPicture(shortcutId, imageUri);
  }

  public void updateShortcutLeaveOnlineUntil(String shortcutId) {
    Calendar date = Calendar.getInstance();
    long t = date.getTimeInMillis();
    Date finalDate = new Date(t + 60 * 1000);
    shortcutPresenter.leaveOnline(shortcutId, finalDate.getTime());
  }

  public void readShortcut(String shortcutId) {
    shortcutPresenter.readShortcut(shortcutId);
  }

  public void pinShortcut(String shortcutId, boolean pinned) {
    shortcutPresenter.pinShortcut(shortcutId, pinned);
  }

  public void removeShortcut(String shortcutId) {
    shortcutPresenter.removeShortcut(shortcutId);
  }
}
