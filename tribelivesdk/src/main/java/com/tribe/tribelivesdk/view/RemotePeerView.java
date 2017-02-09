package com.tribe.tribelivesdk.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tribe.tribelivesdk.model.TribePeerMediaConfiguration;
import com.tribe.tribelivesdk.util.LogUtil;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by tiago on 15/01/2017.
 */

public class RemotePeerView extends PeerView {

  // OBSERVABLES
  private PublishSubject<TribePeerMediaConfiguration> onMediaConfiguration =
      PublishSubject.create();

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

  public void setMediaConfiguration(TribePeerMediaConfiguration mediaConfiguration) {
    LogUtil.d(getClass(), "New media configuration for : "
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
}
