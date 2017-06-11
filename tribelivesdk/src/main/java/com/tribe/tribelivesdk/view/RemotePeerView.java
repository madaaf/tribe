package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import java.util.List;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

/**
 * Created by tiago on 15/01/2017.
 */

public class RemotePeerView extends PeerView {

  // OBSERVABLES
  private PublishSubject<TribePeerMediaConfiguration> onMediaConfiguration =
      PublishSubject.create();

  private PublishSubject<String> onNotificationRemoteJoined = PublishSubject.create();

  public RemotePeerView(Context context) {
    super(context);
  }

  public RemotePeerView(Context context, AttributeSet attributeSet) {
    super(context, attributeSet);
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void onAttachedToWindow() {
    try {
      // Generally, OpenGL is only necessary while this View is attached
      // to a window so there is no point in having the whole rendering
      // infrastructure hooked up while this View is not attached to a
      // window. Additionally, a memory leak was solved in a similar way
      // on iOS.
      tryAddRendererToVideoTrack();
    } finally {
      super.onAttachedToWindow();
    }
  }

  /**
   * Sets the {@code MediaStream} to be rendered by this {@code PeerView}.
   * The implementation renders the first {@link VideoTrack}, if any, of the
   * specified {@code mediaStream}.
   *
   * @param mediaStream The {@code MediaStream} to be rendered by this
   * {@code PeerView} or {@code null}.
   */
  public void setStream(MediaStream mediaStream) {
    VideoTrack videoTrack;

    if (mediaStream == null) {
      videoTrack = null;
    } else {
      List<VideoTrack> videoTracks = mediaStream.videoTracks;

      videoTrack = videoTracks.isEmpty() ? null : videoTracks.get(0);
    }

    setVideoTrack(videoTrack);
  }

  /**
   * Sets the {@code VideoTrack} to be rendered by this {@code PeerView}.
   *
   * @param videoTrack The {@code VideoTrack} to be rendered by this
   * {@code PeerView} or {@code null}.
   */
  protected void setVideoTrack(VideoTrack videoTrack) {
    VideoTrack oldValue = this.videoTrack;

    if (oldValue != videoTrack) {
      if (oldValue != null) {
        dispose();
      }

      this.videoTrack = videoTrack;

      if (videoTrack != null) {
        tryAddRendererToVideoTrack();
      }
    }
  }

  /**
   * Starts rendering {@link #videoTrack} if rendering is not in progress and
   * all preconditions for the start of rendering are met.
   */
  protected void tryAddRendererToVideoTrack() {
    if (remoteRenderer == null && videoTrack != null) {
      TextureViewRenderer textureViewRenderer = getTextureViewRenderer();

      textureViewRenderer.init(/* sharedContext */ null, rendererEvents);

      remoteRenderer = new VideoRenderer(textureViewRenderer);
      videoTrack.addRenderer(remoteRenderer);
    }
  }

  @Override public void onFirstFrameRendered() {
    onNotificationRemoteJoined.onNext(null);
  }

  @Override public void onPreviewSizeChanged(int width, int height) {

  }

  public void setMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    Timber.d("New media configuration for : "
        + mediaConfiguration.getSession().getUserId()
        + " : isAudioEnabled : "
        + mediaConfiguration.isAudioEnabled()
        + " isVideoEnabled : "
        + mediaConfiguration.isVideoEnabled());
    onMediaConfiguration.onNext(mediaConfiguration);
  }

  // OBSERVABLES
  public Observable<TribePeerMediaConfiguration> onMediaConfiguration() {
    return onMediaConfiguration;
  }

  public boolean isRenderingWell() {
    if (textureViewRenderer == null) return true;

    return textureViewRenderer.isRenderingWell();
  }

  public Observable<String> onNotificationRemoteJoined() {
    return onNotificationRemoteJoined;
  }
}
