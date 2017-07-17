package com.tribe.tribelivesdk.stream;

import android.content.Context;
import android.graphics.RectF;
import android.support.v4.util.Pair;
import com.tribe.tribelivesdk.facetracking.UlseeManager;
import com.tribe.tribelivesdk.libyuv.LibYuvConverter;
import com.tribe.tribelivesdk.opencv.OpenCVWrapper;
import com.tribe.tribelivesdk.webrtc.Frame;
import rx.Observable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by tiago on 18/05/2017.
 */

public class FrameManager {

  private static final float SCALE = 0.3f;

  // VARIABLES
  private Context context;
  //private GameManager gameManager;
  private UlseeManager ulseeManager;
  //private VisionAPIManager visionAPIManager;
  private LibYuvConverter libYuvConverter;
  private OpenCVWrapper openCVWrapper;
  //private FilterManager filterManager;
  private byte[] argb, yuvOut;
  private boolean firstFrame, processing = false;
  private int previousWidth = 0, previousHeight = 0, previousRotation = 0;

  // OBSERVABLES
  private CompositeSubscription subscriptions = new CompositeSubscription();
  private PublishSubject<Frame> onFrameSizeChange = PublishSubject.create();
  private PublishSubject<Frame> onNewFrame = PublishSubject.create();
  private PublishSubject<Frame> onRemoteFrame = PublishSubject.create();

  public FrameManager(Context context) {
    this.context = context;
    libYuvConverter = LibYuvConverter.getInstance();
    openCVWrapper = new OpenCVWrapper();
    //initVisionAPIManager();
    //initGameManager();
    //initFilterManager();
    initUlseeManager();
  }

  //private void initFilterManager() {
  //  filterManager = FilterManager.getInstance(context);
  //  filterManager.initFrameSizeChangeObs(onFrameSizeChange);
  //}
  //
  //private void initGameManager() {
  //  gameManager = GameManager.getInstance(context);
  //  gameManager.initSubscriptions();
  //  gameManager.initFrameSizeChangeObs(onFrameSizeChange);
  //  gameManager.initOnNewFrameObs(onNewFrame);
  //}
  //
  //private void initSubscriptions() {
  //  subscriptions.add(gameManager.onRemoteFrame().onBackpressureDrop().subscribe(onRemoteFrame));
  //  subscriptions.add(gameManager.onLocalFrame().onBackpressureDrop().subscribe(onLocalFrame));
  //}

  private void initUlseeManager() {
    ulseeManager = UlseeManager.getInstance(context);
  }

  //private void initVisionAPIManager() {
  //  visionAPIManager = VisionAPIManager.getInstance(context);
  //}

  public void initFrameSubscription(Observable<Frame> onFrame) {
    subscriptions.add(onFrame.onBackpressureDrop()
        .filter(frame -> !processing)
        .observeOn(Schedulers.computation())
        .map(frame1 -> {
          processing = true;
          if (firstFrame ||
              previousWidth != frame1.getWidth() ||
              previousHeight != frame1.getHeight() ||
              previousRotation != frame1.getRotation()) {
            //argb = new byte[(int) ((frame1.getWidth() * frame1.getHeight() * 4) * SCALE)];
            //yuvOut = new byte[(int) ((frame1.getWidth() * frame1.getHeight()) * SCALE)];
            argb = new byte[frame1.getWidth() * frame1.getHeight() * 4];
            yuvOut = new byte[frame1.getData().length];

            frame1.setDataOut(yuvOut);
            //frame1.setWidth((int) (frame1.getWidth() * SCALE));
            //frame1.setHeight((int) (frame1.getHeight() * SCALE));

            firstFrame = false;
            previousHeight = frame1.getHeight();
            previousWidth = frame1.getWidth();
            previousRotation = frame1.getRotation();

            onFrameSizeChange.onNext(frame1);
          }

          boolean shouldSendRemoteFrame = true;

          long timeStart = System.nanoTime();
          openCVWrapper.flipBeforeSending(frame1.getData(), argb, frame1.getWidth(),
              frame1.getHeight(), SCALE);
          long timeEndFlip = System.nanoTime();
          //Timber.d("Total time of flipping frame " +
          //    " / " +
          //    (timeEndFlip - timeStart) / 1000000.0f +
          //    " ms");
          libYuvConverter.ARGBToYUV(argb, frame1.getWidth(), frame1.getHeight(), yuvOut);
          frame1.setDataOut(yuvOut);
          long timeEnd = System.nanoTime();
          //Timber.d("Total time of converting frame " +
          //    " / " +
          //    (timeEnd - timeEndFlip) / 1000000.0f +
          //    " ms");

          if (shouldSendRemoteFrame) onRemoteFrame.onNext(frame1);

          return frame1;
        })
        .subscribe(frame -> processing = false));
  }

  public void initPreviewFrameSubscription(Observable<Frame> onPreviewFrame) {
    ulseeManager.initFrameSubscription(onPreviewFrame);
  }

  public void initNewFacesSubscriptions(Observable<Pair<RectF[], int[]>> observable) {
    ulseeManager.initNewFacesSubscription(observable);
  }

  public void switchFilter() {
    //filterManager.switchFilter();
  }

  public void dispose() {
    //filterManager.dispose();
    //gameManager.dispose();
    //visionAPIManager.dispose();
  }

  public void startCapture() {
    //initSubscriptions();
  }

  public void stopCapture() {
    firstFrame = true;
    subscriptions.clear();
    ulseeManager.stopCapture();
    //visionAPIManager.stopCapture();
  }

  public void switchCamera() {
    firstFrame = true;
  }

  /////////////////
  // OBSERVABLES //
  /////////////////

  public Observable<Frame> onRemoteFrame() {
    return onRemoteFrame;
  }
}
