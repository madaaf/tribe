package com.tribe.app.presentation.utils.preferences;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by tiago on 12/01/16.
 */
@Qualifier
@Retention(RUNTIME)
public @interface LastOnlineNotification {
}
