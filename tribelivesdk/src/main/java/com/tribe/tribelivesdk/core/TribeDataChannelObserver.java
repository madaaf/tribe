package com.tribe.tribelivesdk.core;

import android.util.Log;
import com.tribe.tribelivesdk.util.LogUtil;
import java.lang.ref.WeakReference;
import org.webrtc.DataChannel;
import org.webrtc.MediaConstraints;
import org.webrtc.PeerConnection;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import rx.Observable;
import rx.subjects.PublishSubject;

public class TribeDataChannelObserver implements DataChannel.Observer {

  private PublishSubject<String> onMessage = PublishSubject.create();

  public TribeDataChannelObserver() {

  }

  @Override public void onBufferedAmountChange(long l) {
    LogUtil.d(getClass(), "onBufferedAmountChange");
  }

  @Override public void onMessage(DataChannel.Buffer buffer) {
    LogUtil.d(getClass(), "onMessage dataChannel");
  }

  @Override public void onStateChange() {
    LogUtil.d(getClass(), "onStateChange");
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<String> onMessage() {
    return onMessage;
  }
}
