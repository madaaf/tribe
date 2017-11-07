package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.domain.ShortcutLastSeen;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.adapterDelegate.MessageAdapter;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.data.network.WSService.CHAT_SUBSCRIBE_IMREADING;

/**
 * Created by madaaflak on 03/10/2017.
 */

public class RecyclerMessageView extends ChatMVPView {

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private LinearLayoutManager layoutManager;
  private MessageAdapter messageAdapter;

  private Shortcut shortcut;

  private int counterMessageNotSend, type;
  private boolean load = false, errorLoadingMessages = false;
  private List<Message> unreadMessage = new ArrayList<>();
  private String[] arrIds = null;

  @BindView(R.id.recyclerViewMessageChat) RecyclerView recyclerView;

  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject DateUtils dateUtils;
  @Inject ScreenUtils screenUtils;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Integer> onScrollRecyclerView = PublishSubject.create();
  private PublishSubject<Boolean> shortcutLastSeenUpdated = PublishSubject.create();

  public RecyclerMessageView(@NonNull Context context) {
    super(context);
    this.context = context;
    initView();
  }

  public RecyclerMessageView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    if (context instanceof LiveActivity) {
      this.type = ChatView.FROM_LIVE;
    } else {
      this.type = ChatView.FROM_CHAT;
    }
    initView();
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

  private void initView() {
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_recycler_message, this, true);
    unbinder = ButterKnife.bind(this);
    initRecyclerView();
    initDependencyInjector();
    initSubscriptions();
    if (type == ChatView.FROM_LIVE) {
      recyclerView.setVerticalScrollBarEnabled(false);
    }
  }

  private void initSubscriptions() {
    subscriptions.add(messageAdapter.onClickItem().subscribe(obj -> {
      TextViewFont view = ((TextViewFont) obj.get(0));
      Message m = ((Message) obj.get(1));

      String lastSeenString = "Seen by ";
      List<ShortcutLastSeen> list = shortcut.getShortcutLastSeen();
      List<String> lastSeenListId = new ArrayList<>();

      for (ShortcutLastSeen item : list) {
        String date = item.getDate();
        if (date != null && dateUtils.isBefore(m.getCreationDate(), date)) {
          lastSeenListId.add(item.getUserId());
        }
      }

      for (User user : shortcut.getMembers()) {
        if (lastSeenListId.contains(user.getId())) {
          lastSeenString +=
              user.getDisplayName().substring(0, 1).toUpperCase() + user.getDisplayName()
                  .substring(1) + ", ";
        }
      }

      lastSeenString = (lastSeenListId.isEmpty()) ? context.getString(R.string.chat_not_seen)
          : lastSeenString.substring(0, (lastSeenString.length() - 2));
      view.setText(lastSeenString);
    }));
  }

  public void notifyDataSetChanged() {
    messageAdapter.notifyDataSetChanged();
  }

  public void onResumeView() {
    if (!messagePresenter.isAttached()) {
      messagePresenter.onViewAttached(this);
    }
  }

  @Override protected void onAttachedToWindow() {
    Timber.i("onAttachedToWindow");
    super.onAttachedToWindow();
    messagePresenter.onViewAttached(this);
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();
    super.onDetachedFromWindow();
    Timber.i("onDetachedFromWindow");
  }

  private void initRecyclerView() {
    layoutManager = new LinearLayoutManager(getContext());
    messageAdapter = new MessageAdapter(getContext(), type);
    layoutManager.setStackFromEnd(true);

    DefaultItemAnimator animator = new DefaultItemAnimator() {
      @Override public boolean canReuseUpdatedViewHolder(RecyclerView.ViewHolder viewHolder) {
        return true;
      }
    };
    recyclerView.setItemAnimator(animator);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(messageAdapter);

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        onScrollRecyclerView.onNext(dy);
        if (dy != 0) {
          screenUtils.hideKeyboard((Activity) context);
        }
        if (dy < 0) {
          if (layoutManager.findFirstVisibleItemPosition() < 5 && !load) {
            String lasteDate = messageAdapter.getMessage(0).getCreationDate();
            messagePresenter.loadMessage(arrIds, lasteDate);
            load = true;
          }
        }
      }
    });

    recyclerView.addOnLayoutChangeListener(
        (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
          if (bottom < oldBottom) {
            final int lastAdapterItem = messageAdapter.getItemCount() - 1;
            recyclerView.post(() -> {
              int recyclerViewPositionOffset = -1000000;
              View bottomView = layoutManager.findViewByPosition(lastAdapterItem);
              if (bottomView != null) {
                recyclerViewPositionOffset = 0 - bottomView.getHeight();
              }
              layoutManager.scrollToPositionWithOffset(lastAdapterItem, recyclerViewPositionOffset);
            });
          }
        });
  }

  public void scrollListToBottom() {
    layoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, 0);
  }

  public void setArrIds(String[] arrIds) {
    this.arrIds = arrIds;
    messageAdapter.setArrIds(arrIds);
    messagePresenter.loadMessagesDisk(arrIds, dateUtils.getUTCDateAsString());
    messagePresenter.loadMessage(arrIds, dateUtils.getUTCDateAsString());
    messagePresenter.onMessageReceivedFromDisk();
  }

  public void sendMessageToNetwork(String[] arrIds, String data, String type, int position) {
    Timber.w("sendMessageToNetwork " + position + "  " + data);
    messagePresenter.createMessage(arrIds, data, type, position);
  }

  public void sendMyMessageToAdapter(Message pendingMessage) {
    Timber.w("SEND PENDING MESSAGE " + pendingMessage.toString());
    messageAdapter.setItem(pendingMessage);
    scrollListToBottom();
  }

  private void sortMessageList(List<Message> list) {
    Collections.sort(list, (o1, o2) -> {
      DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
      DateTime d1 = parser.parseDateTime(o1.getCreationDate());
      DateTime d2 = parser.parseDateTime(o2.getCreationDate());
      return d1.compareTo(d2);
    });
  }

  /**
   * MESSAGE RECEPTION
   */

  private boolean isDisplayedMessageDisk = false;
  private boolean successLoadingMessage = false;

  @Override public void successLoadingMessage(List<Message> messages) {
    successLoadingMessage = true;
    Timber.w("SOEF successLoadingMessage " + messages.size() + " ");
    if (isDisplayedMessageDisk) {
      messageAdapter.clearItem();
      isDisplayedMessageDisk = false;
    }
    errorLoadingMessages = false;
    for (Message m : messages) {
      if (!messageAdapter.getItems().contains(m)) {
        unreadMessage.add(m);
      }
    }

    int ok = 0;
    if (!messageAdapter.getItems().isEmpty()) {
      Message m = messageAdapter.getItems().get(0);
      String s1 = m.getCreationDate();
      String s2 = messages.get(messages.size() - 1).getCreationDate();

      DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
      DateTime d1 = parser.parseDateTime(s1);
      DateTime d2 = parser.parseDateTime(s2);
      ok = d1.compareTo(d2);
    }

    if (!unreadMessage.isEmpty()) {
      sortMessageList(unreadMessage);
      if (ok > 0) {
        messageAdapter.setItems(unreadMessage, 0);
        int index = messageAdapter.getIndexOfMessage(unreadMessage.get(unreadMessage.size() - 1));
        Timber.e("SOEF OK INDEX " + index + " ");
        layoutManager.scrollToPosition(index + 5);
      } else {
        messageAdapter.setItems(unreadMessage, messageAdapter.getItemCount());
        scrollListToBottom();
      }
      unreadMessage.clear();
    }

    load = false;
  }

  @Override public void successLoadingMessageDisk(List<Message> messages) {
    Timber.w("successLoadingMessageDisk " + messages.size() + " ");
    if (errorLoadingMessages || !successLoadingMessage) {
      messageAdapter.setItems(messages, 0);
      scrollListToBottom();
      isDisplayedMessageDisk = true;
    }

    // DO SAME THING THE SUCVCESSLOADING MESSAGE/
  }

  @Override public void successMessageCreated(Message message, int position) {
    Timber.w("successMessageCreated " + position + " " + message.toString());
    messageAdapter.updateItem(messageAdapter.getItemCount() - 1, message);
  }

  /**
   * ERROR NETWORK
   */

  @Override public void errorLoadingMessageDisk() {
    Timber.w("errorLoadingMessageDisk");
  }

  @Override public void errorMessageCreation(int position) {
    counterMessageNotSend++;
    Timber.i("SOEF errorMessageCreation " + position + " " + counterMessageNotSend);
  }

  @Override public void errorLoadingMessage() {
    Timber.w("errorLoadingMessage");
    //  errorLoadingMessages = true; //TODO DECOMMENT
  }

  public void successMessageReceived(List<Message> messages) {
    messageAdapter.setItem(messages.get(0));
    context.startService(WSService.getCallingSubscribeChat(context, CHAT_SUBSCRIBE_IMREADING,
        JsonUtils.arrayToJson(arrIds)));
    scrollListToBottom();
    Timber.i("SOOoOOOOOOOOOOOOEF successMessageReceived " + messages);
  }

  public void errorMessageReveived() {
    Timber.i("SOOoOOOOOOOOOOOOEF errorMessageReveived ");
  }

  public Observable<Integer> onScrollRecyclerView() {
    return onScrollRecyclerView;
  }

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;
  }

  public void refreshLayout() {
    recyclerView.getLayoutParams().width = RecyclerView.LayoutParams.MATCH_PARENT;
    recyclerView.getLayoutParams().height = RecyclerView.LayoutParams.MATCH_PARENT;
  }
}
