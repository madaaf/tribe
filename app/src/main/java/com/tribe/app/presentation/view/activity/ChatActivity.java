package com.tribe.app.presentation.view.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
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

import com.jakewharton.rxbinding.view.RxView;
import com.squareup.picasso.Picasso;
import com.tribe.app.R;
import com.tribe.app.domain.entity.ChatMessage;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.Section;
import com.tribe.app.presentation.internal.di.components.DaggerChatComponent;
import com.tribe.app.presentation.mvp.presenter.ChatPresenter;
import com.tribe.app.presentation.mvp.view.MessageView;
import com.tribe.app.presentation.utils.FileUtils;
import com.tribe.app.presentation.view.adapter.MessageAdapter;
import com.tribe.app.presentation.view.adapter.manager.MessageLayoutManager;
import com.tribe.app.presentation.view.component.ChatInputView;
import com.tribe.app.presentation.view.utils.DialogFactory;
import com.tribe.app.presentation.view.utils.MessageDownloadingStatus;
import com.tribe.app.presentation.view.utils.RoundedCornersTransformation;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.video.VideoSize;
import com.tribe.app.presentation.view.widget.ScalableTextureView;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.app.presentation.view.widget.TribeVideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChatActivity extends BaseActivity implements MessageView {

    // REQUEST CODES
    public static final int REQUEST_GALLERY = 100;

    // UI CONSTANTS
    public static final int DURATION = 300;
    public static final float OVERSHOOT = 1.2f;
    public static final int ERROR_MARGIN = 5;

    public static final String RECIPIENT = "RECIPIENT";

    public static Intent getCallingIntent(Context context, Recipient recipient) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(RECIPIENT, recipient);
        return intent;
    }

    @Inject ChatPresenter chatPresenter;
    @Inject ScreenUtils screenUtils;
    @Inject Picasso picasso;

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

    @BindView(R.id.imgTrash)
    ImageView imgTrash;

    // PARAMS
    private Recipient recipient;

    // RESOURCES
    private int marginHorizontalLeftSmall;
    private int radiusGalleryImg;
    private int chatBarHeight;
    private int statusBarHeight;
    private int dismissMove;
    private int avatarSize;
    private int marginOfPositionError;

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions;

    // LAYOUT VARIABLES
    private MessageLayoutManager messageLayoutManager;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessageList;
    private ImageView recyclerViewImageView;
    private ImageView imageViewClicked;
    private int widthViewClicked;
    private int heightViewClicked;
    private int marginLeftViewClicked;
    private int marginTopViewClicked;
    private Map<String, Section> avatarsMap;
    private TribeVideoView tribeVideoView;
    private VideoSize videoSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUi();
        initParams();
        initDependencyInjector();
        initResources();
        initSubscriptions();
        initRecyclerView();
        initInfos();
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
        recyclerViewText.setHasFixedSize(true);
        recyclerViewText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                recyclerViewText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        avatarsMap = new HashMap<>();

        subscriptions.add(messageLayoutManager.itemsDoneLayoutCallback().subscribe(integer -> {
            updateAvatars();
        }));

        subscriptions.add(
            RxView.scrollChangeEvents(recyclerViewText)
                .subscribe(viewScrollChangeEvent -> {
                    updateAvatars();
                }));

        subscriptions.add(messageAdapter
                .clickPhoto()
                .subscribe(imageViewFrom -> {
                    imageViewClicked = null;

                    int position = (Integer) imageViewFrom.getTag(R.id.tag_position);
                    imageViewFrom.getGlobalVisibleRect(new Rect());
                    Rect scrollBounds = new Rect();
                    recyclerViewText.getHitRect(scrollBounds);

                    if (!imageViewFrom.getLocalVisibleRect(scrollBounds)
                            || scrollBounds.height() < imageViewFrom.getHeight()) {
                        recyclerViewText.smoothScrollToPosition(position);
                        Observable.timer(500, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .subscribe(aLong -> {
                                   showImage(imageViewFrom);
                                });
                    } else {
                        showImage(imageViewFrom);
                    }
                })
        );

        subscriptions.add(messageAdapter
                .clickVideo()
                .subscribe(imageViewFrom -> {
                    tribeVideoView = null;

                    int position = (Integer) imageViewFrom.getTag(R.id.tag_position);
                    ChatMessage message = messageAdapter.getItems().get(position);
                    if ((message.getMessageDownloadingStatus() != null
                            && message.getMessageDownloadingStatus().equals(MessageDownloadingStatus.STATUS_DOWNLOADED))
                            || message.getContent().contains("content://")) {
                        imageViewFrom.getGlobalVisibleRect(new Rect());
                        Rect scrollBounds = new Rect();
                        recyclerViewText.getHitRect(scrollBounds);

                        if (!imageViewFrom.getLocalVisibleRect(scrollBounds)
                                || scrollBounds.height() < imageViewFrom.getHeight()) {
                            recyclerViewText.smoothScrollToPosition(position);
                            Observable.timer(500, TimeUnit.MILLISECONDS)
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeOn(AndroidSchedulers.mainThread())
                                    .subscribe(aLong -> {
                                        showVideo(imageViewFrom, message);
                                    });
                        } else {
                            showVideo(imageViewFrom, message);
                        }
                    } else {
                        chatPresenter.loadVideo(message);
                    }
                })
        );
    }

    private void initResources() {
        radiusGalleryImg = getResources().getDimensionPixelSize(R.dimen.radius_gallery_img);
        chatBarHeight = getResources().getDimensionPixelSize(R.dimen.chat_bar_height);
        dismissMove = getResources().getDimensionPixelOffset(R.dimen.threshold_dismiss);
        avatarSize = getResources().getDimensionPixelSize(R.dimen.avatar_size_small);
        marginHorizontalLeftSmall = getResources().getDimensionPixelSize(R.dimen.horizontal_margin_small);
        marginOfPositionError = screenUtils.dpToPx(ERROR_MARGIN);

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

        subscriptions.add(chatInputView.sendClick().subscribe(s -> {
            ChatMessage chatMessage = ChatMessage.createMessage(ChatMessage.TEXT, s, getCurrentUser(), recipient, getApplicationComponent().dateUtils());
            chatPresenter.sendMessage(chatMessage);
        }));

        subscriptions.add(chatInputView.textChanges().subscribe(s -> chatPresenter.sendTypingEvent()));

        subscriptions.add(chatInputView.chooseImageFromGallery().subscribe(aVoid -> {
            Intent openGalleryIntent = null;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                openGalleryIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                openGalleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                openGalleryIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                openGalleryIntent.setType("image/jpeg");
                openGalleryIntent.putExtra(Intent.EXTRA_MIME_TYPES, new String[] {"image/jpeg", "video/mp4"});
                openGalleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
            } else {
                openGalleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                openGalleryIntent.setType("image/jpeg video/mp4");
            }

            openGalleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            openGalleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivityForResult(openGalleryIntent, REQUEST_GALLERY);
        }));
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
        chatPresenter.loadChatMessages(recipient);
        chatPresenter.loadThumbnail(radiusGalleryImg);
        chatPresenter.updateErrorMessages(recipient.getId());
    }

    private void initInfos() {
        txtTitle.setText(recipient.getDisplayName());
    }

    @OnClick(R.id.imgTrash)
    public void deleteConversation() {
        DialogFactory.createConfirmationDialog(this, getString(R.string.chat_erase_conversation_title),
                    getString(R.string.chat_erase_conversation_message),
                    getString(R.string.common_delete),
                    (dialog, which) -> chatPresenter.deleteConversation(recipient.getId()))
                .show();
    }

    @OnClick(R.id.imgBack)
    public void exit() {
        finish();
    }

    @Override
    public void renderMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = new ArrayList<>(chatMessageList);

        if (chatMessageList.size() > 0) {
            imgTrash.setEnabled(true);
        } else {
            imgTrash.setEnabled(false);
        }

        ChatMessage tutorial = new ChatMessage();
        tutorial.setTo(recipient);
        tutorial.setLocalId("tutorial");
        tutorial.setTutorial(true);

        List<ChatMessage> result = ChatMessage.computeMessageList(tutorial, chatMessageList);

        if (avatarsMap.size() > 0) {
            for (ChatMessage message : result) {
                for (Section section : avatarsMap.values()) {
                    if (message.getBeginOfSection() == section.getBegin()) {
                        section.setEnd(message.getEndOfSection());
                    }
                }
            }
        }

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

        chatPresenter.markMessageListAsRead(recipient, chatMessageList);
    }

    @Override
    public void renderMessage(ChatMessage chatMessage) {
        final boolean addedToTop = chatMessage != null && !chatMessage.equals(messageAdapter.getOldestChatMessage());
        final boolean scrollToEndAfterUpdate = isLastItemDisplayed();
        final int lastVisiblePosition = messageLayoutManager.findLastCompletelyVisibleItemPosition();
        final ChatMessage lastVisible = lastVisiblePosition != -1 && messageAdapter.getItemCount() > 0 ? messageAdapter.getMessage(lastVisiblePosition) : null;

        List<ChatMessage> result = ChatMessage.computeMessageList(null, chatMessageList);
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

    private void showImage(ImageView imageViewFrom) {
        recyclerViewImageView = imageViewFrom;

        imageViewClicked = new ImageView(this);
        imageViewClicked.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageViewClicked.setImageDrawable(imageViewFrom.getDrawable());

        prepareViewOpen(recyclerViewImageView, imageViewClicked);
    }

    private void showVideo(ImageView imageViewFrom, ChatMessage message) {
        recyclerViewImageView = imageViewFrom;

        tribeVideoView = new TribeVideoView(this);
        tribeVideoView.setMute(false);
        tribeVideoView.setLooping(true);
        tribeVideoView.setAutoStart(true);
        tribeVideoView.setSpeedControl(false);
        tribeVideoView.setScaleType(ScalableTextureView.CENTER_CROP);
        tribeVideoView.createPlayer(message.getContent().contains("content://") ? message.getContent() : FileUtils.getPathForId(message.getId()));
        subscriptions.add(tribeVideoView.videoSize().subscribe(videoSize -> {
            this.videoSize = videoSize;
            if (!recyclerViewText.isEnabled()) animateFullScreen(tribeVideoView);
        }));

        prepareViewOpen(recyclerViewImageView, tribeVideoView);
    }

    private void prepareViewOpen(View viewFrom, View viewTo) {
        int [] location = new int[2];
        viewFrom.getLocationOnScreen(location);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewFrom.getWidth(), viewFrom.getHeight());
        params.leftMargin = location[0];
        params.topMargin = location[1] - statusBarHeight;
        viewTo.setLayoutParams(params);

        widthViewClicked = viewFrom.getWidth();
        heightViewClicked = viewFrom.getHeight();
        marginLeftViewClicked = params.leftMargin;
        marginTopViewClicked = params.topMargin;

        rootView.addView(viewTo);

        viewTo.setClickable(true);
        viewTo.setFocusableInTouchMode(true);
        viewTo.setOnTouchListener(new View.OnTouchListener() {

            int yDelta, downY;
            int ogTopMargin, ogLeftMargin;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int touchY = (int) event.getRawY();

                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) viewTo.getLayoutParams();

                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        ogLeftMargin = layoutParams.leftMargin;
                        ogTopMargin = layoutParams.topMargin;
                        yDelta = touchY - layoutParams.topMargin;
                        downY = touchY;
                        break;

                    case MotionEvent.ACTION_CANCEL: case MotionEvent.ACTION_UP:
                        if (Math.abs(downY - touchY) > dismissMove) {
                            snapImageBack(viewTo);
                        } else {
                            ValueAnimator animator = ValueAnimator.ofInt(layoutParams.topMargin, ogTopMargin);
                            animator.setDuration(DURATION);
                            animator.addUpdateListener(animation -> {
                                layoutParams.topMargin = (Integer) animation.getAnimatedValue();
                                viewTo.setLayoutParams(layoutParams);
                            });

                            animator.start();
                        }

                    case MotionEvent.ACTION_MOVE:
                        layoutParams.topMargin = touchY - yDelta;
                        viewTo.setLayoutParams(layoutParams);

                        break;
                }

                viewTo.invalidate();
                return true;
            }
        });

        viewTo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (viewTo == imageViewClicked) animateFullScreen(viewTo);
                recyclerViewText.setEnabled(false);
                viewTo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void animateFullScreen(View viewToAnimate) {
        int targetHeight = viewToAnimate ==
                imageViewClicked ?
                (widthViewClicked > heightViewClicked ? (int) (screenUtils.getWidthPx() * ((float) widthViewClicked / heightViewClicked)) : screenUtils.getHeightPx())
                : screenUtils.getHeightPx();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewToAnimate.getLayoutParams();
        ValueAnimator animatorTopMargin = ValueAnimator.ofInt(marginTopViewClicked, ((screenUtils.getHeightPx() - statusBarHeight) >> 1) - (targetHeight >> 1));
        animatorTopMargin.setDuration(DURATION);
        animatorTopMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorTopMargin.addUpdateListener(animation -> {
            params.topMargin = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });
        animatorTopMargin.start();

        ValueAnimator animatorLeftMargin = ValueAnimator.ofInt(params.leftMargin, 0);
        animatorLeftMargin.setDuration(DURATION);
        animatorLeftMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorLeftMargin.addUpdateListener(animation -> {
            params.leftMargin = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });

        animatorLeftMargin.start();

        ValueAnimator animatorWidth = ValueAnimator.ofInt(widthViewClicked, screenUtils.getWidthPx());
        animatorWidth.setDuration(DURATION);
        animatorWidth.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorWidth.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });

        animatorWidth.start();

        ValueAnimator animatorHeight = ValueAnimator.ofInt(heightViewClicked, targetHeight);
        animatorHeight.setDuration(DURATION);
        animatorHeight.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorHeight.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
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

    private void snapImageBack(View viewBack) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewBack.getLayoutParams();
        ValueAnimator animatorTopMargin = ValueAnimator.ofInt(params.topMargin, marginTopViewClicked);
        animatorTopMargin.setDuration(DURATION);
        animatorTopMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorTopMargin.addUpdateListener(animation -> {
            params.topMargin = (Integer) animation.getAnimatedValue();
            viewBack.setLayoutParams(params);
        });
        animatorTopMargin.start();

        ValueAnimator animatorLeftMargin = ValueAnimator.ofInt(params.leftMargin, marginLeftViewClicked);
        animatorLeftMargin.setDuration(DURATION);
        animatorLeftMargin.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorLeftMargin.addUpdateListener(animation -> {
            params.leftMargin = (Integer) animation.getAnimatedValue();
            viewBack.setLayoutParams(params);
        });

        animatorLeftMargin.start();

        ValueAnimator animatorWidth = ValueAnimator.ofInt(params.width, widthViewClicked);
        animatorWidth.setDuration(DURATION);
        animatorWidth.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorWidth.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            viewBack.setLayoutParams(params);
        });

        animatorWidth.start();

        ValueAnimator animatorHeight = ValueAnimator.ofInt(params.height, heightViewClicked);
        animatorHeight.setDuration(DURATION);
        animatorHeight.setInterpolator(new OvershootInterpolator(OVERSHOOT));
        animatorHeight.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            viewBack.setLayoutParams(params);
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
                rootView.removeView(viewBack);

                if (viewBack == tribeVideoView) tribeVideoView.releasePlayer();

                if (recyclerViewImageView != null) {
                    recyclerViewImageView.setAlpha(1f);
                    recyclerViewImageView = null;
                }
            }
        });

        animatorAlpha.start();
    }

    private void updateAvatars() {
        int firstVisibleItem = messageLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItem = messageLayoutManager.findLastVisibleItemPosition();

        List<String> toRemove = new ArrayList<>();

        for (String sectionId : avatarsMap.keySet()) {
            Section section = avatarsMap.get(sectionId);
            View vBegin = messageLayoutManager.findViewByPosition(section.getBegin());
            View vEnd = messageLayoutManager.findViewByPosition(section.getEnd());

            if (lastVisibleItem < section.getBegin() || firstVisibleItem > section.getEnd())
                toRemove.add(sectionId);
            else {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) section.getImageView().getLayoutParams();
                params.topMargin = Math.max(
                        vBegin != null ? vBegin.getTop() + vBegin.getPaddingTop() : Integer.MIN_VALUE,
                        Math.min(vEnd != null ? vEnd.getTop() + vEnd.getHeight() - avatarSize - vEnd.getPaddingBottom() : Integer.MAX_VALUE,
                                layoutContent.getHeight() - avatarSize - marginHorizontalLeftSmall));
                section.getImageView().requestLayout();
            }
        }

        for (String id : toRemove) {
            Observable.just(avatarsMap.remove(id))
                    .delay(0, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(section1 -> {
                        layoutContent.removeView(section1.getImageView());
                        layoutContent.requestLayout();
                        section1.setImageView(null);
                        section1 = null;
                    });
        }

        if (messageLayoutManager.findFirstVisibleItemPosition() != -1) {
            for (int i = firstVisibleItem; i <= lastVisibleItem; i++) {
                ChatMessage chatMessage = messageAdapter.getMessage(i);
                View vEnd = messageLayoutManager.findViewByPosition(chatMessage.getEndOfSection());
                View vBegin = messageLayoutManager.findViewByPosition(chatMessage.getBeginOfSection());

                if ((chatMessage.isLastOfPerson() || chatMessage.isOtherPerson()) && !avatarsMap.containsKey(chatMessage.getSectionId())) {
                    int topMargin = Math.max(
                            vBegin != null ? vBegin.getTop() + vBegin.getPaddingTop() : Integer.MIN_VALUE,
                            Math.min(vEnd != null ? vEnd.getTop() + vEnd.getHeight() - avatarSize - vEnd.getPaddingBottom() : Integer.MAX_VALUE,
                                    layoutContent.getHeight() - avatarSize - marginHorizontalLeftSmall));

                    ImageView avatar = new ImageView(this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(avatarSize, avatarSize);
                    params.leftMargin = marginHorizontalLeftSmall;
                    params.topMargin = topMargin;
                    avatar.setLayoutParams(params);

                    picasso.load(chatMessage.getFrom().getProfilePicture())
                            .fit()
                            .centerCrop()
                            .transform(new RoundedCornersTransformation(avatarSize >> 1, 0, RoundedCornersTransformation.CornerType.ALL))
                            .into(avatar);

                    layoutContent.addView(avatar);
                    layoutContent.requestLayout();
                    avatarsMap.put(chatMessage.getSectionId(), new Section(chatMessage.getBeginOfSection(), chatMessage.getEndOfSection(), avatar));
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
            String type = getContentResolver().getType(data.getData());
            ChatMessage chatMessage = null;

            if (type.equals("image/jpeg")) {
                chatMessage = ChatMessage.createMessage(ChatMessage.PHOTO, data.getData().toString(), getCurrentUser(), recipient,  getApplicationComponent().dateUtils());
            } else if (type.equals("video/mp4")) {
                chatMessage = ChatMessage.createMessage(ChatMessage.VIDEO, data.getData().toString(), getCurrentUser(), recipient,  getApplicationComponent().dateUtils());
            }

            if (chatMessage != null) chatPresenter.sendMessage(chatMessage);
        }
    }

    @Override
    public void onBackPressed() {
        if (layoutContent.getAlpha() == 0f) snapImageBack(imageViewClicked == null ? tribeVideoView : imageViewClicked);
        else super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.activity_in_scale, R.anim.activity_out_to_right);
    }
}