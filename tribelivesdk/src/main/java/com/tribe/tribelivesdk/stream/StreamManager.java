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
import java.util.HashMap;
import java.util.Map;
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
  private Map<String, TribePeerMediaConfiguration> pendingPeerMediaConfigurationMap;

  // OBSERVABLES
  private CompositeSubscription localSubscriptions = new CompositeSubscription();
  private Subscription subscriptionRenderingWell, subscriptionFreezeLive;
  private PublishSubject<TribePeerMediaConfiguration> onMediaChanged = PublishSubject.create();
  private PublishSubject<Boolean> isFreeze = PublishSubject.create();

  public StreamManager(Context context) {
    this.context = context;
  }

  public void initLocalStreamView(LocalPeerView localPeerView,
      PeerConnectionFactory peerConnectionFactory) {
    this.localPeerView = localPeerView;
    generateLocalStream(context, peerConnectionFactory);
    liveLocalStream.startVideoCapture();

    pendingPeerMediaConfigurationMap = new HashMap<>();

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

    subscriptionFreezeLive =
        Observable.interval(0, DURATION, TimeUnit.SECONDS).onBackpressureDrop().subscribe(aLong -> {
          if (localPeerView.isFreeze()) isFreeze.onNext(true);
        });

    subscriptionRenderingWell =
        Observable.interval(0, DURATION, TimeUnit.SECONDS).onBackpressureDrop().subscribe(aLong -> {
          Collection<RemotePeer> remotePeerCollection = remotePeerMap.getMap().values();
          for (RemotePeer remotePeer : remotePeerCollection) {
            boolean isRenderingWell = remotePeer.isRenderingWell();
            TribePeerMediaConfiguration mediaConfiguration = remotePeer.getMediaConfiguration();

            if (!isRenderingWell &&
                mediaConfiguration.isAudioEnabled() &&
                mediaConfiguration.isVideoEnabled()) {
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

    if (pendingPeerMediaConfigurationMap.get(session.getPeerId()) != null) {
      Timber.d("Setting pending media configuration");
      remotePeer.setMediaConfiguration(pendingPeerMediaConfigurationMap.get(session.getPeerId()));
      pendingPeerMediaConfigurationMap.remove(session.getPeerId());
    }
  }

  public void removePeer(TribeSession tribeSession) {
    if (remotePeerMap != null && remotePeerMap.size() > 0) {
      RemotePeer remotePeer = remotePeerMap.get(tribeSession.getPeerId());
      if (remotePeer != null) remotePeer.dispose();
      remotePeerMap.remove(tribeSession.getPeerId(), true);
    }
  }

  public RemotePeer setMediaStreamForClient(@NonNull String peerId,
      @NonNull MediaStream mediaStream) {
    if (TextUtils.isEmpty(peerId)) {
      Timber.d("We found a null peerId it doesn't make sense!");
      return null;
    }

    if (mediaStream == null) {
      Timber.d("Cannot set a null mediaStream to peerId: " + mediaStream);
      return null;
    }

    RemotePeer remotePeer = remotePeerMap.get(peerId);
    if (remotePeer == null) {
      Timber.d("Attempted to set MediaStream for non-existent RemotePeer: " + peerId);
      return null;
    }

    Timber.d("Setting the stream to peer : " + peerId);
    remotePeer.getPeerView().setStream(mediaStream);
    return remotePeer;
  }

  public void setPeerMediaConfiguration(TribePeerMediaConfiguration tribePeerMediaConfiguration) {
    RemotePeer remotePeer = remotePeerMap.get(tribePeerMediaConfiguration.getSession().getPeerId());

    if (remotePeer == null) {
      Timber.d("setMediaConfiguration to a null remotePeer");
      pendingPeerMediaConfigurationMap.put(tribePeerMediaConfiguration.getSession().getPeerId(),
          tribePeerMediaConfiguration);
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

  public RemotePeer getRemotePeer(String userId) {
    for (RemotePeer remotePeer : remotePeerMap.getMap().values()) {
      if (userId.equals(remotePeer.getSession().getUserId())) {
        return remotePeer;
      }
    }

    return null;
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
      if (subscriptionFreezeLive != null) subscriptionFreezeLive.unsubscribe();
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

  public Observable<Boolean> isFreeze() {
    return isFreeze;
  }
}
