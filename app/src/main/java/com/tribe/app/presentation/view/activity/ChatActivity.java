package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.internal.di.components.DaggerChatComponent;
import com.tribe.app.presentation.mvp.presenter.ChatPresenter;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.view.adapter.MessageAdapter;
import com.tribe.app.presentation.view.adapter.manager.MessageLayoutManager;
import com.tribe.app.presentation.view.component.ChatInputView;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class ChatActivity extends BaseActivity implements MessageView {

    public static final String RECIPIENT = "RECIPIENT";

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(RECIPIENT, recipient);
        return intent;
    }

    @Inject ChatPresenter chatPresenter;

    @BindView(R.id.recyclerViewText)
    RecyclerView recyclerViewText;

    @BindView(R.id.viewChatInput)
    ChatInputView chatInputView;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    // PARAMS
    private Recipient recipient;

    // RESOURCES
    private int radiusGalleryImg;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // LAYOUT VARIABLES
    private MessageLayoutManager messageLayoutManager;
    private MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initResources();
        initDependencyInjector();
        initRecyclerView();
        initSubscriptions();
        initInfos();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPresenter();
    }

    @Override
    protected void onStop() {
        chatPresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        if (chatPresenter != null) chatPresenter.onDestroy();
        if (chatInputView != null) chatInputView.onDestroy();
        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }
        if (messageAdapter != null) messageAdapter.releaseSubscriptions();

        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_text);
        unbinder = ButterKnife.bind(this);
    }

    private void initRecyclerView() {
        messageLayoutManager = new MessageLayoutManager(this);
        recyclerViewText.setLayoutManager(messageLayoutManager);
        messageAdapter = new MessageAdapter(LayoutInflater.from(this), this);
        recyclerViewText.setAdapter(messageAdapter);

        List<Message> messageList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -5);

        Friendship friendship = (Friendship) recipient;

        Message message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setFrom(getCurrentUser());
        message.setText("Hey ça roule ou quoi batard ?");
        message.setRecordedAt(calendar.getTime());
        message.setTo(recipient);
        messageList.add(message);

        calendar.add(Calendar.DATE, 2);

        Message message2 = new Message();
        message2.setId(UUID.randomUUID().toString());
        message2.setFrom(getCurrentUser());
        message2.setText("LOLs");
        message2.setRecordedAt(calendar.getTime());
        message2.setTo(recipient);
        messageList.add(message2);

        //calendar.add(Calendar.MINUTE, 3);

//        Message message3 = new Message();
//        message3.setId(UUID.randomUUID().toString());
//        message3.setFrom(getCurrentUser());
//        message3.setText("MDR");
//        message3.setRecordedAt(calendar.getTime());
//        message3.setTo(recipient);
//        messageList.add(message3);
//
//        Message message4 = new Message();
//        message4.setId(UUID.randomUUID().toString());
//        message4.setFrom(friendship.getFriend());
//        message4.setText("Ha ha ha ha j'avoue t'as trop réson");
//        message4.setRecordedAt(new Date());
//        message4.setTo(recipient);
//        messageList.add(message4);
//
//        Message message5 = new Message();
//        message5.setId(UUID.randomUUID().toString());
//        message5.setFrom(friendship.getFriend());
//        message5.setText("\uD83D\uDE00" + "\uD83D\uDE00" + "\uD83D\uDE00" + "\uD83D\uDE00");
//        message5.setRecordedAt(new Date());
//        message5.setTo(recipient);
//        messageList.add(message5);
//
//        Message message6 = new Message();
//        message6.setId(UUID.randomUUID().toString());
//        message6.setFrom(friendship.getFriend());
//        message6.setText("\uD83D\uDC8C" + "love ya");
//        message6.setRecordedAt(new Date());
//        message6.setTo(recipient);
//        messageList.add(message6);

        Message message7 = new Message();
        message7.setId(UUID.randomUUID().toString());
        message7.setFrom(friendship.getFriend());
        message7.setText("http://www.google.fr");
        message7.setRecordedAt(new Date());
        message7.setTo(recipient);
        messageList.add(message7);

        renderMessageList(Message.computeMessageList(messageList));
    }

    private void initResources() {
        radiusGalleryImg = getResources().getDimensionPixelSize(R.dimen.radius_gallery_img);
    }

    private void initParams() {
        if (getIntent() != null && getIntent().hasExtra(RECIPIENT))
            recipient = (Recipient) getIntent().getSerializableExtra(RECIPIENT);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
        subscriptions.add(chatInputView.sendClick().subscribe(s -> chatPresenter.sendMessage(s)));
        subscriptions.add(chatInputView.textChanges().subscribe(s -> chatPresenter.sendTypingEvent()));
    }

    private void initDependencyInjector() {
        DaggerChatComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initPresenter() {
        chatPresenter.onStart();
        chatPresenter.attachView(this);
        chatPresenter.loadThumbnail(radiusGalleryImg);
    }

    private void initInfos() {
        txtTitle.setText(recipient.getDisplayName());
    }

    @Override
    public void renderMessageList(List<Message> messageList) {
        final boolean addedToTop = !messageList.isEmpty() && !messageList.get(0).equals(messageAdapter.getOldestMessage());
        final boolean scrollToEndAfterUpdate = isLastItemDisplayed();
        final int lastVisiblePosition = messageLayoutManager.findLastCompletelyVisibleItemPosition();
        final Message lastVisible = lastVisiblePosition != -1 && messageAdapter.getItemCount() > 0 ? messageAdapter.getMessage(lastVisiblePosition) : null;

        messageAdapter.setItems(messageList);

        if (scrollToEndAfterUpdate) {
            recyclerViewText.scrollToPosition(messageAdapter.getItemCount() - 1);
        } else if (addedToTop) {
            int index = messageAdapter.getIndexOfMessage(lastVisible);
            if (index != -1) {
                recyclerViewText.scrollToPosition(index);
            }
        }
    }

    @Override
    public void renderMessage(Message message) {
        final boolean addedToTop = message != null && !message.equals(messageAdapter.getOldestMessage());
        final boolean scrollToEndAfterUpdate = isLastItemDisplayed();
        final int lastVisiblePosition = messageLayoutManager.findLastCompletelyVisibleItemPosition();
        final Message lastVisible = lastVisiblePosition != -1 && messageAdapter.getItemCount() > 0 ? messageAdapter.getMessage(lastVisiblePosition) : null;

        messageAdapter.addItem(message);

        if (scrollToEndAfterUpdate) {
            recyclerViewText.scrollToPosition(messageAdapter.getItemCount() - 1);
        } else if (addedToTop) {
            int index = messageAdapter.getIndexOfMessage(lastVisible);
            if (index != -1) {
                recyclerViewText.scrollToPosition(index);
            }
        }
    }

    @Override
    public void showGalleryImage(Bitmap bitmap) {
        chatInputView.setImageGallery(bitmap);
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showRetry() {

    }

    @Override
    public void hideRetry() {

    }

    @Override
    public void showError(String message) {

    }

    @Override
    public Context context() {
        return this;
    }

    public boolean isLastItemDisplayed() {
        return messageAdapter.getItemCount() == 0 || messageLayoutManager.findLastVisibleItemPosition() == messageAdapter.getItemCount() - 1;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }
}