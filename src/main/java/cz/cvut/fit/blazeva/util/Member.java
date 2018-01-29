/*
 * Copyright LWJGL. All rights reserved.
 * License terms: http://lwjgl.org/license.php
 */
package cz.cvut.fit.blazeva.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate a struct member (i.e. field in a class representing a GLSL struct).
 * 
 * @author Kai Burjack
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Member {

    /**
     * If that member is an array, this indicates the static size of the array.
     */
    int length() default 0;

}
