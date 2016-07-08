package com.tribe.app.presentation.internal.di.scope;

import java.io.Serializable;

import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by tiago on 05/07/2016.
 */
public class SerializableSerializedSubject<T, R> extends SerializedSubject<T, R> implements Serializable {

    public SerializableSerializedSubject(final Subject<T, R> actual) {
        super(actual);
    }
}
