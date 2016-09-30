package com.tribe.app.presentation.view.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.tribe.app.R;
import com.tribe.app.domain.entity.PTSEntity;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.utils.StringUtils;
import com.tribe.app.presentation.view.adapter.PullToSearchAdapter;
import com.tribe.app.presentation.view.adapter.manager.PullToSearchLayoutManager;
import com.tribe.app.presentation.view.decorator.GridDividerTopAllItemDecoration;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.TextViewUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 10/06/2016.
 */
public class PullToSearchView extends FrameLayout {

    private static final SpringConfig PULL_TO_SEARCH_BOUNCE_SPRING_CONFIG = SpringConfig.fromBouncinessAndSpeed(3, 15);
    private static final int DURATION = 300;
    public static final String DIEZ = "#";
    public static final String EMOJI = "\uD83D\uDE03";
    public static final String UNREAD = "\u2709";
    public static final String HOME = "\uD83C\uDFE0";
    private static final String[] ENTITIES = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z", DIEZ, EMOJI, UNREAD, HOME };

    @Inject
    ScreenUtils screenUtils;

    @BindView(R.id.recyclerViewPTS)
    RecyclerView recyclerViewPTS;

    // VARIABLES
    private PullToSearchLayoutManager ptsLayoutManager;
    private PullToSearchAdapter ptsAdapter;
    private static Map<String, PTSEntity> entityMap = new HashMap<>();
    private int emojiPosition, unreadPosition, homePosition;
    private int selectedPosition;
    private TextViewFont textViewClicked;
    private TextViewFont recyclerViewTextView;
    private int widthViewClicked, heightViewClicked, marginLeftViewClicked, marginTopViewClicked;
    private SpringSystem springSystem = null;
    private Spring spring;
    private SpringListener springListener;

    // RESOURCES
    private int marginTopDivider;
    private int sizeLetterSelected;

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<String> letterSelected = PublishSubject.create();

    public PullToSearchView(Context context) {
        super(context);
        init(context, null);
    }

    public PullToSearchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        spring.addListener(springListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
        }

        ptsAdapter.releaseSubscriptions();
        spring.removeListener(springListener);

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_pull_to_search, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initUI();
        initRecyclerView();
        initItems();

        super.onFinishInflate();
    }

    private void init(Context context, AttributeSet attrs) {
        List<String> entities = Arrays.asList(ENTITIES);
        emojiPosition = entities.indexOf(EMOJI);
        unreadPosition = entities.indexOf(UNREAD);
        homePosition = entities.indexOf(HOME);
    }

    private void initUI() {
        springSystem = SpringSystem.create();
        spring = springSystem.createSpring();
        spring.setSpringConfig(PULL_TO_SEARCH_BOUNCE_SPRING_CONFIG);
        spring.setEndValue(screenUtils.getHeightPx()).setAtRest();
        springListener = new SpringListener();
    }

    private void initResources() {
        marginTopDivider = getContext().getResources().getDimensionPixelSize(R.dimen.vertical_margin_mid);
        sizeLetterSelected = getContext().getResources().getDimensionPixelSize(R.dimen.pull_to_search_letter_selected_size);
    }

    private void initRecyclerView() {
        ptsLayoutManager = new PullToSearchLayoutManager(getContext());
        recyclerViewPTS.setLayoutManager(ptsLayoutManager);
        ptsAdapter = new PullToSearchAdapter(getContext());
        recyclerViewPTS.setAdapter(ptsAdapter);
        recyclerViewPTS.setHasFixedSize(true);
        recyclerViewPTS.addItemDecoration(new GridDividerTopAllItemDecoration(marginTopDivider, ptsLayoutManager.getSpanCount()));

        subscriptions.add(ptsAdapter.onClickLetter().subscribe(viewFrom ->  {
            textViewClicked = null;

            selectedPosition = (Integer) viewFrom.getTag(R.id.tag_position);
            viewFrom.getGlobalVisibleRect(new Rect());
            Rect scrollBounds = new Rect();
            recyclerViewPTS.getHitRect(scrollBounds);

            if (!viewFrom.getLocalVisibleRect(scrollBounds)
                    || scrollBounds.height() < viewFrom.getHeight()) {
                recyclerViewPTS.smoothScrollToPosition(selectedPosition);
                Observable.timer(500, TimeUnit.MILLISECONDS)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            showTextView(viewFrom);
                        });
            } else {
                showTextView(viewFrom);
            }
        }));
    }

    private void initItems() {
        List<PTSEntity> ptsEntityList = new ArrayList<>();

        for (String str : ENTITIES) {
            PTSEntity ptsEntity = new PTSEntity(PTSEntity.LETTER);
            ptsEntity.setLetter(str);
            ptsEntityList.add(ptsEntity);
            if (str.equals(HOME)) ptsEntity.setActivated(true);
            entityMap.put(str, ptsEntity);
        }

        ptsAdapter.setItems(ptsEntityList);
    }

    public void updatePTSList(List<Recipient> recipientList) {
        for (Recipient recipient : recipientList) {
            if (isUnread(recipient)) {
                entityMap.get(UNREAD).setActivated(true);
            } else if (isEmoji(recipient)) {
                entityMap.get(EMOJI).setActivated(true);
            } else if (isLetter(recipient)) {
                String firstCharacter = StringUtils.getFirstCharacter(recipient.getDisplayName());
                PTSEntity entity = entityMap.get(firstCharacter.toUpperCase());
                entity.setActivated(true);
            } else {
                PTSEntity entitySpecialCharacters = entityMap.get(DIEZ);
                entitySpecialCharacters.setActivated(true);
            }
        }

        ptsAdapter.notifyDataSetChanged();
    }

    //////////////////////
    //    ANIMATIONS    //
    //////////////////////

    private void showTextView(TextViewFont textViewFrom) {
        recyclerViewTextView = textViewFrom;

        textViewClicked = new TextViewFont(getContext());
        TextViewUtils.setTextAppearence(getContext(), textViewClicked, R.style.Title_2_White);
        textViewClicked.setCustomFont(getContext(), textViewFrom.getCustomFont());
        textViewClicked.setGravity(Gravity.CENTER);

        textViewClicked.setText(textViewFrom.getText());
        prepareViewSelect(recyclerViewTextView, textViewClicked);
    }

    private void prepareViewSelect(View viewFrom, View viewTo) {
        int [] location = new int[2];
        viewFrom.getLocationOnScreen(location);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(viewFrom.getWidth(), viewFrom.getHeight());
        params.leftMargin = location[0];
        params.topMargin = location[1];
        viewTo.setLayoutParams(params);

        widthViewClicked = viewFrom.getWidth();
        heightViewClicked = viewFrom.getHeight();
        marginLeftViewClicked = params.leftMargin;
        marginTopViewClicked = params.topMargin;

        addView(viewTo);

        viewTo.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (viewTo == textViewClicked) animateSelect(viewTo);
                recyclerViewPTS.setEnabled(false);
                viewTo.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void animateSelect(View viewToAnimate) {
        int targetHeight = sizeLetterSelected;
        int targetWidth = sizeLetterSelected;

        Drawable backgrounds[] = new Drawable[2];
        backgrounds[0] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_rect_white20_rounded_corners, null);
        backgrounds[1] = ResourcesCompat.getDrawable(getResources(), R.drawable.shape_rect_white_rounded_corners, null);

        TransitionDrawable crossFader = new TransitionDrawable(backgrounds);
        textViewClicked.setBackground(crossFader);
        crossFader.startTransition(DURATION);

        int colorFrom = Color.WHITE;
        int colorTo = Color.BLACK;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.addUpdateListener(animator -> textViewClicked.setTextColor((Integer)animator.getAnimatedValue()));
        colorAnimation.start();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) viewToAnimate.getLayoutParams();
        ValueAnimator animatorTopMargin = ValueAnimator.ofInt(marginTopViewClicked, (screenUtils.getHeightPx() >> 1) - (targetHeight >> 1));
        animatorTopMargin.setDuration(DURATION);
        animatorTopMargin.setInterpolator(new DecelerateInterpolator());
        animatorTopMargin.addUpdateListener(animation -> {
            params.topMargin = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });
        animatorTopMargin.start();

        ValueAnimator animatorLeftMargin = ValueAnimator.ofInt(marginLeftViewClicked, (screenUtils.getWidthPx() >> 1) - (targetWidth >> 1));
        animatorLeftMargin.setDuration(DURATION);
        animatorLeftMargin.setInterpolator(new DecelerateInterpolator());
        animatorLeftMargin.addUpdateListener(animation -> {
            params.leftMargin = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });

        animatorLeftMargin.start();

        ValueAnimator animatorAlpha = ValueAnimator.ofInt(1, 0);
        animatorAlpha.setDuration(DURATION >> 1);
        animatorAlpha.setInterpolator(new DecelerateInterpolator());
        animatorAlpha.addUpdateListener(animation -> {
            int value = (Integer) animation.getAnimatedValue();
            recyclerViewPTS.setAlpha(value);
        });

        animatorAlpha.start();

        ValueAnimator animatorWidth = ValueAnimator.ofInt(widthViewClicked, targetWidth);
        animatorWidth.setDuration(DURATION);
        animatorWidth.setInterpolator(new DecelerateInterpolator());
        animatorWidth.addUpdateListener(animation -> {
            params.width = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });

        animatorWidth.start();

        ValueAnimator animatorHeight = ValueAnimator.ofInt(heightViewClicked, targetHeight);
        animatorHeight.setDuration(DURATION);
        animatorHeight.setInterpolator(new DecelerateInterpolator());
        animatorHeight.addUpdateListener(animation -> {
            params.height = (Integer) animation.getAnimatedValue();
            viewToAnimate.setLayoutParams(params);
        });
        animatorHeight.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                recyclerViewPTS.setEnabled(true);
                letterSelected.onNext(ENTITIES[selectedPosition]);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                recyclerViewTextView.setAlpha(0f);
            }
        });

        animatorHeight.start();
    }

    public void close() {
        cleanUp();
    }

    public void open() {
        spring.setEndValue(0);
    }

    private class SpringListener extends SimpleSpringListener {
        @Override
        public void onSpringUpdate(Spring spring) {
            if (ViewCompat.isAttachedToWindow(PullToSearchView.this)) {
                int value = (int) spring.getCurrentValue();
                translateTop(value);
            }
        }

        @Override
        public void onSpringAtRest(Spring spring) {

        }
    }

    private void translateTop(float value) {
        recyclerViewPTS.setTranslationY(value);
    }

    private void cleanUp() {
        recyclerViewPTS.setAlpha(1f);
        spring.setEndValue(screenUtils.getHeightPx()).setAtRest();
        if (recyclerViewTextView != null) {
            recyclerViewTextView.setAlpha(1f);
            removeView(textViewClicked);
            textViewClicked = null;
            recyclerViewTextView = null;
        }
    }

    //////////////////////
    //     HELPERS      //
    //////////////////////

    public static boolean isLetter(Recipient recipient) {
        String firstCharacter = StringUtils.getFirstCharacter(recipient.getDisplayName());
        PTSEntity entity = entityMap.get(firstCharacter.toUpperCase());
        if (entity != null)
            return true;


        return false;
    }

    public static boolean isUnread(Recipient recipient) {
        if (recipient.getReceivedTribes() != null && !recipient.getReceivedTribes().isEmpty()
                || recipient.getReceivedMessages() != null && !recipient.getReceivedMessages().isEmpty()) {
            return true;
        }

        return false;
    }

    public static boolean isEmoji(Recipient recipient) {
        if (recipient.getDisplayName().length() > 5 && recipient.getDisplayName().substring(0, 5) == ("/[\u2190-\u21FF] | [\u2600-\u26FF] | [\u2700-\u27BF] | [\u3000-\u303F] | [\u1F300-\u1F64F] | [\u1F680-\u1F6FF]/g")) {
            return true;
        }

        return false;
    }

    public static boolean shouldFilter(String filter, Recipient recipient) {
        if (filter.equals(PullToSearchView.UNREAD) && PullToSearchView.isUnread(recipient)
                || filter.equals(PullToSearchView.EMOJI) && PullToSearchView.isEmoji(recipient)
                || filter.equals(PullToSearchView.DIEZ) && !PullToSearchView.isLetter(recipient)) {
            return true;
        } else if (PullToSearchView.isLetter(recipient)) {
            String firstCharacter = StringUtils.getFirstCharacter(recipient.getDisplayName());
            if (!firstCharacter.isEmpty() && firstCharacter.equalsIgnoreCase(filter)) {
                return true;
            }
        } else if (filter.equals(PullToSearchView.HOME)) {
            return true;
        }

        return false;
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////

    public Observable<String> onLetterSelected() {
        return letterSelected;
    }
}
