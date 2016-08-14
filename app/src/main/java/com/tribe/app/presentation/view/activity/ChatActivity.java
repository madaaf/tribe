package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.internal.di.components.DaggerChatComponent;
import com.tribe.app.presentation.mvp.presenter.ChatPresenter;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.view.adapter.MessageAdapter;
import com.tribe.app.presentation.view.adapter.manager.MessageLayoutManager;
import com.tribe.app.presentation.view.component.ChatInputView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.subscriptions.CompositeSubscription;

public class ChatActivity extends BaseActivity implements MessageView {

    public static final int DURATION = 300;
    public static final float OVERSHOOT = 2f;

    public static final String RECIPIENT = "RECIPIENT";

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(RECIPIENT, recipient);
        return intent;
    }

    @Inject ChatPresenter chatPresenter;
    @Inject ScreenUtils screenUtils;

    @BindView(android.R.id.content)
    ViewGroup rootView;

    @BindView(R.id.layoutMaster)
    ViewGroup layoutMaster;

    @BindView(R.id.layoutContent)
    ViewGroup layoutContent;

    @BindView(R.id.recyclerViewText)
    RecyclerView recyclerViewText;

    @BindView(R.id.layoutBar)
    ViewGroup layoutBar;

    @BindView(R.id.viewChatInput)
    ChatInputView chatInputView;

    @BindView(R.id.txtTitle)
    TextViewFont txtTitle;

    // PARAMS
    private Recipient recipient;

    // RESOURCES
    private int radiusGalleryImg;
    private int chatBarHeight;
    private int statusBarHeight;
    private int dismissMove;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // LAYOUT VARIABLES
    private MessageLayoutManager messageLayoutManager;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessageList;
    private ImageView recyclerViewImageView;
    private ImageView imageViewClicked;
    private int widthImageViewClicked;
    private int heightImageViewClicked;
    private int marginLeftImageViewClicked;
    private int marginTopImageViewClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initResources();
        initDependencyInjector();
        initSubscriptions();
        initRecyclerView();
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

        //recyclerViewText.getItemAnimator().setChangeDuration(0);

        subscriptions.add(messageAdapter
                .clickPhoto()
                .subscribe(imageViewFrom -> {
                    recyclerViewImageView = imageViewFrom;

                    int [] location = new int[2];
                    imageViewFrom.getLocationOnScreen(location);

                    imageViewClicked = new ImageView(this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(imageViewFrom.getWidth(), imageViewFrom.getHeight());
                    params.leftMargin = location[0];
                    params.topMargin = location[1] - statusBarHeight;
                    imageViewClicked.setLayoutParams(params);

                    widthImageViewClicked = imageViewFrom.getWidth();
                    heightImageViewClicked = imageViewFrom.getHeight();
                    marginLeftImageViewClicked = params.leftMargin;
                    marginTopImageViewClicked = params.topMargin;

                    imageViewClicked.setImageDrawable(imageViewFrom.getDrawable());
                    rootView.addView(imageViewClicked);

                    imageViewClicked.setClickable(true);
                    imageViewClicked.setFocusableInTouchMode(true);
                    imageViewClicked.setOnTouchListener(new View.OnTouchListener() {

                        int yDelta, downY;
                        int ogTopMargin, ogLeftMargin;

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            final int touchY = (int) event.getRawY();

                            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageViewClicked.getLayoutParams();

                            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                                case MotionEvent.ACTION_DOWN:
                                    ogLeftMargin = layoutParams.leftMargin;
                                    ogTopMargin = layoutParams.topMargin;
                                    yDelta = touchY - layoutParams.topMargin;
                                    downY = touchY;
                                    break;

                                case MotionEvent.ACTION_CANCEL: case MotionEvent.ACTION_UP:
                                    if (Math.abs(downY - touchY) > dismissMove) {
                                        snapImageBack();
                                    } else {
                                        ValueAnimator animator = ValueAnimator.ofInt(layoutParams.topMargin, ogTopMargin);
                                        animator.setDuration(DURATION);
                                        animator.addUpdateListener(animation -> {
                                            layoutParams.topMargin = (Integer) animation.getAnimatedValue();
                                            imageViewClicked.setLayoutParams(layoutParams);
                                        });

                                        animator.start();
                                    }

                                case MotionEvent.ACTION_MOVE:
                                    layoutParams.topMargin = touchY - yDelta;
                                    imageViewClicked.setLayoutParams(layoutParams);

                                    break;
                            }

                            imageViewClicked.invalidate();
                            return true;
                        }
                    });

                    imageViewClicked.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            animateFullScreen();
                            recyclerViewText.setEnabled(false);
                            imageViewClicked.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                })
        );

//        List<ChatMessage> chatMessageList = new ArrayList<>();
//
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE, -5);
//
//        Friendship friendship = (Friendship) recipient;
//
//        ChatMessage message = new ChatMessage();
//        message.setId(UUID.randomUUID().toString());
//        message.setFrom(getCurrentUser());
//        message.setContent("Hey ça roule ou quoi batard ?");
//        message.setRecordedAt(calendar.getTime());
//        message.setTo(recipient);
//        message.setType(ChatMessage.TEXT);
//        chatMessageList.add(message);
//
//        calendar.add(Calendar.DATE, 2);
//
//        ChatMessage message2 = new ChatMessage();
//        message2.setId(UUID.randomUUID().toString());
//        message2.setFrom(getCurrentUser());
//        message2.setContent("LOLs");
//        message2.setRecordedAt(calendar.getTime());
//        message2.setTo(recipient);
//        message2.setType(ChatMessage.TEXT);
//        chatMessageList.add(message2);
//
//        calendar.add(Calendar.MINUTE, 3);
//
//        ChatMessage message3 = new ChatMessage();
//        message3.setId(UUID.randomUUID().toString());
//        message3.setFrom(getCurrentUser());
//        message3.setContent("MDR");
//        message3.setRecordedAt(calendar.getTime());
//        message3.setTo(recipient);
//        message3.setType(ChatMessage.TEXT);
//        chatMessageList.add(message3);
//
//        ChatMessage message4 = new ChatMessage();
//        message4.setId(UUID.randomUUID().toString());
//        message4.setFrom(friendship.getFriend());
//        message4.setContent("Ha ha ha ha j'avoue t'as trop réson");
//        message4.setRecordedAt(new Date());
//        message4.setTo(recipient);
//        message4.setType(ChatMessage.TEXT);
//        chatMessageList.add(message4);
//
//        ChatMessage message5 = new ChatMessage();
//        message5.setId(UUID.randomUUID().toString());
//        message5.setFrom(friendship.getFriend());
//        message5.setContent("\uD83D\uDE00" + "\uD83D\uDE00" + "\uD83D\uDE00" + "\uD83D\uDE00");
//        message5.setRecordedAt(new Date());
//        message5.setTo(recipient);
//        message5.setType(ChatMessage.TEXT);
//        chatMessageList.add(message5);
//
//        ChatMessage chatMessage6 = new ChatMessage();
//        chatMessage6.setId(UUID.randomUUID().toString());
//        chatMessage6.setFrom(friendship.getFriend());
//        chatMessage6.setContent("\uD83D\uDC8C" + "love ya");
//        chatMessage6.setRecordedAt(new Date());
//        chatMessage6.setTo(recipient);
//        chatMessage6.setType(ChatMessage.TEXT);
//        chatMessageList.add(chatMessage6);
//
//        ChatMessage chatMessage7 = new ChatMessage();
//        chatMessage7.setId(UUID.randomUUID().toString());
//        chatMessage7.setFrom(getCurrentUser());
//        chatMessage7.setContent("Hey ça va ? Tu connais : http://www.google.fr ? Ca défonce !");
//        chatMessage7.setRecordedAt(new Date());
//        chatMessage7.setTo(recipient);
//        chatMessage7.setType(ChatMessage.TEXT);
//        chatMessageList.add(chatMessage7);
//
//        ChatMessage chatMessage8 = new ChatMessage();
//        chatMessage8.setId(UUID.randomUUID().toString());
//        chatMessage8.setFrom(getCurrentUser());
//        chatMessage8.setContent("http://vignette3.wikia.nocookie.net/miamivice/images/e/e2/Stevebuscemi.jpg/revision/latest?cb=20100913014130");
//        chatMessage8.setRecordedAt(new Date());
//        chatMessage8.setTo(recipient);
//        chatMessage8.setType(ChatMessage.PHOTO);
//        chatMessageList.add(chatMessage8);

        //renderMessageList(ChatMessage.computeMessageList(chatMessageList));
    }

    private void initResources() {
        radiusGalleryImg = getResources().getDimensionPixelSize(R.dimen.radius_gallery_img);
        chatBarHeight = getResources().getDimensionPixelSize(R.dimen.chat_bar_height);
        dismissMove = getResources().getDimensionPixelOffset(R.dimen.threshold_dismiss);
        statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
    }

    private void initParams() {
        if (getIntent() != null && getIntent().hasExtra(RECIPIENT))
            recipient = (Recipient) getIntent().getSerializableExtra(RECIPIENT);
    }

    private void initSubscriptions() {
        subscriptions = new CompositeSubscription();
        subscriptions.add(chatInputView.sendClick().subscribe(s -> chatPresenter.sendMessage(getCurrentUser(), recipient, s)));
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
        chatPresenter.loadChatMessages(recipient.getId());
        chatPresenter.loadThumbnail(radiusGalleryImg);
    }

    private void initInfos() {
        txtTitle.setText(recipient.getDisplayName());
    }

    @Override
    public void renderMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = new ArrayList<>(chatMessageList);
        List<ChatMessage> result = ChatMessage.computeMessageList(chatMessageList);

        final boolean addedToTop = !result.isEmpty() && !result.get(0).equals(messageAdapter.getOldestChatMessage());
        final boolean scrollToEndAfterUpdate = isLastItemDisplayed();
        final int lastVisiblePosition = messageLayoutManager.findLastCompletelyVisibleItemPosition();
        final ChatMessage lastVisible = lastVisiblePosition != -1 && messageAdapter.getItemCount() > 0 ? messageAdapter.getMessage(lastVisiblePosition) : null;

        messageAdapter.setItems(result);

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
    public void renderMessage(ChatMessage chatMessage) {
        final boolean addedToTop = chatMessage != null && !chatMessage.equals(messageAdapter.getOldestChatMessage());
        final boolean scrollToEndAfterUpdate = isLastItemDisplayed();
        final int lastVisiblePosition = messageLayoutManager.findLastCompletelyVisibleItemPosition();
        final ChatMessage lastVisible = lastVisiblePosition != -1 && messageAdapter.getItemCount() > 0 ? messageAdapter.getMessage(lastVisiblePosition) : null;

        List<ChatMessage> result = ChatMessage.computeMessageList(chatMessageList);
        result.add(chatMessage);
        messageAdapter.setItems(result);

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

    private void animateFullScreen() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageViewClicked.getLayoutParams();
        ValueAnimator animatorTopMargin = ValueAnimator.ofInt(marginTopImageViewClicked, (screenUtils.getHeight() >> 1) - (params.height >> 1));
        animatorTopMargin.setDuration(DURATION);
        animatorTopMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorTopMargin.addUpdateListener(animation -> {
            params.topMargin = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });
        animatorTopMargin.start();

        ValueAnimator animatorLeftMargin = ValueAnimator.ofInt(params.leftMargin, 0);
        animatorLeftMargin.setDuration(DURATION);
        animatorLeftMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorLeftMargin.addUpdateListener(animation -> {
            params.leftMargin = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });

        animatorLeftMargin.start();

        ValueAnimator animatorWidth = ValueAnimator.ofInt(widthImageViewClicked, screenUtils.getWidth());
        animatorWidth.setDuration(DURATION);
        animatorWidth.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorWidth.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });

        animatorWidth.start();

        ValueAnimator animatorHeight = ValueAnimator.ofInt(heightImageViewClicked, (int) (screenUtils.getWidth() * ((float) widthImageViewClicked / heightImageViewClicked)));
        animatorHeight.setDuration(DURATION);
        animatorHeight.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorHeight.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });

        animatorHeight.start();

        ValueAnimator animatorAlpha = ValueAnimator.ofInt(1, 0);
        animatorAlpha.setDuration(DURATION);
        animatorAlpha.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorAlpha.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            chatInputView.setAlpha(value);
            layoutContent.setAlpha(value);
            layoutBar.setAlpha(value);
        });
        animatorAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                recyclerViewText.setEnabled(true);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                recyclerViewImageView.setAlpha(0f);
            }
        });

        animatorAlpha.start();
    }

    private void snapImageBack() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) imageViewClicked.getLayoutParams();
        ValueAnimator animatorTopMargin = ValueAnimator.ofInt(params.topMargin, marginTopImageViewClicked);
        animatorTopMargin.setDuration(DURATION);
        animatorTopMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorTopMargin.addUpdateListener(animation -> {
            params.topMargin = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });
        animatorTopMargin.start();

        ValueAnimator animatorLeftMargin = ValueAnimator.ofInt(params.leftMargin, marginLeftImageViewClicked);
        animatorLeftMargin.setDuration(DURATION);
        animatorLeftMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorLeftMargin.addUpdateListener(animation -> {
            params.leftMargin = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });

        animatorLeftMargin.start();

        ValueAnimator animatorWidth = ValueAnimator.ofInt(params.width, widthImageViewClicked);
        animatorWidth.setDuration(DURATION);
        animatorWidth.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorWidth.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });

        animatorWidth.start();

        ValueAnimator animatorHeight = ValueAnimator.ofInt(params.width, heightImageViewClicked);
        animatorHeight.setDuration(DURATION);
        animatorHeight.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorHeight.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            imageViewClicked.setLayoutParams(params);
        });

        animatorHeight.start();

        ValueAnimator animatorAlpha = ValueAnimator.ofInt(0, 1);
        animatorAlpha.setDuration(DURATION);
        animatorAlpha.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorAlpha.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            chatInputView.setAlpha(value);
            layoutContent.setAlpha(value);
            layoutBar.setAlpha(value);
        });
        animatorAlpha.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                recyclerViewText.setEnabled(true);
                rootView.removeView(imageViewClicked);
                recyclerViewImageView.setAlpha(1f);
                recyclerViewImageView = null;
            }
        });

        animatorAlpha.start();
    }

    @Override
    public void onBackPressed() {
        if (layoutContent.getAlpha() == 0f) snapImageBack();
        else super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down);
    }
}