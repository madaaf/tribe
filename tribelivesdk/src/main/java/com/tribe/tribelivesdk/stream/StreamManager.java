package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.tribe.tribelivesdk.core.TribePeerConnection;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.LogUtil;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import com.tribe.tribelivesdk.view.PeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

public class StreamManager {

  private Context context;
  private TribeLiveLocalStream liveLocalStream;
  private LocalPeerView localPeerView;
  private final ObservableRxHashMap<String, RemotePeer> remotePeerMap = new ObservableRxHashMap<>();

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Void> onMediaChanged = PublishSubject.create();

  public StreamManager(Context context) {
    this.context = context;
  }

  public void initLocalStreamView(LocalPeerView localPeerView,
      PeerConnectionFactory peerConnectionFactory) {
    this.localPeerView = localPeerView;
    generateLocalStream(context, peerConnectionFactory);
    liveLocalStream.startVideoCapture();

    subscriptions.add(this.localPeerView.onSwitchCamera().subscribe(aVoid -> {
      switchCamera();
    }));

    subscriptions.add(this.localPeerView.onEnableCamera().doOnNext(enabled -> {
      setLocalCameraEnabled(enabled);
    }).map(aBoolean -> null).subscribe(o -> onMediaChanged.onNext(null)));
  }

  public MediaStream generateLocalStream(Context context,
      PeerConnectionFactory peerConnectionFactory) {
    if (peerConnectionFactory == null) {
      throw new IllegalArgumentException(
          "Attempt to generateLocalStream but PeerConnectionFactory is null");
    }

    if (localPeerView == null) {
      throw new IllegalStateException("Attempt to generateLocalStream but view has not been set");
    }

    if (liveLocalStream == null) {
      liveLocalStream = new TribeLiveLocalStream(context, localPeerView, peerConnectionFactory);
    }

    return liveLocalStream.asNativeMediaStream();
  }

  public void generateNewRemotePeer(TribeSession session) {
    if (session == null) {
      LogUtil.e(getClass(), "Attempt to generate remote stream with null peerId.");
      return;
    }

    LogUtil.d(getClass(), "Generating new remote peer : " + session.getPeerId());
    RemotePeer remotePeer = new RemotePeer(session);
    RemotePeerView remotePeerView = new RemotePeerView(context);
    remotePeer.setPeerView(remotePeerView);

    remotePeerMap.put(session.getPeerId(), remotePeer);
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

  private void setLocalCameraEnabled(boolean enabled) {
    if (liveLocalStream == null) {
        return;
    }

    liveLocalStream.setCameraEnabled(enabled);
  }

  public boolean isLocalAudioEnabled() {
    return liveLocalStream.isAudioEnabled();
  }

  public boolean isLocalCameraEnabled() {
    return liveLocalStream.isCameraEnabled();
  }

  public void dispose() {
    localPeerView.dispose();
    localPeerView = null;

    liveLocalStream.dispose();
    liveLocalStream = null;

    if (remotePeerMap != null && remotePeerMap.size() > 0) {
      for (RemotePeer remotePeer : remotePeerMap.getMap().values()) {
        remotePeer.dispose();
      }

      remotePeerMap.clear();
    }

    subscriptions.clear();
  }

  // OBSERVABLES
  public Observable<ObservableRxHashMap.RxHashMap<String, RemotePeer>> onRemotePeersChanged() {
    return remotePeerMap.getObservable();
  }

  public Observable<Void> onMediaChanged() {
    return onMediaChanged;
  }
}
