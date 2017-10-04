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
import com.tribe.app.presentation.view.widget.chat.adapterDelegate.MessageAdapter;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import timber.log.Timber;

/**
 * Created by madaaflak on 03/10/2017.
 */

public class RecyclerMessageView extends ChatMVPView {

  public final static int FROM_CHAT = 0;
  public final static int FROM_LIVE = 1;

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private LinearLayoutManager layoutManager;
  private MessageAdapter messageAdapter;

  int counterMessageNotSend;
  boolean load = false;
  private String[] arrIds = null;
  private Set<Message> treeSet = new TreeSet<>((o1, o2) -> {
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime d1 = parser.parseDateTime(o1.getCreationDate());
    DateTime d2 = parser.parseDateTime(o2.getCreationDate());
    return d1.compareTo(d2);
  });

  @BindView(R.id.recyclerViewChatHOHO) RecyclerView recyclerView;

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
    inflater.inflate(R.layout.test, this, true);
    unbinder = ButterKnife.bind(this);
    initRecyclerView();
    initDependencyInjector();
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
    messageAdapter = new MessageAdapter(getContext(), FROM_CHAT);
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
          if (layoutManager.findFirstVisibleItemPosition() < 3 && !load) {
            Timber.w("SCROOL OK " + messageAdapter.getMessage(0).getContent());
            String lasteDate = messageAdapter.getMessage(0).getCreationDate();
            /* messagePresenter.loadMessage(arrIds, lasteDate);
            load = true;*/
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
              Timber.e(
                  "scrollToPosWithOffset" + lastAdapterItem + " " + recyclerViewPositionOffset);
            });
          }
        });
  }

  public void onResumeView() {
    Timber.w("SOEF SET CHAT ID AND CALL PRESENTER ");
    if (arrIds == null) {
      return;
    }
    messagePresenter.loadMessagesDisk(arrIds, dateUtils.getUTCDateAsString());
    messagePresenter.loadMessage(arrIds, dateUtils.getUTCDateAsString());
    messagePresenter.onMessageReceivedFromDisk();
  }

  public void scrollListToBottom() {
    layoutManager.scrollToPositionWithOffset(messageAdapter.getItemCount() - 1, 0);
  }

  public void setArrIds(String[] arrIds) {
    this.arrIds = arrIds;
  }

  public void sendMessageToNetwork(String[] arrIds, String data, String type, int position) {
    Timber.w("sendMessageToNetwork " + position + "  " + data);
    messagePresenter.createMessage(arrIds, data, type, position);
  }

  public void sendMyMessageToAdapter(Message pendingMessage) {
    messageAdapter.setItem(pendingMessage);
    scrollListToBottom();
  }

  /**
   * MESSAGE RECEPTION
   */

  List<Message> unreadMessage = new ArrayList<>();
  boolean successMessageCreated = false;

  @Override public void successLoadingMessage(List<Message> messages) {
    Timber.w("SOEF successLoadingMessage " + messages.size() + " ");
    treeSet.addAll(messages);
    messageAdapter.setItems(new ArrayList<Message>(treeSet));
    scrollListToBottom();
    load = false;
  }

  @Override public void successMessageCreated(Message message, int position) {
    Timber.w("successMessageCreated " + position + " " + message.toString());
    messageAdapter.notifyItemChanged(messageAdapter.getItemCount() - 1, message);
  }

  @Override public void successLoadingMessageDisk(List<Message> messages) {
    Timber.w("successLoadingMessageDisk " + messages.size() + " ");
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

































  /*
    Set<Message> adapterList = new HashSet<>();
    adapterList.addAll(messageAdapter.getItems());

    for (Message m : messasges) {
      if (!adapterList.contains(m)) {
        unreadDiskMessages.add(m);
        Timber.i(m.toString());
      } else {
        //Timber.w("WGY / " + m.toString());
      }
    }
    Timber.e(
        "SOEF successLoadingMessageDisk " + messasges.size() + " " + unreadDiskMessages.size());
    if (!unreadDiskMessages.isEmpty()) {
      messageAdapter.setItems(unreadDiskMessages);
      //int index = messageAdapter.getIndexOfMessage(unreadDiskMessages.last());
      int index = messageAdapter.getItemCount();
      if (index >= 0) {
        recyclerView.post(() -> {
          Timber.e("smooth scroll to position " + (index + 1));
          // recyclerView.scrollToPosition(index + 1);
        });
      } else {
        Timber.e("SOEF scroll error index = -1");
      }
      unreadDiskMessages.clear();
    }*/


