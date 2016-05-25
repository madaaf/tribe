package com.tribe.app.presentation.view.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Message;
import com.tribe.app.presentation.internal.di.components.DaggerChatComponent;
import com.tribe.app.presentation.mvp.presenter.ChatPresenter;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.view.adapter.MessageAdapter;
import com.tribe.app.presentation.view.adapter.manager.MessageLayoutManager;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ChatActivity extends BaseActivity implements MessageView {

    public static final String FRIEND_ID = "FRIEND_ID";

    public static Intent getCallingIntent(Context context, String id) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(FRIEND_ID, id);
        return intent;
    }

    @Inject ChatPresenter chatPresenter;
    @Inject MessageAdapter messageAdapter;

    @BindView(R.id.recyclerViewText)
    RecyclerView recyclerViewText;

    private String friendId;

    private Unbinder unbinder;
    private MessageLayoutManager messageLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initializeDependencyInjector();
        initRecyclerView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePresenter();
    }

    @Override
    protected void onStop() {
        chatPresenter.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (unbinder != null) unbinder.unbind();
        chatPresenter.onDestroy();
        super.onDestroy();
    }

    private void initUi() {
        setContentView(R.layout.activity_text);
        unbinder = ButterKnife.bind(this);
    }

    private void initRecyclerView() {
        messageLayoutManager = new MessageLayoutManager(this);
        recyclerViewText.setLayoutManager(messageLayoutManager);
        recyclerViewText.setAdapter(messageAdapter);
    }

    private void initParams() {
        if (getIntent() != null && getIntent().hasExtra(FRIEND_ID))
        friendId = getIntent().getStringExtra(FRIEND_ID);
    }

    private void initializeDependencyInjector() {
        DaggerChatComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(getActivityModule())
                .build()
                .inject(this);
    }

    private void initializePresenter() {
        chatPresenter.onStart();
        chatPresenter.attachView(this);
        chatPresenter.subscribe(friendId);
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
        return null;
    }

    public boolean isLastItemDisplayed() {
        return messageAdapter.getItemCount() == 0 || messageLayoutManager.findLastVisibleItemPosition() == messageAdapter.getItemCount() - 1;
    }
}