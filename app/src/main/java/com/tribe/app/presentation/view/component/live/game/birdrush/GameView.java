package com.tribe.app.presentation.view.component.live.game.birdrush;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by madaaflak on 31/01/2018.
 */

public class GameView extends SurfaceView {

  /** Milliseconds for game timer tick */
  public static final long UPDATE_INTERVAL = 50;        // = 20 FPS

  private Timer timer = new Timer();
  private TimerTask timerTask;

  /** The surfaceholder needed for the canvas drawing */
  private SurfaceHolder holder;

  private Test player;

  public GameView(Context context) {
    super(context);

    holder = getHolder();
    player = new Test(getResources());
  }

  public void resume() {
    startTimer();
  }

  private void startTimer() {
    setUpTimerTask();
    timer = new Timer();
    timer.schedule(timerTask, UPDATE_INTERVAL, UPDATE_INTERVAL);
  }

  private Canvas getCanvas() {
    Canvas canvas;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      canvas = holder.lockHardwareCanvas();
    } else {
      canvas = holder.lockCanvas();
    }

    return canvas;
  }

  private void setUpTimerTask() {
    stopTimer();
    timerTask = new TimerTask() {
      @Override public void run() {
        GameView.this.run();
      }
    };
  }

  private void stopTimer() {
    if (timer != null) {
      timer.cancel();
      timer.purge();
    }
    if (timerTask != null) {
      timerTask.cancel();
    }
  }

  /**
   * content of the timertask
   */

  public void run() {
    draw();
  }

  /**
   * Draws all gameobjects on the surface
   */
  private void draw() {
    while (!holder.getSurface().isValid()) {
            /*wait*/
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    Canvas canvas = getCanvas();

    drawCanvas(canvas);

    holder.unlockCanvasAndPost(canvas);
  }

  private void drawCanvas(Canvas canvas) {
    player.draw(canvas);
  }

  @Override public boolean onTouchEvent(MotionEvent event) {
    performClick();
    if (event.getAction() == MotionEvent.ACTION_DOWN) { // No support for dead players
      resume();
    }
    return true;
  }
}
