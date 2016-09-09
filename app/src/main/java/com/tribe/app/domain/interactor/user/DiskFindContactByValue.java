package com.tribe.app.domain.interactor.user;

import com.tribe.app.data.repository.user.DiskUserDataRepository;
import com.tribe.app.domain.executor.PostExecutionThread;
import com.tribe.app.domain.interactor.common.UseCaseDisk;

import javax.inject.Inject;

import rx.Observable;

/**
 * Created by tiago on 04/05/2016.
 */
public class DiskFindContactByValue extends UseCaseDisk {

    private String value;
    private UserRepository userRepository;

    @Inject
    public DiskFindContactByValue(DiskUserDataRepository userRepository, PostExecutionThread postExecutionThread) {
        super(postExecutionThread);
        this.userRepository = userRepository;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected Observable buildUseCaseObservable() {
        return this.userRepository.findByValue(value);
    }
}
