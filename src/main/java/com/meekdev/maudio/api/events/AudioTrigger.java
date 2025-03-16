package com.meekdev.maudio.api.events;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AudioTrigger {
    String value();
    AudioEvent.Priority priority() default AudioEvent.Priority.NORMAL;
    boolean ignoreCancelled() default false;
}