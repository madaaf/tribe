package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.hardware.Camera;
import com.tribe.tribelivesdk.game.GameManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DFilter;
import com.tribe.tribelivesdk.rs.lut3d.LUT3DManager;
import com.tribe.tribelivesdk.ulsee.UlseeManager;
import com.tribe.tribelivesdk.webrtc.Frame;
import com.tribe.tribelivesdk.webrtc.TribeI420Frame;
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
  private UlseeManager ulseeManager;
  private LibYuvConverter libYuvConverter;
  private LUT3DManager lut3DManager;
  private byte[] argb, yuvOut;
  private boolean firstFrame, processing = false;
  private int previousWidth, previousHeight = 0;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Frame> onFrameSizeChange = PublishSubject.create();
  private PublishSubject<Frame> onNewFrame = PublishSubject.create();
  private PublishSubject<TribeI420Frame> onLocalFrame = PublishSubject.create();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();

  public FrameManager(Context context) {
    this.context = context;
    libYuvConverter = new LibYuvConverter();

    initGameManager();
    initRenderscript();
    initUlseeManager();
  }

  private void initRenderscript() {
    lut3DManager = new LUT3DManager(context);
    lut3DManager.initFrameSizeChangeObs(onFrameSizeChange);
  }

  private void initGameManager() {
    gameManager = GameManager.getInstance(context);
    gameManager.initFrameSizeChangeObs(onFrameSizeChange);
    gameManager.initOnNewFrameObs(onNewFrame);
  }

  private void initSubscriptions() {
    subscriptions.add(gameManager.onRemoteFrame().onBackpressureDrop().subscribe(onRemoteFrame));
    subscriptions.add(gameManager.onLocalFrame().onBackpressureDrop().subscribe(onLocalFrame));
  }

  private void initUlseeManager() {
    ulseeManager = UlseeManager.getInstance(context);
  }

  public void initFrameSubscription(Observable<Frame> onFrame) {
    //ulseeManager.initFrameSubscription(onFrame);
    subscriptions.add(onFrame
        .onBackpressureDrop()
        .filter(frame -> !processing)
        .observeOn(Schedulers.computation())
        .map(frame1 -> {
          //Timber.d("Processing filters : " + Thread.currentThread().getName());
          processing = true;
          if (firstFrame
              || previousWidth != frame1.getWidth()
              || previousHeight != frame1.getHeight()) {
            argb = new byte[frame1.getWidth() * frame1.getHeight() * 4];
            yuvOut = new byte[frame1.getData().length];

            firstFrame = false;
            previousHeight = frame1.getHeight();
            previousWidth = frame1.getWidth();

            frame1.setDataOut(yuvOut);
            onFrameSizeChange.onNext(frame1);
          }

          boolean shouldSendRemoteFrame = true;

          LUT3DFilter filter = lut3DManager.getFilter();

          if (!filter.getId().equals(LUT3DFilter.LUT3D_NONE)) {
            libYuvConverter.YUVToARGB(frame1.getData(), frame1.getWidth(), frame1.getHeight(),
                argb);
            filter.apply(argb);

            if (gameManager.getCurrentGame() != null && gameManager.getCurrentGame()
                .isLocalFrameDifferent()) {
              shouldSendRemoteFrame = false;
              frame1.setData(argb);
              frame1.setDataOut(yuvOut);
              onNewFrame.onNext(frame1);
            } else {
              libYuvConverter.ARGBToYUV(argb, frame1.getWidth(), frame1.getHeight(), yuvOut);
              frame1.setDataOut(yuvOut);
            }
          } else {
            frame1.setDataOut(frame1.getData());
          }

          if (shouldSendRemoteFrame) onRemoteFrame.onNext(frame1);

          //Timber.d("End processing filters");

          return frame1;
        })
        .subscribe(frame -> processing = false));
  }

  public void initNewFacesObs(Observable<Camera.Face[]> obs) {
    ulseeManager.initNewFacesObs(obs);
  }

  public void switchFilter() {
    lut3DManager.switchFilter();
  }

  public void dispose() {
    lut3DManager.dispose();
    gameManager.dispose();
  }

  public void startCapture() {
    initSubscriptions();
  }

  public void stopCapture() {
    firstFrame = true;
    subscriptions.clear();
    ulseeManager.stopCapture();
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
