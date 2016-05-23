package com.tribe.app.domain.interactor.text;

import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import org.w3c.dom.Text;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class ConnectAndSubscribeMQTT extends UseCase {

    private TextRepository textRepository;
    private String topic;

    @Inject
    public ConnectAndSubscribeMQTT(TextRepository textRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.textRepository = textRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return textRepository.connectAndSubscribe(topic);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
