package org.reflections.adapters;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.Ops;
import jsr166y.forkjoin.ParallelArray;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

public class Jsr166ForkJoiner implements ForkJoiner {
	private final ForkJoinPool forkJoinPool = new ForkJoinPool();

	public <K, V> List<V> transform(Iterable<K> source, final Function<K, V> function, Class<K> keyClass) throws InterruptedException, ExecutionException {
		return transform(source.iterator(), function, keyClass);
	}

	public <K, V> List<V> transform(Iterator<K> source, final Function<K, V> function, Class<K> keyClass) throws InterruptedException, ExecutionException {
		ParallelArray<K> parallelArray = ParallelArray.createFromCopy(Iterators.toArray(source, keyClass), forkJoinPool);
		return parallelArray.withMapping(new Ops.Mapper<K, V>() {
			public V map(K key) {
				return function.apply(key);
			}
		}).all().asList();
	}

}
