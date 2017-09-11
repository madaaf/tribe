package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.entity.Room;
import com.tribe.app.domain.entity.User;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.live.CreateRoom;
import com.tribe.app.domain.interactor.live.DeclineInvite;
import com.tribe.app.domain.interactor.user.LookupUsername;
import com.tribe.app.domain.interactor.user.RemoveInstall;
import com.tribe.app.domain.interactor.user.UpdateUser;
import com.tribe.app.domain.interactor.user.UpdateUserFacebook;
import com.tribe.app.domain.interactor.user.UpdateUserPhoneNumber;
import com.tribe.app.presentation.mvp.view.MVPView;
import com.tribe.app.presentation.mvp.view.ProfileMVPView;
import com.tribe.app.presentation.mvp.view.UpdateUserMVPView;
import com.tribe.app.presentation.utils.facebook.RxFacebook;
import javax.inject.Inject;

/**
 * Created by madaaflak on 28/01/2017.
 */

public class ProfilePresenter extends UpdateUserPresenter {

  private ProfileMVPView profileView;

  private final RemoveInstall removeInstall;
  private DeclineInvite declineInvite;
  private CreateRoom createRoom;

  @Inject ProfilePresenter(UpdateUser updateUser, LookupUsername lookupUsername,
      RxFacebook rxFacebook, RemoveInstall removeInstall,
      DeclineInvite declineInvite,
      CreateRoom createRoom, UpdateUserFacebook updateUserFacebook,
      UpdateUserPhoneNumber updateUserPhoneNumber) {
    super(updateUser, lookupUsername, rxFacebook, updateUserFacebook, updateUserPhoneNumber);
    this.removeInstall = removeInstall;
    this.declineInvite = declineInvite;
    this.createRoom = createRoom;
  }

  @Override public void onViewDetached() {
    removeInstall.unsubscribe();
    declineInvite.unsubscribe();
    createRoom.unsubscribe();
    profileView = null;
    super.onViewDetached();
  }

  @Override public void onViewAttached(MVPView v) {
    profileView = (ProfileMVPView) v;
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
}
