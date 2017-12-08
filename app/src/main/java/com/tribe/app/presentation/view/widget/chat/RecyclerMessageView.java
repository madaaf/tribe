package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.google.common.collect.Lists;
import com.google.firebase.iid.FirebaseInstanceId;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.AccessToken;
import com.tribe.app.domain.ShortcutLastSeen;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.preferences.SupportId;
import com.tribe.app.presentation.view.ShortcutUtil;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.chat.adapterDelegate.MessageAdapter;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.zendesk.sdk.model.access.Identity;
import com.zendesk.sdk.model.access.JwtIdentity;
import com.zendesk.sdk.model.push.PushRegistrationResponse;
import com.zendesk.sdk.model.request.Comment;
import com.zendesk.sdk.model.request.CommentResponse;
import com.zendesk.sdk.model.request.CommentsResponse;
import com.zendesk.sdk.model.request.CreateRequest;
import com.zendesk.sdk.model.request.EndUserComment;
import com.zendesk.sdk.model.request.Request;
import com.zendesk.sdk.network.RequestProvider;
import com.zendesk.sdk.network.impl.ZendeskConfig;
import com.zendesk.service.ErrorResponse;
import com.zendesk.service.ZendeskCallback;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.data.network.WSService.CHAT_SUBSCRIBE_IMREADING;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_EVENT;

/**
 * Created by madaaflak on 03/10/2017.
 */

public class RecyclerMessageView extends IChat {

  private static final int MAX_DURATION_MIN_DELETE_MESSAGE = 60;
  private static final long ONE_HOUR_DURATION = 1000 * 60 * 60; //1000 ms * 60 secondes * 60

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private LinearLayoutManager layoutManager;
  private MessageAdapter messageAdapter;
  private static String supportId;

  private Shortcut shortcut;
  private boolean receiverRegistered;

  private int type;
  private boolean load = false, errorLoadingMessages = false;
  private List<Message> unreadMessage = new ArrayList<>();
  private String[] arrIds = null;
  private String token;
  private RequestProvider provider;

  @BindView(R.id.recyclerViewMessageChat) RecyclerView recyclerView;

  @Inject User user;
  @Inject AccessToken accessToken;
  @Inject MessagePresenter messagePresenter;
  @Inject DateUtils dateUtils;
  @Inject ScreenUtils screenUtils;
  @Inject StateManager stateManager;
  @Inject TagManager tagManager;
  @Inject Navigator navigator;
  @Inject @SupportId Preference<String> supportIdPref;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Integer> onScrollRecyclerView = PublishSubject.create();
  private PublishSubject<Boolean> shortcutLastSeenUpdated = PublishSubject.create();

  public RecyclerMessageView(@NonNull Context context) {
    super(context, null);
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

  private void initZendesk() {
    provider = ZendeskConfig.INSTANCE.provider().requestProvider();
    token = FirebaseInstanceId.getInstance().getToken();

    Identity jwtUserIdentity = new JwtIdentity(accessToken.getAccessToken());
    ZendeskConfig.INSTANCE.setIdentity(jwtUserIdentity);
    enablePushZendesk();

    if (supportIdPref.get() == null || supportIdPref.get().isEmpty()) {
      createZendeskRequest();
    } else {
      supportId = supportIdPref.get();
      Timber.e("support id : " + supportId);
      getCommentZendesk();
    }

    //getRequestProvider();
  }

  @Override public void successMessageSupport(List<Message> messages) {
    List<Message> list = new ArrayList<>();
    User u = new User(Shortcut.SUPPORT);
    u.setDisplayName("Live Support");
    u.setProfilePicture("https://static.tribe.pm/assets/support-avatar-love.png");

    for (Message message : messages) {
      MessageText m = new MessageText(Shortcut.SUPPORT);
      m.setAuthor(u);
      m.setCreationDate(dateUtils.getUTCDateForMessage());
      m.setMessage(message.getContent());
      list.add(m);
    }
    messageAdapter.setItems(list, 0);
    scrollListToBottom();
  }

  private void getCommentZendesk() {
    provider.getComments(supportId, new ZendeskCallback<CommentsResponse>() {
      @Override public void onSuccess(CommentsResponse commentsResponse) {
        String supportUserId = null;
        for (com.zendesk.sdk.model.request.User u : commentsResponse.getUsers()) {
          if (u.isAgent()) {
            supportUserId = u.getId().toString();
          }
        }
        unreadMessage.clear();
        List<Message> list = new ArrayList<>();
        for (CommentResponse response : commentsResponse.getComments()) {

          MessageText m = new MessageText();
          m.setId(response.getId().toString());
          if (response.getAuthorId().toString().equals(supportUserId)) {
            m.setAuthor(ShortcutUtil.createUserSupport());
          } else {
            m.setAuthor(user);
          }
          m.setCreationDate(dateUtils.getUTCDateForMessage());
          m.setMessage(response.getBody());
          list.add(m);

          if (!messageAdapter.getItems().contains(m)) {
            unreadMessage.add(m);
          }

          Timber.e("getCommentZendesk onSuccess " + response.getBody());
        }
        messageAdapter.setItems(Lists.reverse(unreadMessage),
            messageAdapter.getItemCount()); // SOEF MAFA
        scrollListToBottom();
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e(" getCommentZendesk onError " + errorResponse);
      }
    });
  }

  private void addCommentZendesk(String data) {
    EndUserComment o = new EndUserComment();
    o.setValue(data);

    provider.addComment(supportId, o, new ZendeskCallback<Comment>() {
      @Override public void onSuccess(Comment comment) {
        Timber.e("SOEF onSuccess ADD COMmENT " + comment.getBody());
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("SOEF onSuccess ADD COMmENT " + errorResponse);
      }
    });
  }

  private void getRequestProvider() {
    provider.getRequest(supportId, new ZendeskCallback<Request>() {
      @Override public void onSuccess(Request request) {
        Timber.e("SOEF onSuccess getRequestProvider " + request.toString());
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("SOEF error getRequestProvider " + errorResponse.toString());
      }
    });
  }

  private void createZendeskRequest() {
    CreateRequest request = new CreateRequest();
    request.setSubject("Chat with " + user.getDisplayName());
    request.setTags(Arrays.asList("chat", "mobile"));

    provider.createRequest(request, new ZendeskCallback<CreateRequest>() {
      @Override public void onSuccess(CreateRequest createRequest) {
        Timber.e("SOEF onSuccess REQUEST TIQUET " + createRequest.getId());
        supportIdPref.set(createRequest.getId());
        supportId = createRequest.getId();
        getCommentZendesk();
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("SOEF MyLogTag" + errorResponse);
      }
    });
  }

  private void enablePushZendesk() {
    ZendeskConfig.INSTANCE.enablePushWithIdentifier(token,
        new ZendeskCallback<PushRegistrationResponse>() {
          @Override public void onSuccess(PushRegistrationResponse pushRegistrationResponse) {
            Timber.e("SOEF onSuccess enablePushZendesk" + pushRegistrationResponse);
          }

          @Override public void onError(ErrorResponse errorResponse) {
            Timber.e("SOEF  onError enablePushZendesk" + errorResponse);
          }
        });
  }

  private void disablePushZendesk() {
    ZendeskConfig.INSTANCE.disablePush(token, new ZendeskCallback<Void>() {
      @Override public void onSuccess(Void aVoid) {
        Timber.e("SOEF disable PushZendesk " + aVoid);
      }

      @Override public void onError(ErrorResponse errorResponse) {
        Timber.e("SOEF  onError disable PushZendesk " + errorResponse);
      }
    });
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
    initZendesk();
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

    subscriptions.add(messageAdapter.onLongClickItem().subscribe(m -> {
      Timber.i("on long click message " + dateUtils.getDiffDate(m.getCreationDate(),
          dateUtils.getUTCDateAsString()));
      boolean enableUnsendMessage = false;
      if (!m.getType().equals(MESSAGE_EVENT)
          && m.getAuthor().getId().equals(user.getId())
          && dateUtils.getDiffDate(m.getCreationDate(), dateUtils.getUTCDateAsString())
          < MAX_DURATION_MIN_DELETE_MESSAGE) {
        enableUnsendMessage = true;
      }
      subscriptions.add(
          DialogFactory.showBottomSheetForMessageLongClick(context, enableUnsendMessage)
              .flatMap(labelType -> {
                if (labelType != null) {
                  if (labelType.getTypeDef().equals(LabelType.MESSAGE_OPTION_UNSEND)) {
                    messagePresenter.removeMessage(m);
                  } else if (labelType.getTypeDef().equals(LabelType.MESSAGE_OPTION_COPY)) {
                    copyToClipboard(m.getMessageContent());
                  }
                }
                return null;
              })
              .subscribe());
    }));
  }

  public void copyToClipboard(String copyText) {
    ClipboardManager clipboard =
        (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clip = ClipData.newPlainText("text", copyText);
    clipboard.setPrimaryClip(clip);
    Toast toast = Toast.makeText(context, "Message is copied : " + copyText, Toast.LENGTH_SHORT);
    toast.show();
  }

  public void notifyDataSetChanged() {
    messageAdapter.notifyDataSetChanged();
  }

  public void onResumeView() {
    if (!messagePresenter.isAttached()) {
      messagePresenter.onViewAttached(this);
    }

    if (!shortcut.isSupport()) {
      context.startService(WSService.getCallingSubscribeChat(context, WSService.CHAT_SUBSCRIBE,
          JsonUtils.arrayToJson(arrIds)));
      messagePresenter.updateShortcutForUserIds(arrIds);
      messagePresenter.getIsTyping();
      messagePresenter.getIsTalking();
      messagePresenter.getIsReading();
    } else {
      messagePresenter.getMessageSupport();
    }
  }

  @Override protected void onAttachedToWindow() {
    Timber.i("onAttachedToWindow");
    super.onAttachedToWindow();
    if (!messagePresenter.isAttached()) {
      messagePresenter.onViewAttached(this);
    }
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
            if (!shortcut.isSupport()) messagePresenter.loadMessage(arrIds, lasteDate, null);
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

    if (!shortcut.isSupport()) {
      messagePresenter.loadMessage(arrIds, dateUtils.getUTCDateAsString(),
          dateUtils.getUTCDateWithDeltaAsString(-(2 * ONE_HOUR_DURATION)));
      messagePresenter.loadMessagesDisk(arrIds, dateUtils.getUTCDateAsString(), null);
      messagePresenter.onMessageReceivedFromDisk();
      messagePresenter.onMessageRemovedFromDisk();
      messagePresenter.loadMessage(arrIds, dateUtils.getUTCDateForMessage(), null);
    }
  }

  public void sendMessageToNetwork(String[] arrIds, String data, String type, int position) {
    if (!shortcut.isSupport()) {
      messagePresenter.createMessage(arrIds, data, type, position);
    } else {
      addCommentZendesk(data);
    }
  }

  public void sendMyMessageToAdapter(Message pendingMessage) {
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

  public void onStartRecording() {
    messageAdapter.onStartRecording();

    layoutManager.findViewByPosition(0);
  }

  public void setShortcut(Shortcut shortcut) {
    this.shortcut = shortcut;
  }

  /**
   * MESSAGE RECEPTION
   */

  private boolean isDisplayedMessageDisk = false;
  private boolean successLoadingMessage = false;

  @Override public void successLoadingBetweenTwoDateMessage(List<Message> messages) {
    Timber.i("successLoadingBetweenTwoDateMessage " + messages.size());
  }

  @Override public void successLoadingMessage(List<Message> messages) {
    Timber.i("successLoadingMessage " + messages.size());
    successLoadingMessage = true;
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
    Timber.i("successLoadingMessageDisk " + messages.size());
    if (errorLoadingMessages || !successLoadingMessage) {
      Timber.i("message disk displayed " + messages.size());
      messageAdapter.setItems(messages, 0);
      scrollListToBottom();
      isDisplayedMessageDisk = true;
    }
  }

  @Override public void successMessageCreated(Message message, int position) {
    Timber.i("successMessageCreated " + message.getId());
    messageAdapter.updateItem(messageAdapter.getItemCount() - 1, message);
  }

  /**
   * ERROR NETWORK
   */

  @Override public void errorLoadingMessageDisk() {
    Timber.w("errorLoadingMessageDisk");
  }

  @Override public void errorMessageCreation(int position) {

  }

  @Override public void errorLoadingMessage() {
    Timber.w("errorLoadingMessage");
    errorLoadingMessages = true;
  }

  public void successMessageReceived(List<Message> messages) {
    messageAdapter.setItem(messages.get(0));
    context.startService(WSService.getCallingSubscribeChat(context, CHAT_SUBSCRIBE_IMREADING,
        JsonUtils.arrayToJson(arrIds)));
    scrollListToBottom();
  }

  @Override public void errorRemovedMessage(Message m) {
    Toast toast =
        Toast.makeText(context, R.string.onboarding_code_error_status, Toast.LENGTH_SHORT);
    toast.show();
  }

  @Override public void successRemovedMessage(Message m) {
    messageAdapter.removeItem(m);
  }

  @Override public void onShortcutCreatedSuccess(Shortcut shortcut) {

  }

  @Override public void onShortcutCreatedError() {

  }

  @Override public void onShortcutRemovedSuccess() {

  }

  @Override public void onShortcutRemovedError() {

  }

  @Override public void onShortcutUpdatedSuccess(Shortcut shortcut, BaseListViewHolder viewHolder) {

  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  @Override public void isTypingEvent(String userId, boolean typeEvent) {
    if (userId.equals(user.getId())) {
      return;
    }
    for (User u : members) {
      if (u.getId().equals(userId)) {
        if (!u.isActive()) {
          u.setActive(true);
          u.setTyping(typeEvent);
          u.setIsOnline(true);
          if (showOnlineUsers()) {
            expendRecyclerViewGrp();
          }
          int pos = chatUserAdapter.getIndexOfUser(u);
          chatUserAdapter.notifyItemChanged(pos, u);
        }

        if (subscriptionList.get(userId) == null) {
          Subscription subscribe = Observable.interval(10, TimeUnit.SECONDS)
              .timeInterval()
              .observeOn(AndroidSchedulers.mainThread())
              .onBackpressureDrop()
              .subscribe(avoid -> {
                // Timber.w("CLOCK ==> : " + avoid.getValue() + " " + u.toString());
                if (u.isActive()) {
                  u.setActive(false);
                  if (showOnlineUsers()) {
                    shrankRecyclerViewGrp();
                  }
                  int i = chatUserAdapter.getIndexOfUser(u);
                  chatUserAdapter.notifyItemChanged(i, u);
                }
              });

          subscriptionList.put(userId, subscribe);
          subscriptions.add(subscribe);
        }
      }
    }
  }

  @Override public void isReadingUpdate(String userId) {
    for (ShortcutLastSeen shortcutLastSeen : shortcut.getShortcutLastSeen()) {
      if (shortcutLastSeen.getUserId().equals(userId)) {
        shortcutLastSeen.setDate(dateUtils.getUTCDateAsString());
      }
    }
    setShortcut(shortcut);
    notifyDataSetChanged();
  }

  private void shrankRecyclerViewGrp() {
    //   containerUsers.setVisibility(GONE);
  }

  private void expendRecyclerViewGrp() {
    //  containerUsers.setVisibility(VISIBLE);
  }

  private boolean showOnlineUsers() {
    return members.size() < 2 && fromShortcut == null;
  }

  public Observable<Integer> onScrollRecyclerView() {
    return onScrollRecyclerView;
  }

  public void onReceiveZendeskNotif() {
    getCommentZendesk();
  }
}