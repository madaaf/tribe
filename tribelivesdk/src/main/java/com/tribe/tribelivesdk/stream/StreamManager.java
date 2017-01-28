package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.util.LogUtil;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;

import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;

import rx.Observable;

public final class StreamManager {

    private Context context;
    private TribeLiveLocalStream liveLocalStream;
    private PeerView localStreamView;
    private final ObservableRxHashMap<String, RemotePeer> remotePeerMap = new ObservableRxHashMap<>();

    public StreamManager(Context context) {
        this.context = context;
    }

    public void initLocalStreamView(PeerView localStreamView, PeerConnectionFactory peerConnectionFactory) {
        this.localStreamView = localStreamView;
        generateLocalStream(context, peerConnectionFactory);
        liveLocalStream.startVideoCapture();
    }

    public MediaStream generateLocalStream(Context context, PeerConnectionFactory peerConnectionFactory) {
        if (peerConnectionFactory == null) {
            throw new IllegalArgumentException("Attempt to generateLocalStream but PeerConnectionFactory is null");
        }

        if (localStreamView == null) {
            throw new IllegalStateException("Attempt to generateLocalStream but view has not been set");
        }

        if (liveLocalStream == null) {
            liveLocalStream = new TribeLiveLocalStream(context, localStreamView, peerConnectionFactory);
        }

        return liveLocalStream.asNativeMediaStream();
    }

    public void generateNewRemotePeer(String peerId) {
        if (TextUtils.isEmpty(peerId)) {
            LogUtil.e(getClass(), "Attempt to generate remote stream with null peerId.");
            return;
        }

        LogUtil.d(getClass(), "Generating new remote peer : " + peerId);
        RemotePeer remotePeer = new RemotePeer(peerId);
        RemotePeerView remotePeerView = new RemotePeerView(context);
        remotePeer.setPeerView(remotePeerView);

        remotePeerMap.put(peerId, remotePeer);
    }

    public void setMediaStreamForClient(@NonNull String peerId, @NonNull MediaStream mediaStream) {
        if (TextUtils.isEmpty(peerId)) {
            LogUtil.e(getClass(), "We found a null peerId it doesn't make sense!");
            return;
        }

        if (mediaStream == null) {
            LogUtil.e(getClass(), "Cannot set a null mediaStream to peerId: " + mediaStream);
            return;
        }

        RemotePeer remotePeer = remotePeerMap.get(peerId);
        if (remotePeer == null) {
            LogUtil.e(getClass(), "Attempted to set MediaStream for non-existent RemotePeer: " + peerId);
            return;
        }

        LogUtil.d(getClass(), "Setting the stream to peer : " + peerId);
        remotePeer.getPeerView().setStream(mediaStream);
    }

    public void switchCamera() {
        if (liveLocalStream == null) {
            LogUtil.d(getClass(), "Live Local Stream is null");
        }

        liveLocalStream.switchCamera();
    }

    public void dispose() {
        liveLocalStream.dispose();
        localStreamView.dispose();
    }

    public Observable<ObservableRxHashMap.RxHashMap<String, RemotePeer>> onRemotePeersChanged() {
        return remotePeerMap.getObservable();
    }
}
