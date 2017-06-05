package com.tribe.app.presentation.mvp.presenter;

import android.util.Pair;
import com.tribe.app.data.realm.FriendshipRealm;
import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.UpdateFriendship;
import com.tribe.app.presentation.mvp.view.MVPView;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * Created by madaaflak on 04/06/2017.
 */

public class FriendshipPresenter implements Presenter {

  protected UpdateFriendship updateFriendship;

  public FriendshipPresenter() {
  }

  @Inject public FriendshipPresenter(UpdateFriendship updateFriendship) {
    this.updateFriendship = updateFriendship;
  }

  public void updateFriendship(String friendshipId, boolean mute,
      @FriendshipRealm.FriendshipStatus String status) {
    List<Pair<String, String>> values = new ArrayList<>();
    values.add(new Pair<>(FriendshipRealm.MUTE, String.valueOf(mute)));
    values.add(new Pair<>(FriendshipRealm.STATUS, status));

    if (values.size() > 0) {
      updateFriendship.prepare(friendshipId, values);
      updateFriendship.execute(new DefaultSubscriber());
    }
  }

  @Override public void onViewAttached(MVPView view) {

  }

  @Override public void onViewDetached() {
    updateFriendship.unsubscribe();
  }
}
