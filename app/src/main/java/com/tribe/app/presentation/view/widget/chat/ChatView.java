package com.tribe.app.presentation.view.widget.chat;

import android.app.Activity;
import android.content.Context;
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
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.tribe.app.R;
import com.tribe.app.domain.entity.LabelType;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.internal.di.components.DaggerUserComponent;
import com.tribe.app.presentation.internal.di.modules.ActivityModule;
import com.tribe.app.presentation.mvp.presenter.MessagePresenter;
import com.tribe.app.presentation.mvp.view.ChatMVPView;
import com.tribe.app.presentation.utils.mediapicker.RxImagePicker;
import com.tribe.app.presentation.utils.mediapicker.Sources;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.widget.EditTextFont;
import com.tribe.app.presentation.view.widget.PulseLayout;
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
  private MessageAdapter adapter;
  private ChatUserAdapter chatUserAdapter;
  private LinearLayoutManager layoutManager;
  private LinearLayoutManager layoutManagerGrp;
  private List<Message> items = new ArrayList<>();
  private List<User> users = new ArrayList<>();
  private boolean editTextChange = false, isHeart = false;

  @BindView(R.id.editText) EditTextFont editText;
  @BindView(R.id.recyclerViewChat) RecyclerView recyclerView;
  @BindView(R.id.recyclerViewGrp) RecyclerView recyclerViewGrp;
  @BindView(R.id.uploadBtn) ImageView uploadImageBtn;
  @BindView(R.id.sendBtn) ImageView sendBtn;
  @BindView(R.id.videoCallBtn) ImageView videoCallBtn;
  @BindView(R.id.layoutPulse) PulseLayout pulseLayout;

  @Inject User user;
  @Inject MessagePresenter messagePresenter;
  @Inject RxImagePicker rxImagePicker;

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

  private void initSubscriptions() {
    subscriptions.add(RxView.clicks(uploadImageBtn)
        .delay(200, TimeUnit.MILLISECONDS)
        .observeOn(AndroidSchedulers.mainThread())
        .flatMap(aVoid -> DialogFactory.showBottomSheetForCamera(context), ((aVoid, labelType) -> {
          if (labelType.getTypeDef().equals(LabelType.OPEN_CAMERA)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.CAMERA).subscribe(uri -> {
              sendContent(MESSAGE_IMAGE, uri.toString());
            }));
          } else if (labelType.getTypeDef().equals(LabelType.OPEN_PHOTOS)) {
            subscriptions.add(rxImagePicker.requestImage(Sources.GALLERY).subscribe(uri -> {
              sendContent(MESSAGE_IMAGE, uri.toString());
            }));
          }

          return null;
        }))
        .subscribe());

    subscriptions.add(
        RxTextView.textChanges(editText).map(CharSequence::toString).subscribe(text -> {
          if (text.isEmpty()) {
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

    editText.setOnClickListener(v -> {
      Timber.e("SOEF EDIT TEXT OPEN");
      recyclerView.smoothScrollToPosition(adapter.getItemCount());
    });
  }

  void init() {
    layoutManager = new LinearLayoutManager(getContext());
    adapter = new MessageAdapter(getContext());
    recyclerView.setItemAnimator(null);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.smoothScrollToPosition(adapter.getItemCount());

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
    messagePresenter.loadMessage();
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
      recyclerView.smoothScrollToPosition(adapter.getItemCount());
      String editedMessage = editText.getText().toString();
      if (!editedMessage.isEmpty()) sendContent(MESSAGE_TEXT, editedMessage);
      editText.setText("");
    } else {
      Timber.e("videoCallBtn");
      sendBtn.animate()
          .scaleX(1.3f)
          .scaleY(1.3f)
          .setDuration(200)
          .withEndAction(() -> sendBtn.animate().scaleX(1f).scaleY(1f).setDuration(200).start())
          .start();
      sendContent(MESSAGE_EMOJI, "\u2764");
    }
  }

  @OnClick(R.id.videoCallBtn) void onClickvideoCall() {

  }

  private void sendContent(@Message.Type String type, String content) {
    if (content.isEmpty()) return;
    items.clear();
    Message message = null;
    switch (type) {
      case MESSAGE_TEXT:
        message = new MessageText();
        ((MessageText) message).setMessage(content);
        break;
      case MESSAGE_EMOJI:
        message = new MessageEmoji(content);
        ((MessageEmoji) message).setEmoji(content);
        break;
      case MESSAGE_IMAGE:
        message = new MessageImage(user.getId());
        Original o = new Original();
        o.setUrl(content);
        ((MessageImage) message).setOriginal(o);
        break;
    }

    message.setType(type);
    message.setAuthor(user);
    items.add(message);
    adapter.setItems(items);
    recyclerView.smoothScrollToPosition(adapter.getItemCount());
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
