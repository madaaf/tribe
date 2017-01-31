package com.tribe.app.presentation.utils.preferences;

import java.lang.annotation.Retention;

import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by tiagoduarte on 10/22/16.
 */
@Qualifier @Retention(RUNTIME) public @interface WasAskedForPermissions {
}
