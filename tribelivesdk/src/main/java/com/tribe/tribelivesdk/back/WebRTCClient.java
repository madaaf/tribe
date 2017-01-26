package com.tribe.tribelivesdk.back;

import android.content.Context;

import com.tribe.tribelivesdk.core.TribePeerConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeAnswer;
import com.tribe.tribelivesdk.model.TribeCandidate;
import com.tribe.tribelivesdk.model.TribeMediaStream;
import com.tribe.tribelivesdk.model.TribeOffer;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.stream.StreamManager;
import com.tribe.tribelivesdk.util.LogUtil;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.PeerView;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SessionDescription;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

import static android.R.attr.id;

@Singleton
public class WebRTCClient {

    // VARIABLES
    private Context context;
    private Map<String, TribePeerConnection> peerConnections;
    private MediaStream localMediaStream;
    private PeerConnectionFactory peerConnectionFactory;
    private List<PeerConnection.IceServer> iceServers;
    private StreamManager streamManager;

    // OBSERVABLES
    private CompositeSubscription subscriptions = new CompositeSubscription();
    private PublishSubject<TribeOffer> onReadyToSendSdpOffer = PublishSubject.create();
    private PublishSubject<TribeAnswer> onReadyToSendSdpAnswer = PublishSubject.create();
    private PublishSubject<TribeCandidate> onReceivedTribeCandidate = PublishSubject.create();
    private PublishSubject<TribeMediaStream> onReceivedPeer = PublishSubject.create();

    @Inject
    public WebRTCClient(Context context, TribeLiveOptions options) {
        this.context = context;
        this.iceServers = options.getIceServers();
        this.streamManager = new StreamManager(context);
        this.peerConnections = new HashMap<>();
        initPeerConnectionFactory();
    }

    private void initPeerConnectionFactory() {
        if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true, false)) {
            LogUtil.e(getClass(), "Failed to initializeAndroidGlobals");
        }

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = new PeerConnectionFactory(options);
    }

    public PeerConnectionFactory getPeerConnectionFactory() {
        return peerConnectionFactory;
    }

    public void addPeerConnection(TribeSession session, boolean isOffer) {
        if (session == null) {
            LogUtil.e(getClass(), "Attempt to addPeerConnection with null peerId");
            return;
        }

        if (peerConnections.get(session.getPeerId()) != null) {
            LogUtil.i(getClass(), "Client already exists - not adding client again. " + id);
            return;
        }

        TribePeerConnection remotePeer = createPeerConnection(session, isOffer);
        peerConnections.put(session.getPeerId(), remotePeer);
        remotePeer.getPeerConnection().addStream(localMediaStream);

        subscriptions.add(remotePeer.onReadyToSendSdpOffer().subscribe(onReadyToSendSdpOffer));
        subscriptions.add(remotePeer.onReadyToSendSdpAnswer().subscribe(onReadyToSendSdpAnswer));
        subscriptions.add(remotePeer.onReceiveTribeCandidate().subscribe(onReceivedTribeCandidate));
        subscriptions.add(
                remotePeer.onReceivedMediaStream()
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnNext(tribeMediaStream -> streamManager.generateNewRemotePeer(tribeMediaStream.getId()))
                        .doOnNext(tribeMediaStream -> streamManager.setMediaStreamForClient(session.getPeerId(), tribeMediaStream.getMediaStream()))
                        .subscribe(onReceivedPeer)
        );
    }

    private TribePeerConnection createPeerConnection(TribeSession session, boolean isOffer) {
        return new TribePeerConnection(session, peerConnectionFactory, iceServers, isOffer);
    }

    //
//    public void dropAllPeerConnections() {
//        try {
//            Iterator localIterator = peerConnections.values().iterator();
//            while (localIterator.hasNext()) {
//                ((TribePeerConnection) localIterator.next()).remove();
//            }
//
//            peerConnections.clear();
//        } catch (Throwable localThrowable) {
//            throw localThrowable;
//        }
//    }
//
//    private boolean removePeerConnection(String peerId) {
//        TribePeerConnection tribePeerConnection = peerConnections.get(peerId);
//
//        if (tribePeerConnection == null) {
//            LogUtil.e(getClass(), "Attempt to removePeerConnection on invalid clientId");
//            return false;
//        }
//
//        tribePeerConnection.remove();
//
//        try {
//            peerConnections.remove(paramString);
//            return true;
//        } catch (Throwable throwable) {
//            throw throwable;
//        }
//    }
//
    private void initLocalStream() {
        if (localMediaStream == null) {
            localMediaStream = streamManager.generateLocalStream(context, peerConnectionFactory);
        }

        for (TribePeerConnection tpc : peerConnections.values()) {
            //tpc.addStream(localMediaStream);
        }
    }

    public void switchCamera() {
        streamManager.switchCamera();
    }

    public void setLocalStreamView(PeerView peerView, PeerConnectionFactory peerConnectionFactory) {
        streamManager.initLocalStreamView(peerView, peerConnectionFactory);
        initLocalStream();
    }

    public void addIceCandidate(String peerId, IceCandidate iceCandidate) {
        TribePeerConnection tribePeerConnection = peerConnections.get(peerId);

        if (tribePeerConnection == null) {
            LogUtil.e(getClass(), "Attempt to addIceCandidate on invalid clientId");
            return;
        }

        tribePeerConnection.addIceCandidate(iceCandidate);
    }

//    public void createOffer(String paramString) {
//        TribePeerConnection tribePeerConnection = (TribePeerConnection) peerConnections.get(paramString);
//
//        if (tribePeerConnection == null) {
//            LogUtil.d(getClass(), "Attempt to createOffer but client does not exist");
//            return;
//        }
//
//        tribePeerConnection.createOffer();
//    }
//
    public void setRemoteDescription(String peerId, SessionDescription sdp) {
        TribePeerConnection tribePeerConnection = peerConnections.get(peerId);

        if (tribePeerConnection == null) {
            LogUtil.d(getClass(), "Peer is null, creating it");
            addPeerConnection(new TribeSession(peerId, peerId), false);
            tribePeerConnection = peerConnections.get(peerId);
        }

        tribePeerConnection.setRemoteDescription(sdp);
    }

    public void leaveRoom() {
        streamManager.dispose();
    }
//
//    public void setLocalAudioEnabled(boolean bool) {
//        streamManager.setLocalAudioEnabled(bool);
//    }
//
//    public void setLocalCameraEnabled(boolean bool) {
//        // TODO streamManager.setLocalCameraEnabled(bool);
//    }


    /////////////////
    // OBSERVABLES //
    /////////////////

    public Observable<TribeOffer> onReadyToSendSdpOffer() {
        return onReadyToSendSdpOffer;
    }

    public Observable<TribeAnswer> onReadyToSendSdpAnswer() {
        return onReadyToSendSdpAnswer;
    }

    public Observable<TribeCandidate> onReceivedTribeCandidate() {
        return onReceivedTribeCandidate;
    }

    public Observable<ObservableRxHashMap.RxHashMap<RemotePeer>> onRemotePeersChanged() {
        return streamManager.onRemotePeersChanged();
    }
}