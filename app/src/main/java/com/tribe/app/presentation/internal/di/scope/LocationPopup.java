package com.tribe.app.presentation.internal.di.scope;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by tiago on 10/02/16.
 */
@Qualifier
@Retention(RUNTIME)
public @interface LocationPopup {
}
