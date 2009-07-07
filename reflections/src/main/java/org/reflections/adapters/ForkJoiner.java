package org.reflections.adapters;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.base.Function;

public interface ForkJoiner {
	<K, V> List<V> transform(Iterable<K> source, final Function<K, V> function, Class<K> keyClass) throws InterruptedException, ExecutionException;
	<K, V> List<V> transform(Iterator<K> source, final Function<K, V> function, Class<K> keyClass) throws InterruptedException, ExecutionException;
}
