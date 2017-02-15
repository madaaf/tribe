package com.tribe.tribelivesdk.core;

import java.nio.ByteBuffer;
import org.webrtc.DataChannel;
import rx.Observable;
import rx.subjects.PublishSubject;
import timber.log.Timber;

public class TribeDataChannelObserver implements DataChannel.Observer {

  private PublishSubject<String> onMessage = PublishSubject.create();

  private PublishSubject<Void> onStateChange = PublishSubject.create();

  public TribeDataChannelObserver() {

  }

  @Override public void onBufferedAmountChange(long l) {
    Timber.d( "onBufferedAmountChange");
  }

  @Override public void onMessage(DataChannel.Buffer buffer) {
    ByteBuffer data = buffer.data;
    byte[] bytes = new byte[data.remaining()];
    data.get(bytes);
    final String message = new String(bytes);
    Timber.d( "onMessage dataChannel : " + message);
    onMessage.onNext(message);
  }

  @Override public void onStateChange() {
    Timber.d( "onStateChange");
    onStateChange.onNext(null);
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<String> onMessage() {
    return onMessage;
  }

  public Observable<Void> onStateChanged() {
    return onStateChange;
  }
}
