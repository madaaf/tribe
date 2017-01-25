package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.Membership;
import com.tribe.app.domain.entity.Recipient;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.component.TileView;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.widget.TextViewFont;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.view.LocalPeerView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 01/18/2017.
 */
public class LiveView extends FrameLayout {

    @Inject
    User user;

    @Inject
    ScreenUtils screenUtils;

    @Inject
    TribeLiveSDK tribeLiveSDK;

    @BindView(R.id.viewRoom)
    LiveRoomView viewRoom;

    @BindView(R.id.btnInviteLive)
    View btnInviteLive;

    @BindView(R.id.btnLeave)
    View btnLeave;

    @BindView(R.id.btnNotify)
    View btnNotify;

    @BindView(R.id.txtName)
    TextViewFont txtName;

    // VARIABLES
    private Recipient recipient;
    private LocalPeerView viewLocalPeer;
    private Room room;
    private LiveRowView latestView;

    // RESOURCES

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<Void> onOpenInvite = PublishSubject.create();

    public LiveView(Context context) {
        super(context);
        init(context, null);
    }

    public LiveView(Context context, AttributeSet attrs) {
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
        LayoutInflater.from(getContext()).inflate(R.layout.view_live, this);
        unbinder = ButterKnife.bind(this);
        ((AndroidApplication) getContext().getApplicationContext()).getApplicationComponent().inject(this);

        initResources();
        initUI();
        //initRoom();

        super.onFinishInflate();
    }

    //////////////////////
    //      INIT        //
    //////////////////////

    private void init(Context context, AttributeSet attrs) {

    }

    private void initUI() {
        setBackgroundColor(Color.BLACK);

//        ObjectAnimator a = ObjectAnimator.ofFloat(null, View.SCALE_Y, 0, 1);
//
//        AnimatorSet animator = new AnimatorSet();
//        animator.setStartDelay(0);
//
//        animator.playTogether(a);
//
//        LayoutTransition transition = new LayoutTransition();
//        transition.setAnimator(LayoutTransition.APPEARING, animator);
//        viewRoom.setLayoutTransition(transition);
    }

    private void initResources() {

    }

    private void initRoom() {
        String roomName = "trip";
        room = tribeLiveSDK.getRoom();
        room.initLocalStream(viewLocalPeer);

        subscriptions.add(room.onRoomStateChanged()
                .subscribe(state -> {
                    Timber.d("Room state change : " + state);

                    if (state.equals(Room.CONNECTED)) {
                        joinRoom(roomName);
                    }
                })
        );

        subscriptions.add(room.onRemotePeerAdded()
                .subscribe(remotePeer -> {
                    Timber.d("Remote peer added with id : " + remotePeer.getId() + " & view : " + remotePeer.getPeerView());
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenUtils.getHeightPx() >> 1);
                    addView(remotePeer.getPeerView(), params);
                })
        );

        subscriptions.add(room.onRemotePeerRemoved()
                .subscribe(remotePeer -> {
                    Timber.d("Remote peer removed with id : " + remotePeer.getId());
                })
        );

        subscriptions.add(room.onRemotePeerUpdated()
                .subscribe(remotePeer -> {
                    Timber.d("Remote peer updated with id : " + remotePeer.getId());
                })
        );

        Timber.d("Initiating Room");
        room.connect();
    }

    private void joinRoom(String roomName) {
        Timber.d("Joining room : " + roomName);
        room.joinRoom(roomName);
    }

    ///////////////////
    //    CLICKS     //
    ///////////////////

    @OnClick(R.id.btnInviteLive)
    void openInvite() {
        onOpenInvite.onNext(null);
    }

    ///////////////////
    //    PUBLIC     //
    ///////////////////

    public void initInviteOpenSubscription(Observable<Integer> obs) {
        subscriptions.add(
                obs.subscribe(event -> {
                    viewRoom.setType(event == LiveContainer.EVENT_OPENED ? LiveRoomView.LINEAR : LiveRoomView.GRID);
                })
        );
    }

    public void initOnStartDragSubscription(Observable<TileView> obs) {
        subscriptions.add(
                obs.subscribe(tileView -> {
                    latestView = new LiveRowView(getContext());
                    latestView.setColor(tileView.getBackgroundColor());
                    latestView.setRecipient(tileView.getRecipient());
                    latestView.setRoomType(viewRoom.getType());
                    ViewGroup.LayoutParams params =
                            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    viewRoom.addView(latestView, params);
                })
        );
    }

    public void initOnEndDragSubscription(Observable<Void> obs) {
        subscriptions.add(
                obs.subscribe(aVoid -> {
                    viewRoom.removeView(latestView);
                })
        );
    }

    public void initOnAlphaSubscription(Observable<Float> obs) {
        subscriptions.add(
                obs.subscribe(alpha -> {
                    btnNotify.setAlpha(alpha);
                    btnInviteLive.setAlpha(alpha);
                    btnLeave.setAlpha(alpha);
                })
        );
    }

    public void initDropEnabledSubscription(Observable<Boolean> obs) {
        subscriptions.add(
                obs.subscribe(enabled -> {
                    // TODO DO SOMETHING WITH THIS ?
                })
        );
    }

    public void initDropSubscription(Observable<TileView> obs) {
        subscriptions.add(
                obs.subscribe(tileView -> {
                    tileView.onDrop(latestView);

                    subscriptions.add(tileView.onEndDrop()
                            .subscribe(aVoid -> latestView.startPulse()));
                })
        );
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;

        if (recipient instanceof Membership) {
            txtName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.picto_group_small, 0, 0, 0);
        } else {
            txtName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }

        txtName.setText(recipient.getDisplayName());
    }

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////

    public Observable<Void> onOpenInvite() {
        return onOpenInvite;
    }
}

