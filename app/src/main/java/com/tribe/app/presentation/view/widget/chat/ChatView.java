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
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
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
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Shortcut;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.navigation.Navigator;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.listener.AnimationListenerAdapter;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import com.tribe.tribelivesdk.util.JsonUtils;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.data.network.WSService.CHAT_SUBSCRIBE_IMTYPING;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_EMOJI;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_IMAGE;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_TEXT;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatView extends ChatMVPView {

  private final static int INTERVAL_IM_TYPING = 2;
  public final static int FROM_CHAT = 0;
  public final static int FROM_LIVE = 1;
  private final static String TYPE_NORMAL = "TYPE_NORMAL";
  private final static String TYPE_LIVE = "TYPE_LIVE";
  private final static String TYPE_ONLINE = "TYPE_ONLINE";

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private ChatUserAdapter chatUserAdapter;
  private LinearLayoutManager layoutManagerGrp;
  private List<User> members = new ArrayList<>();

  private String editTextString;
  private int type, widthRefExpended, widthRefInit, containerUsersHeight, refMaxExpendedWidth;
  private boolean editTextChange = false, isHeart = false;
  private String[] arrIds = null;
  private Shortcut shortcut;
  private Recipient recipient;

  @BindView(R.id.editText) EditTextFont editText;
  @BindView(R.id.recyclerViewChat) RecyclerMessageView recyclerView;
  @BindView(R.id.recyclerViewGrp) RecyclerView recyclerViewGrp;
  @BindView(R.id.uploadBtn) ImageView uploadImageBtn;
  @BindView(R.id.sendBtn) ImageView sendBtn;
  @BindView(R.id.videoCallBtn) ImageView videoCallBtn;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;
  @BindView(R.id.viewAvatar) AvatarView avatarView;
  @BindView(R.id.refExpended) FrameLayout refExpended;
  @BindView(R.id.refMaxExpended) FrameLayout refMaxExpended;
  @BindView(R.id.refInit) FrameLayout refInit;
  @BindView(R.id.txtTitle) TextViewFont title;

  @BindView(R.id.topbar) FrameLayout topbar;
  @BindView(R.id.containerUsers) FrameLayout containerUsers;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.containerEditText) RelativeLayout containerEditText;
  @BindView(R.id.separator) View separator;
  @BindView(R.id.voiceNoteBtn) View voiceNoteBtn;
  @BindView(R.id.recordingView) FrameLayout recordingView;
  @BindView(R.id.pictoVoiceNote) ImageView pictoVoiceNote;

  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject RxImagePicker rxImagePicker;
  @Inject DateUtils dateUtils;
  @Inject ScreenUtils screenUtils;
  @Inject Navigator navigator;

  private CompositeSubscription subscriptions = new CompositeSubscription();
  private Map<String, Subscription> callDurationSubscription = new HashMap<>();

  public ChatView(@NonNull Context context) {
    super(context);
    initView(context);
  }

  public ChatView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChatView);
    this.type = a.getInt(R.styleable.ChatView_chatViewType, 0);
    initView(context);
  }

  void initView(Context context) {
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_chat, this, true);
    unbinder = ButterKnife.bind(this);
    Timber.w("SOEF  INIT VIEW");
    initRecyclerView();
    initDependencyInjector();
    initSubscriptions();
    initParams();
  }

  public int getType() {
    return type;
  }

  public void setChatId(List<User> friends, Shortcut shortcut, Recipient recipient) {
    this.recipient = recipient;
    this.shortcut = shortcut;
    setTypeChatUX();
    avatarView.load(friends.get(0).getProfilePicture());
    List<String> userIds = new ArrayList<>();
    for (User friend : friends) {
      userIds.add(friend.getId());
    }
    this.members = friends;
    this.arrIds = userIds.toArray(new String[userIds.size()]);
    recyclerView.setArrIds(arrIds);

    if (friends.size() > 1) {
      String txt = context.getString(R.string.shortcut_members_count, friends.size()) + " ";
      title.setText(txt);
      title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.picto_edit_chat, 0);
    } else {
      title.setText(friends.get(0).getDisplayName());
      title.setTextColor(Color.BLACK);
    }
  }

  public void onResumeView() {
    Timber.w(" SOEF SET CHAT ID AND CALL PRESENTER ");
    if (arrIds == null) {
      return;
    }
    context.startService(WSService.getCallingSubscribeChat(context, WSService.CHAT_SUBSCRIBE,
        JsonUtils.arrayToJson(arrIds)));
    if (shortcut != null) messagePresenter.getDiskShortcut(shortcut.getId());
    messagePresenter.getIsTyping();
    recyclerView.onResumeView();
  }

  public void dispose() {
    if (arrIds != null) {
      context.startService(
          WSService.getCallingUnSubscribeChat(context, JsonUtils.arrayToJson(arrIds)));
    }
  }

  private void initParams() {
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        widthRefExpended = refExpended.getWidth();
        widthRefInit = refInit.getWidth();
        refMaxExpendedWidth = refMaxExpended.getWidth();
        containerUsersHeight = containerUsers.getHeight();

        int size = editText.getHeight() - screenUtils.dpToPx(8);
        voiceNoteBtn.getLayoutParams().height = size;
        voiceNoteBtn.getLayoutParams().width = size;

        voiceNoteBtn.setTranslationX(
            editText.getX() + editText.getWidth() - voiceNoteBtn.getWidth() - screenUtils.dpToPx(
                5));
        voiceNoteBtn.setTranslationY(-editText.getHeight() + (voiceNoteBtn.getHeight() / 2));

        pictoVoiceNote.setTranslationX(
            voiceNoteBtn.getX() + (voiceNoteBtn.getWidth() / 2) - (pictoVoiceNote.getWidth() / 2));

        pictoVoiceNote.setTranslationY(-editText.getHeight() + (voiceNoteBtn.getHeight() / 2) - (pictoVoiceNote.getHeight()/2));
        //+ (pictoVoiceNote.getHeight() / 2)
        voiceNoteBtn.setOnClickListener(view -> onClickVoiceNote());

        float transX =
            voiceNoteBtn.getX() + (voiceNoteBtn.getWidth() / 2) - (recordingView.getWidth() / 2);

        recordingView.setTranslationX(transX);
        recordingView.setTranslationY(recordingView.getHeight());

        if (members.size() < 2) {
          containerUsers.setVisibility(GONE);
        }
      }
    });
  }

  private void setTypeChatUX() {
    if (type == (FROM_LIVE)) {
      topbar.setVisibility(GONE);
      containerUsers.setVisibility(GONE);
      pulseLayout.getLayoutParams().height = 0;
      pulseLayout.getLayoutParams().width = 0;
      container.setBackground(null);
      uploadImageBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_chat_upload_white));
      videoCallBtn.setVisibility(GONE);
      sendBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_like_heart_white));
      separator.setVisibility(GONE);

    /*  editText.setBackground(
          ContextCompat.getDrawable(context, R.drawable.shape_rect_chat_black10));*/
    /*  containerEditText.setBackground(
          ContextCompat.getDrawable(context, R.drawable.background_blur));*/
      editText.setTextColor(ContextCompat.getColor(context, R.color.white));
    }
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
        recyclerView.sendMessageToNetwork(arrIds, downloadUrl.toString(), MessageRealm.IMAGE,
            position);
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Timber.e("error load image " + e.toString());
    }
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

    subscriptions.add(Observable.interval(INTERVAL_IM_TYPING, TimeUnit.SECONDS)
        .timeInterval()
        .observeOn(AndroidSchedulers.mainThread())
        .onBackpressureDrop()
        .subscribe(avoid -> {
          if (!editTextString.isEmpty()) {
            //  messagePresenter.imTypingMessage(arrIds);// TODO SEE IF YOU SEND THE MESSAGE THREW SIGNALING OR API
            context.startService(WSService.getCallingSubscribeChat(context, CHAT_SUBSCRIBE_IMTYPING,
                JsonUtils.arrayToJson(arrIds)));
          }
        }));

    subscriptions.add(
        RxTextView.textChanges(editText).map(CharSequence::toString).subscribe(text -> {
          this.editTextString = text;

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

            shrankEditText();
          } else if (!text.isEmpty() && !editTextChange) {
            Timber.e("OOK " + text);
            editTextChange = true;
            isHeart = false;
            sendBtn.animate().setDuration(200).alpha(0f).withEndAction(() -> {
              sendBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_chat_send));
              sendBtn.animate().setDuration(200).alpha(1f).start();

              expendEditText();
            }).start();
          }
        }));
  }

  private void expendEditText() {
    if (type == FROM_LIVE) {
      return;
    }
    editText.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(100);
            a.setInterpolator(new LinearInterpolator());
            a.setAnimationListener(new AnimationListenerAdapter() {
              @Override public void onAnimationStart(Animation animation) {
                uploadImageBtn.setAlpha(0f);
                uploadImageBtn.setVisibility(GONE);
              }
            });
            a.setParams(editText.getWidth(), widthRefExpended, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
            editText.startAnimation(a);
          }
        });
  }

  private void shrankEditText() {
    if (type == FROM_LIVE) {
      return;
    }
    editText.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(100);
            a.setInterpolator(new LinearInterpolator());
            a.setAnimationListener(new AnimationListenerAdapter() {
              @Override public void onAnimationEnd(Animation animation) {
                uploadImageBtn.setVisibility(VISIBLE);
                uploadImageBtn.animate().setDuration(100).alpha(1f).start();
              }
            });
            a.setParams(editText.getWidth(), widthRefInit, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
            editText.startAnimation(a);
          }
        });
  }

  private void initRecyclerView() {
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
    message.setId(Message.PENDING);//+ UUID.randomUUID()
    recyclerView.sendMyMessageToAdapter(message);
    if (type.equals(MESSAGE_IMAGE)) {
      sendPicture(uri, 0);
    } else {
      recyclerView.sendMessageToNetwork(arrIds, content, realmType, 0);
    }
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
    Timber.w("onAttachedToWindow");
    super.onAttachedToWindow();
    messagePresenter.onViewAttached(this);
    populateUsersHorizontalList();
    setAnimation(TYPE_NORMAL);
    if (type == FROM_CHAT) screenUtils.showKeyboard(editText, 0);
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();
    Timber.w("DETACHED SUBSC onDetachedFromWindow");

    if (subscriptions != null && subscriptions.hasSubscriptions()) {

      Iterator it = callDurationSubscription.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        it.remove();
        Subscription sub = (Subscription) pair.getValue();
        subscriptions.remove(sub);
        sub.unsubscribe();
      }
      callDurationSubscription = null;
      subscriptions.unsubscribe();
      subscriptions.clear();
    }
    pulseLayout.clearAnimation();
    super.onDetachedFromWindow();
  }

  @OnClick(R.id.sendBtn) void onClickSend() {
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

  @OnClick(R.id.txtTitle) void onClickTitle() {
    if (members.size() < 2) return;
    subscriptions.add(
        DialogFactory.inputDialog(context, context.getString(R.string.shortcut_update_name_title),
            context.getString(R.string.shortcut_update_name_description),
            context.getString(R.string.shortcut_update_name_validate),
            context.getString(R.string.action_cancel), InputType.TYPE_CLASS_TEXT).subscribe(s -> {
          Timber.e("SOU SUH " + s);
          messagePresenter.updateShortcutName(shortcut.getId(), s);
          title.setText(s + " ");
          title.setTextColor(Color.BLACK);
        }));
  }

  private void onClickVoiceNote() {
    Timber.e("VOICE NOTE TAP");
    voiceNoteBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.shape_circle_blue));
    voiceNoteBtn.animate()
        .scaleX(2f)
        .scaleY(2f)
        .setInterpolator(new OvershootInterpolator())
        .setDuration(300)
        .withStartAction(() -> {
          sendBtn.setVisibility(GONE);
          uploadImageBtn.setVisibility(GONE);
          pulseLayout.setVisibility(GONE);
          recordingView.animate()
              .translationY(-(recordingView.getHeight() * 2))
              .setInterpolator(new OvershootInterpolator())
              .setDuration(300)
              .start();
          editText.setHint("Slide to cancel");
        })
        .start();

    editText.getViewTreeObserver().
        addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(500);
            a.setInterpolator(new LinearInterpolator());
            a.setAnimationListener(new AnimationListenerAdapter() {
              @Override public void onAnimationStart(Animation animation) {
                uploadImageBtn.setAlpha(0f);
                uploadImageBtn.setVisibility(GONE);
              }
            });
            a.setParams(editText.getWidth(), LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
            editText.startAnimation(a);
          }
        });
  }

  @OnClick(R.id.videoCallBtn) void onClickVideoCall() {

    navigator.navigateToLive((Activity) context, recipient, PaletteGrid.get(0),
        LiveActivity.SOURCE_GRID);
  }

  private void sendMessage() {
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
    return false;
  }

  @OnTouch(R.id.container) boolean onClickRecyclerView() {
    screenUtils.hideKeyboard(this);
    return false;
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
        pulseLayout.setColor(ContextCompat.getColor(context, R.color.blue_new_opacity_40));
        pulseLayout.start();
        break;
    }
  }

  private void shrankRecyclerViewGrp() {
    containerUsers.setVisibility(VISIBLE);
    containerUsers.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            containerUsers.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ResizeAnimation a = new ResizeAnimation(containerUsers);
            a.setDuration(300);
            a.setInterpolator(new LinearInterpolator());
            a.setParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, containerUsersHeight,
                0);
            containerUsers.startAnimation(a);
          }
        });
  }

  private void expendRecyclerViewGrp() {
    containerUsers.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            containerUsers.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ResizeAnimation a = new ResizeAnimation(containerUsers);
            a.setDuration(300);
            a.setInterpolator(new LinearInterpolator());
            a.setParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0,
                containerUsersHeight);
            containerUsers.startAnimation(a);
          }
        });
  }

  @Override public void isTypingEvent(String userId) {
    if (userId.equals(user.getId())) {
      return;
    }
    for (User u : members) {
      if (u.getId().equals(userId)) {
        if (!u.isTyping()) {
          u.setTyping(true);
          u.setIsOnline(true);
          if (members.size() < 2) {
            expendRecyclerViewGrp();
          }
          Timber.i("START TYPING");
          int pos = chatUserAdapter.getIndexOfUser(u);
          chatUserAdapter.notifyItemChanged(pos, u);
        }

        if (callDurationSubscription.get(userId) == null) {
          Subscription ok = Observable.interval(10, TimeUnit.SECONDS)
              .timeInterval()
              .observeOn(AndroidSchedulers.mainThread())
              .onBackpressureDrop()
              .subscribe(avoid -> {
                Timber.w("CLOCK ==> : " + avoid.getValue() + " " + u.toString());
                if (u.isTyping()) {
                  Timber.i("STOP TYPING");
                  u.setTyping(false);
                  if (members.size() < 2) {
                    shrankRecyclerViewGrp();
                  }
                  int i = chatUserAdapter.getIndexOfUser(u);
                  chatUserAdapter.notifyItemChanged(i, u);
                }
              });

          callDurationSubscription.put(userId, ok);
          subscriptions.add(ok);
        }
      }
    }
  }

  @Override public void successShortcutUpdate(Shortcut shortcut) {
    Timber.e(
        "SHORTCUT " + shortcut.isOnline() + " " + shortcut.isLive() + " " + shortcut.toString());

    for (User ok : shortcut.getMembers()) {
      Timber.e("SHORTCUT MEM + " + ok.toString());
    }
    boolean isOnline = false;
    boolean isLive = false;
    for (User u : shortcut.getMembers()) {
      if (u.isOnline()) isOnline = true;
    }
    chatUserAdapter.setItems(shortcut.getMembers());

    if (shortcut.isLive()) {
      setAnimation(TYPE_LIVE);
    } else if (shortcut.isOnline() || isOnline) {
      setAnimation(TYPE_ONLINE);
    } else {
      setAnimation(TYPE_NORMAL);
    }
  }

  @Override public void errorShortcutUpdate() {
    Timber.e("errorShortcutUpdateHORTCUT SOEF ");
  }
}
