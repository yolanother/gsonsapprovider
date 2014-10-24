/**
 *
 */
package com.samsung.gear.gsonsapproviderservice.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author a1.jackson
 * Prevents data from unregistered class from being sent.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PreventUnregistered {

}
