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
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.widget.chat.adapterDelegate.MessageAdapter;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import timber.log.Timber;

/**
 * Created by madaaflak on 03/10/2017.
 */

public class RecyclerMessageView extends ChatMVPView {

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private LinearLayoutManager layoutManager;
  private MessageAdapter messageAdapter;

  private int counterMessageNotSend, type;
  private boolean load = false, errorLoadingMessages = false;
  private List<Message> unreadMessage = new ArrayList<>();
  private String[] arrIds = null;

  @BindView(R.id.recyclerViewMessageChat) RecyclerView recyclerView;

  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject DateUtils dateUtils;

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
    if (type == ChatView.FROM_LIVE) {
      recyclerView.setVerticalScrollBarEnabled(false);
    }
  }

  @Override protected void onAttachedToWindow() {
    Timber.w(" onAttachedToWindow");
    super.onAttachedToWindow();
    messagePresenter.onViewAttached(this);
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();
    super.onDetachedFromWindow();
    Timber.w("DETACHED onDetachedFromWindow");
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
        if (dy < 0) {
          if (layoutManager.findFirstVisibleItemPosition() < 5 && !load) {
            Timber.w("SCROOL OK " + messageAdapter.getMessage(0).getContent());
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

  public void onResumeView() {
    Timber.w("SOEF SET CHAT ID AND CALL PRESENTER ");
    if (arrIds == null) {
      return;
    }
  }

  public void scrollListToBottom() {
    layoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, 0);
  }

  public void setArrIds(String[] arrIds) {
    this.arrIds = arrIds;
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

  @Override public void successLoadingMessage(List<Message> messages) {
    Timber.w("SOEF successLoadingMessage " + messages.size() + " ");
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

  @Override public void successMessageCreated(Message message, int position) {
    Timber.w("successMessageCreated " + position + " " + message.toString());
    messageAdapter.notifyItemChanged(messageAdapter.getItemCount() - 1, message);
  }

  @Override public void successLoadingMessageDisk(List<Message> messages) {
    Timber.w("successLoadingMessageDisk " + messages.size() + " ");
    if (errorLoadingMessages) {
      messageAdapter.setItems(messages, 0);
      scrollListToBottom();
    }
    // DO SAME THING THE SUCVCESSLOADING MESSAGE/
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
    errorLoadingMessages = true;
  }

  public void successMessageReceived(List<Message> messages) {
    messageAdapter.setItem(messages.get(0));
    scrollListToBottom();
    Timber.i("SOOoOOOOOOOOOOOOEF successMessageReceived " + messages);
  }

  public void errorMessageReveived() {
    Timber.i("SOOoOOOOOOOOOOOOEF errorMessageReveived ");
  }
}
