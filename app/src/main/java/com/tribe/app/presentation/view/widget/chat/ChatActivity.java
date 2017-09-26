package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.common.ShortcutPresenter;
import com.tribe.app.presentation.mvp.view.ShortcutMVPView;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by remy on 28/07/2017.
 */

public class ChatActivity extends BaseActivity implements ShortcutMVPView {

  private static final String EXTRA_LIVE = "EXTRA_LIVE";

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  private Shortcut shortcut;
  private Invite invite;
  private String arrayIds;
  private List<String> listId = new ArrayList<>();

  @Inject ShortcutPresenter shortcutPresenter;

  @BindView(R.id.chatview) ChatView chatView;

  public static Intent getCallingIntent(Context context, Recipient recipient) {
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(EXTRA_LIVE, recipient);
    return intent;
  }

  private void initChatService(String usersFromatedId) {
    startService(WSService.getCallingIntent(this, WSService.CHAT_SUBSCRIBE, usersFromatedId));
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    ButterKnife.bind(this);
    initDependencyInjector();

    if (getIntent().hasExtra(EXTRA_LIVE)) {

      // List<User> friends = (ArrayList<User>) getIntent().getSerializableExtra(EXTRA_LIVE);
      Recipient recipient = (Recipient) getIntent().getSerializableExtra(EXTRA_LIVE);
      List<User> friends = new ArrayList<>();

      if (recipient instanceof Shortcut) {
        friends = ((Shortcut) recipient).getMembers();
        shortcut = (Shortcut) recipient;
      } else if (recipient instanceof Invite) {
        invite = (Invite) recipient;
        friends = ((Invite) recipient).getMembers();
        if (friends.isEmpty()) {
          User user = ((Invite) recipient).getRoom().getInitiator();
          friends.add(user);
        }
      }

      if (friends.isEmpty()) {
        Timber.e(" EMPTY LIST ID ");
        return;
      }
      chatView.setChatId(friends, shortcut);

      String[] ids = new String[friends.size()];
      for (int i = 0; i < friends.size(); i++) {
        ids[i] = friends.get(i).getId();
        listId.add(friends.get(i).getId());
      }
      arrayIds = JsonUtils.arrayToJson(ids);
    }

    initSubscription();
    shortcutPresenter.onViewAttached(this);
  }

  private void initSubscription() {
  /*  Invite ok =
    subscriptions.add();*/
    if (shortcut != null) {
      User user = shortcut.getSingleFriend();
      //shortcut.getR
    }
  }

  @Override protected void onResume() {
    initChatService(arrayIds);
    super.onResume();
    //shortcutPresenter.loadSingleShortcuts();
    //Timber.e("SOEF LOAD SHORTCUT FOR : " + listId.toArray(new String[listId.size()]).toString());
    //shortcutPresenter.shortcutForUserIds(listId.toArray(new String[listId.size()]));
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {
    Timber.e(" onShortcutCreatedSuccess " + shortcut.toString());
  }

  @Override public void onShortcutCreatedError() {
    Timber.e("onShortcutCreatedError");
  }

  @Override public void onShortcutRemovedSuccess() {
    Timber.e("onShortcutRemovedSuccess");
  }

  @Override public void onShortcutRemovedError() {
    Timber.e("onShortcutRemovedError");
  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut) {
    Timber.e("onShortcutUpdatedSuccess " + shortcut.toString());
  }

  @Override public void onShortcutUpdatedError() {
    Timber.e("onShortcutUpdatedError ");
  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {
    Timber.e("onSingleShortcutsLoaded " + singleShortcutList.toString());
  }

  @Override public void onShortcut(Shortcut shortcut) {
    Timber.e("onShortcut " + shortcut.toString());
  }
}
