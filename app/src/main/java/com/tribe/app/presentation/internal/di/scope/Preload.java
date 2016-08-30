package com.tribe.app.presentation.internal.di.scope;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Created by horatiothomas on 8/29/16.
 */

@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Preload {
}
