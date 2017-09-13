package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.data.realm.MessageRealm;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.utils.DateUtils;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.PulseLayout;
import com.tribe.app.presentation.view.widget.avatar.AvatarView;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.tribe.app.presentation.view.widget.chat.Message.MESSAGE_EMOJI;
import static com.tribe.app.presentation.view.widget.chat.Message.MESSAGE_IMAGE;
import static com.tribe.app.presentation.view.widget.chat.Message.MESSAGE_TEXT;

/**
 * Created by madaaflak on 05/09/2017.
 */

public class ChatView extends FrameLayout implements ChatMVPView {

  private LayoutInflater inflater;
  private Unbinder unbinder;
  private Context context;
  private MessageAdapter messageAdapter;
  private ChatUserAdapter chatUserAdapter;
  private LinearLayoutManager layoutManager;
  private LinearLayoutManager layoutManagerGrp;
  private List<Message> items = new ArrayList<>();
  private List<User> users = new ArrayList<>();
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

  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject RxImagePicker rxImagePicker;
  @Inject DateUtils dateUtils;

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
    this.context = context;
    inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.view_chat, this, true);
    unbinder = ButterKnife.bind(this);

    init();
    initDependencyInjector();
    initSubscriptions();
  }

  public void setChatId(User friend) {
    avatarView.load(friend.getProfilePicture());
    Timber.e("CHAT_SUBSCRIBE ID " + friend.getId());
    List<String> userIds = new ArrayList<>();
    userIds.add(friend.getId());
    arrIds = userIds.toArray(new String[userIds.size()]);
    messagePresenter.loadMessage(arrIds);
    messagePresenter.getCreatedMessages();
  }

  private void sendPicture(Uri uri) {
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
        messagePresenter.createMessage(arrIds, downloadUrl.toString(), MessageRealm.IMAGE);
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
              sendContent(MESSAGE_IMAGE, uri.toString());
              sendPicture(uri);
            }));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
              sendContent(MESSAGE_IMAGE, uri.toString());
              sendPicture(uri);
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
              sendBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_like_heart));
              sendBtn.animate().setDuration(200).alpha(1f).start();
            }).start();
          } else if (!text.isEmpty() && !editTextChange) {
            editTextChange = true;
            isHeart = false;
            sendBtn.animate().setDuration(200).alpha(0f).withEndAction(() -> {
              sendBtn.setImageDrawable(
                  ContextCompat.getDrawable(context, R.drawable.picto_chat_send));
              sendBtn.animate().setDuration(200).alpha(1f).start();
            }).start();
          }
        }));
  }

  void init() {
    layoutManager = new LinearLayoutManager(getContext());
    messageAdapter = new MessageAdapter(getContext());
    recyclerView.setItemAnimator(null);
/*    layoutManager.setReverseLayout(true);
    layoutManager.setStackFromEnd(true);*/
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(messageAdapter);

    layoutManagerGrp = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
    chatUserAdapter = new ChatUserAdapter(getContext());
    recyclerViewGrp.setLayoutManager(layoutManagerGrp);
    recyclerViewGrp.setItemAnimator(null);
    recyclerViewGrp.setAdapter(chatUserAdapter);
  }

  void mock() { //TODO SOEF DELETE
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
    mock();
    setAnimation();
  }

  @Override protected void onDetachedFromWindow() {
    messagePresenter.onViewDetached();
    if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.clear();
    dispose();
    super.onDetachedFromWindow();
  }

  @OnClick(R.id.sendBtn) void onClickSend() {
    if (!isHeart) {
      String editedMessage = editText.getText().toString();
      if (!editedMessage.isEmpty()) sendContent(MESSAGE_TEXT, editedMessage);
      editText.setText("");
    } else {
      Timber.e("videoCallBtn");
      sendBtn.animate()
          .scaleX(1.3f)
          .scaleY(1.3f)
          .setDuration(300)
          .withEndAction(() -> sendBtn.animate().scaleX(1f).scaleY(1f).setDuration(300).start())
          .start();
      sendContent(MESSAGE_EMOJI, "\u2764");
    }
  }

  @OnClick(R.id.editText) void onClickEditText() {
    Timber.e("SOEF EDIT TEXT OPEN");
    scrollListToBottom();
    editText.setHint("Message");
  }

  private void sendContent(@Message.Type String type, String content) {
    if (content.isEmpty()) return;
    items.clear();
    Message message = null;
    switch (type) {
      case MESSAGE_TEXT:
        message = new MessageText();
        ((MessageText) message).setMessage(content);
        messagePresenter.createMessage(arrIds, content, MessageRealm.TEXT);
        break;
      case MESSAGE_EMOJI:
        message = new MessageEmoji(content);
        ((MessageEmoji) message).setEmoji(content);
        messagePresenter.createMessage(arrIds, content, MessageRealm.EMOJI);
        break;
      case MESSAGE_IMAGE:
        message = new MessageImage(user.getId());
        Image o = new Image();
        o.setUrl(content);
        ((MessageImage) message).setOriginal(o);
        break;
    }

    message.setType(type);
    message.setAuthor(user);
    items.add(message);
    messageAdapter.setItems(items);
    scrollListToBottom();
  }

  private void dispose() {
    pulseLayout.clearAnimation();
  }

  private void setAnimation() {
    videoCallBtn.setImageDrawable(
        ContextCompat.getDrawable(context, R.drawable.picto_chat_video_red));
    pulseLayout.start();
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

  private void scrollListToBottom() {
    Timber.e("SOEF SMOOTH " + messageAdapter.getItemCount());
    recyclerView.post(() -> recyclerView.smoothScrollToPosition(messageAdapter.getItemCount()));
  }

  @Override public void successLoadingMessage(List<Message> messages) {
    messageAdapter.setItems(messages);
    scrollListToBottom();

    Timber.e("SOEF successLoadingMessage");
  }

  @Override public void errorLoadingMessage() {
    Timber.e("SOEF errorLoadingMessage");
  }

  @Override public void successMessageCreated(Message message) {
    Timber.e("SOEF successMessageCreated " + message.toString());
  }

  @Override public void errorMessageCreation() {
    Timber.e("SOEF errorMessageCreation");
  }

  @Override public void successGetSubscribeMessage(Message message) {
    Timber.e("SOEF successGetSubscribeMessage " + message.toString());
    if (!message.getAuthor().getId().equals(user.getId())) {
      items.clear();
      items.add(message);
      messageAdapter.setItems(items);
      scrollListToBottom();
    }
  }

  @Override public void errorGetSubscribeMessage() {
    Timber.e("SOEF errorGetSubscribeMessage ");
  }
}
