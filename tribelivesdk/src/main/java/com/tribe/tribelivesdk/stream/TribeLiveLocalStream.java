package com.tribe.tribelivesdk.stream;

import android.content.Context;
import com.tribe.tribelivesdk.util.LogUtil;
import com.tribe.tribelivesdk.view.PeerView;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

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
  private VideoTrack videoTrack;
  private VideoCapturer capturer;

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
    MediaConstraints audioConstraints = new MediaConstraints();

    audioConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair(AUDIO_ECHO_CANCELLATION_CONSTRAINT, "true"));
    audioConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair(AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "true"));
    audioConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair(AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "true"));
    audioConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair(AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "true"));
    audioConstraints.mandatory.add(
        new MediaConstraints.KeyValuePair(AUDIO_LEVEL_CONTROL_CONSTRAINT, "true"));

    AudioSource localAudioSource = peerConnectionFactory.createAudioSource(audioConstraints);
    audioTrack = peerConnectionFactory.createAudioTrack("APPEARa0", localAudioSource);
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
    CameraEnumerator enumerator = new Camera1Enumerator(false);

    //if (Camera2Enumerator.isSupported(context)) {
    //    enumerator = new Camera2Enumerator(context);
    //} else {
    //    enumerator =
    //}

    LogUtil.d(getClass(), "Creating capturer");

    final String[] deviceNames = enumerator.getDeviceNames();

    // First, try to find front facing camera
    LogUtil.d(getClass(), "Looking for front facing cameras.");
    for (String deviceName : deviceNames) {
      if (enumerator.isFrontFacing(deviceName)) {
        LogUtil.d(getClass(), "Creating front facing camera capturer.");
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

        if (videoCapturer != null) {
          capturer = videoCapturer;
          return;
        }
      }
    }

    // Front facing camera not found, try something else
    LogUtil.d(getClass(), "Looking for other cameras.");
    for (String deviceName : deviceNames) {
      if (!enumerator.isFrontFacing(deviceName)) {
        LogUtil.d(getClass(), "Creating other camera capturer.");
        VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

        if (videoCapturer != null) {
          capturer = videoCapturer;
        }
      }
    }
  }

  public void startVideoCapture() {
    if (capturer != null) {
      LogUtil.d(getClass(), "Start video source.");
      capturer.startCapture(mediaConstraints.getMaxWidth(), mediaConstraints.getMaxHeight(),
          mediaConstraints.getMaxFrameRate());
    }
  }

  public void stopVideoCapture() {
    if (capturer != null) {
      LogUtil.d(getClass(), "Stop video source.");
      try {
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
    LogUtil.d(getClass(), "Disposing live local stream");
    LogUtil.d(getClass(), "Stop video capture");
    stopVideoCapture();
    LogUtil.d(getClass(), "Disposing video capturer");
    capturer.dispose();
    capturer = null;

    if (videoSource != null) {
      LogUtil.d(getClass(), "Disposing video source");
      videoSource.dispose();
    }

    //try {
    //  if (mediaStream != null) {
    //    for (VideoTrack videoTrack : mediaStream.videoTracks) {
    //      mediaStream.removeTrack(videoTrack);
    //    }
    //
    //    for (AudioTrack audioTrack : mediaStream.audioTracks) {
    //      mediaStream.removeTrack(audioTrack);
    //    }
    //  }
    //} catch (Exception ex) {
    //  ex.printStackTrace();
    //}

    audioTrack = null;
    videoTrack = null;
    LogUtil.d(getClass(), "End disposing live local stream");
  }

  public void setAudioEnabled(boolean enabled) {
    if (audioTrack == null) {
      return;
    }

    audioTrack.setEnabled(enabled);
  }

  public boolean isAudioEnabled() {
    if (audioTrack == null || isReattachingCamera)
      return false;

    return audioTrack.enabled();
  }

  public void setCameraEnabled(boolean enabled) {
    if (videoTrack == null || isReattachingCamera) {
      return;
    }

    videoTrack.setEnabled(enabled);
  }

  public boolean isCameraEnabled() {
    if (videoTrack == null || isReattachingCamera)
      return false;

    return videoTrack.enabled();
  }

  public void switchCamera() {
    if (capturer == null) {
      return;
    }

    if (capturer instanceof CameraVideoCapturer) {
      LogUtil.d(getClass(), "Switch camera");
      CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) capturer;
      cameraVideoCapturer.switchCamera(null);
    } else {
      LogUtil.d(getClass(), "Will not switch camera, video caputurer is not a camera");
    }
  }
}
