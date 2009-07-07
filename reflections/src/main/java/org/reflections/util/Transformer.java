package org.reflections.util;

import org.reflections.ReflectionsException;

/**
 *
 */
public interface Transformer<F,T> {
    T transform(F f);
}
