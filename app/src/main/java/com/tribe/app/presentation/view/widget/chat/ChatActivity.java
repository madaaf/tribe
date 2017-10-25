package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.domain.entity.Invite;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.view.activity.BaseActivity;
import rx.subscriptions.CompositeSubscription;

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

  @BindView(R.id.chatview) ChatView chatView;

  public static Intent getCallingIntent(Context context, Recipient recipient, Shortcut shortcut,
      String gesture, String section) {
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(EXTRA_LIVE, recipient);
    intent.putExtra(FROM_SHORTCUT, shortcut);
    intent.putExtra(EXTRA_SECTION, section);
    intent.putExtra(EXTRA_GESTURE, gesture);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_test);
    ButterKnife.bind(this);
    initDependencyInjector();

    Intent intent = getIntent();

    if (intent.hasExtra(FROM_SHORTCUT)) {
      Shortcut fromShortcut = (Shortcut) intent.getSerializableExtra(FROM_SHORTCUT);
      chatView.setFromShortcut(fromShortcut);
      intent.removeExtra(FROM_SHORTCUT);
    }

    if (intent.hasExtra(EXTRA_LIVE)) {
      Recipient recipient = (Recipient) intent.getSerializableExtra(EXTRA_LIVE);

      if (recipient instanceof Shortcut) {
        shortcut = (Shortcut) recipient;
      } else if (recipient instanceof Invite) {
        shortcut = ((Invite) recipient).getRoom().getShortcut();
      }

      chatView.setChatId(shortcut, recipient);
    }

    chatView.setGestureAndSection(intent.getStringExtra(EXTRA_GESTURE),
        intent.getStringExtra(EXTRA_SECTION));
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
