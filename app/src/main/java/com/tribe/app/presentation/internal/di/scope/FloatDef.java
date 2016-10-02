package com.tribe.app.presentation.internal.di.scope;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes that the annotated element of integer type, represents
 * a logical type and that its value should be one of the explicitly
 * named constants. If the FloatDef#flag() attribute is set to true,
 * multiple constants can be combined.
 * <p>
 * Example:
 * <pre>{@code
 *  &#64;Retention(SOURCE)
 *  &#64;FloatDef(&#123;NAVIGATION_MODE_STANDARD, NAVIGATION_MODE_LIST, NAVIGATION_MODE_TABS&#125;)
 *  public &#64;interface NavigationMode &#123;&#125;
 *  public static final int NAVIGATION_MODE_STANDARD = 0;
 *  public static final int NAVIGATION_MODE_LIST = 1;
 *  public static final int NAVIGATION_MODE_TABS = 2;
 *  ...
 *  public base void setNavigationMode(&#64;NavigationMode int mode);
 *  &#64;NavigationMode
 *  public base int getNavigationMode();
 * }</pre>
 * For a flag, set the flag attribute:
 * <pre>{@code
 *  &#64;FloatDef(
 *      flag = true
 *      value = &#123;NAVIGATION_MODE_STANDARD, NAVIGATION_MODE_LIST, NAVIGATION_MODE_TABS&#125;)
 * }</pre>
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE})
public @interface FloatDef {
    /** Defines the allowed constants for this element */
    float[] value() default {};

    /** Defines whether the constants can be used as a flag, or just as an enum (the default) */
    boolean flag() default false;
}