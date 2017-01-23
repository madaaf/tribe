package com.tribe.app.presentation.view.component.live;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.tribe.app.R;
import com.tribe.app.domain.entity.User;
import com.tribe.app.presentation.AndroidApplication;
import com.tribe.app.presentation.view.utils.PaletteGrid;
import com.tribe.app.presentation.view.utils.ScreenUtils;
import com.tribe.app.presentation.view.utils.UIUtils;
import com.tribe.tribelivesdk.TribeLiveSDK;
import com.tribe.tribelivesdk.core.Room;
import com.tribe.tribelivesdk.view.LocalPeerView;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
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

    @BindView(R.id.viewWaitingLive)
    LiveWaitingView viewWaitingLive;

    // VARIABLES
    private LocalPeerView viewLocalPeer;
    private Room room;

    // RESOURCES

    // OBSERVABLES
    private Unbinder unbinder;
    private CompositeSubscription subscriptions = new CompositeSubscription();

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

        viewWaitingLive.setColor(PaletteGrid.get(0));
        viewWaitingLive.setAvatarPicture(user.getProfilePicture());
        UIUtils.changeHeightOfView(viewWaitingLive, screenUtils.getHeightPx() >> 1);

//        viewLocalPeer = new LocalPeerView(getContext());
//        FrameLayout.LayoutParams params =
//                new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, screenUtils.getHeightPx() >> 1);
//        params.gravity = Gravity.BOTTOM;
//        addView(viewLocalPeer, params);
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

    //////////////////////
    //   OBSERVABLES    //
    //////////////////////

}

