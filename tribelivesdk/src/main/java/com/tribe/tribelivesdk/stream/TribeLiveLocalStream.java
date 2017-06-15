package com.tribe.tribelivesdk.stream;

import android.content.Context;
import com.tribe.tribelivesdk.game.Game;
import com.tribe.tribelivesdk.model.TribeMediaConstraints;
import com.tribe.tribelivesdk.view.LocalPeerView;
import com.tribe.tribelivesdk.webrtc.Camera1Enumerator;
import com.tribe.tribelivesdk.webrtc.CameraCapturer;
import com.tribe.tribelivesdk.webrtc.TribeVideoRenderer;
import java.util.List;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class TribeLiveLocalStream {

  private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
  private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
  private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
  private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
  private static final String AUDIO_LEVEL_CONTROL_CONSTRAINT = "levelControl";

  private Context context;
  private boolean isReattachingCamera = false;
  private MediaStream mediaStream;
  private final PeerConnectionFactory peerConnectionFactory;
  private AudioTrack audioTrack;
  private com.tribe.tribelivesdk.core.MediaConstraints mediaConstraints;
  private LocalPeerView peerView;
  private VideoRenderer videoRenderer;
  private TribeVideoRenderer localVideoRenderer;
  private VideoSource videoSource;
  private AudioSource audioSource;
  private VideoTrack videoTrack;
  private CameraCapturer capturer;
  private List<CameraEnumerationAndroid.CaptureFormat> captureFormatList;
  private boolean capturing = false;

  // OBSERVABLE
  private CompositeSubscription subscriptions = new CompositeSubscription();

  public TribeLiveLocalStream(Context context, LocalPeerView peerView,
      PeerConnectionFactory peerConnectionFactory) {
    if (peerView == null) {
      throw new IllegalArgumentException("Peerview cannot be null");
    }

    if (peerConnectionFactory == null) {
      throw new IllegalArgumentException("PeerConnectionFactory can not be null");
    }

    this.context = context;
    this.peerView = peerView;
    this.videoRenderer = peerView.getRemoteRenderer();
    this.localVideoRenderer = peerView.getLocalRenderer();
    this.peerConnectionFactory = peerConnectionFactory;
    mediaConstraints =
        new com.tribe.tribelivesdk.core.MediaConstraints.MediaConstraintsBuilder().build();

    generateVideoCapturer();

    if (capturer != null) {
      subscriptions.add(
          capturer.onLocalFrame().subscribe(frame -> localVideoRenderer.renderFrame(frame)));
    }
  }

  private void addAudioTrackToMediaStream() {
    audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
    audioTrack = peerConnectionFactory.createAudioTrack("APPEARa0", audioSource);
    mediaStream.addTrack(audioTrack);
  }

  private void addVideoTrackToMediaStream() {
    if (capturer == null) {
      return;
    }

    videoSource = peerConnectionFactory.createVideoSource(capturer);
    videoTrack = peerConnectionFactory.createVideoTrack("APPEARv0", videoSource);
    videoTrack.addRenderer(videoRenderer);
    mediaStream.addTrack(videoTrack);
  }

  private void generateVideoCapturer() {
    Camera1Enumerator enumerator = new Camera1Enumerator(true);

    Timber.d("Creating capturer");

    final String[] deviceNames = enumerator.getDeviceNames();

    // First, try to find front facing camera
    Timber.d("Looking for front facing cameras.");
    for (String deviceName : deviceNames) {
      if (enumerator.isFrontFacing(deviceName)) {
        Timber.d("Creating front facing camera capturer.");
        CameraCapturer cameraCapturer = enumerator.createCapturer(deviceName, null);

        captureFormatList = enumerator.getSupportedFormats(deviceName);

        if (cameraCapturer != null) {
          capturer = cameraCapturer;
          return;
        }
      }
    }

    // Front facing camera not found, try something else
    Timber.d("Looking for other cameras.");
    for (String deviceName : deviceNames) {
      if (!enumerator.isFrontFacing(deviceName)) {
        Timber.d("Creating other camera capturer.");
        CameraCapturer cameraCapturer = enumerator.createCapturer(deviceName, null);
        captureFormatList = enumerator.getSupportedFormats(deviceName);
        if (cameraCapturer != null) {
          capturer = cameraCapturer;
          return;
        }
      }
    }
  }

  private com.tribe.tribelivesdk.core.MediaConstraints generateCorrectMediaConstraints(
      TribeMediaConstraints tribeMediaConstraints) {
    CaptureFormatConstraints localCaptureFormatConstraints =
        new CaptureFormatConstraints(captureFormatList);
    return localCaptureFormatConstraints.getConstraintsClosestToDimensions(
        tribeMediaConstraints.getMaxWidth(), tribeMediaConstraints.getMaxHeight());
  }

  public void startVideoCapture() {
    if (capturer != null) {
      Timber.d("Start video source.");
      capturing = true;
      capturer.startCapture(mediaConstraints.getMaxWidth(), mediaConstraints.getMaxHeight(),
          mediaConstraints.getMaxFrameRate());
    }
  }

  public void updateMediaConstraints(TribeMediaConstraints tribeMediaConstraints) {
    Timber.d("updatingMediaConstraints : " + tribeMediaConstraints);
    mediaConstraints = generateCorrectMediaConstraints(tribeMediaConstraints);
    if (capturer != null && capturing) {
      capturer.changeCaptureFormat(mediaConstraints.getMaxWidth(), mediaConstraints.getMaxHeight(),
          mediaConstraints.getMaxFrameRate());
    }
  }

  public void stopVideoCapture() {
    if (capturer != null) {
      Timber.d("Stop video source.");
      capturing = false;
      capturer.stopCapture();
    }
  }

  public MediaStream asNativeMediaStream() {
    if (mediaStream != null) {
      return mediaStream;
    }

    mediaStream = peerConnectionFactory.createLocalMediaStream("APPEAR");
    addVideoTrackToMediaStream();
    addAudioTrackToMediaStream();
    return mediaStream;
  }

  public void dispose() {
    Timber.d("Disposing live local stream");

    if (audioSource != null) {
      Timber.d("Disposing audio source");
      audioSource.dispose();
    }

    Timber.d("Stop video capture");
    stopVideoCapture();
    Timber.d("Disposing video capturer");
    capturer.dispose();
    capturer = null;

    if (videoSource != null) {
      Timber.d("Disposing video source");
      videoSource.dispose();
    }

    audioTrack = null;
    videoTrack = null;
    Timber.d("End disposing live local stream");
  }

  public void setAudioEnabled(boolean enabled) {
    if (audioTrack == null) {
      return;
    }

    audioTrack.setEnabled(enabled);
  }

  public boolean isAudioEnabled() {
    if (audioTrack == null || isReattachingCamera) return false;

    return audioTrack.enabled();
  }

  public void setCameraEnabled(boolean enabled) {
    if (videoTrack == null || isReattachingCamera) {
      return;
    }

    if (!enabled && capturing) {
      stopVideoCapture();
    } else if (enabled && !capturing) {
      startVideoCapture();
    }

    videoTrack.setEnabled(enabled);
  }

  public boolean isCameraEnabled() {
    if (videoTrack == null || isReattachingCamera) return false;

    return videoTrack.enabled();
  }

  public void switchCamera() {
    if (capturer == null) {
      return;
    }

    capturer.switchCamera(null);
  }

  public void switchFilter() {
    if (capturer == null) {
      return;
    }

    capturer.switchFilter();
  }

  public void startGame(Game game) {
    if (game.isLocalFrameDifferent()) {
      stopVideoCapture();
      videoTrack.removeRenderer(videoRenderer);
      startVideoCapture();
    }
  }

  public void stopGame() {
    peerView.initRemoteRenderer();
    videoRenderer = peerView.getRemoteRenderer();
    stopVideoCapture();
    videoTrack.addRenderer(videoRenderer);
    startVideoCapture();
  }
}
