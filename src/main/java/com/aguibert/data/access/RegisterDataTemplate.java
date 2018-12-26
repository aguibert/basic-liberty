/**
 *
 */
package com.aguibert.data.access;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Stereotype;

/**
 * @author Andrew
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.TYPE })
@Stereotype
@Dependent
public @interface RegisterDataTemplate {

}
