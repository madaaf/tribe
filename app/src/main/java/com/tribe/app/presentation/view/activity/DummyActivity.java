package com.tribe.app.presentation.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

// SEE THIS : https://medium.com/@amitshekhar/android-memory-leaks-inputmethodmanager-solved-a6f2fe1d1348#.lprwxuobz
public class DummyActivity extends AppCompatActivity {
  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    new Handler().postDelayed(new Runnable() {
      @Override public void run() {
        finish();
      }
    }, 500);
  }
}