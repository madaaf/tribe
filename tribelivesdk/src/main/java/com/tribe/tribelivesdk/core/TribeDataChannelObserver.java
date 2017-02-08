package com.tribe.tribelivesdk.core;

import com.tribe.tribelivesdk.util.LogUtil;
import java.nio.ByteBuffer;
import org.webrtc.DataChannel;
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
    ByteBuffer data = buffer.data;
    byte[] bytes = new byte[data.remaining()];
    data.get(bytes);
    final String message = new String(bytes);
    LogUtil.d(getClass(), "onMessage dataChannel : " + message);
    onMessage.onNext(message);
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
