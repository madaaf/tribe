package com.tribe.app.presentation.utils.preferences;

import java.lang.annotation.Retention;
import javax.inject.Qualifier;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by madaaflak on 08/05/2017.
 */

@Qualifier @Retention(RUNTIME) public @interface ImmersiveCallState {
}
