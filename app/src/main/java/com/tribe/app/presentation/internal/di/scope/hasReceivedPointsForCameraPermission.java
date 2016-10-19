package com.tribe.app.presentation.internal.di.scope;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by horatiothomas on 10/14/16.
 */
@Qualifier
@Retention(RUNTIME)
public @interface HasReceivedPointsForCameraPermission {
}
