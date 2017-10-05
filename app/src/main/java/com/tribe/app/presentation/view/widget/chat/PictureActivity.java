package com.tribe.app.presentation.view.widget.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.tribe.app.R;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.PictureMVPView;
import com.tribe.app.presentation.view.activity.BaseActivity;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Created by madaaflak on 08/09/2017.
 */

public class PictureActivity extends BaseActivity implements PictureMVPView {

  private String[] arrIds;
  private String messageId;
  private List<Message> messages;
  private PicturePagerAdapter pagerAdapter;

  @BindView(R.id.pager) ViewPager viewPager;
  @Inject MessagePresenter messagePresenter;

  public static Intent getCallingIntent(Context context, String messageId, String[] arrIds) {
    Intent intent = new Intent(context, PictureActivity.class);
    intent.putExtra("SOEF", messageId);
    intent.putExtra("OK", arrIds);

    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_picture);
    ButterKnife.bind(this);
    initDependencyInjector();

    if (getIntent().hasExtra("SOEF")) {
      this.messageId = getIntent().getStringExtra("SOEF");
    }

    if (getIntent().hasExtra("OK")) {
      this.arrIds = (String[]) getIntent().getExtras().getStringArray("OK");
    }
  }

  @Override protected void onStart() {
    super.onStart();
    messagePresenter.onViewAttached(this);
  }

  @Override protected void onResume() {
    super.onResume();
    messagePresenter.getMessageImage(arrIds);
  }

  @Override protected void onStop() {
    messagePresenter.onViewDetached();
    super.onStop();
  }

  private void initDependencyInjector() {
    DaggerUserComponent.builder()
        .applicationComponent(getApplicationComponent())
        .activityModule(getActivityModule())
        .build()
        .inject(this);
  }

  @Override public void successGetMessageImageFromDisk(List<Message> messages) {
    Timber.e("successGetMessageImageFromDisk " + messages.size());
    Message currentmessage = null;
    for (Message m : messages) {
      Timber.i(m.toString());
      if (m.getId().equals(messageId)) {
        currentmessage = m;
      }
    }
    int index = messages.indexOf(currentmessage);
    List<Message> before = messages.subList(0, index);
    List<Message> after = messages.subList(index, messages.size());
    List<Message> renderList = new ArrayList<>();
    renderList.addAll(after);
    renderList.addAll(before);

    this.messages = messages;
    pagerAdapter = new PicturePagerAdapter(this, renderList);
    viewPager.setAdapter(pagerAdapter);
  }

  @Override public void errorGetMessageImageFromDisk() {
    Timber.e("errorGetMessageImageFromDisk ");
  }
}
