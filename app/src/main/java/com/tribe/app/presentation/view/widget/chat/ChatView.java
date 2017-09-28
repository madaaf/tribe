package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.adapterDelegate.MessageAdapter;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_EMOJI;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_IMAGE;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_TEXT;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatView extends FrameLayout implements ChatMVPView {

  final public static int FROM_CHAT = 0;
  final public static int FROM_LIVE = 1;
  private final static String TYPE_NORMAL = "TYPE_NORMAL";
  private final static String TYPE_LIVE = "TYPE_LIVE";
  private final static String TYPE_ONLINE = "TYPE_ONLINE";

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private int type;
  private MessageAdapter messageAdapter;
  private ChatUserAdapter chatUserAdapter;
  private LinearLayoutManager layoutManager, layoutManagerGrp;
  private List<Message> items = new ArrayList<>();
  private List<User> members = new ArrayList<>();
  private TreeSet<Message> unreadDiskMessages = new TreeSet<>((o1, o2) -> {
    DateTimeFormatter parser = ISODateTimeFormat.dateTimeParser();
    DateTime d1 = parser.parseDateTime(o1.getCreationDate());
    DateTime d2 = parser.parseDateTime(o2.getCreationDate());
    return d1.compareTo(d2);
  });
  private Set<Message> diskMessages = new HashSet<>();
  private boolean editTextChange = false, isHeart = false;
  private String[] arrIds;

  @BindView(R.id.editText) EditTextFont editText;
  @BindView(R.id.recyclerViewChat) RecyclerView recyclerView;
  @BindView(R.id.recyclerViewGrp) RecyclerView recyclerViewGrp;
  @BindView(R.id.uploadBtn) ImageView uploadImageBtn;
  @BindView(R.id.sendBtn) ImageView sendBtn;
  @BindView(R.id.videoCallBtn) ImageView videoCallBtn;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;
  @BindView(R.id.viewAvatar) AvatarView avatarView;
  @BindView(R.id.refExpended) FrameLayout refExpended;
  @BindView(R.id.refInit) FrameLayout refInit;
  @BindView(R.id.txtTitle) TextViewFont title;

  @BindView(R.id.topbar) FrameLayout topbar;
  @BindView(R.id.containerUsers) FrameLayout containerUsers;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.containerEditText) RelativeLayout containerEditText;

  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject RxImagePicker rxImagePicker;
  @Inject DateUtils dateUtils;
  @Inject ScreenUtils screenUtils;

  private CompositeSubscription subscriptions = new CompositeSubscription();

  public ChatView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public ChatView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChatView);
    this.type = a.getInt(R.styleable.ChatView_chatViewType, 0);
    //setType();

    initView(context);
  }

  void initView(Context context) {
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_chat, this, true);
    unbinder = ButterKnife.bind(this);

    initRecyclerView();
    initDependencyInjector();
    initSubscriptions();
  }

  public void setChatId(List<User> friends, Shortcut shortcut) {

    if (type == (FROM_LIVE)) {
      topbar.setVisibility(GONE);
      containerUsers.setVisibility(GONE);
      pulseLayout.setVisibility(INVISIBLE);
      container.setBackground(null);
      uploadImageBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_chat_upload_white));
      videoCallBtn.setVisibility(GONE);
      sendBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_like_heart_white));
      editText.setBackground(
          ContextCompat.getDrawable(context, R.drawable.shape_rect_chat_black10));
      containerEditText.setBackground(
          ContextCompat.getDrawable(context, R.drawable.background_blur));
      editText.setTextColor(ContextCompat.getColor(context, R.color.white));
    }

    avatarView.load(friends.get(0).getProfilePicture());
    List<String> userIds = new ArrayList<>();
    for (User friend : friends) {
      userIds.add(friend.getId());
    }
    this.members = friends;
    arrIds = userIds.toArray(new String[userIds.size()]);
    if (friends.size() > 1) {
      title.setText(context.getString(R.string.shortcut_members_count, friends.size()));
      title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.picto_edit_chat, 0);
    } else {
      title.setText(friends.get(0).getDisplayName());
      title.setTextColor(Color.BLACK);
    }

    messagePresenter.loadMessagesDisk(arrIds, dateUtils.getUTCDateAsString());
    messagePresenter.loadMessage(arrIds, dateUtils.getUTCDateAsString());
    messagePresenter.getDiskShortcut(shortcut.getId());
    messagePresenter.getIsTyping();
  }

  private void sendPicture(Uri uri, int position) {
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(uri);
      StorageReference riversRef = storageRef.child(
          "app/uploads/" + user.getId() + "/" + dateUtils.getUTCDateAsString() + ".jpg");
      UploadTask uploadTask = riversRef.putStream(inputStream);

      uploadTask.addOnFailureListener(exception -> {
        Timber.e(exception.getMessage());
      }).addOnSuccessListener(taskSnapshot -> {
        Uri downloadUrl = taskSnapshot.getDownloadUrl();
        Timber.e("downloadUrl " + downloadUrl);
        sendMessage(arrIds, downloadUrl.toString(), MessageRealm.IMAGE, position);
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Timber.e("error load image " + e.toString());
    }
  }

  private void sendMessage(String[] arrIds, String data, String type, int position) {
    messagePresenter.createMessage(arrIds, data, type, position);
  }

  private void initSubscriptions() {

    subscriptions.add(RxView.clicks(uploadImageBtn)
        .delay(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(aVoid -> DialogFactory.showBottomSheetForCamera(context), ((aVoid, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
              sendItemToAdapter(MESSAGE_IMAGE, null, uri);
            }));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
              sendItemToAdapter(MESSAGE_IMAGE, null, uri);
            }));
          }

          return null;
        }))
        .subscribe());

    subscriptions.add(
        RxTextView.textChanges(editText).map(CharSequence::toString).subscribe(text -> {
          if (text.isEmpty()) {
            editText.setHint("Aa");
            isHeart = true;
            editTextChange = false;
            sendBtn.animate().setDuration(200).alpha(0f).withEndAction(() -> {
              if (type == FROM_LIVE) {
                sendBtn.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.picto_like_heart_white));
              } else {
                sendBtn.setImageDrawable(
                    ContextCompat.getDrawable(context, R.drawable.picto_like_heart));
              }
              sendBtn.animate().setDuration(200).alpha(1f).start();
            }).start();

            // shrankEditText();
          } else if (!text.isEmpty() && !editTextChange) {
            editTextChange = true;
            isHeart = false;
            sendBtn.animate().setDuration(200).alpha(0f).withEndAction(() -> {
              sendBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_chat_send));
              sendBtn.animate().setDuration(200).alpha(1f).start();

              // expendEditText();
            }).start();
          }
        }));

    editText.setOnEditorActionListener((v, actionId, event) -> {
      boolean handled = false;
      if (actionId == EditorInfo.IME_ACTION_SEND) {
        Timber.e("SOEF SEND");
        sendMessage();
        handled = true;
      }
      return handled;
    });
  }

  private void expendEditText() {
    editText.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            Timber.e("SOEF expendEditText "
                + refExpended.getWidth()
                + " "
                + editText.getWidth()
                + " "
                + editText.getHeight());
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(300);
            a.setInterpolator(new LinearInterpolator());
            a.setAnimationListener(new AnimationListenerAdapter() {
              @Override public void onAnimationStart(Animation animation) {
                uploadImageBtn.setVisibility(GONE);
              }
            });
            a.setParams(editText.getWidth(), refExpended.getWidth(), editText.getHeight(),
                editText.getHeight());
            editText.startAnimation(a);
          }
        });
  }

  private void shrankEditText() {
    editText.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            Timber.e("SOEF shrankEditText "
                + refExpended.getWidth()
                + " "
                + editText.getWidth()
                + " "
                + editText.getHeight());
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(300);
            a.setInterpolator(new LinearInterpolator());
            a.setAnimationListener(new AnimationListenerAdapter() {
              @Override public void onAnimationEnd(Animation animation) {
                //uploadImageBtn.setVisibility(VISIBLE);
              }
            });
            a.setParams(editText.getWidth(), refInit.getWidth(), editText.getHeight(),
                editText.getHeight());
            editText.startAnimation(a);
          }
        });
  }

  boolean load = false;

  private void initRecyclerView() {
    layoutManager = new LinearLayoutManager(getContext());
    messageAdapter = new MessageAdapter(getContext(), type);
    layoutManager.setStackFromEnd(true);
/*
    layoutManager.setReverseLayout(true);*/
    recyclerView.setItemAnimator(null);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(messageAdapter);

    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        if (dy < 0) {
          if (layoutManager.findFirstVisibleItemPosition() < 3 && !load) {
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

    layoutManagerGrp = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    chatUserAdapter = new ChatUserAdapter(getContext());
    recyclerViewGrp.setLayoutManager(layoutManagerGrp);
    recyclerViewGrp.setItemAnimator(new DefaultItemAnimator());
    recyclerViewGrp.setAdapter(chatUserAdapter);
  }

  private void populateUsersHorizontalList() {
    chatUserAdapter.setItems(members);
  }

  private void sendItemToAdapter(@Message.Type String type, String content, Uri uri) {
    Message message = null;
    String realmType = null;

    switch (type) {
      case MESSAGE_TEXT:
        realmType = MessageRealm.TEXT;
        message = new MessageText();
        ((MessageText) message).setMessage(content);
        break;
      case MESSAGE_EMOJI:
        realmType = MessageRealm.EMOJI;
        message = new MessageEmoji(content);
        ((MessageEmoji) message).setEmoji(content);
        break;
      case MESSAGE_IMAGE:
        realmType = MessageRealm.IMAGE;
        message = new MessageImage();
        Image o = new Image();
        o.setUrl(uri.toString());
        ((MessageImage) message).setOriginal(o);
        ((MessageImage) message).setUri(uri);
        break;
    }

    message.setType(type);
    message.setAuthor(user);
    message.setCreationDate(dateUtils.getUTCDateAsString());
    message.setPending(true);
    message.setId("PENDING_");//+ UUID.randomUUID()
    items.add(message);
    messageAdapter.setItems(items);
    int position = messageAdapter.getIndexOfMessage(message);
    if (type.equals(MESSAGE_IMAGE)) {
      sendPicture(uri, position);
    } else {
      sendMessage(arrIds, content, realmType, position);
    }
    scrollListToBottom();
  }

  private void dispose() {
    pulseLayout.clearAnimation();
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

  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    messagePresenter.onViewAttached(this);
    populateUsersHorizontalList();
    setAnimation(TYPE_NORMAL);
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    dispose();
    super.onDetachedFromWindow();
  }

  @OnClick(R.id.sendBtn) void onClickSend() {
    recyclerView.post(() -> recyclerView.scrollToPosition(messageAdapter.getItemCount()));
    if (!isHeart) {
      String m = editText.getText().toString();
      String editedMessage = m.replaceAll("\n", "\"n");
      if (!editedMessage.isEmpty()) {
        if (StringUtils.isOnlyEmoji(editedMessage)) {
          sendItemToAdapter(MESSAGE_EMOJI, editedMessage, null);
        } else {
          sendItemToAdapter(MESSAGE_TEXT, editedMessage, null);
        }
      }
      editText.setText("");
    } else {
      sendMessage();
    }
  }

  private void sendMessage() {
    recyclerView.post(() -> recyclerView.scrollToPosition(messageAdapter.getItemCount()));
    editText.setText("");
    sendBtn.animate()
        .scaleX(1.3f)
        .scaleY(1.3f)
        .setDuration(300)
        .withEndAction(() -> sendBtn.animate().scaleX(1f).scaleY(1f).setDuration(300).start())
        .start();
    sendItemToAdapter(MESSAGE_EMOJI, "\u2764", null);
  }

  @OnTouch(R.id.editText) boolean onClickEditText() {
    editText.setHint("Message");
    scrollListToBottom();
    return false;
  }

  @OnTouch(R.id.container) boolean onClickRecyclerView() {
    screenUtils.hideKeyboard(this);
    return false;
  }

  @Override public void successLoadingMessage(List<Message> messages) {
    Timber.e("SOEF successLoadingMessage " + messages.size());
    load = false;
  }

  @Override public void errorLoadingMessage() {
    Timber.e("SOEF errorLoadingMessage");
  }

  private void scrollListToBottom() {
    //recyclerView.post(() -> recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()),100);
    recyclerView.post(() -> recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()));
  }

  @Override public void successLoadingMessageDisk(List<Message> messasges) {

    Set<Message> adapterList = new HashSet<>();
    adapterList.addAll(messageAdapter.getItems());

    for (Message m : messasges) {
      if (!adapterList.contains(m)) {
        unreadDiskMessages.add(m);
      }
    }
    Timber.e(
        "SOEF successLoadingMessageDisk " + messasges.size() + " " + unreadDiskMessages.size());
    if (!unreadDiskMessages.isEmpty()) {
      messageAdapter.setItems(unreadDiskMessages);
      int index = messageAdapter.getIndexOfMessage(unreadDiskMessages.last());
      recyclerView.post(() -> {
        Timber.e("smooth scroll to position " + (index + 1));
        recyclerView.smoothScrollToPosition(index + 1);
      });
      unreadDiskMessages.clear();
    }
  }

  private void setAnimation(String type) {
    switch (type) {
      case TYPE_NORMAL:
        videoCallBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_chat_video));
        pulseLayout.stop();
        break;
      case TYPE_LIVE:
        videoCallBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_chat_video_red));
        pulseLayout.setColor(ContextCompat.getColor(context, R.color.red_pulse));
        pulseLayout.start();
        break;
      case TYPE_ONLINE:
        videoCallBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_chat_video_live));
        pulseLayout.setColor(ContextCompat.getColor(context, R.color.blue_new));
        pulseLayout.start();
        break;
    }
  }

  @Override public void errorLoadingMessageDisk() {
    Timber.e("SOEF errorLoadingMessageDisk");
  }

  @Override public void isTypingEvent(String userId) {
    Timber.e("SOEF IS TYPING " + userId);
  }

  @Override public void successMessageCreated(Message message, int position) {
    Timber.e("SOEF successMessageCreated " + message.toString());
    messageAdapter.notifyItemChanged(position, message);
  }

  @Override public void errorMessageCreation() {
    Timber.e("SOEF errorMessageCreation");
  }

  @Override public void successShortcutUpdate(Shortcut shortcut) {
   /* for (User u : shortcut.getMembers()) {
      Timber.e("SHORTCUT SOEF " + u.getDisplayName() + " " + u.isOnline());
    }
    Timber.e("SHORTCUT id " + shortcut.getId() + " " + shortcut.isOnline());*/
    chatUserAdapter.setItems(shortcut.getMembers());
    if (shortcut.isLive()) {
      setAnimation(TYPE_LIVE);
    } else if (shortcut.isOnline()) {
      setAnimation(TYPE_ONLINE);
    } else {
      setAnimation(TYPE_NORMAL);
    }
  }

  @Override public void errorShortcutUpdate() {
    Timber.e("errorShortcutUpdateHORTCUT SOEF ");
  }
}
