package com.tribe.app.presentation.view.adapter.delegate;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;

import rx.subscriptions.CompositeSubscription;

/**
 * Created by tiago on 30/05/2016.
 */
public abstract class RxAdapterDelegate<T> implements AdapterDelegate<T> {

    private CompositeSubscription subscriptions = new CompositeSubscription();

    public void releaseSubscriptions() {
        if (subscriptions != null && subscriptions.hasSubscriptions()) subscriptions.unsubscribe();
    }
}
