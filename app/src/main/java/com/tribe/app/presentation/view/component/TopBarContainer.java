package com.tribe.app.presentation.view.component;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.view.utils.SoundManager;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class TopBarContainer extends FrameLayout {

    @Inject
    SoundManager soundManager;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerView;

    @BindView(R.id.topBarView)
    TopBarView topBarView;

    // VARIABLES

    // DIMENS

    // BINDERS / SUBSCRIPTIONS
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> clickSettings = PublishSubject.create();
    private PublishSubject<Void> clickNew = PublishSubject.create();

    public TopBarContainer(Context context) {
        super(context);
    }

    public TopBarContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {

        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
            subscriptions.clear();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        unbinder = ButterKnife.bind(this);

        ApplicationComponent applicationComponent = ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent();
        applicationComponent.inject(this);

        initDimen();
        initUI();
        initSubscriptions();
    }

    private void initUI() {
        recyclerView.setOnTouchListener((v, event) -> {
            int dy = recyclerView.computeVerticalScrollOffset();
            if (event.getY() < topBarView.getHeight() && dy < (topBarView.getHeight() >> 1)) return true;

            return super.onTouchEvent(event);
        });
    }

    private void initDimen() {

    }

    private void initSubscriptions() {
        subscriptions.add(
                topBarView.onClickNew()
                        .subscribe(clickNew)
        );

        subscriptions.add(
                topBarView.onClickSettings()
                        .subscribe(clickSettings)
        );
    }

    public boolean isSearchMode() {
        return topBarView.isSearchMode();
    }

    public void closeSearch() {
        topBarView.closeSearch();
    }

    ///////////////////////
    //    TOUCH EVENTS   //
    ///////////////////////

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean isTouchInTopBar = ev.getRawY() < topBarView.getHeight();
        if (!isEnabled() || canChildScrollUp() || isTouchInTopBar) {
            if (isTouchInTopBar)
                topBarView.onTouchEvent(ev);

            return false;
        }

        return false;
    }

    private boolean canChildScrollUp() {
        return ViewCompat.canScrollVertically(recyclerView, -1);
    }

    ///////////////////////
    //    ANIMATIONS     //
    ///////////////////////

    ///////////////////////
    //    OBSERVABLES    //
    ///////////////////////

    public Observable<Void> onClickSettings() {
        return clickSettings;
    }

    public Observable<Void> onClickNew() {
        return clickNew;
    }
}