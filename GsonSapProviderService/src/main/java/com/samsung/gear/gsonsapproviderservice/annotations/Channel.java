package com.samsung.gear.gsonsapproviderservice.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Set the default service channel
 * @author a1.jackson
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Channel {
    int value();
}
