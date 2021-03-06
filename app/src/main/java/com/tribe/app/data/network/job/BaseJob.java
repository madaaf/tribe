/*
 * Copyright (C) 2015 The Android Open Source Project
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
 *
 */

package com.tribe.app.data.network.job;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.tribe.app.presentation.internal.di.components.ApplicationComponent;
import com.tribe.app.presentation.utils.analytics.TagManager;
import javax.inject.Inject;

abstract public class BaseJob extends Job {

  @Inject JobManager jobManager;

  @Inject TagManager tagManager;

  // VARIABLES
  protected static final boolean DEBUG = false;

  public BaseJob(Params params) {
    super(params);
  }

  public void inject(ApplicationComponent appComponent) {
    appComponent.inject(this);
  }
}
