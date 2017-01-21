package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Friendship;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.adapter.LiveInviteAdapter;
import com.tribe.app.presentation.view.adapter.manager.LiveInviteLayoutManager;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveInviteView extends FrameLayout {

    @Inject
    ScreenUtils screenUtils;

    @Inject
    LiveInviteAdapter adapter;

    @BindView(R.id.recyclerViewFriends)
    RecyclerView recyclerViewFriends;

    // VARIABLES

    // VARIABLES
    private LiveInviteLayoutManager layoutManager;

    // RESOURCES

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Integer> onScroll = PublishSubject.create();
    private PublishSubject<Integer> onScrollStateChanged = PublishSubject.create();

    public LiveInviteView(Context context) {
        super(context);
        init(context, null);
    }

    public LiveInviteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        unbinder.unbind();

        if (subscriptions != null && subscriptions.hasSubscriptions()) {
            subscriptions.unsubscribe();
        }

        super.onDetachedFromWindow();
    }

    @Override
    protected void onFinishInflate() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_live_invite, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initUI();
        initRecyclerView();

        super.onFinishInflate();
    }

    //////////////////////
    //      INIT        //
    //////////////////////

    private void init(Context context, AttributeSet attrs) {

    }

    private void initUI() {
    }

    private void initResources() {

    }

    private void initRecyclerView() {
        layoutManager = new LiveInviteLayoutManager(getContext());
        recyclerViewFriends.setLayoutManager(layoutManager);
        recyclerViewFriends.setItemAnimator(null);
        adapter.setItems(new ArrayList<>());
        recyclerViewFriends.setAdapter(adapter);

        // TODO HACK FIND ANOTHER WAY OF OPTIMIZING THE VIEW?
        recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(0, 50);
        recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(1, 50);
        recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(2, 50);
        recyclerViewFriends.getRecycledViewPool().setMaxRecycledViews(3, 50);

        recyclerViewFriends.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                onScroll.onNext(dy);
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                onScrollStateChanged.onNext(newState);
            }
        });
    }

    public TileView findViewByCoords(float rawX, float rawY) {
        // Find the child view that was touched (perform a hit test)
        int[] recyclerViewCoords = new int[2];
        recyclerViewFriends.getLocationOnScreen(recyclerViewCoords);

        TileView view = (TileView) ViewUtils.findViewAt(recyclerViewFriends, TileView.class, (int) rawX, (int) rawY);
        return view;
    }

    public void renderFriendshipList(List<Friendship> friendshipList) {
        adapter.setItems(new ArrayList<>(friendshipList));
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////

    public Observable<Integer> onScroll() {
        return onScroll;
    }

    public Observable<Integer> onScrollStateChanged() {
        return onScrollStateChanged;
    }
}

