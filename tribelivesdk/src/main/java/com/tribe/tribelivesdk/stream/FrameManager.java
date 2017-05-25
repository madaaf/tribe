package com.tribe.tribelivesdk.stream;

import android.content.Context;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilter;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DManager;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
import java.util.concurrent.Executors;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 18/05/2017.
 */

public class FrameManager {

  // VARIABLES
  private Context context;
  private GameManager gameManager;
  private LibYuvConverter libYuvConverter;
  private LUT3DManager lut3DManager;
  private byte[] argb;
  private byte[] yuvOut;
  private boolean firstFrame;

  private int previousWidth, previousHeight = 0;
  private boolean processing = false;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Frame> onFrameSizeChange = PublishSubject.create();
  private PublishSubject<Frame> onNewFrame = PublishSubject.create();
  private PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();

  public FrameManager(Context context) {
    this.context = context;
    libYuvConverter = new LibYuvConverter();

    lut3DManager = new LUT3DManager(context);
    lut3DManager.initFrameSizeChangeObs(onFrameSizeChange);

    gameManager = GameManager.getInstance(context);
    gameManager.initFrameSizeChangeObs(onFrameSizeChange);
    gameManager.initOnNewFrameObs(onNewFrame);

    subscriptions.add(gameManager.onRemoteFrame().subscribe(onRemoteFrame));
    subscriptions.add(gameManager.onLocalFrame().subscribe(onLocalFrame));
  }

  public void initFrameSubscription(Observable<Frame> onFrame) {
    subscriptions.add(onFrame.subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
        .filter(frame -> !processing)
        .doOnNext(frame -> {
          processing = true;

          if (firstFrame
              || previousWidth != frame.getWidth()
              || previousHeight != frame.getHeight()) {

            argb = new byte[frame.getWidth() * frame.getHeight() * 4];
            yuvOut = new byte[frame.getData().length];

            firstFrame = false;
            previousHeight = frame.getHeight();
            previousWidth = frame.getWidth();

            frame.setDataOut(yuvOut);
            onFrameSizeChange.onNext(frame);
          }

          LUT3DFilter filter = lut3DManager.getFilter();
          boolean shouldSendRemoteFrame = true;

          if (!filter.getId().equals(LUT3DFilter.LUT3D_NONE)) {
            libYuvConverter.YUVToARGB(frame.getData(), frame.getWidth(), frame.getHeight(), argb);
            filter.apply(argb);

            if (gameManager.getCurrentGame() != null && gameManager.getCurrentGame()
                .isLocalFrameDifferent()) {
              shouldSendRemoteFrame = false;
              frame.setData(argb);
              frame.setDataOut(yuvOut);
              onNewFrame.onNext(frame);
            } else {
              libYuvConverter.ARGBToYUV(argb, frame.getWidth(), frame.getHeight(), yuvOut);
              frame.setDataOut(yuvOut);
            }
          } else {
            frame.setDataOut(frame.getData());
          }

          if (shouldSendRemoteFrame) onRemoteFrame.onNext(frame);
        })
        .subscribe(frame -> processing = false));
  }

  public void switchFilter() {
    lut3DManager.switchFilter();
  }

  public void dispose() {
    firstFrame = true;
    subscriptions.clear();
    lut3DManager.dispose();
    gameManager.dispose();
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }

  public Observable<TribeI420Frame> onLocalFrame() {
    return onLocalFrame;
  }
}
