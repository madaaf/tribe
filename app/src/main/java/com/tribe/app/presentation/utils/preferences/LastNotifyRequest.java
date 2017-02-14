package com.tribe.app.presentation.utils.preferences;

import java.lang.annotation.Retention;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier @Retention(RUNTIME) public @interface LastNotifyRequest {
}
