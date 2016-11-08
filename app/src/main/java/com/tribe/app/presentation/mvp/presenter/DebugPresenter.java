package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoBootstrapSupport;
import com.tribe.app.presentation.mvp.view.DebugView;
import com.tribe.app.presentation.mvp.view.View;

import javax.inject.Inject;

public class DebugPresenter implements Presenter {

    // VIEW ATTACHED
    private DebugView debugView;

    // USECASES
    private final DoBootstrapSupport bootstrapSupport;

    // SUBSCRIBERS
    private BootstrapSupportSubscriber bootstrapSupportSubscriber;

    @Inject
    public DebugPresenter(DoBootstrapSupport bootstrapSupport) {
        this.bootstrapSupport = bootstrapSupport;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
        // Unused
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onPause() {
    }

    @Override
    public void onDestroy() {
        bootstrapSupport.unsubscribe();
    }

    @Override
    public void attachView(View v) {
        debugView = (DebugView) v;
    }

    public void boostrapSupport() {
        if (bootstrapSupportSubscriber != null)
            bootstrapSupportSubscriber.unsubscribe();

        bootstrapSupportSubscriber = new BootstrapSupportSubscriber();
        bootstrapSupport.execute(bootstrapSupportSubscriber);
    }

    private class BootstrapSupportSubscriber extends DefaultSubscriber<Void> {

        @Override
        public void onCompleted() {}

        @Override
        public void onError(Throwable e) {
            e.printStackTrace();
        }

        @Override
        public void onNext(Void aVoid) {}
    }
}
