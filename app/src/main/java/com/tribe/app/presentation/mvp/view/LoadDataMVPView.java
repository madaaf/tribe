package com.tribe.app.presentation.mvp.view;

import android.content.Context;

/**
 * Interface representing a MVPView that will use to load data.
 */
public interface LoadDataMVPView extends MVPView {
    /**
     * Show a view with a progress bar indicating a loading process.
     */
    void showLoading();

    /**
     * Hide a loading view.
     */
    void hideLoading();

    /**
     * Show an error message
     *
     * @param message A string representing an error.
     */
    void showError(String message);

    /**
     * Get a {@link android.content.Context}.
     */
    Context context();
}