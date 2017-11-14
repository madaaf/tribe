package com.tribe.app.presentation.mvp.presenter;

import com.birbit.android.jobqueue.JobManager;
import com.tribe.app.data.network.job.RemoveNewStatusContactJob;
import com.tribe.app.data.realm.Installation;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Contact;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.exception.DefaultErrorBundle;
import com.tribe.app.domain.exception.ErrorBundle;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.common.UseCase;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.user.GetDiskContactInviteList;
import com.tribe.app.domain.interactor.user.GetDiskContactOnAppList;
import com.tribe.app.domain.interactor.user.GetDiskFBContactInviteList;
import com.tribe.app.domain.interactor.user.GetDiskUserInfos;
import com.tribe.app.domain.interactor.user.SendInvitations;
import com.tribe.app.domain.interactor.user.SendToken;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.presentation.exception.ErrorMessageFactory;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.HomeGridMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.utils.facebook.FacebookUtils;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

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
  private UseCase synchroContactList;
  private GetDiskContactOnAppList getDiskContactOnAppList;
  private GetDiskContactInviteList getDiskContactInviteList;
  private GetDiskFBContactInviteList getDiskFBContactInviteList;

  private DeclineInvite declineInvite;
  private SendInvitations sendInvitations;
  private CreateRoom createRoom;

  // SUBSCRIBERS
  private RecipientListSubscriber diskRecipientListSubscriber;
  private RecipientListSubscriber cloudRecipientListSubscriber;
  private LookupContactsSubscriber lookupContactsSubscriber;
  private ContactsOnAppSubscriber contactsOnAppSubscriber;
  private ContactListInviteSubscriber contactListInviteSubscriber;
  private FBContactListInviteSubscriber fbContactListInviteSubscriber;

  @Inject public HomePresenter(ShortcutPresenter shortcutPresenter, JobManager jobManager,
      @Named("diskUserInfos") GetDiskUserInfos diskUserInfos,
      @Named("sendToken") SendToken sendToken, @Named("cloudUserInfos") UseCase cloudUserInfos,
      UpdateUserFacebook updateUserFacebook, RxFacebook rxFacebook,
      @Named("synchroContactList") UseCase synchroContactList,
      GetDiskContactOnAppList getDiskContactOnAppList,
      GetDiskContactInviteList getDiskContactInviteList, DeclineInvite declineInvite,
      SendInvitations sendInvitations, CreateRoom createRoom,
      GetDiskFBContactInviteList getDiskFBContactInviteList) {
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

  public void createShortcut(String... userIds) {
    shortcutPresenter.createShortcut(userIds);
  }

  public void muteShortcut(String shortcutId, boolean mute) {
    shortcutPresenter.muteShortcut(shortcutId, mute);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status);
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
