package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.RemotePeer;
import com.tribe.tribelivesdk.model.TribeMediaConstraints;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.model.TribeSession;
import com.tribe.tribelivesdk.util.ObservableRxHashMap;
import com.tribe.tribelivesdk.view.LocalPeerView;
import com.tribe.tribelivesdk.view.RemotePeerView;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class StreamManager {

  private static final int DURATION = 5; // S

  private Context context;
  private TribeLiveLocalStream liveLocalStream;
  private LocalPeerView localPeerView;
  private final ObservableRxHashMap<String, RemotePeer> remotePeerMap = new ObservableRxHashMap<>();

  // OBSERVABLES
  private CompositeSubscription localSubscriptions = new CompositeSubscription();
  private Subscription subscriptionRenderingWell;
  private PublishSubject<TribePeerMediaConfiguration> onMediaChanged = PublishSubject.create();

  public StreamManager(Context context) {
    this.context = context;
  }

  public void initLocalStreamView(LocalPeerView localPeerView,
      PeerConnectionFactory peerConnectionFactory) {
    this.localPeerView = localPeerView;
    generateLocalStream(context, peerConnectionFactory);
    liveLocalStream.startVideoCapture();

    localSubscriptions.add(this.localPeerView.onSwitchCamera().subscribe(aVoid -> switchCamera()));

    localSubscriptions.add(this.localPeerView.onSwitchFilter().subscribe(aVoid -> switchFilter()));

    localSubscriptions.add(this.localPeerView.onStartGame().subscribe(game -> startGame(game)));

    localSubscriptions.add(this.localPeerView.onStopGame().subscribe(aVoid -> stopGame()));

    localSubscriptions.add(this.localPeerView.onEnableCamera()
        .doOnNext(mediaConfiguration -> setLocalCameraEnabled(mediaConfiguration.isVideoEnabled()))
        .subscribe(mediaConfiguration -> onMediaChanged.onNext(mediaConfiguration)));

    localSubscriptions.add(this.localPeerView.onEnableMicro()
        .doOnNext(mediaConfiguration -> setLocalAudioEnabled(mediaConfiguration.isAudioEnabled()))
        .subscribe(mediaConfiguration -> onMediaChanged.onNext(mediaConfiguration)));

    subscriptionRenderingWell =
        Observable.interval(0, DURATION, TimeUnit.SECONDS).onBackpressureDrop().subscribe(aLong -> {
          Collection<RemotePeer> remotePeerCollection = remotePeerMap.getMap().values();
          for (RemotePeer remotePeer : remotePeerCollection) {
            boolean isRenderingWell = remotePeer.isRenderingWell();
            TribePeerMediaConfiguration mediaConfiguration = remotePeer.getMediaConfiguration();

            if (!isRenderingWell
                && mediaConfiguration.isAudioEnabled()
                && mediaConfiguration.isVideoEnabled()) {
              mediaConfiguration.setMediaConfigurationType(TribePeerMediaConfiguration.FPS_DROP);
              remotePeer.setMediaConfiguration(mediaConfiguration);
            } else if (mediaConfiguration.isLowConnection()) {
              mediaConfiguration.setMediaConfigurationType(TribePeerMediaConfiguration.NONE);
              remotePeer.setMediaConfiguration(mediaConfiguration);
            }
          }
        });
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
      Timber.d("We found a null peerId it doesn't make sense!");
      return;
    }

    if (mediaStream == null) {
      Timber.d("Cannot set a null mediaStream to peerId: " + mediaStream);
      return;
    }

    RemotePeer remotePeer = remotePeerMap.get(peerId);
    if (remotePeer == null) {
      Timber.d("Attempted to set MediaStream for non-existent RemotePeer: " + peerId);
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

  public void updateMediaConstraints(TribeMediaConstraints tribeMediaConstraints) {
    if (liveLocalStream == null) {
      return;
    }

    liveLocalStream.updateMediaConstraints(tribeMediaConstraints);
  }

  public void switchCamera() {
    if (liveLocalStream == null) {
      Timber.d("Live Local Stream is null");
    }

    liveLocalStream.switchCamera();
  }

  public void switchFilter() {
    if (liveLocalStream == null) {
      Timber.d("Live Local Stream is null");
    }

    liveLocalStream.switchFilter();
  }

  public void startGame(Game game) {
    if (liveLocalStream == null) {
      Timber.d("Live Local Stream is null");
    }

    liveLocalStream.startGame(game);
  }

  public void stopGame() {
    if (liveLocalStream == null) {
      Timber.d("Live Local Stream is null");
    }

    liveLocalStream.stopGame();
  }

  private void setLocalCameraEnabled(boolean enabled) {
    if (liveLocalStream == null) {
      return;
    }

    liveLocalStream.setCameraEnabled(enabled);
  }

  private void setLocalAudioEnabled(boolean enabled) {
    if (liveLocalStream == null) {
      return;
    }

    liveLocalStream.setAudioEnabled(enabled);
  }

  public boolean isLocalAudioEnabled() {
    if (liveLocalStream == null) {
      return false;
    }

    return liveLocalStream.isAudioEnabled();
  }

  public boolean isLocalCameraEnabled() {
    if (liveLocalStream == null) {
      return false;
    }

    return liveLocalStream.isCameraEnabled();
  }

  public TribePeerMediaConfiguration getMediaConfiguration() {
    if (localPeerView == null) {
      return null;
    }

    return localPeerView.getMediaConfiguration();
  }

  public void dispose(boolean shouldDisposeLocal) {
    Timber.d("Init dispose StreamManager");

    if (remotePeerMap != null && remotePeerMap.size() > 0) {
      Timber.d("Iterating remote peers");
      for (RemotePeer remotePeer : remotePeerMap.getMap().values()) {
        Timber.d("Disposing remote peer");
        remotePeer.dispose();
      }

      Timber.d("Clearing remote peer map");
      remotePeerMap.clear();
    }

    if (shouldDisposeLocal) {
      localSubscriptions.clear();

      Timber.d("Disposing live local stream");
      if (liveLocalStream != null) {
        liveLocalStream.dispose();
        liveLocalStream = null;
      }

      Timber.d("Disposing stream manager");
      if (localPeerView != null) {
        localPeerView.dispose();
        localPeerView = null;
      }

      if (subscriptionRenderingWell != null) subscriptionRenderingWell.unsubscribe();
    }

    Timber.d("End disposing stream manager");
  }

  // OBSERVABLES
  public Observable<ObservableRxHashMap.RxHashMap<String, RemotePeer>> onRemotePeersChanged() {
    return remotePeerMap.getObservable();
  }

  public Observable<TribePeerMediaConfiguration> onMediaChanged() {
    return onMediaChanged;
  }
}
