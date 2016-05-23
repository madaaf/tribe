package com.tribe.app.domain.interactor.text;

import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.executor.ThreadExecutor;
import com.tribe.app.domain.interactor.common.UseCase;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 22/05/2016.
 */
public class SubscribingMQTT extends UseCase {

    private TextRepository textRepository;
    private String topic;

    @Inject
    public SubscribingMQTT(TextRepository textRepository, ThreadExecutor threadExecutor, PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        this.textRepository = textRepository;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return textRepository.subscribing(topic);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
