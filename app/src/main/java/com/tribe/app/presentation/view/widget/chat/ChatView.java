package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;
import com.f2prateek.rx.preferences.Preference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tbruyelle.rxpermissions.RxPermissions;
import com.tribe.app.R;
import com.tribe.app.data.network.WSService;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.data.realm.ShortcutRealm;
import com.tribe.app.domain.ShortcutLastSeen;
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
import com.tribe.app.presentation.utils.PermissionUtils;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.utils.analytics.TagManager;
import com.tribe.app.presentation.utils.analytics.TagManagerUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.utils.preferences.ChatShortcutData;
import com.tribe.app.presentation.utils.preferences.PreferencesUtils;
import com.tribe.app.presentation.view.activity.LiveActivity;
import com.tribe.app.presentation.view.adapter.viewholder.BaseListViewHolder;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.ResizeAnimation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.StateManager;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import com.tribe.app.presentation.view.widget.avatar.NewAvatarView;
import com.tribe.app.presentation.view.widget.chat.model.Image;
import com.tribe.app.presentation.view.widget.chat.model.Message;
import com.tribe.app.presentation.view.widget.chat.model.MessageAudio;
import com.tribe.app.presentation.view.widget.chat.model.MessageEmoji;
import com.tribe.app.presentation.view.widget.chat.model.MessageImage;
import com.tribe.app.presentation.view.widget.chat.model.MessageText;
import com.tribe.tribelivesdk.util.JsonUtils;
import com.wang.avi.AVLoadingIndicatorView;
import java.io.File;
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

import static com.tribe.app.data.network.WSService.CHAT_SUBSCRIBE_IMTALKING;
import static com.tribe.app.data.network.WSService.CHAT_SUBSCRIBE_IMTYPING;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_AUDIO;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_EMOJI;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_IMAGE;
import static com.tribe.app.presentation.view.widget.chat.model.Message.MESSAGE_TEXT;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatView extends ChatMVPView {

  private final static int INTERVAL_IM_TYPING = 2;
  protected static int ANIM_DURATION = 300;
  protected static int ANIM_DURATION_FAST = 150;
  protected static int ANIM_DURATION_LONG = 500;

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
  private ChatView chatView;
  private String editTextString, typePulseAnim = TYPE_NORMAL;
  protected int type, widthRefExpended, widthRefInit, containerUsersHeight, refMaxExpendedWidth,
      voiceNoteBtnX, recordingViewX, recordingViewInitWidth, voiceNoteBtnWidth;
  private boolean editTextChange = false, isHeart = false;
  private String[] arrIds = null;
  private Shortcut shortcut;
  private Recipient recipient;

  protected String fileName = null;
  public Float audioDuration = 0f;
  private Shortcut fromShortcut = null;
  private boolean hideVideoCallBtn = false;
  private Map<String, Object> tagMap;
  private String section, gesture;
  protected int heartCount = 0, textCount = 0, audioCount = 0, imageCount = 0;
  boolean onRecord = false;

  @BindView(R.id.editText) EditTextFont editText;
  @BindView(R.id.recyclerViewChat) RecyclerMessageView recyclerView;
  @BindView(R.id.recyclerViewGrp) RecyclerView recyclerViewGrp;
  @BindView(R.id.uploadBtn) ImageView uploadImageBtn;
  @BindView(R.id.sendBtn) ImageView sendBtn;
  @BindView(R.id.videoCallBtn) ImageView videoCallBtn;
  @BindView(R.id.layoutPulse) PulseLayout layoutPulse;
  @BindView(R.id.viewNewAvatar) NewAvatarView avatarView;
  @BindView(R.id.refExpended) FrameLayout refExpended;
  @BindView(R.id.refMaxExpended) FrameLayout refMaxExpended;
  @BindView(R.id.refInit) FrameLayout refInit;
  @BindView(R.id.txtTitle) TextViewFont title;
  @BindView(R.id.timerVoiceNote) TextViewFont timerVoiceNote;
  @BindView(R.id.hintEditText) TextViewFont hintEditText;

  @BindView(R.id.topbar) FrameLayout topbar;
  @BindView(R.id.containerUsers) FrameLayout containerUsers;
  @BindView(R.id.containerQuickChat) FrameLayout containerQuickChat;
  @BindView(R.id.container) FrameLayout container;
  @BindView(R.id.containerEditText) RelativeLayout containerEditText;
  @BindView(R.id.blurBackEditText) View blurBackEditText;
  @BindView(R.id.separator) View separator;
  @BindView(R.id.voiceNoteBtn) ImageView voiceNoteBtn;
  @BindView(R.id.viewRecording) View recordingView;
  @BindView(R.id.recordingView) FrameLayout recordingFrame;
  @BindView(R.id.btnSendLikeContainer) FrameLayout btnSendLikeContainer;
  @BindView(R.id.pictoVoiceNote) ImageView pictoVoiceNote;
  @BindView(R.id.trashBtn) ImageView trashBtn;
  @BindView(R.id.playBtn) ImageView playerBtn;
  @BindView(R.id.likeBtn) ImageView likeBtn;
  @BindView(R.id.loadingRecordView) AVLoadingIndicatorView loadingRecordView;
  @BindView(R.id.equalizer) ImageView equalizer;
  @BindView(R.id.pauseBtn) ImageView pauseBtn;
  @BindView(R.id.btnContainer) ImageView btnContainer;
  @BindView(R.id.cardViewIndicator) public CardView cardViewIndicator;
  @BindView(R.id.viewPlayerProgress) public View viewPlayerProgress;

  @Inject @ChatShortcutData Preference<String> chatShortcutData;
  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject RxImagePicker rxImagePicker;
  @Inject DateUtils dateUtils;
  @Inject ScreenUtils screenUtils;
  @Inject Navigator navigator;
  @Inject StateManager stateManager;
  @Inject TagManager tagManager;

  protected CompositeSubscription subscriptions = new CompositeSubscription();
  private Map<String, Subscription> subscriptionList = new HashMap<>();

  private RxPermissions rxPermissions;

  public ChatView(@NonNull Context context, int type) {
    super(context);
    this.type = type;
    initView(context);
  }

  public ChatView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initView(context);
  }

  void initView(Context context) {
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_chat, this, true);
    unbinder = ButterKnife.bind(this);
    tagMap = new HashMap<>();

    initDependencyInjector();
    initRecyclerView();
    initSubscriptions();
    initParams();
  }

  public int getType() {
    return type;
  }

  public void setGestureAndSection(String gesture, String section) {
    this.gesture = gesture;
    this.section = section;
  }

  public void displayBlockNotif() {
    String blockedShortcutId = null;

    for (Recipient r : user.getRecipientList()) {
      if (r instanceof Shortcut) {
        Shortcut s = (Shortcut) r;
        if (s.getMembers().size() == 1 && s.isBlocked()) {
          User u = s.getMembers().get(0);
          if (shortcut.getMembers().contains(u)) {
            blockedShortcutId = s.getId();
            break;
          }
        }
      }
    }
    if (blockedShortcutId != null) {
      subscriptions.add(
          DialogFactory.dialog(context, context.getString(R.string.mute_group_popup_title),
              context.getString(R.string.mute_group_popup_message),
              context.getString(R.string.mute_group_popup_validate),
              context.getString(R.string.mute_group_popup_cancel)).subscribe(blockGrp -> {
            if (blockGrp) {
              messagePresenter.updateShortcutStatus(shortcut.getId(), ShortcutRealm.BLOCKED);
            }
          }));
    }
  }

  public void setChatId(Shortcut shortcut, Recipient recipient) {
    this.recipient = recipient;
    this.shortcut = shortcut;
    recyclerView.setShortcut(shortcut);

    if (shortcut.getMembers() != null) {
      this.members = shortcut.getMembers();
      avatarView.load(members.get(0).getProfilePicture());
    } else {
      Timber.e("MEMBERS NULL IN SHORTCUT");
    }

    List<String> userIds = new ArrayList<>();
    for (User friend : members) {
      userIds.add(friend.getId());
    }
    displayBlockNotif();

    this.arrIds = userIds.toArray(new String[userIds.size()]);
    recyclerView.setArrIds(arrIds);
    setTitle();
  }

  private void setTitle() {
    if (members.size() > 1) {
      if (shortcut.getName() != null) {
        title.setTextColor(Color.BLACK);
        title.setText(shortcut.getName() + " ");
      } else {
        String txt =
            (context.getString(R.string.shortcut_members_count, (members.size() + 1)) + " ");
        title.setText(txt);
      }
      title.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.picto_edit_chat, 0);
    } else {
      title.setText(members.get(0).getDisplayName());
      title.setTextColor(Color.BLACK);
    }
  }

  public void onResumeView() {
    if (arrIds == null) {
      return;
    }
    recyclerView.onResumeView();
    context.startService(WSService.getCallingSubscribeChat(context, WSService.CHAT_SUBSCRIBE,
        JsonUtils.arrayToJson(arrIds)));
    messagePresenter.updateShortcutForUserIds(arrIds);
    messagePresenter.getIsTyping();
    messagePresenter.getIsTalking();
    messagePresenter.getIsReading();
  }

  public void dispose() {
    if (arrIds != null) {
      context.startService(
          WSService.getCallingUnSubscribeChat(context, JsonUtils.arrayToJson(arrIds)));
      messagePresenter.onViewDetached();
      recyclerView.onDetachedFromWindow();
    }
  }

  private void initParams() {
    chatView = this;
    rxPermissions = new RxPermissions((Activity) context);

    loadingRecordView.setIndicator("LineScalePulseOutIndicator");
    loadingRecordView.show();

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        equalizer.setVisibility(GONE);
        pauseBtn.setVisibility(GONE);
        recyclerView.setOnTouchListener((view, motionEvent) -> {
          screenUtils.hideKeyboard((Activity) context);
          return false;
        });

        playerBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_recording_voice));
        widthRefExpended = refExpended.getWidth();
        widthRefInit = refInit.getWidth();

        editText.getLayoutParams().width = widthRefInit;
        editText.getLayoutParams().height = LayoutParams.WRAP_CONTENT;

        refMaxExpendedWidth = refMaxExpended.getWidth();
        containerUsersHeight = containerUsers.getHeight();
        recordingViewInitWidth = recordingFrame.getWidth();

        voiceNoteBtnWidth = editText.getHeight() - screenUtils.dpToPx(8);
        voiceNoteBtn.getLayoutParams().height = voiceNoteBtnWidth;
        voiceNoteBtn.getLayoutParams().width = voiceNoteBtnWidth;

        voiceNoteBtn.setTranslationX(
            editText.getX() + widthRefInit - voiceNoteBtn.getWidth() - screenUtils.dpToPx(5));
        voiceNoteBtn.setTranslationY(
            -editText.getHeight() + voiceNoteBtn.getHeight() - screenUtils.dpToPx(7));

        pictoVoiceNote.setTranslationX(
            voiceNoteBtn.getX() + (voiceNoteBtn.getWidth() / 2) - (pictoVoiceNote.getWidth() / 2));

        pictoVoiceNote.setTranslationY(-editText.getHeight() + (voiceNoteBtn.getHeight() / 2) - (
            pictoVoiceNote.getHeight()
                / 2) + screenUtils.dpToPx(12));

        voiceNoteBtnX = (int) (voiceNoteBtn.getX());
        float transX =
            voiceNoteBtn.getX() + (voiceNoteBtn.getWidth() / 2) - (recordingView.getWidth() / 2);
        recordingView.setX(transX);
        recordingViewX = (int) transX;
        recordingView.setVisibility(INVISIBLE);

        SwipeDetector moveListener = new SwipeDetector(chatView, voiceNoteBtn, recordingView,
            trashBtn.getX() - (trashBtn.getWidth() / 2), screenUtils);

        Boolean microEnabledState = PermissionUtils.hasPermissionsMicroOnly(rxPermissions);
        if (microEnabledState) {

          voiceNoteBtn.setOnTouchListener(moveListener);
        } else {
          voiceNoteBtn.setOnTouchListener((view, motionEvent) -> {
            initVoiceCallPerm(moveListener);
            return false;
          });
        }

        Map<String, String> map = PreferencesUtils.getMapFromJsonString(chatShortcutData);

        if (map != null && shortcut != null) {
          String editTextContent = map.get(shortcut.getId());
          if (editTextContent != null && !editTextContent.isEmpty()) {
            editText.setText(editTextContent);
            hideVideoCallBtn(false);
            editText.setSelection(editText.getText().length());
          }
        }

        setTypeChatUX();

       /* if (showOnlineUsers()) {
          containerUsers.setVisibility(VISIBLE);
        } else {
          containerUsers.setVisibility(GONE);
        }*/
      }
    });
  }

  private boolean showOnlineUsers() {
    return true;
    // return members.size() < 2 && fromShortcut == null; // TODO
  }

  private void initVoiceCallPerm(SwipeDetector moveListener) {
    subscriptions.add(
        rxPermissions.requestEach(PermissionUtils.RECORD_AUDIO).subscribe(permission -> {
          if (permission.granted) {
            voiceNoteBtn.setOnTouchListener(moveListener);
          } else if (permission.shouldShowRequestPermissionRationale) {
            Timber.d("Denied micro permission without ask never again");
          } else {
            Timber.d("Denied micro permission and ask never again");
            if (!stateManager.shouldDisplay(StateManager.NEVER_ASK_AGAIN_MICRO_PERMISSION)) {

            }
            stateManager.addTutorialKey(StateManager.NEVER_ASK_AGAIN_MICRO_PERMISSION);
          }
        }));
  }

  private void setTypeChatUX() {
    if (type == (FROM_LIVE)) {
      videoCallBtn.getLayoutParams().height = 0;
      videoCallBtn.getLayoutParams().width = 0;
      videoCallBtn.setImageDrawable(null);
      pictoVoiceNote.setImageDrawable(null);
      topbar.setVisibility(GONE);
      layoutPulse.setVisibility(GONE);
      container.setBackground(null);
      widthRefInit = refInit.getWidth();

      RelativeLayout.LayoutParams op = (RelativeLayout.LayoutParams) editText.getLayoutParams();
      op.addRule(RelativeLayout.START_OF, btnSendLikeContainer.getId());

      uploadImageBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_chat_upload_white));

      sendBtn.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.picto_send_btn_white));
      likeBtn.setImageDrawable(
          ContextCompat.getDrawable(context, R.drawable.picto_like_heart_white));
      separator.setVisibility(GONE);
      voiceNoteBtn.setVisibility(GONE);
      pictoVoiceNote.setVisibility(GONE);

      blurBackEditText.setBackground(
          ContextCompat.getDrawable(context, R.drawable.background_blur));
      editText.setTextColor(ContextCompat.getColor(context, R.color.white));
    }
  }

  private void sendMedia(Uri uri, String audioFile, int position, String type) {
    String suffix = "";
    String netType = "";
    if (type.equals(MESSAGE_IMAGE)) {
      suffix = ".jpg";
      netType = MessageRealm.IMAGE;
    } else if (type.equals(MESSAGE_AUDIO)) {
      suffix = ".mp4";
      netType = MessageRealm.AUDIO;
    }
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    UploadTask uploadTask = null;
    try {
      if (type.equals(MESSAGE_IMAGE)) {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        StorageReference riversRef = storageRef.child(
            "app/uploads/" + user.getId() + "/" + dateUtils.getUTCDateAsString() + suffix);
        uploadTask = riversRef.putStream(inputStream);
      } else if (type.equals(MESSAGE_AUDIO)) {
        Uri file = Uri.fromFile(new File(audioFile));
        StorageReference riversRef = storageRef.child("app/uploads/"
            + user.getId()
            + "/"
            + dateUtils.getUTCDateAsString()
            + file.getLastPathSegment());
        uploadTask = riversRef.putFile(file);
      }

      String finalNetType = netType;
      uploadTask.addOnFailureListener(exception -> {
        Timber.e(exception.getMessage());
      }).addOnSuccessListener(taskSnapshot -> {
        Uri downloadUrl = taskSnapshot.getDownloadUrl();
        recyclerView.sendMessageToNetwork(arrIds, downloadUrl.toString(), finalNetType, position);
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
      Timber.e("error load file " + e.toString());
    }
  }

  private void initSubscriptions() {

    subscriptions.add(recyclerView.onScrollRecyclerView().subscribe(dy -> {
      if (dy < 0 && blurBackEditText.getAlpha() != 1f) {
        blurBackEditText.animate().alpha(1f).setDuration(ANIM_DURATION_FAST).start();
      } else if (blurBackEditText.getAlpha() != 0f) {
        blurBackEditText.animate().alpha(0f).setDuration(ANIM_DURATION_FAST).start();
      }
    }));

    subscriptions.add(chatUserAdapter.onQuickChat().subscribe(id -> {
      messagePresenter.quickShortcutForUserIds(id);
    }));

    subscriptions.add(RxView.clicks(uploadImageBtn)
        .delay(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(aVoid -> DialogFactory.showBottomSheetForCamera(context), ((aVoid, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
              sendMessageToAdapter(MESSAGE_IMAGE, null, uri);
              imageCount++;
            }));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
              sendMessageToAdapter(MESSAGE_IMAGE, null, uri);
              imageCount++;
            }));
          }

          return null;
        }))
        .subscribe());

    subscriptions.add(Observable.interval(INTERVAL_IM_TYPING, TimeUnit.SECONDS)
        .onBackpressureDrop()
        .timeInterval()
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(avoid -> {
          if (!editTextString.isEmpty()) {
            //  messagePresenter.imTypingMessage(arrIds);// TODO SEE IF YOU SEND THE MESSAGE THREW SIGNALING OR API
            context.startService(WSService.getCallingSubscribeChat(context, CHAT_SUBSCRIBE_IMTYPING,
                JsonUtils.arrayToJson(arrIds)));
          }

          if (onRecord) {
            context.startService(
                WSService.getCallingSubscribeChat(context, CHAT_SUBSCRIBE_IMTALKING,
                    JsonUtils.arrayToJson(arrIds)));
          }
        }));

    subscriptions.add(
        RxTextView.textChanges(editText).map(CharSequence::toString).subscribe(text -> {
          this.editTextString = text;
          if (text.isEmpty()) {
            editText.setHint("Aa");
            voiceNoteBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(ANIM_DURATION_FAST)
                .withEndAction(() -> pictoVoiceNote.setVisibility(VISIBLE))
                .start();
            isHeart = true;
            editTextChange = false;

            if (type == FROM_LIVE) {
              likeBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_like_heart_white));
              sendBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_send_btn_white));
            } else {
              likeBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_like_heart));
              sendBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_chat_send));
            }
            switchLikeToSendBtn(false);
            layoutPulse.setVisibility(VISIBLE);
          } else if (!text.isEmpty() && !editTextChange) {
            layoutPulse.setVisibility(INVISIBLE);
            voiceNoteBtn.animate()
                .scaleX(0f)
                .scaleY(0f)
                .setDuration(ANIM_DURATION_FAST)
                .withStartAction(() -> pictoVoiceNote.setVisibility(GONE))
                .start();
            editTextChange = true;
            isHeart = false;

            switchLikeToSendBtn(true);
          }
        }));
  }

  private void switchLikeToSendBtn(boolean fromLikeToSend) {
    if (!fromLikeToSend) {
      sendBtn.animate()
          .scaleX(0f)
          .scaleY(0f)
          .rotation(45)
          .setDuration(ANIM_DURATION)
          .alpha(0f)
          .withStartAction(() -> {
            shrankEditText();
            sendBtn.setRotation(0);
            sendBtn.setScaleX(1);
            sendBtn.setScaleY(1);
            sendBtn.setAlpha(1f);
            sendBtn.setVisibility(VISIBLE);

            likeBtn.setRotation(90);
            likeBtn.setScaleX(0);
            likeBtn.setScaleY(0);
            likeBtn.setAlpha(0f);
            likeBtn.setVisibility(VISIBLE);

            likeBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setDuration(ANIM_DURATION)
                .setInterpolator(new AccelerateInterpolator())
                .alpha(1f)
                .withEndAction(() -> {
                  sendBtn.setVisibility(GONE);
                })
                .start();
          })
          .start();
    } else {
      likeBtn.animate()
          .scaleX(0f)
          .scaleY(0f)
          .rotation(0)
          .setDuration(ANIM_DURATION)
          .alpha(0f)
          .withStartAction(() -> {
            likeBtn.setRotation(90);
            likeBtn.setScaleX(1);
            likeBtn.setScaleY(1);
            likeBtn.setAlpha(1f);
            likeBtn.setVisibility(VISIBLE);

            sendBtn.setRotation(45);
            sendBtn.setScaleX(0);
            sendBtn.setScaleY(0);
            sendBtn.setAlpha(0f);
            sendBtn.setVisibility(VISIBLE);

            sendBtn.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(ANIM_DURATION)
                .alpha(1f)
                .withEndAction(() -> {
                  likeBtn.setVisibility(GONE);
                })
                .start();
          })
          .withEndAction(() -> expendEditText())
          .start();
    }
  }

  private void hideVideoCallBtn(boolean withAnim) {
    videoCallBtn.setClickable(false);
    hideVideoCallBtn = true;
    videoCallBtn.getLayoutParams().height = 0;
    videoCallBtn.getLayoutParams().width = 0;
    videoCallBtn.setImageDrawable(null);
  }

  private void expendEditText() {
    if (type == FROM_LIVE) {
      return;
    }
    btnSendLikeContainer.animate()
        .translationX(videoCallBtn.getWidth())
        .setDuration(ANIM_DURATION)
        .withStartAction(() -> {
          videoCallBtn.setClickable(false);
          videoCallBtn.animate().alpha(0f).setDuration(ANIM_DURATION_FAST).withEndAction(() -> {
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(ANIM_DURATION);
            a.setInterpolator(new LinearInterpolator());
            a.setParams(editText.getWidth(), widthRefExpended, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
            editText.startAnimation(a);
          }).start();
        });
  }

  private void showVideoCallBtn(boolean withAnim) {
    videoCallBtn.setClickable(true);
    hideVideoCallBtn = false;
    videoCallBtn.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
    videoCallBtn.getLayoutParams().height = LayoutParams.WRAP_CONTENT;
    setPulseAnimation(typePulseAnim);
  }

  private void shrankEditText() {
    if (type == FROM_LIVE) {
      return;
    }
    btnSendLikeContainer.animate()
        .translationX(0)
        .setDuration(ANIM_DURATION)
        .withEndAction(
            () -> videoCallBtn.animate().alpha(1f).setDuration(ANIM_DURATION_FAST).start())
        .start();
    showVideoCallBtn(false);

    editText.getViewTreeObserver()
        .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override public void onGlobalLayout() {
            editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            ResizeAnimation a = new ResizeAnimation(editText);
            a.setDuration(ANIM_DURATION);
            a.setInterpolator(new LinearInterpolator());
            a.setParams(editText.getWidth(), widthRefInit, LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
            editText.startAnimation(a);
          }
        });
  }

  private void initRecyclerView() {
    layoutManagerGrp = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    chatUserAdapter = new ChatUserAdapter(getContext(), user, type);
    recyclerViewGrp.setLayoutManager(layoutManagerGrp);
    recyclerViewGrp.setItemAnimator(new DefaultItemAnimator());
    recyclerViewGrp.setAdapter(chatUserAdapter);
  }

  private void populateUsersHorizontalList() {
    chatUserAdapter.setItems(members);
  }

  protected void sendMessageToAdapter(@Message.Type String type, String content, Uri uri) {
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
      case MESSAGE_AUDIO:
        realmType = MessageRealm.AUDIO;
        message = new MessageAudio();
        // ((MessageAudio) message).setTime(content);
        Image m = new Image();
        m.setUrl(fileName);
        m.setDuration(audioDuration);
        ((MessageAudio) message).setOriginal(m);
        ((MessageAudio) message).setTime(content);
        break;
    }

    message.setType(type);
    message.setAuthor(user);
    message.setCreationDate(dateUtils.getUTCDateAsString());
    message.setPending(true);
    message.setId(Message.PENDING);//+ UUID.randomUUID()
    recyclerView.sendMyMessageToAdapter(message);
    if (type.equals(MESSAGE_IMAGE) || type.equals(MESSAGE_AUDIO)) {
      sendMedia(uri, fileName, 0, type);
    } else {
      String replaced = content.replace("\"", "“");
      recyclerView.sendMessageToNetwork(arrIds, replaced, realmType, 0);
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
    super.onAttachedToWindow();
    messagePresenter.onViewAttached(this);
    populateUsersHorizontalList();
    setPulseAnimation(TYPE_NORMAL);
    if (type == FROM_CHAT) screenUtils.showKeyboard(editText, 0);
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();

    if (shortcut != null) {
      Map<String, String> list = PreferencesUtils.getMapFromJsonString(chatShortcutData);
      if (list == null || list.isEmpty()) {
        list = new HashMap<>();
      }
      list.put(shortcut.getId(), editText.getText().toString());
      Gson gson = new Gson();
      String jsonString = gson.toJson(list);
      chatShortcutData.set(jsonString);
    }

    if (subscriptions != null && subscriptions.hasSubscriptions()) {

      Iterator it = subscriptionList.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry pair = (Map.Entry) it.next();
        it.remove();
        Subscription sub = (Subscription) pair.getValue();
        subscriptions.remove(sub);
        sub.unsubscribe();
      }
      subscriptionList = null;
      subscriptions.unsubscribe();
      subscriptions.clear();
    }

    layoutPulse.stop();
    layoutPulse.clearAnimation();

    tagMap.put(TagManagerUtils.EVENT, TagManagerUtils.Chats);
    if (!StringUtils.isEmpty(section)) tagMap.put(TagManagerUtils.SECTION, section);
    if (!StringUtils.isEmpty(gesture)) tagMap.put(TagManagerUtils.GESTURE, gesture);
    tagMap.put(TagManagerUtils.HEART_COUNT, heartCount);
    tagMap.put(TagManagerUtils.AUDIO_COUNT, audioCount);
    tagMap.put(TagManagerUtils.IMAGE_COUNT, imageCount);
    tagMap.put(TagManagerUtils.TEXT_COUNT, textCount);
    TagManagerUtils.manageTags(tagManager, tagMap);

    super.onDetachedFromWindow();
  }

  @OnClick(R.id.likeBtn) void onClickLike() {
    heartCount++;
    sendMessage();
  }

  @OnClick(R.id.sendBtn) void onClickSend() {
    textCount++;

    String m = editText.getText().toString();
    String editedMessage = m.replaceAll("\n", "\"n");
    if (!editedMessage.isEmpty()) {
      if (StringUtils.isOnlyEmoji(editedMessage)) {
        sendMessageToAdapter(MESSAGE_EMOJI, editedMessage, null);
      } else {
        sendMessageToAdapter(MESSAGE_TEXT, editedMessage, null);
      }
    }
    editText.setText("");
  }

  @OnClick(R.id.txtTitle) void onClickTitle() {
    if (members.size() < 2) return;

    subscriptions.add(DialogFactory.showBottomSheetForCustomizeShortcut(getContext())
        .flatMap(labelType -> {
          if (labelType != null) {
            if (labelType.getTypeDef().equals(LabelType.CHANGE_NAME)) {
              subscriptions.add(DialogFactory.inputDialog(getContext(),
                  getContext().getString(R.string.shortcut_update_name_title),
                  getContext().getString(R.string.shortcut_update_name_description),
                  getContext().getString(R.string.shortcut_update_name_validate),
                  getContext().getString(R.string.action_cancel), InputType.TYPE_CLASS_TEXT)
                  .subscribe(s -> {
                    sendEventEditGroupName();
                    messagePresenter.updateShortcutName(shortcut.getId(), s);
                  }));
            }
          }

          return Observable.just(labelType);
        })
        .filter(labelType -> labelType.getTypeDef().equals(LabelType.CHANGE_PICTURE))
        .flatMap(pair -> DialogFactory.showBottomSheetForCamera(getContext()),
            (pair, labelType) -> {
              if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
                subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
                  sendEventEditGroupName();
                  messagePresenter.updateShortcutPicture(shortcut.getId(), uri.toString());
                }));
              } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
                subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
                  sendEventEditGroupName();
                  messagePresenter.updateShortcutPicture(shortcut.getId(), uri.toString());
                }));
              }

              return null;
            })
        .subscribe());
  }

  public Shortcut getShortcut() {
    return shortcut;
  }

  @OnClick(R.id.videoCallBtn) void onClickVideoCall() {
    navigator.navigateToLive((Activity) context, recipient, LiveActivity.SOURCE_GRID,
        TagManagerUtils.SECTION_SHORTCUT, null);
  }

  private void sendEventEditGroupName() {
    Bundle bundle = new Bundle();
    bundle.putString(TagManagerUtils.ACTION, TagManagerUtils.SAVE);
    tagManager.trackEvent(TagManagerUtils.EditGroupName, bundle);
  }

  private void sendMessage() {
    editText.setText("");
    sendBtn.animate()
        .scaleX(1.3f)
        .scaleY(1.3f)
        .setDuration(ANIM_DURATION)
        .withEndAction(
            () -> sendBtn.animate().scaleX(1f).scaleY(1f).setDuration(ANIM_DURATION).start())
        .start();
    sendMessageToAdapter(MESSAGE_EMOJI, "\u2764", null);
  }

  @OnTouch(R.id.editText) boolean onClickEditText() {
    editText.setHint(context.getResources().getString(R.string.chat_placeholder_message));
    return false;
  }

  @OnTouch(R.id.container) boolean onClickRecyclerView() {
    screenUtils.hideKeyboard(this);
    return false;
  }

  private void setPulseAnimation(String type) {
    if (hideVideoCallBtn || this.type == (FROM_LIVE)) return;
    this.typePulseAnim = type;
    switch (type) {
      case TYPE_NORMAL:
        videoCallBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_chat_video));
        layoutPulse.stop();
        avatarView.setType(NewAvatarView.NORMAL);
        break;
      case TYPE_LIVE:
        videoCallBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_chat_video_red));
        layoutPulse.setColor(ContextCompat.getColor(context, R.color.red_new_contacts));
        layoutPulse.start();
        avatarView.setType(NewAvatarView.LIVE);
        break;
      case TYPE_ONLINE:
        videoCallBtn.setImageDrawable(
            ContextCompat.getDrawable(context, R.drawable.picto_chat_video_live));
        layoutPulse.setColor(ContextCompat.getColor(context, R.color.blue_new));
        layoutPulse.start();
        avatarView.setType(NewAvatarView.ONLINE);
        break;
    }
  }

  private void shrankRecyclerViewGrp() {
    //   containerUsers.setVisibility(GONE);
  }

  private void expendRecyclerViewGrp() {
    //  containerUsers.setVisibility(VISIBLE);
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

  @Override public void onShortcutUpdate(Shortcut shortcut) {
    boolean isOnline = false;
    boolean isLive = false;
    for (User u : shortcut.getMembers()) {
      if (u.isOnline()) isOnline = true;
    }
    chatUserAdapter.setItems(shortcut.getMembers());

    if (shortcut.isLive()) {
      setPulseAnimation(TYPE_LIVE);
    } else if (shortcut.isOnline() || isOnline) {
      setPulseAnimation(TYPE_ONLINE);
    } else {
      setPulseAnimation(TYPE_NORMAL);
    }
    recyclerView.setShortcut(shortcut);
    recyclerView.notifyDataSetChanged();
  }

  @Override public void errorShortcutUpdate() {
  }

  @Override public void isReadingUpdate(String userId) {
    for (ShortcutLastSeen shortcutLastSeen : shortcut.getShortcutLastSeen()) {
      if (shortcutLastSeen.getUserId().equals(userId)) {
        shortcutLastSeen.setDate(dateUtils.getUTCDateAsString());
      }
    }
    recyclerView.setShortcut(shortcut);
    recyclerView.notifyDataSetChanged();
  }

  @Override public void onQuickShortcutUpdated(Shortcut shortcutQuickChat) {
    tagManager.trackEvent(TagManagerUtils.Shortcut);
    navigator.navigateToChat((Activity) context, shortcutQuickChat, shortcut,
        TagManagerUtils.GESTURE_TAP, TagManagerUtils.SECTION_SHORTCUT, false);
  }

  public void setFromShortcut(Shortcut fromShortcut) {
    this.fromShortcut = fromShortcut;
    if (fromShortcut != null) {
      containerQuickChat.setVisibility(VISIBLE);
      LayoutInflater inflater = LayoutInflater.from(context);
      LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_user_chat, null, false);

      TextViewFont name = layout.findViewById(R.id.name);
      AvatarView viewAvatar = layout.findViewById(R.id.viewAvatar);
      viewAvatar.setVisibility(GONE);
      String nameGrp =
          (fromShortcut.getName() == null || fromShortcut.getName().isEmpty()) ? context.getString(
              R.string.chat_quickchat_back_to_group) : fromShortcut.getName();
      nameGrp += "  ";
      name.setText(nameGrp);
      name.setTextColor(ContextCompat.getColor(context, R.color.black_opacity_40));
      name.setPadding(0, 15, 0, 15);
      containerQuickChat.addView(layout);
      name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow, 0);

      layout.setOnClickListener(view -> {
        navigator.navigateToChat((Activity) context, fromShortcut, null, gesture,
            recipient.getSectionTag(), true);
        this.fromShortcut = null;
      });
    }
  }

  @Override public boolean dispatchKeyEvent(KeyEvent event) {
    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
      if (fromShortcut != null) {
        navigator.navigateToChat((Activity) context, fromShortcut, null, gesture,
            recipient.getSectionTag(), true);
        return true;
      }
    }
    return super.dispatchKeyEvent(event);
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
    if (shortcut.isBlocked()) {
      ((Activity) context).finish();
    }
  }

  @Override public void onShortcutUpdatedError() {

  }

  @Override public void onSingleShortcutsLoaded(List<Shortcut> singleShortcutList) {

  }

  @Override public void onShortcut(Shortcut shortcut) {

  }

  public void onStartRecording() {
    recyclerView.onStartRecording();
  }
}
