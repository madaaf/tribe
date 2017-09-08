package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.view.widget.EditTextFont;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatView extends FrameLayout implements ChatMVPView {

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private MessageAdapter adapter;
  private ChatUserAdapter chatUserAdapter;
  private LinearLayoutManager layoutManager;
  private LinearLayoutManager layoutManagerGrp;
  private List<Message> items = new ArrayList<>();
  private List<User> users = new ArrayList<>();

  @BindView(R.id.editText) EditTextFont countrySearchView;
  @BindView(R.id.recyclerViewChat) RecyclerView recyclerView;
  @BindView(R.id.recyclerViewGrp) RecyclerView recyclerViewGrp;

  @Inject User user;
  @Inject MessagePresenter messagePresenter;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ChatView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public ChatView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  void initView(Context context) {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_chat, this, true);
    unbinder = ButterKnife.bind(this);

    init();
    initDependencyInjector();
  }

  void init() {
    layoutManager = new LinearLayoutManager(getContext());
    adapter = new MessageAdapter(getContext());
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);

    layoutManagerGrp = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    chatUserAdapter = new ChatUserAdapter(getContext());
    recyclerViewGrp.setLayoutManager(layoutManagerGrp);
    recyclerViewGrp.setItemAnimator(null);
    recyclerViewGrp.setAdapter(chatUserAdapter);
  }

  void mock() {
    users.clear();
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    users.add(user);
    chatUserAdapter.setItems(users);
  }

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    messagePresenter.onViewAttached(this);
    messagePresenter.loadMessage();
    mock();
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    super.onDetachedFromWindow();
  }

  @OnClick(R.id.sendBtn) public void envoyer() {
    items.clear();
    String ok = countrySearchView.getText().toString();
    Timber.e("SOEF " + ok);
    MessageText t = new MessageText("");
    t.setMessage(ok);
    t.setType(Message.MESSAGE_TEXT);
    t.setAuthor(user);
    items.add(t);
    adapter.setItems(items);
    countrySearchView.setText("");
    recyclerViewGrp.scrollToPosition(0);
    //layoutManager.setReverseLayout(true);
  }

  protected void initDependencyInjector() {
    DaggerUserComponent.builder()
        .activityModule(getActivityModule())
        .applicationComponent(getApplicationComponent())
        .build()
        .inject(this);
  }

  protected ApplicationComponent getApplicationComponent() {
    return ((AndroidApplication) ((Activity) getContext()).getApplication()).getApplicationComponent();
  }

  protected ActivityModule getActivityModule() {
    return new ActivityModule(((Activity) getContext()));
  }

  @Override public void successLoadingMessage(List<Message> messages) {
    adapter.setItems(messages);
    for (Message m : messages) {
      Timber.e("SOEF " + m.toString());
    }
    Timber.e("SOEF successLoadingMessage");
  }

  @Override public void errorLoadingMessage() {
    Timber.e("SOEF errorLoadingMessage");
  }
}
