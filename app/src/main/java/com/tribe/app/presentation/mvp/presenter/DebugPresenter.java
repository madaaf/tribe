package com.tribe.app.presentation.mvp.presenter;

import com.tribe.app.domain.interactor.common.DefaultSubscriber;
import com.tribe.app.domain.interactor.user.DoBootstrapSupport;
import com.tribe.app.presentation.mvp.view.DebugMVPView;
import com.tribe.app.presentation.mvp.view.MVPView;

import javax.inject.Inject;

public class DebugPresenter implements Presenter {

    // VIEW ATTACHED
    private DebugMVPView debugView;

    // USECASES
    private final DoBootstrapSupport bootstrapSupport;

    // SUBSCRIBERS
    private BootstrapSupportSubscriber bootstrapSupportSubscriber;

    @Inject
    public DebugPresenter(DoBootstrapSupport bootstrapSupport) {
        this.bootstrapSupport = bootstrapSupport;
    }

    @Override
    public void onViewDetached() {
        bootstrapSupport.unsubscribe();
    }

    @Override
    public void onViewAttached(MVPView v) {
        debugView = (DebugMVPView) v;
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
