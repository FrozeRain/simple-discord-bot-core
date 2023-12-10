package com.frozerain.sdbc.util;

@FunctionalInterface
public interface SilentCallable<V> {

    V call();
}
