package com.tribe.tribelivesdk.facetracking;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class DeviceRotationDetector implements SensorEventListener {
  private SensorManager mSensorManager;
  private Sensor mSensor;
  float x, y, z;

  public DeviceRotationDetector(Context context) {
    mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    boolean registered =
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
    Log.d("GRAVITY", "gravity sensor registered: " + registered);
    if (!registered) {
      mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
      registered =
          mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
      Log.d("GRAVITY", "accelerometer registered: " + registered);
    }
  }

  @Override public void onSensorChanged(SensorEvent sensorEvent) {
    x = sensorEvent.values[0];
    y = sensorEvent.values[1];
    z = sensorEvent.values[2];
  }

  @Override public void onAccuracyChanged(Sensor sensor, int i) {

  }

  public int getRotationDegree() {
    if (Math.abs(y) >= Math.abs(x)) {
      if (y >= 0) {
        return 0;
      } else {
        return 180;
      }
    } else {
      if (x >= 0) {
        return 90;
      } else {
        return 270;
      }
    }
  }
}