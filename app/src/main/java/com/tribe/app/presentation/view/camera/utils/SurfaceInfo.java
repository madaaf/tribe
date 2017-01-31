/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tribe.app.presentation.view.camera.utils;

import android.graphics.SurfaceTexture;

/**
 * Stores information about the {@link SurfaceTexture} showing camera preview.
 */
public class SurfaceInfo {

  private SurfaceTexture surface;
  private int width;
  private int height;

  public void configure(SurfaceTexture s, int w, int h) {
    surface = s;
    width = w;
    height = h;
  }

  public SurfaceTexture getSurface() {
    return surface;
  }

  public void setSurface(SurfaceTexture surfaceTexture) {
    surface = surfaceTexture;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
