package com.frozerain.sdbc.annotation;

import com.frozerain.sdbc.event.EventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Processor {

    EventType type() default EventType.UNKNOWN_EVENT;

    boolean contextRequired() default true;
}
