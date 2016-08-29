package com.tribe.app.presentation.view.adapter;

import com.hannesdorfmann.adapterdelegates2.AdapterDelegate;
import com.hannesdorfmann.adapterdelegates2.AdapterDelegatesManager;
import com.tribe.app.presentation.view.adapter.delegate.RxAdapterDelegate;

/**
 * Created by tiago on 30/05/2016.
 */
public class RxAdapterDelegatesManager<T> extends AdapterDelegatesManager<T> {

    public void releaseSubscriptions() {
        for (int i = 0, size = delegates.size(); i < size; i++) {
            if (delegates.valueAt(i) instanceof RxAdapterDelegate) {

                RxAdapterDelegate<T> delegate = (RxAdapterDelegate<T>) delegates.valueAt(i);
                delegate.releaseSubscriptions();
            }
        }
    }
}
