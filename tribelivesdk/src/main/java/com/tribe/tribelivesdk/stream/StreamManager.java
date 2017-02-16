package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

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
    }).map(aBoolean -> {
      return null;
    }).subscribe(o -> {
      onMediaChanged.onNext(null);
    }));
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
      Timber.e("Attempt to generate remote stream with null peerId.");
      return;
    }

    Timber.d("Generating new remote peer : " + session.getPeerId());
    RemotePeer remotePeer = new RemotePeer(session);
    RemotePeerView remotePeerView = new RemotePeerView(context);
    remotePeer.setPeerView(remotePeerView);

    remotePeerMap.put(session.getPeerId(), remotePeer);
  }

  public void removePeer(TribeSession tribeSession) {
    if (remotePeerMap != null && remotePeerMap.size() > 0) {
      RemotePeer remotePeer = remotePeerMap.get(tribeSession.getPeerId());
      if (remotePeer != null) remotePeer.dispose();
      remotePeerMap.remove(tribeSession.getPeerId());
    }
  }

  public void setMediaStreamForClient(@NonNull String peerId, @NonNull MediaStream mediaStream) {
    if (TextUtils.isEmpty(peerId)) {
      Timber.e("We found a null peerId it doesn't make sense!");
      return;
    }

    if (mediaStream == null) {
      Timber.e("Cannot set a null mediaStream to peerId: " + mediaStream);
      return;
    }

    RemotePeer remotePeer = remotePeerMap.get(peerId);
    if (remotePeer == null) {
      Timber.e("Attempted to set MediaStream for non-existent RemotePeer: " + peerId);
      return;
    }

    Timber.d("Setting the stream to peer : " + peerId);
    remotePeer.getPeerView().setStream(mediaStream);
  }

  public void setPeerMediaConfiguration(TribePeerMediaConfiguration tribePeerMediaConfiguration) {
    RemotePeer remotePeer = remotePeerMap.get(tribePeerMediaConfiguration.getSession().getPeerId());

    if (remotePeer == null) {
      Timber.d("setMediaConfiguration to a null remotePeer");
      return;
    }

    remotePeer.setMediaConfiguration(tribePeerMediaConfiguration);
  }

  public void switchCamera() {
    if (liveLocalStream == null) {
      Timber.d("Live Local Stream is null");
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
    subscriptions.clear();

    if (remotePeerMap != null && remotePeerMap.size() > 0) {
      Timber.d("Iterating remote peers");
      for (RemotePeer remotePeer : remotePeerMap.getMap().values()) {
        Timber.d("Disposing remote peer");
        remotePeer.dispose();
      }

      Timber.d("Clearing remote peer map");
      remotePeerMap.clear();
    }

    Timber.d("Disposing live local stream");
    liveLocalStream.dispose();
    liveLocalStream = null;

    Timber.d("Disposing stream manager");
    localPeerView.dispose();
    localPeerView = null;

    Timber.d("End disposing stream manager");
  }

  // OBSERVABLES
  public Observable<ObservableRxHashMap.RxHashMap<String, RemotePeer>> onRemotePeersChanged() {
    return remotePeerMap.getObservable();
  }

  public Observable<Void> onMediaChanged() {
    return onMediaChanged;
  }
}
