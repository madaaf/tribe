package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserAge;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.domain.interactor.user.UpdateUserPhoneNumber;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ProfileMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import javax.inject.Inject;

/**
 * Created by madaaflak on 28/01/2017.
 */

public class ProfilePresenter extends UpdateUserPresenter {

  private ShortcutPresenter shortcutPresenter;

  private ProfileMVPView profileView;

  private final RemoveInstall removeInstall;
  private DeclineInvite declineInvite;
  private CreateRoom createRoom;

  @Inject ProfilePresenter(ShortcutPresenter shortcutPresenter, UpdateUser updateUser,
      LookupUsername lookupUsername, RxFacebook rxFacebook, RemoveInstall removeInstall,
      DeclineInvite declineInvite, CreateRoom createRoom, UpdateUserFacebook updateUserFacebook,
      UpdateUserPhoneNumber updateUserPhoneNumber,  UpdateUserAge updateUserAge) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber, updateUserAge);
    this.shortcutPresenter = shortcutPresenter;
    this.removeInstall = removeInstall;
    this.declineInvite = declineInvite;
    this.createRoom = createRoom;
  }

  @Override public void onViewDetached() {
    shortcutPresenter.onViewDetached();
    removeInstall.unsubscribe();
    declineInvite.unsubscribe();
    createRoom.unsubscribe();
    profileView = null;
    super.onViewDetached();
  }

  @Override public void onViewAttached(MVPView v) {
    profileView = (ProfileMVPView) v;
    shortcutPresenter.onViewAttached(v);
  }

  public void logout() {
    removeInstall.execute(new RemoveInstallSubscriber());
  }

  @Override protected UpdateUserMVPView getUpdateUserView() {
    return profileView;
  }

  private final class RemoveInstallSubscriber extends DefaultSubscriber<User> {

    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
      goToLauncher();
    }

    @Override public void onNext(User user) {
      goToLauncher();
    }
  }

  public void goToLauncher() {
    this.profileView.goToLauncher();
  }

  public void declineInvite(String roomId) {
    declineInvite.setup(roomId);
    declineInvite.execute(new DefaultSubscriber());
  }

  public void createRoom() {
    createRoom.execute(new CreateRoomSubscriber());
  }

  private class CreateRoomSubscriber extends DefaultSubscriber<Room> {
    @Override public void onCompleted() {
    }

    @Override public void onError(Throwable e) {
    }

    @Override public void onNext(Room room) {
      if (profileView != null) profileView.onCreateRoom(room);
    }
  }

  public void muteShortcut(String shortcutId, boolean mute) {
    shortcutPresenter.muteShortcut(shortcutId, mute);
  }

  public void updateShortcutStatus(String shortcutId, @ShortcutRealm.ShortcutStatus String status, BaseListViewHolder viewHolder) {
    shortcutPresenter.updateShortcutStatus(shortcutId, status, viewHolder);
  }

  public void loadSingleShortcuts() {
    shortcutPresenter.loadSingleShortcuts();
  }

  public void unsubscribeLoadShortcuts() {
    shortcutPresenter.unsubscribeLoadShortcuts();
  }

  public void loadBlockedShortcuts() {
    shortcutPresenter.loadBlockedShortcuts();
  }
}
