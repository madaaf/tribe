package com.tribe.app.domain.exception;

/**
 * Created by tiago on 04/05/2016.
 *
 * Interface to represent a wrapper around an {@link java.lang.Exception} to manage errors.
 *
 */
public interface ErrorBundle {

    Exception getException();
    String getErrorMessage();
}
