package com.tribe.tribelivesdk.stream;

import android.content.Context;
import com.tribe.tribelivesdk.model.TribeMediaConstraints;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.view.PeerView;
import java.util.List;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
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
  private final VideoRenderer videoRenderer;
  private VideoSource videoSource;
  private AudioSource audioSource;
  private VideoTrack videoTrack;
  private VideoCapturer capturer;
  private List<CameraEnumerationAndroid.CaptureFormat> captureFormatList;
  private boolean capturing = false;

  public TribeLiveLocalStream(Context context, PeerView peerView,
      PeerConnectionFactory peerConnectionFactory) {
    if (peerView == null || peerView.getVideoRenderer() == null) {
      throw new IllegalArgumentException("VideoStreamsView can not be null");
    }

    if (peerConnectionFactory == null) {
      throw new IllegalArgumentException("PeerConnectionFactory can not be null");
    }

    this.context = context;
    this.videoRenderer = peerView.getVideoRenderer();
    this.peerConnectionFactory = peerConnectionFactory;
    mediaConstraints =
        new com.tribe.tribelivesdk.core.MediaConstraints.MediaConstraintsBuilder().build();

    generateVideoCapturer();
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
    com.tribe.tribelivesdk.webrtc.Camera1Enumerator enumerator =
        new com.tribe.tribelivesdk.webrtc.Camera1Enumerator(false);

    Timber.d("Creating capturer");

    final String[] deviceNames = enumerator.getDeviceNames();

    // First, try to find front facing camera
    Timber.d("Looking for front facing cameras.");
    for (String deviceName : deviceNames) {
      if (enumerator.isFrontFacing(deviceName)) {
        Timber.d("Creating front facing camera capturer.");
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

        captureFormatList = enumerator.getSupportedFormats(deviceName);

        if (videoCapturer != null) {
          capturer = videoCapturer;
          return;
        }
      }
    }

    // Front facing camera not found, try something else
    Timber.d("Looking for other cameras.");
    for (String deviceName : deviceNames) {
      if (!enumerator.isFrontFacing(deviceName)) {
        Timber.d("Creating other camera capturer.");
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

        captureFormatList = enumerator.getSupportedFormats(deviceName);

        if (videoCapturer != null) {
          capturer = videoCapturer;
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
      try {
        capturing = false;
        capturer.stopCapture();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
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

    if (capturer instanceof CameraVideoCapturer) {
      Timber.d("Switch camera");
      CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) capturer;
      cameraVideoCapturer.switchCamera(null);
    } else {
      Timber.d("Will not switch camera, video caputurer is not a camera");
    }
  }
}
