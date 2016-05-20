/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.presentation.mvp.view.View;

public interface Presenter {

    void onStart();

    void onResume();

    void onStop();

    void onPause();

    void onDestroy();

    void attachView(View v);

    void onCreate();
}
