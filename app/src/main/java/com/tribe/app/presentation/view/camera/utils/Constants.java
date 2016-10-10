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


public class Constants {

    public static final int FACING_BACK = 0;
    public static final int FACING_FRONT = 1;

    public static final AspectRatio DEFAULT_ASPECT_RATIO = AspectRatio.of(4, 3);
    public static final AspectRatio ASPECT_RATIO_16_9 = AspectRatio.of(16, 9);
    public static final AspectRatio ASPECT_RATIO_FAKE = AspectRatio.of(19, 1);

    public static final int FLASH_OFF = 0;
    public static final int FLASH_ON = 1;
    public static final int FLASH_TORCH = 2;
    public static final int FLASH_AUTO = 3;
    public static final int FLASH_RED_EYE = 4;
}
