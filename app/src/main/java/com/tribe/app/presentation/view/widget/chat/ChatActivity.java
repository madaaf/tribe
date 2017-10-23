package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
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
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.List;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by remy on 28/07/2017.
 */

public class ChatActivity extends BaseActivity {

  private static final String EXTRA_LIVE = "EXTRA_LIVE";
  private static final String FROM_SHORTCUT = "FROM_SHORTCUT";
  public static final String EXTRA_SHORTCUT_ID = "EXTRA_SHORTCUT_ID";
  public static final String EXTRA_GESTURE = "EXTRA_GESTURE";
  public static final String EXTRA_SECTION = "EXTRA_SECTION";

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();

  private Shortcut shortcut;
  private Invite invite;
  private String arrayIds;
  private List<String> listId = new ArrayList<>();

  @BindView(R.id.chatview) ChatView chatView;

  public static Intent getCallingIntent(Context context, Recipient recipient, Shortcut shortcut) {
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(EXTRA_LIVE, recipient);
    intent.putExtra(FROM_SHORTCUT, shortcut);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    ButterKnife.bind(this);
    initDependencyInjector();

    if (getIntent().hasExtra(FROM_SHORTCUT)) {
      Shortcut fromShortcut = (Shortcut) getIntent().getSerializableExtra(FROM_SHORTCUT);
      chatView.setFromShortcut(fromShortcut);
    }
    if (getIntent().hasExtra(EXTRA_LIVE)) {

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
      chatView.setChatId(friends, shortcut, recipient);

      String[] ids = new String[friends.size()];
      for (int i = 0; i < friends.size(); i++) {
        ids[i] = friends.get(i).getId();
        listId.add(friends.get(i).getId());
      }
      arrayIds = JsonUtils.arrayToJson(ids);
    }

    startService(WSService.getCallingIntentCancelImOnline(this));
  }

  @Override protected void onPause() {
    super.onPause();
    chatView.dispose();
  }

  @Override protected void onResume() {
    super.onResume();
    chatView.onResumeView();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
  }

  @Override public void finish() {
    if (shortcut != null) {
      Intent resultIntent = new Intent();
      resultIntent.putExtra(EXTRA_SHORTCUT_ID, shortcut.getId());
      setResult(Activity.RESULT_OK, resultIntent);
    }
    super.finish();
    overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_left);
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }
}
